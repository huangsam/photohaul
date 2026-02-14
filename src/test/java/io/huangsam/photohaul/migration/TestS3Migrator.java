package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestS3Migrator extends TestMigrationAbstract {
    @Mock
    S3Client s3ClientMock;

    @Mock
    PhotoResolver photoResolverMock;

    @Test
    void testMigratePhotosSuccess() throws Exception {
        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = new S3Migrator("test-bucket", photoResolverMock, s3ClientMock);
        run(migrator);

        verify(s3ClientMock, times(2)).putObject(any(PutObjectRequest.class), any(Path.class));

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close();
        verify(s3ClientMock).close();
    }

    @Test
    void testMigratePhotosUploadFailure() throws Exception {
        when(s3ClientMock.putObject(any(PutObjectRequest.class), any(Path.class))).thenThrow(new RuntimeException("Upload failed"));
        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = new S3Migrator("test-bucket", photoResolverMock, s3ClientMock);
        run(migrator);

        verify(s3ClientMock, times(2)).putObject(any(PutObjectRequest.class), any(Path.class));

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());

        migrator.close();
        verify(s3ClientMock).close();
    }

    @Test
    void testMigratePhotosWithResolutionException() throws Exception {
        when(photoResolverMock.resolveString(any(Photo.class))).thenThrow(new ResolutionException("Resolution failed"));

        Migrator migrator = new S3Migrator("test-bucket", photoResolverMock, s3ClientMock);
        run(migrator);

        verify(s3ClientMock, times(2)).putObject(any(PutObjectRequest.class), any(Path.class));

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close();
        verify(s3ClientMock).close();
    }
}
