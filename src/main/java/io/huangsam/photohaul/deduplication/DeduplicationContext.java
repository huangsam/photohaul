package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Context for photo deduplication, holding unique photos and statistics.
 */
class DeduplicationContext {
    private final Map<String, Photo> uniquePhotos = new LinkedHashMap<>();
    private int duplicateCount = 0;

    /**
     * Mark a photo as unique with a specific key.
     *
     * @param key   The identity key for the photo.
     * @param photo The photo.
     */
    public void addUnique(@NonNull String key, @NonNull Photo photo) {
        if (!uniquePhotos.containsKey(key)) {
            uniquePhotos.put(key, photo);
        } else {
            duplicateCount++;
        }
    }

    /**
     * Check if a key already exists in the unique collection.
     *
     * @param key The identity key.
     * @return true if the key exists.
     */
    public boolean contains(@NonNull String key) {
        return uniquePhotos.containsKey(key);
    }

    /**
     * Mark a photo as a duplicate.
     */
    public void markDuplicate() {
        duplicateCount++;
    }

    /**
     * Get the count of duplicates found.
     *
     * @return duplicate count.
     */
    public int getDuplicateCount() {
        return duplicateCount;
    }

    /**
     * Get all unique photos found.
     *
     * @return collection of unique photos.
     */
    @NonNull
    public Collection<Photo> getUniquePhotos() {
        return uniquePhotos.values();
    }
}
