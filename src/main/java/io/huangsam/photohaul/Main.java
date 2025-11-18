package io.huangsam.photohaul;

import io.huangsam.photohaul.deduplication.PhotoDeduplicator;
import io.huangsam.photohaul.migration.MigratorFactory;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PhotoCollector;

public class Main {
    public static void main(String[] args) {
        Settings settings = Settings.getDefault();
        PhotoCollector photoCollector = new PhotoCollector();
        PathRuleSet pathRuleSet = PathRuleSet.getDefault();
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        PhotoResolver photoResolver = PhotoResolver.getDefault();
        MigratorFactory migratorFactory = new MigratorFactory();

        Application app = new Application(settings, photoCollector, pathRuleSet, deduplicator, photoResolver, migratorFactory);
        app.run();
    }
}
