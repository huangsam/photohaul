package io.huangsam.photohaul.migration.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFileState {

    @Test
    void testValidConstruction() {
        FileState state = new FileState("/path/to/file.jpg", 1024, 1700000000000L);
        assertEquals("/path/to/file.jpg", state.path());
        assertEquals(1024, state.size());
        assertEquals(1700000000000L, state.lastModifiedMillis());
    }

    @Test
    void testNullPathThrowsException() {
        assertThrows(NullPointerException.class, () -> new FileState(null, 1024, 1700000000000L));
    }

    @Test
    void testBlankPathThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new FileState("  ", 1024, 1700000000000L));
    }

    @Test
    void testNegativeSizeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new FileState("/path/to/file.jpg", -1, 1700000000000L));
    }

    @Test
    void testNegativeTimestampThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new FileState("/path/to/file.jpg", 1024, -1));
    }

    @Test
    void testMatchesReturnsTrueForIdenticalSizeAndTime() {
        FileState state1 = new FileState("/path/file1.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file2.jpg", 1024, 1700000000000L);
        assertTrue(state1.matches(state2));
    }

    @Test
    void testMatchesReturnsFalseForDifferentSize() {
        FileState state1 = new FileState("/path/file1.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file1.jpg", 2048, 1700000000000L);
        assertFalse(state1.matches(state2));
    }

    @Test
    void testMatchesReturnsFalseForDifferentTime() {
        FileState state1 = new FileState("/path/file1.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file1.jpg", 1024, 1700000001000L);
        assertFalse(state1.matches(state2));
    }

    @Test
    void testZeroSizeAllowed() {
        FileState state = new FileState("/path/to/empty.jpg", 0, 0);
        assertEquals(0, state.size());
        assertEquals(0, state.lastModifiedMillis());
    }
}
