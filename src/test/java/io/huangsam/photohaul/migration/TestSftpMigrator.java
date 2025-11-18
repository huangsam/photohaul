package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.resolution.PhotoResolver;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void testMigratePhotosSuccess() throws IOException {
        when(sshClientMock.newSFTPClient()).thenReturn(sftpClientMock);

        Migrator migrator = new SftpMigrator("host", 22, "user", "pass", "/target", new PhotoResolver(List.of()), () -> sshClientMock);
        run(migrator);

        verify(sshClientMock).connect("host", 22);
        verify(sshClientMock).authPassword("user", "pass");
        verify(sshClientMock).newSFTPClient();
        verify(sftpClientMock, times(2)).mkdirs(anyString()); // occurs due to mocking
        verify(sftpClientMock, times(2)).put(anyString(), anyString());
        verify(sshClientMock).disconnect();

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosConnectionFailure() throws IOException {
        doThrow(new IOException("Connection failed")).when(sshClientMock).connect("host", 22);

        Migrator migrator = new SftpMigrator("host", 22, "user", "pass", "/target", new PhotoResolver(List.of()), () -> sshClientMock);
        run(migrator);

        verify(sshClientMock).connect("host", 22);
        verify(sshClientMock, times(0)).authPassword(anyString(), anyString());

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosUploadFailure() throws IOException {
        when(sshClientMock.newSFTPClient()).thenReturn(sftpClientMock);
        doThrow(new IOException("Upload failed")).when(sftpClientMock).put(anyString(), anyString());

        Migrator migrator = new SftpMigrator("host", 22, "user", "pass", "/target", new PhotoResolver(List.of()), () -> sshClientMock);
        run(migrator);

        verify(sftpClientMock, times(2)).mkdirs(anyString()); // occurs due to mocking
        verify(sftpClientMock, times(2)).put(anyString(), anyString());

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());
    }
}
