package io.huangsam.photohaul;

import io.huangsam.photohaul.deduplication.PhotoDeduplicator;
import io.huangsam.photohaul.migration.MigrationException;
import io.huangsam.photohaul.migration.MigratorFactory;
import io.huangsam.photohaul.migration.MigratorMode;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PathWalker;
import io.huangsam.photohaul.traversal.PhotoCollector;
import org.slf4j.Logger;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);
    private static final Settings SETTINGS = Settings.getDefault();

    public static void main(String[] args) {
        PhotoCollector photoCollector = new PhotoCollector();
        PathRuleSet pathRuleSet = PathRuleSet.getDefault();

        PathWalker pathWalker = new PathWalker(SETTINGS.getSourcePath(), pathRuleSet);
        pathWalker.traverse(photoCollector);

        // Deduplicate photos based on SHA-256 hash
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photoCollector.getPhotos());

        MigratorMode migratorMode = SETTINGS.getMigratorMode();
        PhotoResolver photoResolver = PhotoResolver.getDefault();

        MigratorFactory migratorFactory = new MigratorFactory();
        try {
            Migrator migrator = migratorFactory.make(migratorMode, SETTINGS, photoResolver);
            migrator.migratePhotos(uniquePhotos);
            LOG.info("Finish with success={} failure={}", migrator.getSuccessCount(), migrator.getFailureCount());
        } catch (MigrationException e) {
            LOG.error("Cannot migrate with mode {}: {}", e.getMode(), e.getMessage());
        }
    }
}
