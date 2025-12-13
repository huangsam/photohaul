package io.huangsam.photohaul.migration;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.UploadBuilder;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestDropboxMigrator extends TestMigrationAbstract {
    @Mock
    DbxClientV2 clientMock;

    @Mock
    PhotoResolver photoResolverMock;

    @Mock
    DbxUserFilesRequests requestsMock;

    @Mock
    ListFolderResult folderResultMock;

    @Mock
    UploadBuilder uploadBuilderMock;

    @Test
    void testMigratePhotosNewFoldersSuccess() throws Exception {
        when(clientMock.files()).thenReturn(requestsMock);
        when(requestsMock.listFolder(anyString())).thenReturn(folderResultMock);
        when(requestsMock.uploadBuilder(anyString())).thenReturn(uploadBuilderMock);

        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = new DropboxMigrator("/Foobar", photoResolverMock, clientMock);
        run(migrator);

        verify(requestsMock, times(0)).createFolderV2(anyString());
        verify(requestsMock, times(2)).uploadBuilder(anyString());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op, but ensures no exception
    }

    @Test
    void testMigratePhotosOldFoldersSuccess() throws Exception {
        when(clientMock.files()).thenReturn(requestsMock);
        when(requestsMock.listFolder(anyString())).thenThrow(ListFolderErrorException.class);
        when(requestsMock.createFolderV2(anyString())).thenReturn(null);
        when(requestsMock.uploadBuilder(anyString())).thenReturn(uploadBuilderMock);

        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = new DropboxMigrator("/Foobar", photoResolverMock, clientMock);
        run(migrator);

        verify(requestsMock, times(2)).createFolderV2(anyString());
        verify(requestsMock, times(2)).uploadBuilder(anyString());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op, but ensures no exception
    }

    @Test
    @SuppressWarnings("resource")
    void testDropboxSetupNotWorking() {
        assertThrows(IllegalArgumentException.class, () ->
                new DropboxMigrator("NoSlashAtStart", photoResolverMock, clientMock));
    }

    @Test
    void testMigratePhotosWithResolutionException() throws Exception {
        when(clientMock.files()).thenReturn(requestsMock);
        when(requestsMock.listFolder(anyString())).thenReturn(folderResultMock);
        when(requestsMock.uploadBuilder(anyString())).thenReturn(uploadBuilderMock);

        when(photoResolverMock.resolveString(any(Photo.class))).thenThrow(new ResolutionException("Resolution failed"));

        Migrator migrator = new DropboxMigrator("/Foobar", photoResolverMock, clientMock);
        run(migrator);

        verify(requestsMock, times(0)).createFolderV2(anyString());
        verify(requestsMock, times(2)).uploadBuilder(anyString());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op, but ensures no exception
    }
}
