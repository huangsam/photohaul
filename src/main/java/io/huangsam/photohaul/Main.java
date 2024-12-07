package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.List;

import io.huangsam.photohaul.migration.MigratorFactory;
import io.huangsam.photohaul.migration.MigratorMode;
import io.huangsam.photohaul.migration.PhotoFunction;
import io.huangsam.photohaul.migration.PhotoResolver;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.traversal.PathRule;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PathTraversal;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);
    private static final Settings SETTINGS = new Settings("config.properties");

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();

        PathRuleSet pathRuleSet = new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.validExtensions().or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));

        PathTraversal pathTraversal = new PathTraversal(SETTINGS.getSourceRootPath(), pathRuleSet);
        pathTraversal.traverse(pathVisitor);

        MigratorMode migratorMode = MigratorMode.PATH;
        PhotoResolver photoResolver = new PhotoResolver(List.of(PhotoFunction.yearTaken()));

        MigratorFactory migratorFactory = new MigratorFactory();
        Migrator migrator = migratorFactory.make(migratorMode, SETTINGS, photoResolver);
        migrator.migratePhotos(pathVisitor.getPhotos());

        LOG.info("Finish with success={} failure={}", migrator.getSuccessCount(), migrator.getFailureCount());
    }
}
