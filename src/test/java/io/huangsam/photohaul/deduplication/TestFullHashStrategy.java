package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFullHashStrategy {
    private final FullHashStrategy strategy = new FullHashStrategy();

    @Test
    void testDeduplicateSinglePhoto() {
        // Create a single photo
        Photo photo = new Photo(Path.of("test.jpg"));
        List<Photo> photos = List.of(photo);
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        int duplicatesRemoved = strategy.deduplicate(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(1, uniquePhotos.size());
        // Should have a hash key
        assertTrue(uniquePhotos.keySet().iterator().next().length() > 10); // SHA-256 hex is 64 chars
    }

    @Test
    void testDeduplicateIdenticalFiles(@TempDir @NonNull Path tempDir) throws IOException {
        // Create two identical files
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        byte[] content = "This is identical content for duplicate detection".getBytes();
        Files.write(file1, content);
        Files.write(file2, content);

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        int duplicatesRemoved = strategy.deduplicate(photos, uniquePhotos);

        assertEquals(1, duplicatesRemoved);
        assertEquals(1, uniquePhotos.size());
    }

    @Test
    void testDeduplicateDifferentFiles(@TempDir @NonNull Path tempDir) throws IOException {
        // Create files with different content
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        Files.write(file1, "Content of first file".getBytes());
        Files.write(file2, "Content of second file".getBytes());

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        int duplicatesRemoved = strategy.deduplicate(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(2, uniquePhotos.size());
    }

    @Test
    void testDeduplicateMultipleDuplicates(@TempDir @NonNull Path tempDir) throws IOException {
        // Create multiple identical files and one different
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");
        Path file3 = tempDir.resolve("file3.jpg");
        Path unique = tempDir.resolve("unique.jpg");

        byte[] duplicateContent = "Duplicate content".getBytes();
        byte[] uniqueContent = "Unique content".getBytes();

        Files.write(file1, duplicateContent);
        Files.write(file2, duplicateContent);
        Files.write(file3, duplicateContent);
        Files.write(unique, uniqueContent);

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2), new Photo(file3), new Photo(unique));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        int duplicatesRemoved = strategy.deduplicate(photos, uniquePhotos);

        assertEquals(2, duplicatesRemoved); // 2 duplicates removed from 3 identical files
        assertEquals(2, uniquePhotos.size()); // 1 from duplicates + 1 unique
    }

    @Test
    void testDeduplicateWithNonExistentFile() {
        // Create a photo with non-existent path
        Path nonExistent = Path.of("nonexistent.jpg");
        Photo photo = new Photo(nonExistent);
        List<Photo> photos = List.of(photo);
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        int duplicatesRemoved = strategy.deduplicate(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(1, uniquePhotos.size());
        // Should have a UUID fallback key for error case
        String key = uniquePhotos.keySet().iterator().next();
        assertTrue(key.contains("error_") || key.length() == 36); // UUID length
    }
}
