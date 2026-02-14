package io.huangsam.photohaul;

import io.huangsam.photohaul.deduplication.PhotoDeduplicator;
import io.huangsam.photohaul.migration.MigrationException;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.MigratorMode;
import io.huangsam.photohaul.migration.factory.MigratorFactory;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PathWalker;
import io.huangsam.photohaul.traversal.PhotoCollector;
import org.slf4j.Logger;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public record Application(Settings settings,
                          PhotoCollector photoCollector, PathRuleSet pathRuleSet,
                          PhotoDeduplicator deduplicator, PhotoResolver photoResolver,
                          MigratorFactory migratorFactory) {
    private static final Logger LOG = getLogger(Application.class);

    public void run() {
        PathWalker pathWalker = new PathWalker(settings.getSourcePath(), pathRuleSet);
        pathWalker.traverse(photoCollector);

        Collection<Photo> uniquePhotos = deduplicator.deduplicate(photoCollector.getPhotos());

        MigratorMode migratorMode = settings.getMigratorMode();

        try (Migrator migrator = migratorFactory.make(migratorMode, settings, photoResolver)) {
            migrator.migratePhotos(uniquePhotos);
            LOG.info("Finish with success={} failure={}", migrator.getSuccessCount(), migrator.getFailureCount());
        } catch (MigrationException e) {
            LOG.error("Cannot migrate with mode {}: {}", e.getMode(), e.getMessage());
        } catch (Exception e) {
            LOG.error("Error during migration or closing migrator: {}", e.getMessage());
        }
    }
}
