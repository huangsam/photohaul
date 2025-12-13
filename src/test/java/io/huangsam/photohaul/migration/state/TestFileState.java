package io.huangsam.photohaul.migration.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    @Test
    void testEmptyPathThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new FileState("", 1024, 1700000000000L));
    }

    @Test
    void testPathWithOnlyWhitespaceThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new FileState("\t\n", 1024, 1700000000000L));
    }

    @Test
    void testMatchesReturnsFalseForBothDifferent() {
        FileState state1 = new FileState("/path/file1.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file1.jpg", 2048, 1700000001000L);
        assertFalse(state1.matches(state2));
    }

    @Test
    void testMatchesIsSymmetric() {
        FileState state1 = new FileState("/path/file1.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file2.jpg", 1024, 1700000000000L);
        assertEquals(state1.matches(state2), state2.matches(state1));
    }

    @Test
    void testLargeSizeValue() {
        long largeSize = Long.MAX_VALUE;
        FileState state = new FileState("/path/file.jpg", largeSize, 1700000000000L);
        assertEquals(largeSize, state.size());
    }

    @Test
    void testLargeTimestampValue() {
        long largeTimestamp = Long.MAX_VALUE;
        FileState state = new FileState("/path/file.jpg", 1024, largeTimestamp);
        assertEquals(largeTimestamp, state.lastModifiedMillis());
    }

    @Test
    void testPathWithSpecialCharacters() {
        String specialPath = "/path/to/file with spaces & special (chars).jpg";
        FileState state = new FileState(specialPath, 1024, 1700000000000L);
        assertEquals(specialPath, state.path());
    }

    @Test
    void testPathWithUnicodeCharacters() {
        String unicodePath = "/photos/日本語/photo.jpg";
        FileState state = new FileState(unicodePath, 1024, 1700000000000L);
        assertEquals(unicodePath, state.path());
    }

    @Test
    void testRecordEquality() {
        FileState state1 = new FileState("/path/file.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file.jpg", 1024, 1700000000000L);
        assertEquals(state1, state2);
    }

    @Test
    void testRecordInequalityDifferentPath() {
        FileState state1 = new FileState("/path/file1.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file2.jpg", 1024, 1700000000000L);
        assertNotEquals(state1, state2);
    }

    @Test
    void testRecordInequalityDifferentSize() {
        FileState state1 = new FileState("/path/file.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file.jpg", 2048, 1700000000000L);
        assertNotEquals(state1, state2);
    }

    @Test
    void testRecordInequalityDifferentTimestamp() {
        FileState state1 = new FileState("/path/file.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file.jpg", 1024, 1700000001000L);
        assertNotEquals(state1, state2);
    }

    @Test
    void testHashCodeConsistency() {
        FileState state1 = new FileState("/path/file.jpg", 1024, 1700000000000L);
        FileState state2 = new FileState("/path/file.jpg", 1024, 1700000000000L);
        assertEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    void testToStringContainsAllFields() {
        FileState state = new FileState("/path/file.jpg", 1024, 1700000000000L);
        String str = state.toString();
        assertTrue(str.contains("/path/file.jpg"));
        assertTrue(str.contains("1024"));
        assertTrue(str.contains("1700000000000"));
    }
}
