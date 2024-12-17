package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMigratorFactory {
    private static final MigratorFactory FACTORY = new MigratorFactory();
    private static final PhotoResolver RESOLVER = new PhotoResolver(List.of());

    @Test
    void testMakePathMigratorSuccess() throws MigrationException {
        Settings settings = new Settings("path-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.PATH, settings, RESOLVER);
        assertSame(PathMigrator.class, migrator.getClass());
    }

    @Test
    void testMakeDropboxMigratorSuccess() throws MigrationException {
        Settings settings = new Settings("dbx-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.DROPBOX, settings, RESOLVER);
        assertSame(DropboxMigrator.class, migrator.getClass());
    }

    @Test
    void testMakeGoogleDriveMigratorFailure() {
        Settings settings = new Settings("drive-example.properties");
        MigrationException exception = assertThrows(MigrationException.class, () -> FACTORY.make(MigratorMode.GOOGLE_DRIVE, settings, RESOLVER));
        assertEquals(MigratorMode.GOOGLE_DRIVE, exception.getMode());
    }
}
