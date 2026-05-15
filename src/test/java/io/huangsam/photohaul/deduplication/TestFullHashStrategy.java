package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFullHashStrategy {
    private final FullHashStrategy strategy = new FullHashStrategy();

    @Test
    void testDeduplicateSinglePhoto(@TempDir @NonNull Path tempDir) throws IOException {
        // Create a single photo
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "test content".getBytes());
        Photo photo = new Photo(testFile);
        List<Photo> photos = List.of(photo);
        DeduplicationContext context = new DeduplicationContext();

        strategy.process(photos, context, null);

        assertEquals(0, context.getDuplicateCount());
        assertEquals(1, context.getUniquePhotos().size());
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
        DeduplicationContext context = new DeduplicationContext();

        strategy.process(photos, context, null);

        assertEquals(1, context.getDuplicateCount());
        assertEquals(1, context.getUniquePhotos().size());
    }

    @Test
    void testDeduplicateDifferentFiles(@TempDir @NonNull Path tempDir) throws IOException {
        // Create files with different content
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        Files.write(file1, "Content of first file".getBytes());
        Files.write(file2, "Content of second file".getBytes());

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        DeduplicationContext context = new DeduplicationContext();

        strategy.process(photos, context, null);

        assertEquals(0, context.getDuplicateCount());
        assertEquals(2, context.getUniquePhotos().size());
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
        DeduplicationContext context = new DeduplicationContext();

        strategy.process(photos, context, null);

        assertEquals(2, context.getDuplicateCount()); // 2 duplicates removed from 3 identical files
        assertEquals(2, context.getUniquePhotos().size()); // 1 from duplicates + 1 unique
    }

    @Test
    void testDeduplicateWithNonExistentFile() {
        // Create a photo with non-existent path
        Path nonExistent = Path.of("nonexistent.jpg");
        Photo photo = new Photo(nonExistent);
        List<Photo> photos = List.of(photo);
        DeduplicationContext context = new DeduplicationContext();

        strategy.process(photos, context, null);

        assertEquals(0, context.getDuplicateCount());
        assertEquals(1, context.getUniquePhotos().size());
    }
}
