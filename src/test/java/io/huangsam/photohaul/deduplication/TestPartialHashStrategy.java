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

public class TestPartialHashStrategy {
    private final PartialHashStrategy strategy = new PartialHashStrategy();

    @Test
    void testDeduplicateSinglePhoto() {
        // Create a single photo
        Photo photo = new Photo(Path.of("test.jpg"));
        List<Photo> photos = List.of(photo);
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        int duplicatesRemoved = strategy.processPhotos(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(1, uniquePhotos.size());
    }

    @Test
    void testDeduplicateSamePartialHashDifferentRest(@TempDir @NonNull Path tempDir) throws IOException {
        // Create files with same first 1KB but different content after
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        // Create content where first 1KB is identical
        byte[] prefix = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            prefix[i] = (byte) (i % 256);
        }

        byte[] content1 = new byte[1024 + 10];
        byte[] content2 = new byte[1024 + 10];

        System.arraycopy(prefix, 0, content1, 0, 1024);
        System.arraycopy(prefix, 0, content2, 0, 1024);

        // Different content after the first 1KB
        content1[1024] = 1;
        content2[1024] = 2;

        Files.write(file1, content1);
        Files.write(file2, content2);

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        // Should delegate to FullHashStrategy and keep both since full content differs
        int duplicatesRemoved = strategy.processPhotos(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(2, uniquePhotos.size());
    }

    @Test
    void testDeduplicateIdenticalFiles(@TempDir @NonNull Path tempDir) throws IOException {
        // Create two identical files
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        byte[] content = "identical content for both files".getBytes();
        Files.write(file1, content);
        Files.write(file2, content);

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        // Should deduplicate since they're identical
        int duplicatesRemoved = strategy.processPhotos(photos, uniquePhotos);

        assertEquals(1, duplicatesRemoved);
        assertEquals(1, uniquePhotos.size());
    }

    @Test
    void testDeduplicateCompletelyDifferentFiles(@TempDir @NonNull Path tempDir) throws IOException {
        // Create files with completely different content
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        Files.write(file1, "content one".getBytes());
        Files.write(file2, "content two".getBytes());

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();

        // Should keep both since they're different
        int duplicatesRemoved = strategy.processPhotos(photos, uniquePhotos);

        assertEquals(0, duplicatesRemoved);
        assertEquals(2, uniquePhotos.size());
    }
}
