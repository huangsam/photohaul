package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import io.huangsam.photohaul.Settings;
import org.jetbrains.annotations.NotNull;

public class MigratorFactory {
    public Migrator make(@NotNull MigratorMode mode, Settings settings, PhotoResolver resolver) {
        return switch (mode) {
            case PATH -> pathInstance(settings, resolver);
            case DROPBOX -> dropboxInstance(settings, resolver);
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
}
