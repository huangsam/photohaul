package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deduplication strategy based on full SHA-256 hash.
 */
class FullHashStrategy implements DeduplicationStrategy {
    private static final Logger LOG = getLogger(FullHashStrategy.class);
    private static final String HASH_ALGORITHM = "SHA-256";

    @Override
    public int deduplicate(@NotNull List<Photo> photos, @NotNull Map<String, Photo> uniquePhotos) {
        return photos.stream()
            .mapToInt(photo -> processPhoto(photo, uniquePhotos))
            .sum();
    }

    private int processPhoto(@NotNull Photo photo, @NotNull Map<String, Photo> uniquePhotos) {
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

    private @NotNull String calculateHash(@NotNull Photo photo) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

        try (InputStream inputStream = Files.newInputStream(photo.path())) {
            byte[] buffer = new byte[65536]; // 64KB
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
    }

    private @NotNull String bytesToHex(byte @NotNull [] bytes) {
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
