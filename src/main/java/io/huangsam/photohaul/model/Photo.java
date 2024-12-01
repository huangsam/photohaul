package io.huangsam.photohaul.model;

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
 * <p> Note that this metadata is not guaranteed for all photos. This metadata exists on
 * RAW formats from providers such as Nikon, Canon and Sony. It also exists on JPG/JPEG
 * files which were recently processed from Adobe Lightroom.
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
     * Get the filename of the photo.
     *
     * @return Photo filename
     */
    public String name() {
        return path.getFileName().toString();
    }

    /**
     * Get photo modified time.
     *
     * @return Modified time as {@link FileTime}
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
     * @return Taken time as {@link LocalDateTime}
     */
    @Nullable
    public LocalDateTime takenAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        return (taken == null) ? null : LocalDateTime.parse(taken, formatter);
    }
}
