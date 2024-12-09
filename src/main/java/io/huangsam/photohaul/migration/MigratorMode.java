package io.huangsam.photohaul.migration;

/**
 * This mode determines whether photos are migrated between local folders or
 * migrated to a cloud service.
 *
 * <p> Please add the necessary fields to {@code config.properties} based on
 * the selected mode. Check sample configurations in {@code src/main/resources}
 * for reference.
 */
public enum MigratorMode {
    /** Migrate photos to a local directory path. */
    PATH,

    /** Migrate photos to a Dropbox account. */
    DROPBOX,

    /** Migrate photos to a Google Drive account. */
    GOOGLE_DRIVE
}
