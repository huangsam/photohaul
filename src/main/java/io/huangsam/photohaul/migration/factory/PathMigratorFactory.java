package io.huangsam.photohaul.migration.factory;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.PathMigrator;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Factory for creating PathMigrator instances.
 */
class PathMigratorFactory implements MigratorFactoryStrategy {
    @Override
    public @NotNull Migrator create(@NotNull Settings settings, @NotNull PhotoResolver resolver) {
        Path target = getPathTargetDirectory(settings);
        String actionValue = settings.getValue("path.action", "MOVE").toUpperCase();
        return new PathMigrator(target, resolver, PathMigrator.Action.valueOf(actionValue));
    }

    private @NotNull Path getPathTargetDirectory(@NotNull Settings settings) {
        return Paths.get(System.getProperty("user.home"))
                .resolve(settings.getValue("path.target"));
    }
}
