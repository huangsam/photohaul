package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

import java.util.Collection;

public class DropboxMigrator implements Migrator {
    @Override
    public void migratePhotos(Collection<Photo> photos) {

    }

    @Override
    public long getSuccessCount() {
        return 0L;
    }

    @Override
    public long getFailureCount() {
        return 0L;
    }
}
