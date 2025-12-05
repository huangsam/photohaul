package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMigratorFactory {
    private static final MigratorFactory FACTORY = new MigratorFactory();
    private static final PhotoResolver RESOLVER = new PhotoResolver(List.of());

    @Test
    void testMakePathMigratorSuccess() throws MigrationException {
        Settings settings = new Settings("path-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.PATH, settings, RESOLVER);
        assertSame(PathMigrator.class, migrator.getClass());
    }

    @Test
    void testMakeDropboxMigratorSuccess() throws MigrationException {
        Settings settings = new Settings("dbx-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.DROPBOX, settings, RESOLVER);
        assertSame(DropboxMigrator.class, migrator.getClass());
    }

    @Test
    void testMakeGoogleDriveMigratorFailure() {
        Settings settings = new Settings("drive-example.properties");
        MigrationException exception = assertThrows(MigrationException.class, () -> FACTORY.make(MigratorMode.GOOGLE_DRIVE, settings, RESOLVER));
        assertEquals(MigratorMode.GOOGLE_DRIVE, exception.getMode());
    }

    @Test
    void testMakeSftpMigratorSuccess() {
        Settings settings = new Settings("sftp-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.SFTP, settings, RESOLVER);
        assertSame(SftpMigrator.class, migrator.getClass());
    }

    @Test
    void testMakeS3MigratorSuccess() {
        Settings settings = new Settings("s3-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.S3, settings, RESOLVER);
        assertSame(S3Migrator.class, migrator.getClass());
    }

    @Test
    void testMakePathMigratorWithDeltaEnabled(@TempDir Path tempDir) throws IOException {
        // Create a temporary properties file with delta enabled
        Path propsFile = tempDir.resolve("delta-path.properties");
        String propsContent = String.format(
                "migrator.mode=PATH%n" +
                "path.source=Dummy/Source%n" +
                "path.target=Dummy/Target%n" +
                "path.action=DRY_RUN%n" +
                "delta.enabled=true%n"
        );
        Files.writeString(propsFile, propsContent);

        Settings settings = new Settings(propsFile.toString());
        Migrator migrator = FACTORY.make(MigratorMode.PATH, settings, RESOLVER);

        assertInstanceOf(DeltaMigrator.class, migrator);
    }

    @Test
    void testMakeDropboxMigratorWithDeltaEnabled(@TempDir Path tempDir) throws IOException {
        // Create a temporary properties file with delta enabled
        Path propsFile = tempDir.resolve("delta-dbx.properties");
        String propsContent = String.format(
                "migrator.mode=DROPBOX%n" +
                "path.source=Dummy/Source%n" +
                "dbx.target=/Demo/Target%n" +
                "dbx.clientId=TestClient%n" +
                "dbx.accessToken=TestToken%n" +
                "delta.enabled=true%n"
        );
        Files.writeString(propsFile, propsContent);

        Settings settings = new Settings(propsFile.toString());
        Migrator migrator = FACTORY.make(MigratorMode.DROPBOX, settings, RESOLVER);

        assertInstanceOf(DeltaMigrator.class, migrator);
    }

    @Test
    void testMakeSftpMigratorWithDeltaEnabled(@TempDir Path tempDir) throws IOException {
        // Create a temporary properties file with delta enabled
        Path propsFile = tempDir.resolve("delta-sftp.properties");
        String propsContent = String.format(
                "migrator.mode=SFTP%n" +
                "path.source=Dummy/Source%n" +
                "sftp.host=localhost%n" +
                "sftp.port=22%n" +
                "sftp.username=user%n" +
                "sftp.password=pass%n" +
                "sftp.target=/photos%n" +
                "delta.enabled=true%n"
        );
        Files.writeString(propsFile, propsContent);

        Settings settings = new Settings(propsFile.toString());
        Migrator migrator = FACTORY.make(MigratorMode.SFTP, settings, RESOLVER);

        assertInstanceOf(DeltaMigrator.class, migrator);
    }

    @Test
    void testMakeS3MigratorWithDeltaEnabled(@TempDir Path tempDir) throws IOException {
        // Create a temporary properties file with delta enabled
        Path propsFile = tempDir.resolve("delta-s3.properties");
        String propsContent = String.format(
                "migrator.mode=S3%n" +
                "path.source=Dummy/Source%n" +
                "s3.bucket=test-bucket%n" +
                "s3.accessKey=accessKey%n" +
                "s3.secretKey=secretKey%n" +
                "s3.region=us-east-1%n" +
                "delta.enabled=true%n"
        );
        Files.writeString(propsFile, propsContent);

        Settings settings = new Settings(propsFile.toString());
        Migrator migrator = FACTORY.make(MigratorMode.S3, settings, RESOLVER);

        assertInstanceOf(DeltaMigrator.class, migrator);
    }

    @Test
    void testMakePathMigratorWithDeltaDisabled() {
        Settings settings = new Settings("path-example.properties");
        Migrator migrator = FACTORY.make(MigratorMode.PATH, settings, RESOLVER);

        // Default is delta disabled, so should be PathMigrator
        assertSame(PathMigrator.class, migrator.getClass());
    }

    @Test
    void testMakePathMigratorWithExplicitDeltaDisabled(@TempDir Path tempDir) throws IOException {
        // Create a temporary properties file with delta explicitly disabled
        Path propsFile = tempDir.resolve("nodelta-path.properties");
        String propsContent = String.format(
                "migrator.mode=PATH%n" +
                "path.source=Dummy/Source%n" +
                "path.target=Dummy/Target%n" +
                "path.action=DRY_RUN%n" +
                "delta.enabled=false%n"
        );
        Files.writeString(propsFile, propsContent);

        Settings settings = new Settings(propsFile.toString());
        Migrator migrator = FACTORY.make(MigratorMode.PATH, settings, RESOLVER);

        assertSame(PathMigrator.class, migrator.getClass());
    }
}
