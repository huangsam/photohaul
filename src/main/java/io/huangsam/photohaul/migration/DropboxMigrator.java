package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import io.huangsam.photohaul.model.Photo;
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
    private final DbxClientV2 dropboxClient;
    private final PhotoResolver photoResolver;

    private long successCount = 0L;
    private long failureCount = 0L;

    public DropboxMigrator(String target, DbxClientV2 client, PhotoResolver resolver) {
        targetRoot = target;
        dropboxClient = client;
        photoResolver = resolver;
    }

    @Override
    public void migratePhotos(@NotNull Collection<Photo> photos) {
        LOG.debug("Start DBX migration to {}", targetRoot);
        DbxUserFilesRequests requests = dropboxClient.files();
        photos.forEach(photo -> {
            String targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            try (InputStream in = Files.newInputStream(photo.path())) {
                if (requests.getMetadata(targetPath) == null) {
                    requests.createFolderV2(targetPath);
                }
                requests.uploadBuilder(targetPath + "/" + photo.name()).uploadAndFinish(in);
                successCount++;
            } catch (IOException e) {
                LOG.error("Cannot move {} with I/O error: {}", photo.name(), e.getMessage());
                failureCount++;
            } catch (CreateFolderErrorException e) {
                LOG.error("Cannot move {} with DBX folder error: {}", photo.name(), e.getMessage());
                failureCount++;
            } catch (DbxException e) {
                LOG.error("Cannot move {} with DBX general error: {}", photo.name(), e.getMessage());
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

    private String getTargetPath(Photo photo) {
        try {
            StringBuilder result = new StringBuilder(targetRoot);
            for (String out : photoResolver.resolveList(photo)) {
                result.append("/").append(out);
            }
            return result.toString();
        } catch (NullPointerException e) {
            return targetRoot + "/Other";
        }
    }
}
