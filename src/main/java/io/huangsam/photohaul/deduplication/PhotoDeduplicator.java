package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles deduplication of photos using SHA-256 hashing.
 *
 * <p> This class identifies duplicate photos by computing their SHA-256 hash
 * and keeps only the first occurrence of each unique file.
 */
public class PhotoDeduplicator {
    private static final Logger LOG = getLogger(PhotoDeduplicator.class);
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Deduplicate a collection of photos based on their SHA-256 hash.
     *
     * <p> For each photo, calculate its SHA-256 hash. If multiple photos have
     * the same hash, only the first occurrence is kept. The order of photos
     * in the input collection determines which photo is kept.
     *
     * <p> Optimization: Uses multi-level deduplication:
     * 1. File size filtering (different sizes cannot be duplicates)
     * 2. Partial hashing (first 1KB) for same-size files
     * 3. Full SHA-256 hashing only when partial hashes match
     *
     * @param photos collection of photos to deduplicate
     * @return collection of unique photos (first occurrence of each hash)
     */
    @NotNull
    public Collection<Photo> deduplicate(@NotNull Collection<Photo> photos) {
        Map<Long, List<Photo>> photosBySize = groupBySize(photos);

        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();
        DeduplicationStrategy strategy = new SizeBasedStrategy();

        int duplicateCount = photosBySize.values().stream()
            .mapToInt(sizeGroup -> strategy.processPhotos(sizeGroup, uniquePhotos))
            .sum();

        LOG.info("Deduplication complete: {} unique photos, {} duplicates removed",
                uniquePhotos.size(), duplicateCount);
        return uniquePhotos.values();
    }

    /**
     * Group photos by file size, preserving order.
     */
    private @NonNull Map<Long, List<Photo>> groupBySize(@NonNull Collection<Photo> photos) {
        return photos.stream()
            .collect(Collectors.groupingBy(this::safeGetFileSize, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * Safely get file size, returning -1 on error.
     */
    private @NonNull Long safeGetFileSize(@NonNull Photo photo) {
        try {
            return getFileSize(photo);
        } catch (IOException e) {
            return -1L;
        }
    }

    /**
     * Get the file size of a photo.
     *
     * @param photo the photo
     * @return file size in bytes
     * @throws IOException if file cannot be accessed
     */
    private long getFileSize(@NotNull Photo photo) throws IOException {
        return Files.size(photo.path());
    }
}
