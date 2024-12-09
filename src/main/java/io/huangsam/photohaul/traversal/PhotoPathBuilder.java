package io.huangsam.photohaul.traversal;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import io.huangsam.photohaul.model.Photo;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class PhotoPathBuilder {
    private static final Logger LOG = getLogger(PhotoPathBuilder.class);

    private Path path;
    private final Map<String, String> info = new HashMap<>();

    public void fillInfo(Path photoPath) {
        path = photoPath;
        try (InputStream input = Files.newInputStream(photoPath)) {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Store tag {} for {}", tag.getTagName(), photoPath);
                    }
                    info.put(tag.getTagName(), tag.getDescription());
                }
            }
        } catch (IOException | ImageProcessingException e) {
            LOG.warn("Cannot process {}: {}", photoPath, e.getMessage());
        }
    }

    public Photo build() {
        return new Photo(
                path,
                info.get("Date/Time Original"),
                info.get("Make"),
                info.get("Model"),
                info.get("Focal Length"),
                info.get("Shutter Speed Value"),
                info.get("Aperture Value"),
                info.get("Flash")
        );
    }
}
