package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.traversal.PhotoPathCollector;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;

public abstract class TestMigrationAbstract {
    void run(@NotNull Migrator migrator) {
        run(migrator, List.of("bauerlite.jpg", "salad.jpg"));
    }

    void run(@NotNull Migrator migrator, List<String> names) {
        PhotoPathCollector pathCollector = getPathCollector(getStaticResources(), names);
        migrator.migratePhotos(pathCollector.getPhotos());
    }

    @NotNull
    private static PhotoPathCollector getPathCollector(Path path, @NotNull List<String> names) {
        PhotoPathCollector pathCollector = new PhotoPathCollector();
        for (String name : names) {
            pathCollector.addPhoto(path.resolve(name));
        }
        return pathCollector;
    }
}
