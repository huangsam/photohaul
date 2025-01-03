package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
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

    private final Path targetRoot;
    private final PhotoResolver photoResolver;
    private final Action migratorAction;

    private long successCount = 0L;
    private long failureCount = 0L;

    public PathMigrator(Path target, PhotoResolver resolver, Action action) {
        targetRoot = target;
        photoResolver = resolver;
        migratorAction = action;
    }

    @Override
    public final void migratePhotos(@NotNull Collection<Photo> photos) {
        LOG.debug("Start path migration to {}", targetRoot);
        photos.forEach(photo -> {
            Path targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            try {
                migratePhoto(targetPath, photo);
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

    @NotNull
    private Path getTargetPath(Photo photo) {
        try {
            return targetRoot.resolve(photoResolver.resolveString(photo));
        } catch (ResolutionException e) {
            return targetRoot.resolve("Other");
        }
    }

    private void migratePhoto(Path target, Photo photo) throws IOException {
        Path photoLocation = target.resolve(photo.name());
        if (migratorAction == Action.DRY_RUN) {
            LOG.info("Dry-run {} to {}", photo.path(), photoLocation);
            return;
        }
        Files.createDirectories(target);
        switch (migratorAction) {
            case MOVE -> Files.move(photo.path(), photoLocation, StandardCopyOption.REPLACE_EXISTING);
            case COPY -> Files.copy(photo.path(), photoLocation, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * This action can be a {@code mv}, {@code cp} or {@code echo} in Linux speak.
     * The {@code echo} op is good to try before settling on other actions.
     */
    public enum Action {
        /**
         * Move the photo from its original location to the target path.
         * Permanently removes the photo from its original location.
         */
        MOVE,

        /**
         * Copy the photo from its original location to the target path.
         * The original photo remains untouched.
         */
        COPY,

        /**
         * Perform a dry run of the migration process.
         * No files are actually moved or copied.
         * Logs information about where each photo would be placed.
         */
        DRY_RUN
    }
}
