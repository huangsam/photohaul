package io.huangsam.photohaul.migrate;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class PhotoMigrator {
    private static final Logger LOG = getLogger(PhotoMigrator.class);

    protected final Path targetRoot;

    public PhotoMigrator(Path targetRoot) {
        this.targetRoot = targetRoot;
    }

    public void performMigration(Photo photo) {
        Path targetLocation = getTargetLocation(photo);
        try {
            LOG.trace("Move {} over to {}", photo.name(), targetLocation);
            Files.createDirectories(targetLocation);
            Files.move(photo.path(), targetLocation.resolve(photo.name()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.warn("Cannot migrate {} to {}: {}", photo.name(), targetLocation, e.getMessage());
        }
    }

    abstract @NotNull Path getTargetLocation(Photo photo);
}