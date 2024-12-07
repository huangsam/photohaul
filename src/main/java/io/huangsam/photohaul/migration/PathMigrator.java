package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class PathMigrator implements Migrator {
    private static final Logger LOG = getLogger(PathMigrator.class);

    protected final Path targetRoot;
    private final PhotoResolver photoResolver;

    private long successCount = 0L;
    private long failureCount = 0L;

    public PathMigrator(Path target, PhotoResolver resolver) {
        targetRoot = target;
        photoResolver = resolver;
    }

    @Override
    public final void migratePhotos(@NotNull Collection<Photo> photos) {
        LOG.debug("Start path migration to {}", targetRoot);
        photos.forEach(photo -> {
            Path targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            try {
                Files.createDirectories(targetPath);
                Files.move(photo.path(), targetPath.resolve(photo.name()), StandardCopyOption.REPLACE_EXISTING);
                successCount++;
            } catch (IOException e) {
                LOG.error("Cannot move {}: {}", photo.name(), e.getMessage());
                failureCount++;
            }
        });
    }

    @Override
    public final long getSuccessCount() {
        return successCount;
    }

    @Override
    public final long getFailureCount() {
        return failureCount;
    }

    private Path getTargetPath(Photo photo) {
        try {
            return targetRoot.resolve(String.join("/", photoResolver.resolveList(photo)));
        } catch (NullPointerException e) {
            return targetRoot.resolve("Other");
        }
    }
}
