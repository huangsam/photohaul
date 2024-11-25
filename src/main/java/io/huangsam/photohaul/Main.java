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

        PhotoVisitor visitor = new PhotoVisitor(new ArrayList<>());

        Main.traversePhotos(picturePath, visitor);

        for (Photo photo : visitor.getPhotos()) {
            LOG.info(photo.name());
        }
    }

    // Traversal
    private static void traversePhotos(Path path, PhotoVisitor visitor) {
        LOG.info("Start scanning {}", path);
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream
                    .filter(Files::isRegularFile)
                    .filter(Main::isPhoto)
                    .sorted(Comparator.comparingLong(Main::getLastModified))
                    .forEach(visitor::visitPhoto);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        LOG.info("Finish scanning {}", path);
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
