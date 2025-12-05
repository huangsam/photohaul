package io.huangsam.photohaul.migration.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestMigrationStateFile {

    @Mock
    StateFileStorage mockStorage;

    private MigrationStateFile stateFile;

    @BeforeEach
    void setUp() {
        stateFile = new MigrationStateFile(mockStorage);
    }

    @Test
    void testLoadWithEmptyFile() throws IOException {
        when(mockStorage.readStateFile(anyString())).thenReturn(null);

        stateFile.load();

        assertEquals(0, stateFile.size());
    }

    @Test
    void testLoadWithValidJson() throws IOException {
        String json = "{\"/path/photo.jpg\":{\"path\":\"/path/photo.jpg\",\"size\":1024,\"lastModifiedMillis\":1700000000000}}";
        when(mockStorage.readStateFile(anyString())).thenReturn(json);

        stateFile.load();

        assertEquals(1, stateFile.size());
    }

    @Test
    void testLoadWithIOException() throws IOException {
        when(mockStorage.readStateFile(anyString())).thenThrow(new IOException("Read error"));

        // Should not throw, just log warning
        stateFile.load();

        assertEquals(0, stateFile.size());
    }

    @Test
    void testNeedsMigrationReturnsTrueForNewFile() {
        FileState newFile = new FileState("/path/new.jpg", 1024, 1700000000000L);

        assertTrue(stateFile.needsMigration(newFile));
    }

    @Test
    void testNeedsMigrationReturnsFalseForUnchangedFile() throws IOException {
        String json = "{\"/path/photo.jpg\":{\"path\":\"/path/photo.jpg\",\"size\":1024,\"lastModifiedMillis\":1700000000000}}";
        when(mockStorage.readStateFile(anyString())).thenReturn(json);
        stateFile.load();

        FileState unchanged = new FileState("/path/photo.jpg", 1024, 1700000000000L);

        assertFalse(stateFile.needsMigration(unchanged));
    }

    @Test
    void testNeedsMigrationReturnsTrueForModifiedFile() throws IOException {
        String json = "{\"/path/photo.jpg\":{\"path\":\"/path/photo.jpg\",\"size\":1024,\"lastModifiedMillis\":1700000000000}}";
        when(mockStorage.readStateFile(anyString())).thenReturn(json);
        stateFile.load();

        FileState modified = new FileState("/path/photo.jpg", 2048, 1700000001000L);

        assertTrue(stateFile.needsMigration(modified));
    }

    @Test
    void testRecordMigration() {
        FileState fileState = new FileState("/path/photo.jpg", 1024, 1700000000000L);

        stateFile.recordMigration(fileState);

        assertEquals(1, stateFile.size());
        assertFalse(stateFile.needsMigration(fileState));
    }

    @Test
    void testSave() throws IOException {
        FileState fileState = new FileState("/path/photo.jpg", 1024, 1700000000000L);
        stateFile.recordMigration(fileState);

        stateFile.save();

        verify(mockStorage).writeStateFile(eq(MigrationStateFile.DEFAULT_STATE_FILE_NAME), anyString());
    }

    @Test
    void testCustomStateFileName() {
        MigrationStateFile customFile = new MigrationStateFile(mockStorage, "custom_state.json");

        assertEquals("custom_state.json", customFile.getStateFileName());
    }

    @Test
    void testDefaultStateFileName() {
        assertEquals(MigrationStateFile.DEFAULT_STATE_FILE_NAME, stateFile.getStateFileName());
    }
}
