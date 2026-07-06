package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class PathMigrator extends AbstractMigrator {
    private static final Logger LOG = getLogger(PathMigrator.class);

    private final Path targetRoot;
    private final Action migratorAction;

    public PathMigrator(Path target, PhotoResolver resolver, Action action, boolean dryRun) {
        this(target, resolver, action, dryRun, 1);
    }

    public PathMigrator(Path target, PhotoResolver resolver, Action action, boolean dryRun, int threadCount) {
        super(resolver, dryRun, threadCount);
        targetRoot = target;
        migratorAction = action;
    }

    @Override
    public final void migratePhotos(@NonNull Collection<Photo> photos) {
        LOG.debug("Start path migration to {}", targetRoot);
        runMigration(photos, photo -> {
            Path targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            try {
                migratePhoto(targetPath, photo);
                successfulPhotos.add(photo.path().toString());
                successCount.incrementAndGet();
            } catch (IOException e) {
                LOG.error("Cannot move {}: {}", photo.name(), e.getMessage());
                failureCount.incrementAndGet();
            }
        });
    }

    @NonNull
    private Path getTargetPath(Photo photo) {
        return targetRoot.resolve(resolvePath(photo));
    }

    private void migratePhoto(@NonNull Path target, @NonNull Photo photo) throws IOException {
        Path photoLocation = target.resolve(photo.name());
        Path sidecarLocal = photo.getSidecarPath();
        Path sidecarLocation = null;
        if (sidecarLocal != null) {
            sidecarLocation = target.resolve(sidecarLocal.getFileName().toString());
        }

        if (dryRun) {
            LOG.info("Dry-run {} to {}", photo.path(), photoLocation);
            if (sidecarLocal != null) {
                LOG.info("Dry-run sidecar {} to {}", sidecarLocal, sidecarLocation);
            }
            return;
        }
        Files.createDirectories(target);
        switch (migratorAction) {
            case MOVE -> {
                Files.move(photo.path(), photoLocation, StandardCopyOption.REPLACE_EXISTING);
                if (sidecarLocal != null) {
                    Files.move(sidecarLocal, sidecarLocation, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            case COPY -> {
                Files.copy(photo.path(), photoLocation, StandardCopyOption.REPLACE_EXISTING);
                if (sidecarLocal != null) {
                    Files.copy(sidecarLocal, sidecarLocation, StandardCopyOption.REPLACE_EXISTING);
                }
            }
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
        COPY
    }
}
