package io.huangsam.photohaul;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import io.huangsam.photohaul.migration.CameraPathMigrator;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.traversal.PathRule;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PathTraversal;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);
    private static final Settings SETTINGS = new Settings();

    public static void main(String[] args) {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();

        PathRuleSet pathRuleSet = new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.allowedExtensions("jpg", "jpeg", "png").or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));

        Path sourceRoot = SETTINGS.getSourceRoot();
        Path targetRoot = SETTINGS.getTargetRoot();

        PathTraversal pathTraversal = new PathTraversal(sourceRoot, pathRuleSet);
        pathTraversal.traverse(pathVisitor);

        Migrator migrator = new CameraPathMigrator(targetRoot, StandardCopyOption.REPLACE_EXISTING);
        migrator.migratePhotos(pathVisitor.getPhotos());

        LOG.info("Finish with success={} failure={}", migrator.getSuccessCount(), migrator.getFailureCount());
    }
}
