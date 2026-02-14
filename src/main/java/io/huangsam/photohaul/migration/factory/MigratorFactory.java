package io.huangsam.photohaul.migration.factory;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.MigratorMode;
import io.huangsam.photohaul.migration.delta.DeltaMigrator;
import io.huangsam.photohaul.migration.state.MigrationStateFile;
import io.huangsam.photohaul.migration.state.PathStateStorage;
import io.huangsam.photohaul.migration.state.StateFileStorage;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A factory class for creating {@link Migrator} instances based on the desired
 * migration strategy.
 */
public class MigratorFactory {
    private static final Logger LOG = getLogger(MigratorFactory.class);

    private final Map<MigratorMode, MigratorFactoryStrategy> factoryStrategies = Map.of(
            MigratorMode.PATH, new PathMigratorFactory(),
            MigratorMode.DROPBOX, new DropboxMigratorFactory(),
            MigratorMode.GOOGLE_DRIVE, new GoogleDriveMigratorFactory(),
            MigratorMode.SFTP, new SftpMigratorFactory(),
            MigratorMode.S3, new S3MigratorFactory()
    );

    /**
     * Create instance for migrating photos.
     *
     * @param mode migrator mode
     * @param settings settings for migration process
     * @param resolver photo resolver for target path
     * @return migrator instance
     */
    public @NonNull Migrator make(@NotNull MigratorMode mode, @NonNull Settings settings, PhotoResolver resolver) {
        Migrator baseMigrator = createBaseMigrator(mode, settings, resolver);

        // Wrap with DeltaMigrator if delta migration is enabled
        if (settings.isDeltaEnabled()) {
            return wrapWithDeltaMigrator(baseMigrator, mode, settings);
        }

        return baseMigrator;
    }

    private @NotNull Migrator createBaseMigrator(@NotNull MigratorMode mode, @NotNull Settings settings, @NotNull PhotoResolver resolver) {
        MigratorFactoryStrategy strategy = factoryStrategies.get(mode);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported migrator mode: " + mode);
        }
        return strategy.create(settings, resolver);
    }

    private @NotNull Migrator wrapWithDeltaMigrator(@NotNull Migrator baseMigrator, @NotNull MigratorMode mode, @NotNull Settings settings) {
        StateFileStorage stateStorage = createStateStorage(mode, settings);
        LOG.info("Delta migration enabled for mode {}", mode);
        MigrationStateFile stateFile = new MigrationStateFile(stateStorage);
        return new DeltaMigrator(baseMigrator, stateFile);
    }

    /**
     * Create a StateFileStorage for the given migrator mode.
     *
     * @param mode     the migrator mode
     * @param settings the settings
     * @return a non-null StateFileStorage instance for the given migrator mode
     */
    private @NonNull StateFileStorage createStateStorage(@NotNull MigratorMode mode, @NotNull Settings settings) {
        return switch (mode) {
            case PATH -> new PathStateStorage(getPathTargetDirectory(settings));
            // Delta migration for cloud storage types requires additional implementation
            // For now, they use local state storage as a fallback
            case DROPBOX, GOOGLE_DRIVE, SFTP, S3 -> {
                // Use source path for state storage as fallback for cloud destinations
                Path sourcePath = settings.getSourcePath();
                LOG.info("Using local state storage at {} for {} destination", sourcePath, mode);
                yield new PathStateStorage(sourcePath);
            }
        };
    }

    /**
     * Get the target directory path for PATH migrator mode.
     *
     * @param settings the settings
     * @return the resolved target path
     */
    @NotNull
    private Path getPathTargetDirectory(@NotNull Settings settings) {
        return java.nio.file.Paths.get(System.getProperty("user.home"))
                .resolve(settings.getValue("path.target"));
    }
}
