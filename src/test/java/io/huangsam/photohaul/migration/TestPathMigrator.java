package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPathMigrator extends TestPathBase {
    @Test
    void testMigratePhotos() {
        PhotoPathVisitor pathVisitor = visitor(getStaticResources());
        PathMigrator pathMigrator = migrator(getTempResources());
        pathMigrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(2, pathMigrator.getSuccessCount());
        assertEquals(0, pathMigrator.getFailureCount());
    }

    @AfterAll
    static void tearDown() {
        PhotoPathVisitor pathVisitor = visitor(getTempResources());
        PathMigrator pathMigrator = migrator(getStaticResources());
        pathMigrator.migratePhotos(pathVisitor.getPhotos());
    }

    private static PhotoPathVisitor visitor(Path path) {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();
        pathVisitor.visitPhoto(path.resolve("bauerlite.jpg"));
        pathVisitor.visitPhoto(path.resolve("salad.jpg"));
        return pathVisitor;
    }

    private static PathMigrator migrator(Path path) {
        return new PathMigrator(
                path, StandardCopyOption.REPLACE_EXISTING, new PhotoResolver(List.of()));
    }
}
