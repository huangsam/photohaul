package io.huangsam.photohaul;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class Main {
    private static final Logger LOG = getLogger(Main.class);

    public static void main(String[] args) {
        String homeDirectory = System.getProperty("user.home");
        Main.printPhotos(Paths.get(homeDirectory + "/Pictures"));
    }

    private static void printPhotos(Path path) {
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream
                    .filter(Files::isRegularFile)
                    .filter(Main::isPhoto)
                    .sorted(Comparator.comparingLong(Main::getLastModified))
                    .forEach(filePath -> LOG.info(filePath.toString()));
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private static boolean isPhoto(Path path) {
        try {
            return Files.probeContentType(path).startsWith("image/");
        } catch (IOException e) {
            LOG.trace(e.getMessage());
            String pathName = path.toString();
            return Stream.of("jpg", "png", "svg").anyMatch(pathName::endsWith);
        } catch (NullPointerException e) {
            LOG.trace(e.getMessage());
            return false;
        }
    }

    private static long getLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            LOG.trace(e.getMessage());
            return Long.MAX_VALUE; // Handle errors appropriately
        }
    }
}
