package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderErrorException;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

public class DropboxMigrator extends AbstractMigrator {
    private static final Logger LOG = getLogger(DropboxMigrator.class);

    private final @NonNull String targetRoot;
    private final DbxClientV2 dropboxClient;

    private final Map<String, Boolean> folderCache = new ConcurrentHashMap<>();

    public DropboxMigrator(@NonNull String target, PhotoResolver resolver, DbxClientV2 client, boolean dryRun) {
        this(target, resolver, client, dryRun, 1);
    }

    public DropboxMigrator(@NonNull String target, PhotoResolver resolver, DbxClientV2 client, boolean dryRun, int threadCount) {
        super(resolver, dryRun, threadCount);
        if (!target.startsWith("/")) {
            throw new IllegalArgumentException("Target must begin with a '/' character");
        }
        targetRoot = target;
        dropboxClient = client;
    }

    @Override
    public void migratePhotos(@NonNull Collection<Photo> photos) {
        LOG.debug("Start Dropbox migration to {}", targetRoot);
        DbxUserFilesRequests requests = dropboxClient.files();
        runMigration(photos, photo -> {
            String targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            if (dryRun) {
                LOG.info("Dry-run {} to {}", photo.path(), targetPath + "/" + photo.name());
                successfulPhotos.add(photo.path().toString());
                successCount.incrementAndGet();
                return;
            }
            try (InputStream in = Files.newInputStream(photo.path())) {
                if (!folderCache.containsKey(targetPath)) {
                    synchronized (this) {
                        if (!folderCache.containsKey(targetPath)) {
                            try {
                                requests.listFolder(targetPath);
                            } catch (ListFolderErrorException e) {
                                try {
                                    requests.createFolderV2(targetPath);
                                } catch (DbxException ex) {
                                    // Ignore if folder was already created by another concurrent thread
                                }
                            } catch (DbxException e) {
                                // Ignore other client metadata checking errors
                            }
                            folderCache.put(targetPath, true);
                        }
                    }
                }
                requests.uploadBuilder(targetPath + "/" + photo.name()).uploadAndFinish(in);
                successfulPhotos.add(photo.path().toString());
                successCount.incrementAndGet();
            } catch (IOException | DbxException e) {
                LOG.error("Cannot move {}: {}", photo.name(), e.getMessage());
                failureCount.incrementAndGet();
            }
        });
    }

    @NonNull
    private String getTargetPath(Photo photo) {
        return targetRoot + "/" + resolvePath(photo);
    }
}
