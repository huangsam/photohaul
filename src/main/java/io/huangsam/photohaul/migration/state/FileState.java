package io.huangsam.photohaul.migration.state;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the state of a migrated file.
 *
 * <p> This class captures the essential metadata used to determine if a file
 * has changed since its last migration: the relative path, file size in bytes,
 * and last modified timestamp in milliseconds since epoch.
 */
public record FileState(
        @NotNull String path,
        long size,
        long lastModifiedMillis
) {
    /**
     * Constructs a FileState with validation.
     *
     * @param path              relative path of the file
     * @param size              file size in bytes
     * @param lastModifiedMillis last modified timestamp in milliseconds since epoch
     * @throws IllegalArgumentException if path is blank or size is negative
     */
    public FileState {
        Objects.requireNonNull(path, "Path cannot be null");
        if (path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be blank");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        if (lastModifiedMillis < 0) {
            throw new IllegalArgumentException("Last modified timestamp cannot be negative");
        }
    }

    /**
     * Check if this file state matches another file's current state.
     *
     * @param other the other file state to compare
     * @return true if size and lastModifiedMillis match
     */
    public boolean matches(@NotNull FileState other) {
        return this.size == other.size && this.lastModifiedMillis == other.lastModifiedMillis;
    }
}
