package io.huangsam.photohaul.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a photo with its metadata.
 *
 * <p> Note that this metadata is not guaranteed for all photos. This metadata
 * exists on the following assets:
 *
 * <ul>
 *     <li>RAW formats from providers such as Nikon, Canon and Sony</li>
 *     <li>JPG/JPEG files which were generated from Adobe Lightroom</li>
 * </ul>
 */
public record Photo(
        Path path,
        @Nullable String taken,
        @Nullable String make,
        @Nullable String model,
        @Nullable String focalLength,
        @Nullable String shutterSpeed,
        @Nullable String aperture,
        @Nullable String flash
) {
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        return (taken == null) ? null : LocalDateTime.parse(taken, formatter);
    }
}
