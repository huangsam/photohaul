package io.huangsam.photohaul;

import io.huangsam.photohaul.migration.MigratorException;
import io.huangsam.photohaul.migration.MigratorFactory;
import io.huangsam.photohaul.migration.MigratorMode;
import io.huangsam.photohaul.migration.PhotoResolver;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PathWalker;
import io.huangsam.photohaul.traversal.PhotoPathCollector;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);
    private static final Settings SETTINGS = Settings.getDefault();

    public static void main(String[] args) {
        PhotoPathCollector pathCollector = new PhotoPathCollector();

        PathRuleSet pathRuleSet = PathRuleSet.getDefault();

        PathWalker pathWalker = new PathWalker(SETTINGS.getSourcePath(), pathRuleSet);
        pathWalker.traverse(pathCollector);

        MigratorMode migratorMode = MigratorMode.PATH;
        PhotoResolver photoResolver = PhotoResolver.getDefault();

        MigratorFactory migratorFactory = new MigratorFactory();
        try {
            Migrator migrator = migratorFactory.make(migratorMode, SETTINGS, photoResolver);
            migrator.migratePhotos(pathCollector.getPhotos());
            LOG.info("Finish with success={} failure={}", migrator.getSuccessCount(), migrator.getFailureCount());
        } catch (MigratorException e) {
            LOG.error("Cannot migrate with mode {}: {}", e.getMode(), e.getMessage());
        }
    }
}
