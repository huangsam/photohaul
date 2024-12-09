package io.huangsam.photohaul;

import io.huangsam.photohaul.migration.PathMigrator;
import io.huangsam.photohaul.migration.PhotoResolver;
import io.huangsam.photohaul.traversal.PhotoPathCollector;

import java.nio.file.Path;
import java.util.List;

public class TestHelper {
    private static final Path TEST_RESOURCES = Path.of("src/test/resources");

    public static Path getStaticResources() {
        return TEST_RESOURCES.resolve("static");
    }

    public static Path getTempResources() {
        return TEST_RESOURCES.resolve("temp");
    }

    public static PhotoPathCollector getPathCollector(Path path, List<String> names) {
        PhotoPathCollector pathCollector = new PhotoPathCollector();
        for (String name : names) {
            pathCollector.addPhoto(path.resolve(name));
        }
        return pathCollector;
    }

    public static PathMigrator getPathMover(Path destination) {
        return new PathMigrator(destination, PathMigrator.Action.DRY_RUN, new PhotoResolver(List.of()));
    }
}
