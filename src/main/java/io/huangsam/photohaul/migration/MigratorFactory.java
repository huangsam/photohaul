package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.state.MigrationStateFile;
import io.huangsam.photohaul.migration.state.PathStateStorage;
import io.huangsam.photohaul.migration.state.StateFileStorage;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A factory class for creating {@link Migrator} instances based on the desired
 * migration strategy.
 */
public class MigratorFactory {
    private static final Logger LOG = getLogger(MigratorFactory.class);

    /**
     * Create instance for migrating photos.
     *
     * @param mode migrator mode
     * @param settings settings for migration process
     * @param resolver photo resolver for target path
     * @return migrator instance
     */
    public @NonNull Migrator make(@NotNull MigratorMode mode, @NonNull Settings settings, PhotoResolver resolver) {
        Migrator baseMigrator = switch (mode) {
            case PATH -> makePath(settings, resolver);
            case DROPBOX -> makeDropbox(settings, resolver);
            case GOOGLE_DRIVE -> makeGoogleDrive(settings, resolver);
            case SFTP -> makeSftp(settings, resolver);
            case S3 -> makeS3(settings, resolver);
        };

        // Wrap with DeltaMigrator if delta migration is enabled
        if (settings.isDeltaEnabled()) {
            StateFileStorage stateStorage = createStateStorage(mode, settings);
            LOG.info("Delta migration enabled for mode {}", mode);
            MigrationStateFile stateFile = new MigrationStateFile(stateStorage);
            return new DeltaMigrator(baseMigrator, stateFile);
        }

        return baseMigrator;
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
        return Paths.get(System.getProperty("user.home"))
                .resolve(settings.getValue("path.target"));
    }

    @NotNull
    private PathMigrator makePath(@NotNull Settings settings, PhotoResolver resolver) {
        Path target = getPathTargetDirectory(settings);
        String actionValue = settings.getValue("path.action", "MOVE").toUpperCase();
        return new PathMigrator(target, resolver, PathMigrator.Action.valueOf(actionValue));
    }

    @NotNull
    private DropboxMigrator makeDropbox(@NotNull Settings settings, PhotoResolver resolver) {
        String target = settings.getValue("dbx.target");
        DbxRequestConfig config = DbxRequestConfig.newBuilder(settings.getValue("dbx.clientId")).build();
        DbxClientV2 client = new DbxClientV2(config, settings.getValue("dbx.accessToken"));
        return new DropboxMigrator(target, resolver, client);
    }

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(DriveScopes.DRIVE);

    @NotNull
    private GoogleDriveMigrator makeGoogleDrive(@NotNull Settings settings, PhotoResolver resolver) {
        String fileName = settings.getValue("drive.credentialFile");
        String app = settings.getValue("drive.appName");
        try {
            com.google.api.client.http.HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
                if (in == null) {
                    throw new FileNotFoundException("Cannot find " + fileName);
                }
                GoogleCredentials credentials = GoogleCredentials.fromStream(in).createScoped(SCOPES);
                HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

                Drive service = new Drive.Builder(transport, JSON_FACTORY, requestInitializer)
                        .setApplicationName(app)
                        .build();

                return new GoogleDriveMigrator(settings.getValue("drive.target"), resolver, service, transport);
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new MigrationException(e.getMessage(), MigratorMode.GOOGLE_DRIVE);
        }
    }

    @NotNull
    private SftpMigrator makeSftp(@NotNull Settings settings, PhotoResolver resolver) {
        String host = settings.getValue("sftp.host");
        int port = Integer.parseInt(settings.getValue("sftp.port", "22"));
        String username = settings.getValue("sftp.username");
        String password = settings.getValue("sftp.password");
        String target = settings.getValue("sftp.target");
        return new SftpMigrator(host, port, username, password, target, resolver);
    }

    @NotNull
    private S3Migrator makeS3(@NotNull Settings settings, PhotoResolver resolver) {
        String accessKey = settings.getValue("s3.accessKey");
        String secretKey = settings.getValue("s3.secretKey");
        String region = settings.getValue("s3.region", "us-east-1");
        String bucket = settings.getValue("s3.bucket");
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
        return new S3Migrator(bucket, resolver, s3Client);
    }
}
