package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import io.huangsam.photohaul.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class MigratorFactory {
    public Migrator make(@NotNull MigratorMode mode, Settings settings, PhotoResolver resolver) throws IOException {
        return switch (mode) {
            case PATH -> pathInstance(settings, resolver);
            case DROPBOX -> dropboxInstance(settings, resolver);
            case GOOGLE_DRIVE -> googleDriveInstance(settings, resolver);
        };
    }

    @NotNull
    private PathMigrator pathInstance(@NotNull Settings settings, PhotoResolver resolver) {
        return new PathMigrator(settings.getTargetRootPath(), resolver);
    }

    @NotNull
    private DropboxMigrator dropboxInstance(@NotNull Settings settings, PhotoResolver resolver) {
        String target = settings.getValue("target.root");
        DbxRequestConfig config = DbxRequestConfig.newBuilder(settings.getValue("dbx.clientId")).build();
        DbxClientV2 client = new DbxClientV2(config, settings.getValue("dbx.accessToken"));
        return new DropboxMigrator(target, client, resolver);
    }

    @NotNull
    private GoogleDriveMigrator googleDriveInstance(@NotNull Settings settings, PhotoResolver resolver) throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(List.of(DriveScopes.DRIVE_FILE));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        String target = settings.getValue("target.root");
        Drive service = new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer).build();
        return new GoogleDriveMigrator(target, service, resolver);
    }
}
