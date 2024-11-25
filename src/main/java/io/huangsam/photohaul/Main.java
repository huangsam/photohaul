package io.huangsam.photohaul;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
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
                    .forEach(Main::visitPhoto);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private static void visitPhoto(Path path) {
        LOG.info(path.toString());
        try (InputStream input = Files.newInputStream(path)) {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    LOG.debug("dir={} name={} description={}",
                            tag.getDirectoryName(),
                            tag.getTagName(),
                            tag.getDescription());
                }
            }
        } catch (IOException | ImageProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isPhoto(Path path) {
        try {
            return Files.probeContentType(path).startsWith("image/");
        } catch (IOException | NullPointerException e) {
            LOG.trace(e.getMessage());
            String pathName = path.toString().toLowerCase();
            return Stream.of("jpg", "jpeg", "png").anyMatch(pathName::endsWith);
        }
    }

    private static long getLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            LOG.trace(e.getMessage());
            return Long.MAX_VALUE;
        }
    }
}
