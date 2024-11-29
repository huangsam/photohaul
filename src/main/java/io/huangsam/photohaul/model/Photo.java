package io.huangsam.photohaul.model;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    public String name() {
        return path.getFileName().toString();
    }

    @Nullable
    public FileTime createdAt() {
        BasicFileAttributes attributes = attributes();
        return (attributes == null) ? null : attributes.creationTime();
    }

    @Nullable
    public FileTime modifiedAt() {
        BasicFileAttributes attributes = attributes();
        return (attributes == null) ? null : attributes.lastModifiedTime();
    }

    @Nullable
    public LocalDateTime takenAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        return (taken == null) ? null : LocalDateTime.parse(taken, formatter);
    }

    @Nullable
    private BasicFileAttributes attributes() {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            return null;
        }
    }
}
