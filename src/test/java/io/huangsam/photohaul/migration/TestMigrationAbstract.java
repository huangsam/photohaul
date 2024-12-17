package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.traversal.PhotoCollector;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;

public abstract class TestMigrationAbstract {
    void run(@NotNull Migrator migrator) {
        run(migrator, List.of("bauerlite.jpg", "salad.jpg"));
    }

    void run(@NotNull Migrator migrator, List<String> names) {
        PhotoCollector photoCollector = getPathCollector(getStaticResources(), names);
        migrator.migratePhotos(photoCollector.getPhotos());
    }

    @NotNull
    private PhotoCollector getPathCollector(Path path, @NotNull List<String> names) {
        PhotoCollector photoCollector = new PhotoCollector();
        for (String name : names) {
            photoCollector.addPhoto(path.resolve(name));
        }
        return photoCollector;
    }
}
