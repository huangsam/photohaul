package io.huangsam.photohaul.visit;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import io.huangsam.photohaul.model.Photo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PhotoBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(PhotoBuilder.class);

    private Path path;
    private final Map<String, Object> info = new HashMap<>();

    public void fillInfo(Path photoPath) {
        path = photoPath;
        try (InputStream input = Files.newInputStream(photoPath)) {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("{} -> Store tag {}", photoPath, tag.getTagName());
                    }
                    info.put(tag.getTagName(), tag.getDescription());
                }
            }
        } catch (IOException | ImageProcessingException e) {
            LOG.warn("Cannot get properties for {}: {}", photoPath, e.getMessage());
        }
    }

    public Photo build() {
        return new Photo(
                path,
                (String) info.get("Date/Time Original"),
                (String) info.get("Make"),
                (String) info.get("Model"),
                (String) info.get("Focal Length"),
                (String) info.get("Shutter Speed Value"),
                (String) info.get("Aperture Value"),
                (String) info.get("Flash")
        );
    }
}
