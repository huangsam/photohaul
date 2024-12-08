package io.huangsam.photohaul.migration;

public enum MigratorMode {
    /** Migrate photos to a local directory path. */
    PATH,

    /** Migrate photos to a Dropbox account. */
    DROPBOX,

    /** Migrate photos to a Google Drive account. */
    GOOGLE_DRIVE
}
