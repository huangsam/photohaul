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
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

public class MigratorFactory {
    /**
     * Creates instance for migrating photos.
     *
     * @param mode Migrator mode
     * @param settings Application settings
     * @param resolver Photo resolver
     * @return {@code Migrator} instance
     * @throws MigratorException due to bad setup
     */
    public Migrator make(@NotNull MigratorMode mode, Settings settings, PhotoResolver resolver) throws MigratorException {
        return switch (mode) {
            case PATH -> makePath(settings, resolver);
            case DROPBOX -> makeDropbox(settings, resolver);
            case GOOGLE_DRIVE -> makeGoogleDrive(settings, resolver);
        };
    }

    @NotNull
    private PathMigrator makePath(@NotNull Settings settings, PhotoResolver resolver) {
        Path target = Paths.get(System.getProperty("user.home"));
        target = target.resolve(settings.getValue("target.root"));
        String optionValue = settings.getValue("path.option", "MOVE").toUpperCase();
        return new PathMigrator(target, PathMigrator.Option.valueOf(optionValue), resolver);
    }

    @NotNull
    private DropboxMigrator makeDropbox(@NotNull Settings settings, PhotoResolver resolver) {
        String target = settings.getValue("target.root");
        DbxRequestConfig config = DbxRequestConfig.newBuilder(settings.getValue("dbx.clientId")).build();
        DbxClientV2 client = new DbxClientV2(config, settings.getValue("dbx.accessToken"));
        return new DropboxMigrator(target, client, resolver);
    }

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(DriveScopes.DRIVE);

    @NotNull
    private GoogleDriveMigrator makeGoogleDrive(@NotNull Settings settings, PhotoResolver resolver) throws MigratorException {
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

            return new GoogleDriveMigrator(settings.getValue("target.root"), service, resolver);
        } catch (GeneralSecurityException | IOException e) {
            throw new MigratorException(e.getMessage(), MigratorMode.GOOGLE_DRIVE);
        }
    }
}
