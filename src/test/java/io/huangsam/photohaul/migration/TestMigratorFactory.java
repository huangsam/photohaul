package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.Settings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

public class TestMigratorFactory {
    private static final MigratorFactory FACTORY = new MigratorFactory();
    private static final PhotoResolver RESOLVER = new PhotoResolver(List.of());

    @Test
    void testMakePathMigratorClass() throws MigratorException {
        Settings settings = new Settings("path-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.PATH, settings, RESOLVER);
        assertSame(PathMigrator.class, migrator.getClass());
    }

    @Test
    void testMakeDropboxMigratorClass() throws MigratorException {
        Settings settings = new Settings("dbx-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.DROPBOX, settings, RESOLVER);
        assertSame(DropboxMigrator.class, migrator.getClass());
    }
}
