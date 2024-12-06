package io.huangsam.photohaul.migration;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import io.huangsam.photohaul.Settings;

public class MigratorFactory {
    public PathMigrator pathInstance(Settings settings, PhotoResolver resolver) {
        return new PathMigrator(settings.getTargetRootPath(), resolver);
    }

    public DropboxMigrator dropboxInstance(Settings settings, PhotoResolver resolver) {
        String target = settings.getValue("target.root");
        DbxRequestConfig config = DbxRequestConfig.newBuilder(settings.getValue("dbx.clientId")).build();
        DbxClientV2 client = new DbxClientV2(config, settings.getValue("dbx.accessToken"));
        return new DropboxMigrator(target, client, resolver);
    }
}
