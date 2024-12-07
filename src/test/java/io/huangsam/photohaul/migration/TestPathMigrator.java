package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPathMigrator extends TestPathBase {
    @Test
    void testMigratePhotos() {
        List<String> names = List.of("bauerlite.jpg", "salad.jpg", "foobar.jpg");
        PhotoPathVisitor pathVisitor = visitor(getStaticResources(), names);
        Migrator migrator = new PathMigrator(getTempResources(), new PhotoResolver(List.of()));
        migrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(1, migrator.getFailureCount());
    }

    @AfterAll
    static void tearDown() {
        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathVisitor pathVisitor = visitor(getTempResources(), names);
        PathMigrator pathMigrator = new PathMigrator(getStaticResources(), new PhotoResolver(List.of()));
        pathMigrator.migratePhotos(pathVisitor.getPhotos());
    }
}
