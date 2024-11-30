package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class PathMigrator implements Migrator {
    private static final Logger LOG = getLogger(PathMigrator.class);

    protected final Path targetRoot;
    private final CopyOption copyOption;
    private final PhotoResolver photoResolver;

    private long successCount = 0L;
    private long failureCount = 0L;

    public PathMigrator(Path targetRoot, CopyOption copyOption, PhotoResolver photoResolver) {
        this.targetRoot = targetRoot;
        this.copyOption = copyOption;
        this.photoResolver = photoResolver;
    }

    @Override
    public final void migratePhotos(Collection<Photo> photos) {
        LOG.debug("Start migration to {}", targetRoot);
        photos.forEach(photo -> {
            Path targetLocation = getTargetLocation(photo);
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

    private Path getTargetLocation(Photo photo) {
        try {
            Path result = targetRoot;
            for (String out : photoResolver.resolveList(photo)) {
                result = result.resolve(out);
            }
            return result;
        } catch (NullPointerException e) {
            return targetRoot.resolve("Other");
        }
    }
}
