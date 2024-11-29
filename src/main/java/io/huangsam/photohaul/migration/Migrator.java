package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

import java.util.Collection;

public interface Migrator {
    void migratePhotos(Collection<Photo> photos);

    long getSuccessCount();

    long getFailureCount();
}
