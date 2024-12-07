package io.huangsam.photohaul.migration;

public class MigratorException extends Exception {
    private final MigratorMode mode;

    public MigratorException(String message, MigratorMode mode) {
        super(message);
        this.mode = mode;
    }

    public MigratorMode getMode() {
        return mode;
    }
}
