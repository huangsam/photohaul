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
import java.util.Map;

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
     * @param photos collection of photos to deduplicate
     * @return collection of unique photos (first occurrence of each hash)
     */
    @NotNull
    public Collection<Photo> deduplicate(@NotNull Collection<Photo> photos) {
        Map<String, Photo> uniquePhotos = new LinkedHashMap<>();
        int duplicateCount = 0;

        for (Photo photo : photos) {
            try {
                String hash = calculateHash(photo);
                if (!uniquePhotos.containsKey(hash)) {
                    uniquePhotos.put(hash, photo);
                    LOG.trace("Added unique photo: {} (hash: {})", photo.name(), hash);
                } else {
                    duplicateCount++;
                    Photo original = uniquePhotos.get(hash);
                    LOG.debug("Skipping duplicate: {} (original: {}, hash: {})", 
                            photo.name(), original.name(), hash);
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                LOG.warn("Cannot calculate hash for {}: {}, including as unique", 
                        photo.name(), e.getMessage());
                // If we can't calculate hash, include the photo to avoid data loss
                uniquePhotos.put(photo.path().toString(), photo);
            }
        }

        LOG.info("Deduplication complete: {} unique photos, {} duplicates removed", 
                uniquePhotos.size(), duplicateCount);
        return uniquePhotos.values();
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
     * Convert byte array to hexadecimal string.
     *
     * @param bytes byte array to convert
     * @return hex-encoded string
     */
    @NotNull
    private String bytesToHex(@NotNull byte[] bytes) {
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
