package io.huangsam.photohaul;

import io.huangsam.photohaul.deduplication.PhotoDeduplicator;
import io.huangsam.photohaul.migration.factory.MigratorFactory;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PhotoCollector;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);

    public static void main(String[] args) {
        Settings settings = Settings.getDefault();
        PhotoCollector photoCollector = new PhotoCollector();
        PathRuleSet pathRuleSet = PathRuleSet.getDefault();
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        PhotoResolver photoResolver = PhotoResolver.getDefault();

        MigratorFactory migratorFactory = new MigratorFactory();
        migratorFactory.registerDefaults();

        Application app = new Application(
                settings, photoCollector, pathRuleSet, deduplicator, photoResolver, migratorFactory);

        try {
            app.run();
        } catch (Exception e) {
            LOG.error("Application failed: {}", e.getMessage());
            System.exit(1);
        }
    }
}

