package io.huangsam.photohaul;

import io.huangsam.photohaul.migration.PathMigrator;
import io.huangsam.photohaul.migration.PhotoResolver;
import io.huangsam.photohaul.traversal.PhotoPathVisitor;

import java.nio.file.Path;
import java.util.List;

public abstract class TestPathBase {
    private static final Path TEST_RESOURCES = Path.of("src/test/resources");

    protected static Path getStaticResources() {
        return TEST_RESOURCES.resolve("static");
    }

    protected static Path getTempResources() {
        return TEST_RESOURCES.resolve("temp");
    }

    protected static PhotoPathVisitor pathVisitor(Path path, List<String> names) {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();
        for (String name : names) {
            pathVisitor.visitPhoto(path.resolve(name));
        }
        return pathVisitor;
    }

    protected static PathMigrator pathMover(Path destination) {
        return new PathMigrator(destination, PathMigrator.Option.DRY_RUN, new PhotoResolver(List.of()));
    }
}
