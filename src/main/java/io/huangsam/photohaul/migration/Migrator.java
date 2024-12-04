package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

import java.util.Collection;

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
     * @param photos {@code Collection} of photo records
     */
    void migratePhotos(Collection<Photo> photos);

    /**
     * Get success count.
     *
     * @return Number of successful migration ops
     */
    long getSuccessCount();

    /**
     * Get failure count.
     *
     * @return Number of failed migration ops
     */
    long getFailureCount();
}
