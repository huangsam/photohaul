package io.huangsam.photohaul.migration.factory;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.PathMigrator;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

/**
 * Factory for creating PathMigrator instances.
 */
public class PathMigratorFactory implements MigratorFactoryStrategy {
    @Override
    public @NonNull Migrator create(@NonNull Settings settings, @NonNull PhotoResolver resolver) {
        Path target = getPathTargetDirectory(settings);
        String actionValue = settings.getValue("path.action", "MOVE").toUpperCase();
        return new PathMigrator(target, resolver, PathMigrator.Action.valueOf(actionValue), settings.isDryRun());
    }

    private @NonNull Path getPathTargetDirectory(@NonNull Settings settings) {
        return settings.fileSystem().getPath(System.getProperty("user.home"))
                .resolve(settings.getValue("path.target"));
    }
}
