package io.huangsam.photohaul.model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Extracts metadata using the metadata-extractor library.
 */
public class ExifMetadataExtractor implements MetadataExtractor {
    private static final String TAKEN_KEY = "Date/Time Original";
    private static final String MAKE_KEY = "Make";
    private static final String MODEL_KEY = "Model";
    private static final String FOCAL_LENGTH_KEY = "Focal Length";
    private static final String SHUTTER_SPEED_KEY = "Shutter Speed Value";
    private static final String APERTURE_KEY = "Aperture Value";
    private static final String FLASH_KEY = "Flash";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    @Override
    public @NonNull PhotoMetadata extract(@NonNull Path path) {
        Map<String, String> tags = new HashMap<>();
        try (InputStream input = Files.newInputStream(path)) {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    tags.put(tag.getTagName(), tag.getDescription());
                }
            }
        } catch (IOException | ImageProcessingException e) {
            return PhotoMetadata.EMPTY;
        }

        return new PhotoMetadata(
            parseDateTime(tags.get(TAKEN_KEY)),
            tags.get(MAKE_KEY),
            tags.get(MODEL_KEY),
            tags.get(FOCAL_LENGTH_KEY),
            tags.get(SHUTTER_SPEED_KEY),
            tags.get(APERTURE_KEY),
            tags.get(FLASH_KEY)
        );
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
