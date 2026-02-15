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

public class TestSizeBasedStrategy {
    private final SizeBasedStrategy strategy = new SizeBasedStrategy();

    @Test
    void testDeduplicateSinglePhoto(@TempDir @NonNull Path tempDir) throws IOException {
        // Create a single photo
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "test content".getBytes());
        Photo photo = new Photo(testFile);
        List<Photo> photos = List.of(photo);
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        int duplicatesRemoved = strategy.processPhotos(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(1, uniquePhotos.size());
        String key = uniquePhotos.keySet().iterator().next();
        assertTrue(key.startsWith("size_12_")); // 12 bytes for "test content"
        assertTrue(key.endsWith("test.jpg"));
    }

    @Test
    void testDeduplicateMultiplePhotosDifferentSizes(@TempDir @NonNull Path tempDir) throws IOException {
        // Create photos with different sizes
        Path smallFile = tempDir.resolve("small.jpg");
        Path largeFile = tempDir.resolve("large.jpg");

        Files.write(smallFile, "small".getBytes());
        Files.write(largeFile, "much larger content here".getBytes());

        List<Photo> photos = List.of(new Photo(smallFile), new Photo(largeFile));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        int duplicatesRemoved = strategy.processPhotos(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(2, uniquePhotos.size());
    }

    @Test
    void testDeduplicateSameSizeDifferentContent(@TempDir @NonNull Path tempDir) throws IOException {
        // Create two files with same size but different content
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        Files.write(file1, "content1".getBytes()); // 8 bytes
        Files.write(file2, "content2".getBytes()); // 8 bytes

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        // This should delegate to PartialHashStrategy, which should delegate to FullHashStrategy
        // Since content is different, both should be kept
        int duplicatesRemoved = strategy.processPhotos(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(2, uniquePhotos.size());
    }

    @Test
    void testDeduplicateSameSizeSameContent(@TempDir @NonNull Path tempDir) throws IOException {
        // Create two files with same size and same content
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        byte[] content = "duplicate content".getBytes();
        Files.write(file1, content);
        Files.write(file2, content);

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        // Should deduplicate the duplicate
        int duplicatesRemoved = strategy.processPhotos(photos, uniquePhotos);

        assertEquals(1, duplicatesRemoved);
        assertEquals(1, uniquePhotos.size());
    }
}
