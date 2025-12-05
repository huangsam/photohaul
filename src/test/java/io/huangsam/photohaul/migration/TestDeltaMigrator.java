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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TestDeltaMigrator {

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
    void testGetFailureCountDelegatesToWrapped() {
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

    @Test
    void testMigratePhotosWithEmptyCollection() throws IOException {
        when(mockStorage.readStateFile(any())).thenReturn(null);

        deltaMigrator.migratePhotos(List.of());

        // Should not call delegate for empty collection
        verify(mockDelegate, never()).migratePhotos(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMigratePhotosSavesStateAfterSuccessfulMigration(@TempDir Path tempDir) throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("photo.jpg");
        Files.writeString(testFile, "test content");
        Photo photo = new Photo(testFile);

        when(mockStorage.readStateFile(any())).thenReturn(null);
        when(mockDelegate.getSuccessCount()).thenReturn(1L);

        deltaMigrator.migratePhotos(List.of(photo));

        // Should save state after successful migration
        verify(mockStorage).writeStateFile(anyString(), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMigratePhotosHandlesStateSaveIOException(@TempDir Path tempDir) throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("photo.jpg");
        Files.writeString(testFile, "test content");
        Photo photo = new Photo(testFile);

        when(mockStorage.readStateFile(any())).thenReturn(null);
        when(mockDelegate.getSuccessCount()).thenReturn(1L);
        doThrow(new IOException("Save failed")).when(mockStorage).writeStateFile(anyString(), anyString());

        // Should not throw, just log error
        deltaMigrator.migratePhotos(List.of(photo));

        verify(mockDelegate).migratePhotos((Collection<Photo>) any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMigratePhotosRecordsOnlySuccessfulMigrations(@TempDir Path tempDir) throws IOException {
        // Create two test files
        Path testFile1 = tempDir.resolve("photo1.jpg");
        Path testFile2 = tempDir.resolve("photo2.jpg");
        Files.writeString(testFile1, "test content 1");
        Files.writeString(testFile2, "test content 2");
        Photo photo1 = new Photo(testFile1);
        Photo photo2 = new Photo(testFile2);

        when(mockStorage.readStateFile(any())).thenReturn(null);
        // Simulate only 1 successful migration out of 2
        when(mockDelegate.getSuccessCount()).thenReturn(1L);

        deltaMigrator.migratePhotos(List.of(photo1, photo2));

        // Should call delegate with both photos
        verify(mockDelegate).migratePhotos((Collection<Photo>) any());
        // State should still be saved
        verify(mockStorage).writeStateFile(anyString(), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMigratePhotosMixedNewAndUnchangedFiles(@TempDir Path tempDir) throws IOException {
        // Create two test files
        Path unchangedFile = tempDir.resolve("unchanged.jpg");
        Path newFile = tempDir.resolve("new.jpg");
        Files.writeString(unchangedFile, "unchanged content");
        Files.writeString(newFile, "new content");
        long unchangedSize = Files.size(unchangedFile);
        long unchangedModified = Files.getLastModifiedTime(unchangedFile).toMillis();

        // Create state file content that only matches the unchanged file
        String stateJson = String.format(
                "{\"%s\":{\"path\":\"%s\",\"size\":%d,\"lastModifiedMillis\":%d}}",
                unchangedFile.toString().replace("\\", "\\\\"),
                unchangedFile.toString().replace("\\", "\\\\"),
                unchangedSize,
                unchangedModified
        );
        when(mockStorage.readStateFile(any())).thenReturn(stateJson);
        when(mockDelegate.getSuccessCount()).thenReturn(1L);

        Photo photo1 = new Photo(unchangedFile);
        Photo photo2 = new Photo(newFile);
        deltaMigrator.migratePhotos(List.of(photo1, photo2));

        // Should call delegate for new file only
        verify(mockDelegate).migratePhotos((Collection<Photo>) any());
        assertEquals(1, deltaMigrator.getSkippedCount());
    }

    @Test
    void testMigratePhotosNoSuccessfulMigrations(@TempDir Path tempDir) throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("photo.jpg");
        Files.writeString(testFile, "test content");
        Photo photo = new Photo(testFile);

        when(mockStorage.readStateFile(any())).thenReturn(null);
        // Simulate no successful migrations
        when(mockDelegate.getSuccessCount()).thenReturn(0L);

        deltaMigrator.migratePhotos(List.of(photo));

        // Should not save state if no successful migrations
        verify(mockStorage, never()).writeStateFile(anyString(), anyString());
    }

    @Test
    void testMigratePhotosHandlesNonExistentFile(@TempDir Path tempDir) throws IOException {
        // Create a path to non-existent file
        Path nonExistentFile = tempDir.resolve("nonexistent.jpg");
        Photo photo = new Photo(nonExistentFile);

        when(mockStorage.readStateFile(any())).thenReturn(null);
        when(mockDelegate.getSuccessCount()).thenReturn(1L);

        // Should not throw, should include file in migration anyway
        deltaMigrator.migratePhotos(List.of(photo));

        verify(mockDelegate).migratePhotos(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMigratePhotosWithDifferentLastModifiedTime(@TempDir Path tempDir) throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("photo.jpg");
        Files.writeString(testFile, "test content");
        long size = Files.size(testFile);

        // Create state file content with same size but different timestamp
        String stateJson = String.format(
                "{\"%s\":{\"path\":\"%s\",\"size\":%d,\"lastModifiedMillis\":%d}}",
                testFile.toString().replace("\\", "\\\\"),
                testFile.toString().replace("\\", "\\\\"),
                size,
                1000L // Different timestamp
        );
        when(mockStorage.readStateFile(any())).thenReturn(stateJson);
        when(mockDelegate.getSuccessCount()).thenReturn(1L);

        Photo photo = new Photo(testFile);
        deltaMigrator.migratePhotos(List.of(photo));

        // Should call delegate since file has different timestamp
        verify(mockDelegate).migratePhotos((Collection<Photo>) any());
    }
}
