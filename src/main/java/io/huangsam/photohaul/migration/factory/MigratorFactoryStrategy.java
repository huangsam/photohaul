package io.huangsam.photohaul.migration.factory;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Factory interface for creating specific migrator instances.
 */
interface MigratorFactoryStrategy {
    /**
     * Create a migrator instance for this strategy.
     *
     * @param settings the settings
     * @param resolver the photo resolver
     * @return the migrator instance
     */
    @NotNull Migrator create(@NotNull Settings settings, @NotNull PhotoResolver resolver);
}
