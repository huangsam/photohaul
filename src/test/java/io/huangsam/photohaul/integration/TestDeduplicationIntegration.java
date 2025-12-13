package io.huangsam.photohaul.integration;

import io.huangsam.photohaul.deduplication.PhotoDeduplicator;
import io.huangsam.photohaul.migration.PathMigrator;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PathWalker;
import io.huangsam.photohaul.traversal.PhotoCollector;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test to verify the full workflow with deduplication:
 * 1. Traverse source directory
 * 2. Collect photos
 * 3. Deduplicate based on SHA-256
 * 4. Migrate unique photos only
 */
public class TestDeduplicationIntegration {
    @Test
    @SuppressWarnings("resource")
    void testFullWorkflowWithDuplicates(@TempDir @NonNull Path tempDir) throws IOException {
        // Setup: Create source and target directories
        Path sourceDir = tempDir.resolve("source");
        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(sourceDir);
        Files.createDirectories(targetDir);

        // Create test files: 2 unique + 2 duplicates = 4 total files
        byte[] content1 = "First unique photo content".getBytes();
        byte[] content2 = "Second unique photo content".getBytes();

        Path photo1 = sourceDir.resolve("photo1.jpg");
        Path photo1Dup = sourceDir.resolve("photo1-duplicate.jpg");
        Path photo2 = sourceDir.resolve("photo2.jpg");
        Path photo2Dup = sourceDir.resolve("photo2-copy.jpg");

        Files.write(photo1, content1);
        Files.write(photo1Dup, content1);  // Duplicate of photo1
        Files.write(photo2, content2);
        Files.write(photo2Dup, content2);  // Duplicate of photo2

        // Step 1: Traverse and collect photos
        PhotoCollector photoCollector = new PhotoCollector();
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(
                Files::isRegularFile
        ));
        PathWalker pathWalker = new PathWalker(sourceDir, pathRuleSet);
        pathWalker.traverse(photoCollector);

        // Verify all 4 files were collected
        assertEquals(4, photoCollector.getPhotos().size());

        // Step 2: Deduplicate photos
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photoCollector.getPhotos());

        // Verify only 2 unique photos remain
        assertEquals(2, uniquePhotos.size());

        // Step 3: Migrate unique photos
        PhotoResolver photoResolver = new PhotoResolver(List.of());
        PathMigrator migrator = new PathMigrator(targetDir, photoResolver, PathMigrator.Action.COPY);
        migrator.migratePhotos(uniquePhotos);

        // Verify migration succeeded for 2 unique photos
        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        // Verify target directory has exactly 2 files (the unique ones)
        // Files are placed in "Other" subdirectory by PhotoResolver
        Path otherDir = targetDir.resolve("Other");
        if (Files.exists(otherDir)) {
            long fileCount = Files.list(otherDir).count();
            assertEquals(2, fileCount);
        }
    }

    @Test
    @SuppressWarnings("resource")
    void testFullWorkflowWithNoDuplicates(@TempDir @NonNull Path tempDir) throws IOException {
        // Setup: Create source and target directories
        Path sourceDir = tempDir.resolve("source");
        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(sourceDir);
        Files.createDirectories(targetDir);

        // Create test files: 3 unique files
        Files.write(sourceDir.resolve("unique1.jpg"), "Content 1".getBytes());
        Files.write(sourceDir.resolve("unique2.jpg"), "Content 2".getBytes());
        Files.write(sourceDir.resolve("unique3.jpg"), "Content 3".getBytes());

        // Traverse and collect
        PhotoCollector photoCollector = new PhotoCollector();
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(
                Files::isRegularFile
        ));
        PathWalker pathWalker = new PathWalker(sourceDir, pathRuleSet);
        pathWalker.traverse(photoCollector);

        assertEquals(3, photoCollector.getPhotos().size());

        // Deduplicate (should keep all 3)
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photoCollector.getPhotos());

        assertEquals(3, uniquePhotos.size());

        // Migrate
        PhotoResolver photoResolver = new PhotoResolver(List.of());
        PathMigrator migrator = new PathMigrator(targetDir, photoResolver, PathMigrator.Action.COPY);
        migrator.migratePhotos(uniquePhotos);

        assertEquals(3, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }
}
