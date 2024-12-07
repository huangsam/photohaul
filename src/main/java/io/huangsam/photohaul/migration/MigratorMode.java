package io.huangsam.photohaul.migration;

public enum MigratorMode {
    /** Migrates photos to a local directory path. */
    PATH,

    /** Migrates photos to a Dropbox account. */
    DROPBOX,

    /** Migrates photos to a Google Drive account. */
    GOOGLE_DRIVE
}
