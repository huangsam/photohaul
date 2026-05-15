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

public class TestSizeBasedStrategy {
    private final SizeBasedStrategy strategy = new SizeBasedStrategy();

    @Test
    void testDeduplicateSinglePhoto(@TempDir @NonNull Path tempDir) throws IOException {
        // Create a single photo
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "test content".getBytes());
        Photo photo = new Photo(testFile);
        List<Photo> photos = List.of(photo);
        DeduplicationContext context = new DeduplicationContext();

        strategy.process(photos, context, (p, c, n) -> { });

        assertEquals(0, context.getDuplicateCount());
        assertEquals(1, context.getUniquePhotos().size());
    }

    @Test
    void testDeduplicateMultiplePhotosDifferentSizes(@TempDir @NonNull Path tempDir) throws IOException {
        // Create photos with different sizes
        Path smallFile = tempDir.resolve("small.jpg");
        Path largeFile = tempDir.resolve("large.jpg");

        Files.write(smallFile, "small".getBytes());
        Files.write(largeFile, "much larger content here".getBytes());

        List<Photo> photos = List.of(new Photo(smallFile), new Photo(largeFile));
        DeduplicationContext context = new DeduplicationContext();

        // SizeBasedStrategy handles single photos directly, but for multiple photos it calls 'next'
        // In the real app, PhotoDeduplicator groups by size FIRST, so SizeBasedStrategy usually
        // sees groups of the same size or single photos.
        for (Photo p : photos) {
            strategy.process(List.of(p), context, (group, ctx, n) -> { });
        }

        assertEquals(0, context.getDuplicateCount());
        assertEquals(2, context.getUniquePhotos().size());
    }

    @Test
    void testDeduplicateSameSizeDelegation(@TempDir @NonNull Path tempDir) throws IOException {
        // Create two files with same size
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        Files.write(file1, "content1".getBytes());
        Files.write(file2, "content2".getBytes());

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        DeduplicationContext context = new DeduplicationContext();

        // Should delegate to the next strategy
        strategy.process(photos, context, (group, ctx, n) -> {
            ctx.addUnique("delegated_1", group.get(0));
            ctx.addUnique("delegated_2", group.get(1));
        });

        assertEquals(0, context.getDuplicateCount());
        assertEquals(2, context.getUniquePhotos().size());
    }
}
