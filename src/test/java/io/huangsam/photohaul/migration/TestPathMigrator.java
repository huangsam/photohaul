package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static io.huangsam.photohaul.TestHelper.getTempResources;
import static io.huangsam.photohaul.TestHelper.pathMover;
import static io.huangsam.photohaul.TestHelper.pathVisitor;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPathMigrator {
    @Test
    void testMigratePhotosAllSuccess() {
        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathVisitor pathVisitor = pathVisitor(getStaticResources(), names);
        Migrator migrator = pathMover(getTempResources());
        migrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());
    }
}
