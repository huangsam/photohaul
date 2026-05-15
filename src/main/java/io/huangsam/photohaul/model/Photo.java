package io.huangsam.photohaul.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * Represents a photo with its metadata.
 *
 * <p>Metadata is extracted lazily on first access to improve performance
 * during photo collection.
 */
public class Photo {
    private final Path path;
    private final Supplier<PhotoMetadata> metadataSupplier;
    private PhotoMetadata metadata;

    public Photo(@NonNull Path path) {
        this(path, PhotoMetadata.EMPTY);
    }

    public Photo(@NonNull Path path, @NonNull PhotoMetadata metadata) {
        this(path, () -> metadata);
    }

    public Photo(@NonNull Path path, @NonNull Supplier<PhotoMetadata> metadataSupplier) {
        this.path = path;
        this.metadataSupplier = metadataSupplier;
    }

    /**
     * Get photo file name.
     *
     * @return file name
     */
    @NotNull
    public String name() {
        return path.getFileName().toString();
    }

    /**
     * Get the file path.
     *
     * @return the path
     */
    @NotNull
    public Path path() {
        return path;
    }

    /**
     * Get photo taken time metadata.
     *
     * @return taken time string or null
     */
    @Nullable
    public String taken() {
        LocalDateTime taken = takenAt();
        return (taken == null) ? null : taken.format(java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));
    }

    /**
     * Get camera make metadata.
     *
     * @return make string or null
     */
    @Nullable
    public String make() {
        return metadata().make();
    }

    /**
     * Get camera model metadata.
     *
     * @return model string or null
     */
    @Nullable
    public String model() {
        return metadata().model();
    }

    /**
     * Get focal length metadata.
     *
     * @return focal length string or null
     */
    @Nullable
    public String focalLength() {
        return metadata().focalLength();
    }

    /**
     * Get shutter speed metadata.
     *
     * @return shutter speed string or null
     */
    @Nullable
    public String shutterSpeed() {
        return metadata().shutterSpeed();
    }

    /**
     * Get aperture metadata.
     *
     * @return aperture string or null
     */
    @Nullable
    public String aperture() {
        return metadata().aperture();
    }

    /**
     * Get flash metadata.
     *
     * @return flash string or null
     */
    @Nullable
    public String flash() {
        return metadata().flash();
    }

    /**
     * Get photo modified time.
     *
     * @return modified time as {@code FileTime}
     */
    @Nullable
    public FileTime modifiedAt() {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get photo taken time, parsed from image tags.
     *
     * @return taken time as {@code LocalDateTime}
     */
    @Nullable
    public LocalDateTime takenAt() {
        return metadata().takenAt();
    }

    /**
     * Get the photo metadata, extracting it if necessary.
     *
     * @return photo metadata
     */
    public synchronized @NotNull PhotoMetadata metadata() {
        if (metadata == null) {
            metadata = metadataSupplier.get();
        }
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (!(obj instanceof Photo other)) { return false; }
        return path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public @NonNull String toString() {
        return "Photo{path=" + path + "}";
    }
}
