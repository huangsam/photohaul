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

import java.nio.file.Path;
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
    void testMigratePhotos() throws DbxException {
        when(clientMock.files()).thenReturn(requestsMock);
        when(requestsMock.listFolder(any())).thenReturn(folderResultMock);
        when(requestsMock.uploadBuilder(any())).thenReturn(uploadBuilderMock);
        List<String> names = List.of("bauerlite.jpg", "salad.jpg", "foobar.jpg");
        PhotoPathVisitor pathVisitor = visitor(getStaticResources(), names);
        DropboxMigrator dbxMigrator = new DropboxMigrator("/Foo", clientMock, new PhotoResolver(List.of()));
        dbxMigrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(2, dbxMigrator.getSuccessCount());
        assertEquals(1, dbxMigrator.getFailureCount());
    }

    private static PhotoPathVisitor visitor(Path path, List<String> names) {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();
        for (String name : names) {
            pathVisitor.visitPhoto(path.resolve(name));
        }
        return pathVisitor;
    }
}
