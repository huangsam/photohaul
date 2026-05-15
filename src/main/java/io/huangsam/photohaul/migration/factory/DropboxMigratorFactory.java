package io.huangsam.photohaul.migration.factory;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.DropboxMigrator;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jspecify.annotations.NonNull;

/**
 * Factory for creating DropboxMigrator instances.
 */
public class DropboxMigratorFactory implements MigratorFactoryStrategy {
    @Override
    public @NonNull Migrator create(@NonNull Settings settings, @NonNull PhotoResolver resolver) {
        String target = settings.getValue("dbx.target");
        DbxRequestConfig config = DbxRequestConfig.newBuilder(settings.getValue("dbx.clientId")).build();
        DbxClientV2 client = new DbxClientV2(config, settings.getValue("dbx.accessToken"));
        return new DropboxMigrator(target, resolver, client);
    }
}
