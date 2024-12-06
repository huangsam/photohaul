package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPathMigrator extends TestPathBase {
    @Test
    void testMigratePhotos() {
        List<String> names = List.of("bauerlite.jpg", "salad.jpg", "foobar.jpg");
        PhotoPathVisitor pathVisitor = visitor(getStaticResources(), names);
        PathMigrator pathMigrator = migrator(getTempResources());
        pathMigrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(2, pathMigrator.getSuccessCount());
        assertEquals(1, pathMigrator.getFailureCount());
    }

    @AfterAll
    static void tearDown() {
        List<String> names = List.of("bauerlite.jpg", "salad.jpg");
        PhotoPathVisitor pathVisitor = visitor(getTempResources(), names);
        PathMigrator pathMigrator = migrator(getStaticResources());
        pathMigrator.migratePhotos(pathVisitor.getPhotos());
    }

    private static PathMigrator migrator(Path path) {
        return new PathMigrator(path, new PhotoResolver(List.of()));
    }
}
