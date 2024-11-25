package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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

        Collection<Photo> photoList = visitor.getPhotos();

        LOG.info("Found {} photos", photoList.size());
    }

    // Traversal
    private static void traversePhotos(Path path, PhotoVisitor visitor) {
        LOG.info("Start traversal of {}", path);
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream.parallel()
                    .filter(Files::isRegularFile)
                    .filter(Main::isPhoto)
                    .forEach(visitor::visitPhoto);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", path, e.getMessage());
        }
        LOG.info("Finish traversal of {}", path);
    }

    // Filter
    private static boolean isPhoto(Path path) {
        String pathName = path.toString().toLowerCase();
        return Stream.of("jpg", "jpeg", "png").anyMatch(pathName::endsWith);
    }
}
