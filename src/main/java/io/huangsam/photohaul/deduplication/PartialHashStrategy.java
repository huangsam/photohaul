package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deduplication strategy based on partial SHA-256 hash (first 1KB).
 */
class PartialHashStrategy implements DeduplicationStrategy {
    private static final Logger LOG = getLogger(PartialHashStrategy.class);
    private static final String HASH_ALGORITHM = "SHA-256";

    @Override
    public int processPhotos(@NotNull List<Photo> photos, @NotNull Map<String, Photo> uniquePhotos) {
        Map<String, List<Photo>> photosByPartialHash = groupByPartialHash(photos);

        return photosByPartialHash.values().stream()
            .mapToInt(group -> processPartialHashGroup(group, uniquePhotos))
            .sum();
    }

    private Map<String, List<Photo>> groupByPartialHash(@NotNull List<Photo> photos) {
        return photos.stream()
            .collect(Collectors.groupingBy(this::safeCalculatePartialHash,
                         LinkedHashMap::new, Collectors.toList()));
    }

    private int processPartialHashGroup(@NotNull List<Photo> group, @NotNull Map<String, Photo> uniquePhotos) {
        if (group.size() == 1) {
            return addUniquePhotoByPartialHash(group.getFirst(), uniquePhotos);
        }

        // Use full hash strategy for groups with same partial hash
        DeduplicationStrategy nextStrategy = new FullHashStrategy();
        return nextStrategy.processPhotos(group, uniquePhotos);
    }

    private int addUniquePhotoByPartialHash(@NotNull Photo photo, @NotNull Map<String, Photo> uniquePhotos) {
        try {
            String partialHash = calculatePartialHash(photo);
            String key = "partial_" + partialHash + "_" + photo.path();
            uniquePhotos.put(key, photo);
            LOG.trace("Added unique photo by partial hash: {}", photo.name());
        } catch (IOException | NoSuchAlgorithmException e) {
            uniquePhotos.put(java.util.UUID.randomUUID().toString(), photo);
        }
        return 0;
    }

    private @NotNull String safeCalculatePartialHash(@NotNull Photo photo) {
        try {
            return calculatePartialHash(photo);
        } catch (IOException | NoSuchAlgorithmException e) {
            return "error_" + java.util.UUID.randomUUID();
        }
    }

    private @NotNull String calculatePartialHash(@NotNull Photo photo) throws IOException, NoSuchAlgorithmException {
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
