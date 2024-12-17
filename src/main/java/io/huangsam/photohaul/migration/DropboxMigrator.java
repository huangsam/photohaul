package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderErrorException;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class DropboxMigrator implements Migrator {
    private static final Logger LOG = getLogger(DropboxMigrator.class);

    private final String targetRoot;
    private final PhotoResolver photoResolver;
    private final DbxClientV2 dropboxClient;

    private long successCount = 0L;
    private long failureCount = 0L;

    public DropboxMigrator(String target, PhotoResolver resolver, DbxClientV2 client) {
        if (!target.startsWith("/")) {
            throw new IllegalArgumentException("Target must begin with a '/' character");
        }
        targetRoot = target;
        photoResolver = resolver;
        dropboxClient = client;
    }

    @Override
    public void migratePhotos(@NotNull Collection<Photo> photos) {
        LOG.debug("Start Dropbox migration to {}", targetRoot);
        DbxUserFilesRequests requests = dropboxClient.files();
        photos.forEach(photo -> {
            String targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            try (InputStream in = Files.newInputStream(photo.path())) {
                try {
                    requests.listFolder(targetPath);
                } catch (ListFolderErrorException e) {
                    requests.createFolderV2(targetPath);
                }
                requests.uploadBuilder(targetPath + "/" + photo.name()).uploadAndFinish(in);
                successCount++;
            } catch (IOException | DbxException e) {
                LOG.error("Cannot move {}: {}", photo.name(), e.getMessage());
                failureCount++;
            }
        });
    }

    @Override
    public long getSuccessCount() {
        return successCount;
    }

    @Override
    public long getFailureCount() {
        return failureCount;
    }

    @NotNull
    private String getTargetPath(Photo photo) {
        try {
            return targetRoot + "/" + photoResolver.resolveString(photo);
        } catch (ResolutionException e) {
            return targetRoot + "/Other";
        }
    }
}
