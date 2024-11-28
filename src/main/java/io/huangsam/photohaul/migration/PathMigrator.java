package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    public final void migratePhoto(Photo photo) {
        Path targetLocation = getTargetLocation(photo);
        try {
            LOG.trace("Move {} over to {}", photo.name(), targetLocation);
            Files.createDirectories(targetLocation);
            Files.move(photo.path(), targetLocation.resolve(photo.name()), copyOption);
            successCount++;
        } catch (IOException e) {
            LOG.warn("Cannot migrate {} to {}: {}", photo.name(), targetLocation, e.getMessage());
            failureCount++;
        }
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

    abstract Path getTargetLocation(Photo photo);
}
