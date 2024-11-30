package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPathMigrator extends TestPathBase {
    @Test
    void testMigratePhotos() {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();
        pathVisitor.visitPhoto(getStaticResources().resolve("bauerlite.jpg"));
        pathVisitor.visitPhoto(getStaticResources().resolve("salad.jpg"));

        PathMigrator pathMigrator = new PathMigrator(
                getTempResources(), StandardCopyOption.REPLACE_EXISTING, new PhotoResolver(List.of()));
        pathMigrator.migratePhotos(pathVisitor.getPhotos());

        assertEquals(2, pathMigrator.getSuccessCount());
        assertEquals(0, pathMigrator.getFailureCount());
    }

    @AfterAll
    static void tearDown() {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();
        pathVisitor.visitPhoto(getTempResources().resolve("bauerlite.jpg"));
        pathVisitor.visitPhoto(getTempResources().resolve("salad.jpg"));

        PathMigrator pathMigrator = new PathMigrator(
                getStaticResources(), StandardCopyOption.REPLACE_EXISTING, new PhotoResolver(List.of()));
        pathMigrator.migratePhotos(pathVisitor.getPhotos());
    }
}
