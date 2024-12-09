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
    void testMigratePhotosAllSuccess() {
        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        Migrator migrator = getPathMover(getTempResources());
        migrator.migratePhotos(pathCollector.getPhotos());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }
}
