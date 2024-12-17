package io.huangsam.photohaul.migration;

/**
 * This resembles an issue with methods called in {@link MigratorFactory}
 * and {@link Migrator}.
 *
 * <p> To provide more context on the issue origin, we provide {@code mode} as
 * another constructor field.
 */
public class MigrationException extends Exception {
    private final MigratorMode mode;

    public MigrationException(String message, MigratorMode mode) {
        super(message);
        this.mode = mode;
    }

    public MigratorMode getMode() {
        return mode;
    }
}
