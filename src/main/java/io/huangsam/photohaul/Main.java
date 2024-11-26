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

        PhotoRuleSet ruleSet = new PhotoRuleSet(List.of(
                Files::isRegularFile,
                PhotoRule.allowedExtensions("jpg", "jpeg", "png"),
                PhotoRule.isValidContent(),
                PhotoRule.minimumBytes(100L)));

        traversePhotos(getHomePath("Pictures"), visitor, ruleSet);
        traversePhotos(getHomePath("Phone"), visitor, ruleSet);

        Collection<Photo> photoList = visitor.getPhotos();

        LOG.info("Found {} photos", photoList.size());
    }

    private static void traversePhotos(Path path, PhotoVisitor visitor, PhotoRuleSet ruleSet) {
        LOG.info("Start traversal of {}", path);
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream.parallel().filter(ruleSet::matches).forEach(visitor::visitPhoto);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", path, e.getMessage());
        }
        LOG.info("Finish traversal of {}", path);
    }

    private static Path getHomePath(String target) {
        return Paths.get(System.getProperty("user.home") + '/' + target);
    }
}
