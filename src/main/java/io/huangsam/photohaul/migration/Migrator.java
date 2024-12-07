package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Defines the contract for a photo migration service.
 *
 * <p> This contract allows consumers to migrate photos without knowing whether
 * the operations happen locally or in the cloud.
 */
public interface Migrator {
    /**
     * Migrate a collection of photos to a specific target.
     *
     * <p> For each photo record, do the following:
     *
     * <ul>
     *     <li>Use its fields to identify target location</li>
     *     <li>Record successful operations</li>
     *     <li>Record failed operations</li>
     * </ul>
     *
     * @param photos collection of photo records
     */
    void migratePhotos(@NotNull Collection<Photo> photos);

    /**
     * Get success count.
     *
     * @return number of successful migration ops
     */
    long getSuccessCount();

    /**
     * Get failure count.
     *
     * @return number of failed migration ops
     */
    long getFailureCount();
}
