package io.huangsam.photohaul.migration.factory;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.SftpMigrator;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating SftpMigrator instances.
 */
class SftpMigratorFactory implements MigratorFactoryStrategy {
    @Override
    public @NotNull Migrator create(@NotNull Settings settings, @NotNull PhotoResolver resolver) {
        String host = settings.getValue("sftp.host");
        int port = Integer.parseInt(settings.getValue("sftp.port", "22"));
        String username = settings.getValue("sftp.username");
        String password = settings.getValue("sftp.password");
        String target = settings.getValue("sftp.target");
        return new SftpMigrator(host, port, username, password, target, resolver);
    }
}
