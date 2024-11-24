package io.huangsam.photohaul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {
        String homeDirectory = System.getProperty("user.home");
        Main.printPhotos(Paths.get(homeDirectory + "/Pictures"));
    }

    private static void printPhotos(Path path) {
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream
                    .filter(Files::isRegularFile)
                    .filter(Main::isPhoto)
                    .forEach(filePath -> LOG.info(filePath.toString()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
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
}
