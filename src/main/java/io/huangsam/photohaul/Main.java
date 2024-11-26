package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import io.huangsam.photohaul.migrate.PhotoMigrator;
import io.huangsam.photohaul.migrate.YearBasedPhotoMigrator;
import io.huangsam.photohaul.visit.PathRule;
import io.huangsam.photohaul.visit.PathRuleSet;
import io.huangsam.photohaul.visit.PhotoVisitor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);

    public static void main(String[] args) {
        PhotoVisitor visitor = new PhotoVisitor();

        PathRuleSet pathRules = new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.allowedExtensions("jpg", "jpeg", "png").or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));

        traversePhotos(getSourcePath(), visitor, pathRules);

        migratePhotos(visitor);
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

    private static void migratePhotos(PhotoVisitor visitor) {
        visitor.getPhotos().forEach(photo -> {
            PhotoMigrator migrator = new YearBasedPhotoMigrator(getTargetPath());
            migrator.performMigration(photo);
        });
    }

    private static Path getSourcePath() {
        return Paths.get(System.getProperty("user.home") + '/' + "Pictures/Camera OLD");
    }

    private static Path getTargetPath() {
        return Paths.get(System.getProperty("user.home") + '/' + "Pictures/Camera NEW");
    }
}
