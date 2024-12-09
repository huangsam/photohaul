package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.traversal.PhotoPathCollector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static io.huangsam.photohaul.TestHelper.getTempResources;
import static io.huangsam.photohaul.TestHelper.getPathMover;
import static io.huangsam.photohaul.TestHelper.getPathCollector;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPathMigrator {
    @Test
    void testMigratePhotosDryRunAllSuccess() {
        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        Migrator migrator = getPathMover(getTempResources(), PathMigrator.Action.DRY_RUN);
        migrator.migratePhotos(pathCollector.getPhotos());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosCopyAllSuccess() {
        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        Migrator migrator = getPathMover(getTempResources(), PathMigrator.Action.COPY);
        migrator.migratePhotos(pathCollector.getPhotos());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }

    @Test
    void testMigratePhotosMoveAllFailure() {
        List<String> names = List.of("foobar.jpg");
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        Migrator migrator = getPathMover(getTempResources(), PathMigrator.Action.MOVE);
        migrator.migratePhotos(pathCollector.getPhotos());

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(1, migrator.getFailureCount());
    }
}
