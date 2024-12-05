package io.huangsam.photohaul.migration;

import com.dropbox.core.v2.DbxClientV2;
import io.huangsam.photohaul.model.Photo;
import org.slf4j.Logger;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class DropboxMigrator implements Migrator {
    private static final Logger LOG = getLogger(DropboxMigrator.class);

    private final DbxClientV2 dropboxClient;
    private final PhotoResolver photoResolver;

    private long successCount = 0L;
    private long failureCount = 0L;

    public DropboxMigrator(DbxClientV2 dropboxClient, PhotoResolver photoResolver) {
        this.dropboxClient = dropboxClient;
        this.photoResolver = photoResolver;
    }

    @Override
    public void migratePhotos(Collection<Photo> photos) {

    }

    @Override
    public long getSuccessCount() {
        return successCount;
    }

    @Override
    public long getFailureCount() {
        return failureCount;
    }
}
