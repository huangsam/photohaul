package io.huangsam.photohaul;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);

    // Runner
    public static void main(String[] args) {
        String homeDirectory = System.getProperty("user.home");
        Path picturePath = Paths.get(homeDirectory + "/Pictures");

        List<Photo> photoList = new ArrayList<>();

        Main.traversePhotos(picturePath, photoList);

        for (Photo photo : photoList) {
            LOG.info(photo.name());
        }
    }

    // Traversal
    private static void traversePhotos(Path path, List<Photo> photoList) {
        LOG.info("Start scanning {}", path);
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream
                    .filter(Files::isRegularFile)
                    .filter(Main::isPhoto)
                    .sorted(Comparator.comparingLong(Main::getLastModified))
                    .forEach(photoPath -> visitPhoto(photoPath, photoList));
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        LOG.info("Finish scanning {}", path);
    }

    // Collector
    private static void visitPhoto(Path path, List<Photo> photoList) {
        LOG.trace(path.toString());
        Map<String, Object> properties = new HashMap<>();
        try (InputStream input = Files.newInputStream(path)) {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    properties.put(tag.getTagName(), tag.getDescription());
                }
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
        } catch (IOException | ImageProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Filter
    private static boolean isPhoto(Path path) {
        try {
            return Files.probeContentType(path).startsWith("image/");
        } catch (IOException | NullPointerException e) {
            LOG.trace(e.getMessage());
            String pathName = path.toString().toLowerCase();
            return Stream.of("jpg", "jpeg", "png").anyMatch(pathName::endsWith);
        }
    }

    // Sorter
    private static long getLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            LOG.trace(e.getMessage());
            return Long.MAX_VALUE;
        }
    }
}
