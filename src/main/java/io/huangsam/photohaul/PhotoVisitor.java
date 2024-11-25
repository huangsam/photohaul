package io.huangsam.photohaul;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class PhotoVisitor {
    private static final Logger LOG = getLogger(PhotoVisitor.class);
    private final List<Photo> photoList;

    public PhotoVisitor(List<Photo> photoList) {
        this.photoList = photoList;
    }

    public List<Photo> getPhotos() {
        return photoList;
    }

    public void visitPhoto(Path path) {
        LOG.trace(path.toString());
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> properties = getPhotoProperties(input);
            if (properties.isEmpty()) {
                return;
            }
            Photo photo = new Photo(
                    path.toString(),
                    (String) properties.get("Date/Time"),
                    (String) properties.get("Make"),
                    (String) properties.get("Model"),
                    (String) properties.get("Focal Length"),
                    (String) properties.get("Shutter Speed Value"),
                    (String) properties.get("Aperture Value"),
                    (String) properties.get("Flash")
            );
            photoList.add(photo);
        } catch (IOException e) {
            LOG.warn("Cannot open photo: {}", e.getMessage());
        }
    }

    private Map<String, Object> getPhotoProperties(InputStream input) {
        Map<String, Object> properties = new HashMap<>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    properties.put(tag.getTagName(), tag.getDescription());
                }
            }
        } catch (IOException | ImageProcessingException e) {
            LOG.warn("Cannot get photo properties: {}", e.getMessage());
        }
        return properties;
    }
}
