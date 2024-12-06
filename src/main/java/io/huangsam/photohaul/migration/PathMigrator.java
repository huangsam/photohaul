package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
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

    public PathMigrator(Path target, CopyOption option, PhotoResolver resolver) {
        targetRoot = target;
        copyOption = option;
        photoResolver = resolver;
    }

    @Override
    public final void migratePhotos(@NotNull Collection<Photo> photos) {
        LOG.debug("Start migration to {}", targetRoot);
        photos.forEach(photo -> {
            Path targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            try {
                Files.createDirectories(targetPath);
                Files.move(photo.path(), targetPath.resolve(photo.name()), copyOption);
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
