package io.huangsam.photohaul.migration;

/**
 * This mode determines whether photos are migrated between local folders or
 * migrated to a cloud service.
 *
 * <p> Please add the necessary fields to your properties file based on
 * the selected mode. Check sample files in {@code src/main/resources}
 * for reference.
 */
public enum MigratorMode {
    /** Migrate photos to a local directory path. */
    PATH,

    /** Migrate photos to a Dropbox account. */
    DROPBOX,

    /** Migrate photos to a Google Drive account. */
    GOOGLE_DRIVE,

    /** Migrate photos to an SFTP server. */
    SFTP,

    /** Migrate photos to an Amazon S3 bucket. */
    S3
}

