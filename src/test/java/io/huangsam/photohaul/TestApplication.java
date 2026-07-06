package io.huangsam.photohaul;

import io.huangsam.photohaul.deduplication.PhotoDeduplicator;
import io.huangsam.photohaul.migration.MigrationException;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.MigratorMode;
import io.huangsam.photohaul.migration.factory.MigratorFactory;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.traversal.PathRuleSet;
import io.huangsam.photohaul.traversal.PhotoCollector;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestApplication {

    @Mock
    private Migrator migratorMock;

    private Settings createSettings(Path tempDir) throws Exception {
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);
        Path imageFile = sourceDir.resolve("img1.jpg");
        Files.writeString(imageFile, "dummy image content ".repeat(10));

        Path propsFile = tempDir.resolve("app.properties");
        String propsContent = String.format(
                "migrator.mode=PATH%n"
                        + "path.source=%s%n",
                sourceDir.toString().replace("\\", "/")
        );
        Files.writeString(propsFile, propsContent);

        return Settings.load(propsFile.toString());
    }

    @Test
    void testRunSuccess(@TempDir @NonNull Path tempDir) throws Exception {
        Settings settings = createSettings(tempDir);

        MigratorFactory factory = new MigratorFactory();
        factory.register(MigratorMode.PATH, (s, r) -> migratorMock);

        when(migratorMock.getSuccessCount()).thenReturn(1L);
        when(migratorMock.getFailureCount()).thenReturn(0L);

        PhotoCollector collector = new PhotoCollector();
        PathRuleSet rules = PathRuleSet.getDefault();
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        PhotoResolver resolver = PhotoResolver.fromSettings(settings);

        Application app = new Application(settings, collector, rules, deduplicator, resolver, factory);
        app.run();

        // Verify that the collector gathered the photo and the deduplicator passed it to the migrator
        assertEquals(1, collector.getPhotos().size());
        verify(migratorMock).migratePhotos(any());
        verify(migratorMock).close();
    }

    @Test
    void testRunWithMigrationException(@TempDir @NonNull Path tempDir) throws Exception {
        Settings settings = createSettings(tempDir);

        MigratorFactory factory = new MigratorFactory();
        factory.register(MigratorMode.PATH, (s, r) -> migratorMock);

        doThrow(new MigrationException("Migration failed", MigratorMode.PATH)).when(migratorMock).migratePhotos(any());

        PhotoCollector collector = new PhotoCollector();
        PathRuleSet rules = PathRuleSet.getDefault();
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        PhotoResolver resolver = PhotoResolver.fromSettings(settings);

        Application app = new Application(settings, collector, rules, deduplicator, resolver, factory);
        assertThrows(MigrationException.class, app::run);

        verify(migratorMock).close();
    }

    @Test
    void testRunWithGenericException(@TempDir @NonNull Path tempDir) throws Exception {
        Settings settings = createSettings(tempDir);

        MigratorFactory factory = new MigratorFactory();
        factory.register(MigratorMode.PATH, (s, r) -> migratorMock);

        doThrow(new RuntimeException("Generic failure")).when(migratorMock).migratePhotos(any());

        PhotoCollector collector = new PhotoCollector();
        PathRuleSet rules = PathRuleSet.getDefault();
        PhotoDeduplicator deduplicator = new PhotoDeduplicator();
        PhotoResolver resolver = PhotoResolver.fromSettings(settings);

        Application app = new Application(settings, collector, rules, deduplicator, resolver, factory);
        assertThrows(RuntimeException.class, app::run);

        verify(migratorMock).close();
    }
}
