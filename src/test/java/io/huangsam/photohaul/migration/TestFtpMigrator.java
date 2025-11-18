package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.resolution.PhotoResolver;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestFtpMigrator extends TestMigrationAbstract {
    @Mock
    FTPClient ftpClientMock;

    @Test
    void testMigratePhotosSuccess() throws IOException {
        when(ftpClientMock.storeFile(anyString(), any())).thenReturn(true);
        when(ftpClientMock.isConnected()).thenReturn(true);

        Migrator migrator = new FtpMigrator("host", 21, "user", "pass", "/target", new PhotoResolver(List.of()), () -> ftpClientMock);
        run(migrator);

        verify(ftpClientMock).connect("host", 21);
        verify(ftpClientMock).login("user", "pass");
        verify(ftpClientMock, times(2)).storeFile(anyString(), any());
        verify(ftpClientMock).logout();
        verify(ftpClientMock).disconnect();

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosConnectionFailure() throws IOException {
        doThrow(new IOException("Connection failed")).when(ftpClientMock).connect("host", 21);

        Migrator migrator = new FtpMigrator("host", 21, "user", "pass", "/target", new PhotoResolver(List.of()), () -> ftpClientMock);
        run(migrator);

        verify(ftpClientMock).connect("host", 21);
        verify(ftpClientMock, times(0)).login(anyString(), anyString());

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosUploadFailure() throws IOException {
        when(ftpClientMock.storeFile(anyString(), any())).thenReturn(false);
        when(ftpClientMock.isConnected()).thenReturn(true);

        Migrator migrator = new FtpMigrator("host", 21, "user", "pass", "/target", new PhotoResolver(List.of()), () -> ftpClientMock);
        run(migrator);

        verify(ftpClientMock, times(2)).storeFile(anyString(), any());

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());
    }
}
