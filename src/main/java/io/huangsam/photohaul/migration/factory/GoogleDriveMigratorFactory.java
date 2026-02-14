package io.huangsam.photohaul.migration.factory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.GoogleDriveMigrator;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.MigrationException;
import io.huangsam.photohaul.migration.MigratorMode;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Factory for creating GoogleDriveMigrator instances.
 */
class GoogleDriveMigratorFactory implements MigratorFactoryStrategy {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(DriveScopes.DRIVE);

    @Override
    public @NotNull Migrator create(@NotNull Settings settings, @NotNull PhotoResolver resolver) {
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
}
