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
    Drive.Files.Create driveCreateMock;

    @Mock
    FileList fileListMock;

    @Mock
    File fileMock;

    @Test
    void testMigratePhotos() throws IOException {
        when(driveMock.files()).thenReturn(filesMock);

        when(filesMock.create(any(), any())).thenReturn(driveCreateMock);
        when(driveCreateMock.setFields(any())).thenReturn(driveCreateMock);
        when(driveCreateMock.execute()).thenReturn(fileMock);

        when(filesMock.list()).thenReturn(driveListMock);
        when(driveListMock.setQ(any())).thenReturn(driveListMock);
        when(driveListMock.execute()).thenReturn(fileListMock);
        when(fileListMock.getFiles()).thenReturn(List.of(fileMock));

        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathVisitor pathVisitor = pathVisitor(getStaticResources(), names);
        Migrator migrator = new GoogleDriveMigrator("Foo", driveMock, new PhotoResolver(List.of()));
        migrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }
}
