package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deduplication strategy based on file size.
 * Photos with different sizes cannot be duplicates.
 */
class SizeBasedStrategy implements DeduplicationStrategy {
    private static final Logger LOG = getLogger(SizeBasedStrategy.class);

    @Override
    public int deduplicate(@NotNull List<Photo> photos, @NotNull Map<String, Photo> uniquePhotos) {
        if (photos.size() == 1) {
            return addUniquePhoto(photos.getFirst(), uniquePhotos);
        }

        // Group by partial hash for same-size photos
        DeduplicationStrategy nextStrategy = new PartialHashStrategy();
        return nextStrategy.deduplicate(photos, uniquePhotos);
    }

    private int addUniquePhoto(@NotNull Photo photo, @NotNull Map<String, Photo> uniquePhotos) {
        try {
            long size = Files.size(photo.path());
            String key = "size_" + size + "_" + photo.path();
            uniquePhotos.put(key, photo);
            LOG.trace("Added unique photo by size: {} (size: {})", photo.name(), size);
        } catch (IOException e) {
            uniquePhotos.put(java.util.UUID.randomUUID().toString(), photo);
        }
        return 0;
    }
}
