package io.huangsam.photohaul.migration;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.traversal.PhotoPathCollector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static io.huangsam.photohaul.TestHelper.getPathCollector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class TestGoogleDriveMigrator {
    private static final String TARGET_ROOT = "rootId123";

    @Mock
    Drive driveMock;

    @Mock
    Drive.Files filesMock;

    @Mock
    Drive.Files.List driveListMock;

    @Mock
    FileList fileListMock;

    @Mock
    File listedFileMock;

    @Mock
    Drive.Files.Create driveCreateFolderMock;

    @Mock
    File createdFolderMock;

    @Mock
    Drive.Files.Create driveCreatePhotoMock;

    @Mock
    File createdPhotoMock;

    @Test
    void testMigratePhotosAllSuccess() throws IOException {
        when(driveMock.files()).thenReturn(filesMock);

        when(filesMock.list()).thenReturn(driveListMock);
        when(driveListMock.setQ(anyString())).thenReturn(driveListMock);
        when(driveListMock.execute()).thenReturn(fileListMock);
        when(fileListMock.getFiles()).thenReturn(List.of(listedFileMock));
        when(listedFileMock.getId()).thenReturn(null);

        when(filesMock.create(any())).thenReturn(driveCreateFolderMock);
        when(driveCreateFolderMock.setFields(anyString())).thenReturn(driveCreateFolderMock);
        when(driveCreateFolderMock.execute()).thenReturn(createdFolderMock);
        when(createdFolderMock.getId()).thenReturn("nestedId123");

        when(filesMock.create(any(), any())).thenReturn(driveCreatePhotoMock);
        when(driveCreatePhotoMock.setFields(anyString())).thenReturn(driveCreatePhotoMock);
        when(driveCreatePhotoMock.execute()).thenReturn(createdPhotoMock);

        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        Migrator migrator = new GoogleDriveMigrator(TARGET_ROOT, PhotoResolver.getDefault(), driveMock);
        migrator.migratePhotos(pathCollector.getPhotos());

        verify(filesMock, times(4)).list();
        verify(driveCreateFolderMock, times(2)).execute();
        verify(driveCreatePhotoMock, times(2)).execute();

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosAllExisting() throws IOException {
        when(driveMock.files()).thenReturn(filesMock);

        when(filesMock.list()).thenReturn(driveListMock);
        when(driveListMock.setQ(anyString())).thenReturn(driveListMock);
        when(driveListMock.execute()).thenReturn(fileListMock);
        when(fileListMock.getFiles()).thenReturn(List.of(listedFileMock));
        when(listedFileMock.getId()).thenReturn("existingId123");

        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        Migrator migrator = new GoogleDriveMigrator(TARGET_ROOT, PhotoResolver.getDefault(), driveMock);
        migrator.migratePhotos(pathCollector.getPhotos());

        verify(listedFileMock, times(4)).getId();
        verify(filesMock, times(0)).create(any());
        verify(filesMock, times(0)).create(any(), any());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosWithNullFolder() throws IOException {
        when(driveMock.files()).thenReturn(filesMock);

        when(filesMock.list()).thenReturn(driveListMock);
        when(driveListMock.setQ(anyString())).thenReturn(driveListMock);
        when(driveListMock.execute()).thenReturn(fileListMock);
        when(fileListMock.getFiles()).thenReturn(List.of(listedFileMock));

        when(filesMock.create(any())).thenReturn(driveCreateFolderMock);
        when(driveCreateFolderMock.setFields(anyString())).thenReturn(driveCreateFolderMock);
        when(driveCreateFolderMock.execute()).thenReturn(createdFolderMock);
        when(createdFolderMock.getId()).thenReturn(null);

        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        Migrator migrator = new GoogleDriveMigrator(TARGET_ROOT, PhotoResolver.getDefault(), driveMock);
        migrator.migratePhotos(pathCollector.getPhotos());

        verify(filesMock, times(2)).list();
        verify(driveCreateFolderMock, times(2)).execute();

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(2, migrator.getFailureCount());
    }
}
