package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);

    // Runner
    public static void main(String[] args) {
        String homeDirectory = System.getProperty("user.home");
        Path picturePath = Paths.get(homeDirectory + "/Pictures");

        PhotoVisitor visitor = new PhotoVisitor();

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
