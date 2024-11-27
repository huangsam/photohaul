package io.huangsam.photohaul.migrate;

import io.huangsam.photohaul.model.Photo;

public interface Migrator {
    void migratePhoto(Photo photo);

    long getSuccessCount();

    long getFailureCount();
}
