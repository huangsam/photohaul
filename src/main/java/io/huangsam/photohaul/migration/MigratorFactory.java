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
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * A factory class for creating {@link Migrator} instances based on the desired
 * migration strategy.
 */
public class MigratorFactory {
    /**
     * Create instance for migrating photos.
     *
     * @param mode migrator mode
     * @param settings settings for migration process
     * @param resolver photo resolver for target path
     * @return migrator instance
     */
    public Migrator make(@NotNull MigratorMode mode, Settings settings, PhotoResolver resolver) {
        return switch (mode) {
            case PATH -> makePath(settings, resolver);
            case DROPBOX -> makeDropbox(settings, resolver);
            case GOOGLE_DRIVE -> makeGoogleDrive(settings, resolver);
            case FTP -> makeFtp(settings, resolver);
        };
    }

    @NotNull
    private PathMigrator makePath(@NotNull Settings settings, PhotoResolver resolver) {
        Path target = Paths.get(System.getProperty("user.home"));
        target = target.resolve(settings.getValue("path.target"));
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
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new FileNotFoundException("Cannot find " + fileName);
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(in).createScoped(SCOPES);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            Drive service = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, requestInitializer)
                    .setApplicationName(app)
                    .build();

            return new GoogleDriveMigrator(settings.getValue("drive.target"), resolver, service);
        } catch (GeneralSecurityException | IOException e) {
            throw new MigrationException(e.getMessage(), MigratorMode.GOOGLE_DRIVE);
        }
    }

    @NotNull
    private FtpMigrator makeFtp(@NotNull Settings settings, PhotoResolver resolver) {
        String host = settings.getValue("ftp.host");
        int port;
        try {
            port = Integer.parseInt(settings.getValue("ftp.port", "21"));
        } catch (NumberFormatException e) {
            throw new MigrationException("Invalid FTP port number", MigratorMode.FTP);
        }
        String username = settings.getValue("ftp.username");
        String password = settings.getValue("ftp.password");
        String target = settings.getValue("ftp.target");
        return new FtpMigrator(host, port, username, password, target, resolver);
    }
}
