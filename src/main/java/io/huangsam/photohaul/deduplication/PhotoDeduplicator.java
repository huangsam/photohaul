package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        Map<Long, List<Photo>> photosBySize = groupBy(photos, this::safeGetFileSize);

        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();
        int duplicateCount = photosBySize.values().stream()
            .mapToInt(sizeGroup -> processSizeGroup(sizeGroup, uniquePhotos))
            .sum();

        LOG.info("Deduplication complete: {} unique photos, {} duplicates removed",
                uniquePhotos.size(), duplicateCount);
        return uniquePhotos.values();
    }

    /**
     * Group photos by a key function, preserving order.
     */
    private <K> Map<K, List<Photo>> groupBy(Collection<Photo> photos, java.util.function.Function<Photo, K> keyFunction) {
        return photos.stream()
            .collect(Collectors.groupingBy(keyFunction, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * Safely get file size, returning -1 on error.
     */
    private Long safeGetFileSize(Photo photo) {
        try {
            return getFileSize(photo);
        } catch (IOException e) {
            return -1L;
        }
    }

    /**
     * Process a group of photos with the same size.
     */
    private int processSizeGroup(List<Photo> sizeGroup, Map<String, Photo> uniquePhotos) {
        return sizeGroup.size() == 1
            ? addUniquePhotoBySize(sizeGroup.get(0), uniquePhotos)
            : deduplicateByPartialHash(sizeGroup, uniquePhotos);
    }

    /**
     * Add a photo that is unique by size.
     */
    private int addUniquePhotoBySize(Photo photo, Map<String, Photo> uniquePhotos) {
        try {
            long size = getFileSize(photo);
            String key = "size_" + size + "_" + photo.path().toString();
            uniquePhotos.put(key, photo);
            LOG.trace("Added unique photo by size: {} (size: {})", photo.name(), size);
        } catch (IOException e) {
            uniquePhotos.put(java.util.UUID.randomUUID().toString(), photo);
        }
        return 0;
    }

    /**
     * Calculate SHA-256 hash for a photo file.
     *
     * @param photo the photo to hash
     * @return hex-encoded SHA-256 hash of the file content
     * @throws IOException if file cannot be read
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     */
    @NotNull
    private String calculateHash(@NotNull Photo photo) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

        try (InputStream inputStream = Files.newInputStream(photo.path())) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
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

    /**
     * Calculate partial SHA-256 hash for the first 1KB of a photo file.
     *
     * @param photo the photo to hash
     * @return hex-encoded SHA-256 hash of the first 1KB
     * @throws IOException if file cannot be read
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     */
    @NotNull
    private String calculatePartialHash(@NotNull Photo photo) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

        try (InputStream inputStream = Files.newInputStream(photo.path())) {
            byte[] buffer = new byte[1024]; // 1KB
            int bytesRead = inputStream.read(buffer);
            if (bytesRead > 0) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
    }

    /**
     * Deduplicate a group of photos with the same file size using partial hashing.
     *
     * @param photos photos with the same size
     * @param uniquePhotos map to add unique photos to
     * @return number of duplicates found
     */
    private int deduplicateByPartialHash(@NotNull List<Photo> photos, @NotNull Map<String, Photo> uniquePhotos) {
        Map<String, List<Photo>> photosByPartialHash = groupBy(photos, this::safeCalculatePartialHash);

        return photosByPartialHash.values().stream()
            .mapToInt(partialGroup -> processPartialHashGroup(partialGroup, uniquePhotos))
            .sum();
    }

    /**
     * Safely calculate partial hash, returning error key on failure.
     */
    private String safeCalculatePartialHash(Photo photo) {
        try {
            return calculatePartialHash(photo);
        } catch (IOException | NoSuchAlgorithmException e) {
            return "error_" + java.util.UUID.randomUUID().toString();
        }
    }

    /**
     * Process a group of photos with the same partial hash.
     */
    private int processPartialHashGroup(List<Photo> partialGroup, Map<String, Photo> uniquePhotos) {
        return partialGroup.size() == 1
            ? addUniquePhotoByPartialHash(partialGroup.get(0), uniquePhotos)
            : deduplicateByFullHash(partialGroup, uniquePhotos);
    }

    /**
     * Add a photo that is unique by partial hash.
     */
    private int addUniquePhotoByPartialHash(Photo photo, Map<String, Photo> uniquePhotos) {
        try {
            String partialHash = calculatePartialHash(photo);
            String key = "partial_" + partialHash + "_" + photo.path().toString();
            uniquePhotos.put(key, photo);
            LOG.trace("Added unique photo by partial hash: {}", photo.name());
        } catch (IOException | NoSuchAlgorithmException e) {
            uniquePhotos.put(java.util.UUID.randomUUID().toString(), photo);
        }
        return 0;
    }

    /**
     * Deduplicate a group of photos using full SHA-256 hashing.
     *
     * @param photos photos to deduplicate
     * @param uniquePhotos map to add unique photos to
     * @return number of duplicates found
     */
    private int deduplicateByFullHash(@NotNull List<Photo> photos, @NotNull Map<String, Photo> uniquePhotos) {
        return photos.stream()
            .mapToInt(photo -> processPhotoForFullHash(photo, uniquePhotos))
            .sum();
    }

    /**
     * Process a single photo for full hash deduplication.
     */
    private int processPhotoForFullHash(Photo photo, Map<String, Photo> uniquePhotos) {
        try {
            String hash = calculateHash(photo);
            if (!uniquePhotos.containsKey(hash)) {
                uniquePhotos.put(hash, photo);
                LOG.trace("Added unique photo: {} (hash: {})", photo.name(), hash);
                return 0;
            } else {
                Photo original = uniquePhotos.get(hash);
                LOG.debug("Skipping duplicate: {} (original: {}, hash: {})",
                        photo.name(), original.name(), hash);
                return 1;
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            LOG.warn("Cannot calculate hash for {}: {}, including as unique",
                    photo.name(), e.getMessage());
            uniquePhotos.put(java.util.UUID.randomUUID().toString(), photo);
            return 0;
        }
    }

    /**
     * Convert byte array to hexadecimal string.
     *
     * @param bytes byte array to convert
     * @return hex-encoded string
     */
    @NotNull
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
