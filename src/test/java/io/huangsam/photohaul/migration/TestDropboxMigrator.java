package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.UploadBuilder;
import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestDropboxMigrator extends TestPathBase {
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
        when(requestsMock.listFolder(any())).thenReturn(folderResultMock);
        when(requestsMock.uploadBuilder(any())).thenReturn(uploadBuilderMock);

        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathVisitor pathVisitor = pathVisitor(getStaticResources(), names);
        Migrator migrator = new DropboxMigrator("folderId123", clientMock, new PhotoResolver(List.of()));
        migrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }
}
