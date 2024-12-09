package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.UploadBuilder;
import io.huangsam.photohaul.traversal.PhotoPathCollector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static io.huangsam.photohaul.TestHelper.getPathCollector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestDropboxMigrator {
    @Mock
    DbxClientV2 clientMock;

    @Mock
    DbxUserFilesRequests requestsMock;

    @Mock
    ListFolderResult folderResultMock;

    @Mock
    UploadBuilder uploadBuilderMock;

    @Test
    void testMigratePhotosAllSuccess() throws DbxException {
        when(clientMock.files()).thenReturn(requestsMock);
        when(requestsMock.listFolder(anyString())).thenReturn(folderResultMock);
        when(requestsMock.uploadBuilder(anyString())).thenReturn(uploadBuilderMock);

        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        Migrator migrator = new DropboxMigrator("/Foobar", clientMock, new PhotoResolver(List.of()));
        migrator.migratePhotos(pathCollector.getPhotos());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }

    @Test
    void testDropboxSetupNotWorking() {
        assertThrows(IllegalArgumentException.class, () ->
                new DropboxMigrator("NoSlashAtStart", clientMock, new PhotoResolver(List.of())));
    }
}
