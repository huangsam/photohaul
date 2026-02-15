package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for photo deduplication at different levels.
 */
interface DeduplicationStrategy {
    /**
     * Process a group of photos and add unique ones to the result map.
     *
     * @param photos the photos to process
     * @param uniquePhotos the map to add unique photos to
     * @return the number of duplicates found
     */
    int processPhotos(@NotNull List<Photo> photos, @NotNull Map<String, Photo> uniquePhotos);
}
