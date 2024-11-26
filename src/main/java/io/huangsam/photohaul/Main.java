package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);

    public static void main(String[] args) {
        PhotoVisitor visitor = new PhotoVisitor();

        PathRuleSet pathRules = new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.allowedExtensions("jpg", "jpeg", "png"),
                PathRule.isImageContent(),
                PathRule.minimumBytes(100L)));

        traversePhotos(getHomePath("Pictures"), visitor, pathRules);
        traversePhotos(getHomePath("Phone"), visitor, pathRules);

        Collection<Photo> photoList = visitor.getPhotos();

        LOG.info("Found {} photos", photoList.size());
    }

    private static void traversePhotos(Path path, PhotoVisitor visitor, PathRuleSet pathRules) {
        LOG.info("Start traversal of {}", path);
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream.parallel().filter(pathRules::matches).forEach(visitor::visitPhoto);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", path, e.getMessage());
        }
        LOG.info("Finish traversal of {}", path);
    }

    private static Path getHomePath(String target) {
        return Paths.get(System.getProperty("user.home") + '/' + target);
    }
}
