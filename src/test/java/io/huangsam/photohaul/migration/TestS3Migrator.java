package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.resolution.PhotoResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestS3Migrator extends TestMigrationAbstract {
    @Mock
    S3Client s3ClientMock;

    @Test
    void testMigratePhotosSuccess() {
        Migrator migrator = new S3Migrator("test-bucket", new PhotoResolver(List.of()), s3ClientMock);
        run(migrator);

        verify(s3ClientMock, times(2)).putObject(any(PutObjectRequest.class), any(Path.class));

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosUploadFailure() {
        when(s3ClientMock.putObject(any(PutObjectRequest.class), any(Path.class))).thenThrow(new RuntimeException("Upload failed"));

        Migrator migrator = new S3Migrator("test-bucket", new PhotoResolver(List.of()), s3ClientMock);
        run(migrator);

        verify(s3ClientMock, times(2)).putObject(any(PutObjectRequest.class), any(Path.class));

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());
    }
}
