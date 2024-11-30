package io.huangsam.photohaul;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import io.huangsam.photohaul.migration.PhotoFunction;
import io.huangsam.photohaul.migration.PhotoResolver;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.PathMigrator;
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
                PathRule.validExtensions().or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));

        PathTraversal pathTraversal = new PathTraversal(SETTINGS.getSourceRoot(), pathRuleSet);
        pathTraversal.traverse(pathVisitor);

        CopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
        PhotoResolver photoResolver = new PhotoResolver(List.of(PhotoFunction.yearTaken()));

        Migrator migrator = new PathMigrator(SETTINGS.getTargetRoot(), copyOption, photoResolver);
        migrator.migratePhotos(pathVisitor.getPhotos());

        LOG.info("Finish with success={} failure={}", migrator.getSuccessCount(), migrator.getFailureCount());
    }
}
