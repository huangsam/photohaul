package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPhotoDeduplicator {
    @Test
    void testDeduplicateWithNoDuplicates() {
        // Use existing test files which are unique
        List<Photo> photos = List.of(
                new Photo(getStaticResources().resolve("bauerlite.jpg")),
                new Photo(getStaticResources().resolve("salad.jpg"))
        );

        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photos);

        assertEquals(2, uniquePhotos.size());
    }

    @Test
    void testDeduplicateWithDuplicates(@TempDir Path tempDir) throws IOException {
        // Create duplicate files
        Path original = tempDir.resolve("original.jpg");
        Path duplicate1 = tempDir.resolve("duplicate1.jpg");
        Path duplicate2 = tempDir.resolve("duplicate2.jpg");
        Path unique = tempDir.resolve("unique.jpg");

        // Write same content to original and duplicates
        byte[] content1 = "Test photo content for duplicate detection".getBytes();
        Files.write(original, content1);
        Files.write(duplicate1, content1);
        Files.write(duplicate2, content1);

        // Write different content to unique file
        byte[] content2 = "Different photo content".getBytes();
        Files.write(unique, content2);

        // Create Photo objects
        List<Photo> photos = List.of(
                new Photo(original),
                new Photo(duplicate1),
                new Photo(duplicate2),
                new Photo(unique)
        );

        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photos);

        // Should only have 2 unique photos (original and unique)
        assertEquals(2, uniquePhotos.size());

        // Verify the first occurrence is kept
        List<Photo> uniqueList = new ArrayList<>(uniquePhotos);
        assertEquals("original.jpg", uniqueList.get(0).name());
        assertEquals("unique.jpg", uniqueList.get(1).name());
    }

    @Test
    void testDeduplicateWithEmptyCollection() {
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(List.of());

        assertTrue(uniquePhotos.isEmpty());
    }

    @Test
    void testDeduplicateWithSinglePhoto() {
        List<Photo> photos = List.of(
                new Photo(getStaticResources().resolve("bauerlite.jpg"))
        );

        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photos);

        assertEquals(1, uniquePhotos.size());
    }

    @Test
    void testDeduplicateKeepsFirstOccurrence(@TempDir Path tempDir) throws IOException {
        // Create three files with same content but different names
        Path first = tempDir.resolve("first.jpg");
        Path second = tempDir.resolve("second.jpg");
        Path third = tempDir.resolve("third.jpg");

        byte[] content = "Same content for all files".getBytes();
        Files.write(first, content);
        Files.write(second, content);
        Files.write(third, content);

        List<Photo> photos = List.of(
                new Photo(first),
                new Photo(second),
                new Photo(third)
        );

        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photos);

        // Should only keep the first occurrence
        assertEquals(1, uniquePhotos.size());
        List<Photo> uniqueList = new ArrayList<>(uniquePhotos);
        assertEquals("first.jpg", uniqueList.get(0).name());
    }

    @Test
    void testDeduplicateWithNonExistentFile(@TempDir Path tempDir) {
        // Create a photo with a path that doesn't exist
        Path nonExistent = tempDir.resolve("nonexistent.jpg");

        List<Photo> photos = List.of(
                new Photo(nonExistent)
        );

        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photos);

        // Should still include the photo (fail-safe behavior)
        assertEquals(1, uniquePhotos.size());
    }
}
