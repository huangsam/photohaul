package io.huangsam.photohaul.migration;

/**
 * This resembles an issue with methods called in {@link MigratorFactory}
 * and {@link Migrator}.
 *
 * <p> To provide more context on the issue's origin, we provide {@code mode} as
 * an additional constructor field.
 */
public class MigrationException extends RuntimeException {
    private final MigratorMode mode;

    public MigrationException(String message, MigratorMode mode) {
        super(message);
        this.mode = mode;
    }

    public MigratorMode getMode() {
        return mode;
    }
}
