package io.huangsam.photohaul.migration;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestGoogleDriveMigrator extends TestPathBase {
    @Mock
    Drive driveMock;

    @Mock
    Drive.Files filesMock;

    @Mock
    Drive.Files.List driveListMock;

    @Mock
    FileList fileListMock;

    @Mock
    File fileMock;

    @Test
    void testMigratePhotos() throws IOException {
        when(driveMock.files()).thenReturn(filesMock);
        when(filesMock.list()).thenReturn(driveListMock);
        when(driveListMock.setQ(any())).thenReturn(driveListMock);
        when(driveListMock.execute()).thenReturn(fileListMock);
        when(fileListMock.getFiles()).thenReturn(List.of(fileMock));

        List<String> names = List.of("bauerlite.jpg", "salad.jpg", "foobar.jpg");
        PhotoPathVisitor pathVisitor = visitor(getStaticResources(), names);
        Migrator migrator = new GoogleDriveMigrator("Foo", driveMock, new PhotoResolver(List.of()));
        migrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(3, migrator.getFailureCount());
    }
}
