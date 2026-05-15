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

public class TestPartialHashStrategy {
    private final PartialHashStrategy strategy = new PartialHashStrategy();

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
    void testDeduplicateSamePartialHashDelegation(@TempDir @NonNull Path tempDir) throws IOException {
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
        DeduplicationContext context = new DeduplicationContext();

        // Should delegate to the next strategy because partial hashes match
        strategy.process(photos, context, (group, ctx, n) -> {
            ctx.addUnique("delegated_1", group.get(0));
            ctx.addUnique("delegated_2", group.get(1));
        });

        assertEquals(0, context.getDuplicateCount());
        assertEquals(2, context.getUniquePhotos().size());
    }

    @Test
    void testDeduplicateDifferentPartialHashes(@TempDir @NonNull Path tempDir) throws IOException {
        // Create files with completely different content
        Path file1 = tempDir.resolve("file1.jpg");
        Path file2 = tempDir.resolve("file2.jpg");

        Files.write(file1, "content one".getBytes());
        Files.write(file2, "content two".getBytes());

        List<Photo> photos = List.of(new Photo(file1), new Photo(file2));
        DeduplicationContext context = new DeduplicationContext();

        // Should keep both without delegation because partial hashes differ
        strategy.process(photos, context, (group, ctx, n) -> {
            throw new RuntimeException("Should not delegate!");
        });

        assertEquals(0, context.getDuplicateCount());
        assertEquals(2, context.getUniquePhotos().size());
    }
}
