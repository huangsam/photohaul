package io.huangsam.photohaul;

import io.huangsam.photohaul.deduplication.PhotoDeduplicator;
import io.huangsam.photohaul.migration.MigratorMode;
import io.huangsam.photohaul.migration.factory.DropboxMigratorFactory;
import io.huangsam.photohaul.migration.factory.GoogleDriveMigratorFactory;
import io.huangsam.photohaul.migration.factory.MigratorFactory;
import io.huangsam.photohaul.migration.factory.PathMigratorFactory;
import io.huangsam.photohaul.migration.factory.S3MigratorFactory;
import io.huangsam.photohaul.migration.factory.SftpMigratorFactory;
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
        migratorFactory.register(MigratorMode.PATH, new PathMigratorFactory());
        migratorFactory.register(MigratorMode.DROPBOX, new DropboxMigratorFactory());
        migratorFactory.register(MigratorMode.GOOGLE_DRIVE, new GoogleDriveMigratorFactory());
        migratorFactory.register(MigratorMode.SFTP, new SftpMigratorFactory());
        migratorFactory.register(MigratorMode.S3, new S3MigratorFactory());

        Application app = new Application(
                settings, photoCollector, pathRuleSet, deduplicator, photoResolver, migratorFactory);
        app.run();
    }
}
