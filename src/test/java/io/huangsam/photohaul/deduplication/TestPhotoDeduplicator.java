package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.model.PhotoBuilder;
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
        PhotoBuilder pb = new PhotoBuilder();
        List<Photo> photos = List.of(
                pb.fill(getStaticResources().resolve("bauerlite.jpg")).build(),
                pb.fill(getStaticResources().resolve("salad.jpg")).build()
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
        PhotoBuilder pb = new PhotoBuilder();
        List<Photo> photos = List.of(
                pb.fill(original).build(),
                pb.fill(duplicate1).build(),
                pb.fill(duplicate2).build(),
                pb.fill(unique).build()
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
        PhotoBuilder pb = new PhotoBuilder();
        List<Photo> photos = List.of(
                pb.fill(getStaticResources().resolve("bauerlite.jpg")).build()
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

        PhotoBuilder pb = new PhotoBuilder();
        List<Photo> photos = List.of(
                pb.fill(first).build(),
                pb.fill(second).build(),
                pb.fill(third).build()
        );

        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photos);

        // Should only keep the first occurrence
        assertEquals(1, uniquePhotos.size());
        List<Photo> uniqueList = new ArrayList<>(uniquePhotos);
        assertEquals("first.jpg", uniqueList.getFirst().name());
    }

    @Test
    void testDeduplicateWithNonExistentFile(@TempDir Path tempDir) {
        // Create a photo with a path that doesn't exist
        Path nonExistent = tempDir.resolve("nonexistent.jpg");

        PhotoBuilder pb = new PhotoBuilder();
        List<Photo> photos = List.of(
                pb.fill(nonExistent).build()
        );

        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photos);

        // Should still include the photo (fail-safe behavior)
        assertEquals(1, uniquePhotos.size());
    }
}
