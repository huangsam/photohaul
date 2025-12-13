package io.huangsam.photohaul.model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a photo with its metadata.
 *
 * <p> Note that this metadata is not guaranteed for all photos. This metadata
 * exists on the following assets:
 *
 * <ul>
 *     <li>RAW formats from providers such as Canon, Nikon and Sony</li>
 *     <li>JPG/JPEG files which were generated from Adobe Lightroom</li>
 * </ul>
 *
 * <p> Metadata is extracted lazily on first access to improve performance
 * during photo collection.
 */
public class Photo {
    // Metadata keys
    private static final String TAKEN_KEY = "Date/Time Original";
    private static final String MAKE_KEY = "Make";
    private static final String MODEL_KEY = "Model";
    private static final String FOCAL_LENGTH_KEY = "Focal Length";
    private static final String SHUTTER_SPEED_KEY = "Shutter Speed Value";
    private static final String APERTURE_KEY = "Aperture Value";
    private static final String FLASH_KEY = "Flash";

    private final Path path;
    private final Map<String, String> metadata = new ConcurrentHashMap<>();
    private volatile boolean metadataLoaded = false;

    public Photo(Path path) {
        this.path = path;
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
        return getMetadata(TAKEN_KEY);
    }

    /**
     * Get camera make metadata.
     *
     * @return make string or null
     */
    @Nullable
    public String make() {
        return getMetadata(MAKE_KEY);
    }

    /**
     * Get camera model metadata.
     *
     * @return model string or null
     */
    @Nullable
    public String model() {
        return getMetadata(MODEL_KEY);
    }

    /**
     * Get focal length metadata.
     *
     * @return focal length string or null
     */
    @Nullable
    public String focalLength() {
        return getMetadata(FOCAL_LENGTH_KEY);
    }

    /**
     * Get shutter speed metadata.
     *
     * @return shutter speed string or null
     */
    @Nullable
    public String shutterSpeed() {
        return getMetadata(SHUTTER_SPEED_KEY);
    }

    /**
     * Get aperture metadata.
     *
     * @return aperture string or null
     */
    @Nullable
    public String aperture() {
        return getMetadata(APERTURE_KEY);
    }

    /**
     * Get flash metadata.
     *
     * @return flash string or null
     */
    @Nullable
    public String flash() {
        return getMetadata(FLASH_KEY);
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
        String taken = taken();
        if (taken == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        try {
            return LocalDateTime.parse(taken, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get metadata value by key, ensuring metadata is loaded first.
     *
     * @param key the metadata key
     * @return the metadata value or null
     */
    @Nullable
    private String getMetadata(String key) {
        ensureMetadataLoaded();
        return metadata.get(key);
    }

    /**
     * Ensure metadata is loaded before accessing.
     */
    private void ensureMetadataLoaded() {
        if (!metadataLoaded) {
            synchronized (this) {
                if (!metadataLoaded) {
                    loadMetadata();
                    metadataLoaded = true;
                }
            }
        }
    }

    /**
     * Load metadata from the photo file.
     */
    private void loadMetadata() {
        extractMetadata(path, metadata);
    }

    /**
     * Extract metadata from a photo file into the provided map.
     *
     * @param photoPath the path to the photo file
     * @param metadata the map to store metadata in
     */
    private static void extractMetadata(@NonNull Path photoPath, @NonNull Map<String, String> metadata) {
        try (InputStream input = Files.newInputStream(photoPath)) {
            Metadata imageMetadata = ImageMetadataReader.readMetadata(input);
            for (Directory directory : imageMetadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    metadata.put(tag.getTagName(), tag.getDescription());
                }
            }
        } catch (IOException | ImageProcessingException e) {
            // Metadata extraction failed, metadata map remains empty
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Photo other)) return false;
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
