package io.huangsam.photohaul.model;

import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

/**
 * Interface for extracting metadata from photo files.
 */
public interface MetadataExtractor {
    /**
     * Extracts metadata from the given path.
     *
     * @param path The path to the photo file.
     * @return The extracted metadata.
     */
    @NonNull PhotoMetadata extract(@NonNull Path path);
}
