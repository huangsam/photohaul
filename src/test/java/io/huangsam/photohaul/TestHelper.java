package io.huangsam.photohaul;

import io.huangsam.photohaul.migration.PathMigrator;
import io.huangsam.photohaul.migration.PhotoResolver;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.traversal.PhotoPathCollector;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class TestHelper {
    private static final Path TEST_RESOURCES = Path.of("src/test/resources");

    @NotNull
    public static Path getStaticResources() {
        return TEST_RESOURCES.resolve("static");
    }

    @NotNull
    public static Path getTempResources() {
        return TEST_RESOURCES.resolve("temp");
    }

    @NotNull
    public static PhotoPathCollector getPathCollector(Path path, @NotNull List<String> names) {
        PhotoPathCollector pathCollector = new PhotoPathCollector();
        for (String name : names) {
            pathCollector.addPhoto(path.resolve(name));
        }
        return pathCollector;
    }

    @NotNull
    public static PathMigrator getPathMover(Path destination, PathMigrator.Action action) {
        return new PathMigrator(destination, new PhotoResolver(List.of()), action);
    }

    @NotNull
    public static Photo getPhoto(String pathName) {
        return new Photo(Path.of(pathName), null, null, null, null, null, null, null);
    }
}
