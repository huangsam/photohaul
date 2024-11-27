package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import io.huangsam.photohaul.migrate.CameraPathMigrator;
import io.huangsam.photohaul.migrate.Migrator;
import io.huangsam.photohaul.visit.PathRule;
import io.huangsam.photohaul.visit.PathRuleSet;
import io.huangsam.photohaul.visit.PhotoPathVisitor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);
    private static final Settings SETTINGS = new Settings();

    public static void main(String[] args) {
        PhotoPathVisitor visitor = new PhotoPathVisitor();

        PathRuleSet pathRules = new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.allowedExtensions("jpg", "jpeg", "png").or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));

        traversePhotos(SETTINGS.getSourcePath(), visitor, pathRules);

        Migrator migrator = new CameraPathMigrator(
                SETTINGS.getTargetPath(), StandardCopyOption.REPLACE_EXISTING);

        migratePhotos(migrator, visitor);
    }

    private static void traversePhotos(Path source, PhotoPathVisitor visitor, PathRuleSet pathRules) {
        LOG.info("Start traversal of {}", source);
        try (Stream<Path> fileStream = Files.walk(source)) {
            fileStream.parallel().filter(pathRules::matches).forEach(visitor::visitPhoto);
            LOG.info("Finish traversal of {}", source);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", source, e.getMessage());
        }
    }

    private static void migratePhotos(Migrator migrator, PhotoPathVisitor visitor) {
        LOG.info("Start migration");
        visitor.getPhotos().forEach(migrator::migratePhoto);
        LOG.info("Finish migration with {} successful", migrator.getSuccessCount());
    }
}
