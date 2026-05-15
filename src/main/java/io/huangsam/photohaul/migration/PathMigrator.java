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
        super(resolver, dryRun);
        targetRoot = target;
        migratorAction = action;
    }

    @Override
    public final void migratePhotos(@NonNull Collection<Photo> photos) {
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

    @NonNull
    private Path getTargetPath(Photo photo) {
        return targetRoot.resolve(resolvePath(photo));
    }

    private void migratePhoto(@NonNull Path target, @NonNull Photo photo) throws IOException {
        Path photoLocation = target.resolve(photo.name());
        if (dryRun) {
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
        COPY
    }
}
