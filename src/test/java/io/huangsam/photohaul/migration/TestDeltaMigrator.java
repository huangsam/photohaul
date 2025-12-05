package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.migration.state.MigrationStateFile;
import io.huangsam.photohaul.migration.state.StateFileStorage;
import io.huangsam.photohaul.model.Photo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestDeltaMigrator {

    @Mock
    Migrator mockDelegate;

    @Mock
    StateFileStorage mockStorage;

    private DeltaMigrator deltaMigrator;
    private MigrationStateFile stateFile;

    @BeforeEach
    void setUp() {
        stateFile = new MigrationStateFile(mockStorage);
        deltaMigrator = new DeltaMigrator(mockDelegate, stateFile);
    }

    @Test
    void testMigratePhotosCallsDelegateForNewFiles(@TempDir Path tempDir) throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("photo.jpg");
        Files.writeString(testFile, "test content");
        Photo photo = new Photo(testFile);

        when(mockStorage.readStateFile(any())).thenReturn(null);
        when(mockDelegate.getSuccessCount()).thenReturn(1L);

        deltaMigrator.migratePhotos(List.of(photo));

        verify(mockDelegate).migratePhotos(any());
    }

    @Test
    void testMigratePhotosSkipsUnchangedFiles(@TempDir Path tempDir) throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("photo.jpg");
        Files.writeString(testFile, "test content");
        long size = Files.size(testFile);
        long lastModified = Files.getLastModifiedTime(testFile).toMillis();

        // Create state file content that matches the file
        String stateJson = String.format(
                "{\"%s\":{\"path\":\"%s\",\"size\":%d,\"lastModifiedMillis\":%d}}",
                testFile.toString().replace("\\", "\\\\"),
                testFile.toString().replace("\\", "\\\\"),
                size,
                lastModified
        );
        when(mockStorage.readStateFile(any())).thenReturn(stateJson);

        Photo photo = new Photo(testFile);
        deltaMigrator.migratePhotos(List.of(photo));

        // Should not call delegate since file is unchanged
        verify(mockDelegate, never()).migratePhotos(any());
        assertEquals(1, deltaMigrator.getSkippedCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMigratePhotosMigratesModifiedFiles(@TempDir Path tempDir) throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("photo.jpg");
        Files.writeString(testFile, "test content");
        long lastModified = Files.getLastModifiedTime(testFile).toMillis();

        // Create state file content with different size (simulating modification)
        String stateJson = String.format(
                "{\"%s\":{\"path\":\"%s\",\"size\":%d,\"lastModifiedMillis\":%d}}",
                testFile.toString().replace("\\", "\\\\"),
                testFile.toString().replace("\\", "\\\\"),
                100L, // Different size
                lastModified
        );
        when(mockStorage.readStateFile(any())).thenReturn(stateJson);
        when(mockDelegate.getSuccessCount()).thenReturn(1L);

        Photo photo = new Photo(testFile);
        deltaMigrator.migratePhotos(List.of(photo));

        // Should call delegate since file is modified
        verify(mockDelegate).migratePhotos((Collection<Photo>) any());
    }

    @Test
    void testGetSuccessCountDelegatesToWrapped() {
        when(mockDelegate.getSuccessCount()).thenReturn(5L);

        assertEquals(5L, deltaMigrator.getSuccessCount());
    }

    @Test
    void testGetFailureCountDelegatesToWrapped(@TempDir Path tempDir) {
        when(mockDelegate.getFailureCount()).thenReturn(3L);

        assertEquals(3L, deltaMigrator.getFailureCount());
    }

    @Test
    void testCloseCallsDelegateClose() throws Exception {
        deltaMigrator.close();

        verify(mockDelegate).close();
    }

    @Test
    void testGetSkippedCountInitiallyZero() {
        assertEquals(0, deltaMigrator.getSkippedCount());
    }
}
