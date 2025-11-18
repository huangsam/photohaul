package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestSftpMigrator extends TestMigrationAbstract {
    @Mock
    SSHClient sshClientMock;

    @Mock
    SFTPClient sftpClientMock;

    @Mock
    PhotoResolver photoResolverMock;

    @Test
    void testMigratePhotosSuccess() throws Exception {
        when(sshClientMock.newSFTPClient()).thenReturn(sftpClientMock);
        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = new SftpMigrator("host", 22, "user", "pass", "/target", photoResolverMock, () -> sshClientMock);
        run(migrator);

        verify(sshClientMock).connect("host", 22);
        verify(sshClientMock).authPassword("user", "pass");
        verify(sshClientMock).newSFTPClient();
        verify(sftpClientMock, times(2)).mkdirs(anyString()); // occurs due to mocking
        verify(sftpClientMock, times(2)).put(anyString(), anyString());
        verify(sshClientMock).close();

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosConnectionFailure() throws Exception {
        doThrow(new IOException("Connection failed")).when(sshClientMock).connect("host", 22);

        Migrator migrator = new SftpMigrator("host", 22, "user", "pass", "/target", photoResolverMock, () -> sshClientMock);
        run(migrator);

        verify(sshClientMock).connect("host", 22);
        verify(sshClientMock, times(0)).authPassword(anyString(), anyString());
        verify(sshClientMock).close();

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosUploadFailure() throws Exception {
        when(sshClientMock.newSFTPClient()).thenReturn(sftpClientMock);
        doThrow(new IOException("Upload failed")).when(sftpClientMock).put(anyString(), anyString());
        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = new SftpMigrator("host", 22, "user", "pass", "/target", photoResolverMock, () -> sshClientMock);
        run(migrator);

        verify(sftpClientMock, times(2)).mkdirs(anyString()); // occurs due to mocking
        verify(sftpClientMock, times(2)).put(anyString(), anyString());
        verify(sshClientMock).close();

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosCloseFailure() throws Exception {
        when(sshClientMock.newSFTPClient()).thenReturn(sftpClientMock);
        doThrow(new IOException("Close failed")).when(sshClientMock).close();
        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = new SftpMigrator("host", 22, "user", "pass", "/target", photoResolverMock, () -> sshClientMock);
        run(migrator);

        verify(sshClientMock).connect("host", 22);
        verify(sshClientMock).authPassword("user", "pass");
        verify(sshClientMock).newSFTPClient();
        verify(sftpClientMock, times(2)).mkdirs(anyString());
        verify(sftpClientMock, times(2)).put(anyString(), anyString());
        verify(sshClientMock).close();

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosWithResolutionException() throws Exception {
        when(sshClientMock.newSFTPClient()).thenReturn(sftpClientMock);
        when(photoResolverMock.resolveString(any(Photo.class))).thenThrow(new ResolutionException("Resolution failed"));

        Migrator migrator = new SftpMigrator("host", 22, "user", "pass", "/target", photoResolverMock, () -> sshClientMock);
        run(migrator);

        verify(sshClientMock).connect("host", 22);
        verify(sshClientMock).authPassword("user", "pass");
        verify(sshClientMock).newSFTPClient();
        verify(sftpClientMock, times(2)).mkdirs(anyString());
        verify(sftpClientMock, times(2)).put(anyString(), anyString());
        verify(sshClientMock).close();

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op
    }
}
