package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.Settings;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestMigratorFactory {
    private static final MigratorFactory FACTORY = new MigratorFactory();
    private static final PhotoResolver RESOLVER = new PhotoResolver(List.of());

    @Test
    void testMakePathMigrator() throws MigratorException {
        Settings settings = new Settings("path-example.properties");
        FACTORY.make(MigratorMode.PATH, settings, RESOLVER);
    }

    @Test
    void testMakeDropboxMigrator() throws MigratorException {
        Settings settings = new Settings("dbx-example.properties");
        FACTORY.make(MigratorMode.DROPBOX, settings, RESOLVER);
    }
}
