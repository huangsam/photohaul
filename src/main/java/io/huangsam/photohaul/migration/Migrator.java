package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

public interface Migrator {
    void migratePhoto(Photo photo);

    long getSuccessCount();

    long getFailureCount();
}
