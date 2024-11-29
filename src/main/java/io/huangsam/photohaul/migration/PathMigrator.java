package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class PathMigrator implements Migrator {
    private static final Logger LOG = getLogger(PathMigrator.class);

    protected final Path targetRoot;
    private final CopyOption copyOption;
    private long successCount = 0L;
    private long failureCount = 0L;

    public PathMigrator(Path targetRoot, CopyOption copyOption) {
        this.targetRoot = targetRoot;
        this.copyOption = copyOption;
    }

    @Override
    public final void migratePhotos(Collection<Photo> photos) {
        LOG.debug("Start migration to {}", targetRoot);
        photos.forEach(photo -> {
            Path targetLocation = getTargetLocation(photo);
            if (targetLocation == null) {
                LOG.warn("Resort to fallback for {}", photo);
                targetLocation = fallback();
            }
            try {
                LOG.trace("Move {} to {}", photo.name(), targetLocation);
                Files.createDirectories(targetLocation);
                Files.move(photo.path(), targetLocation.resolve(photo.name()), copyOption);
                successCount++;
            } catch (IOException e) {
                LOG.warn("Cannot move {}: {}", photo.name(), e.getMessage());
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

    Path fallback() {
        return targetRoot.resolve("Other");
    }

    abstract @Nullable Path getTargetLocation(Photo photo);
}
