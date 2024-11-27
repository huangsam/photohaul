package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import io.huangsam.photohaul.migrate.PhotoMigrator;
import io.huangsam.photohaul.migrate.CameraPhotoMigrator;
import io.huangsam.photohaul.visit.PathRule;
import io.huangsam.photohaul.visit.PathRuleSet;
import io.huangsam.photohaul.visit.PhotoVisitor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);
    private static final Settings SETTINGS = new Settings();

    public static void main(String[] args) {
        PhotoVisitor visitor = new PhotoVisitor();

        PathRuleSet pathRules = new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.allowedExtensions("jpg", "jpeg", "png").or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));

        traversePhotos(SETTINGS.getSourcePath(), visitor, pathRules);

        migratePhotos(SETTINGS.getTargetPath(), visitor);
    }

    private static void traversePhotos(Path path, PhotoVisitor visitor, PathRuleSet pathRules) {
        LOG.info("Start traversal of {}", path);
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream.parallel().filter(pathRules::matches).forEach(visitor::visitPhoto);
            LOG.info("Finish traversal of {}", path);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", path, e.getMessage());
        }
    }

    private static void migratePhotos(Path targetPath, PhotoVisitor visitor) {
        LOG.info("Start migration");
        PhotoMigrator migrator = new CameraPhotoMigrator(targetPath);
        visitor.getPhotos().forEach(migrator::performMigration);
        LOG.info("Finish migration with {} successful", migrator.getSuccessCount());
    }
}
