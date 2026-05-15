package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

public class SftpMigrator extends AbstractMigrator {
    private static final Logger LOG = getLogger(SftpMigrator.class);

    private final @NonNull Config config;
    private final @NonNull String targetRoot;
    private final Supplier<SSHClient> sshClientSupplier;

    public record Config(@NonNull String host, int port, @NonNull String username, @NonNull String password) { }

    public SftpMigrator(@NonNull Config config,
                       @NonNull String target, PhotoResolver resolver, boolean dryRun) {
        this(config, target, resolver, SSHClient::new, dryRun);
    }

    // For testing
    SftpMigrator(@NonNull Config config,
                @NonNull String target, PhotoResolver resolver, Supplier<SSHClient> sshClientSupplier, boolean dryRun) {
        super(resolver, dryRun);
        this.config = config;
        this.targetRoot = target;
        this.sshClientSupplier = sshClientSupplier;
    }

    @Override
    public void migratePhotos(java.util.@NonNull Collection<Photo> photos) {
        LOG.debug("Start SFTP migration to {}@{}:{}", config.username(), config.host(), config.port());
        int processedCount = 0;
        try (SSHClient sshClient = sshClientSupplier.get()) {
            sshClient.loadKnownHosts();
            sshClient.connect(config.host(), config.port());
            sshClient.authPassword(config.username(), config.password());

            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                for (Photo photo : photos) {
                    String targetPath = getTargetPath(photo);
                    LOG.trace("Upload {} to {}", photo.name(), targetPath);
                    if (dryRun) {
                        LOG.info("Dry-run {} to sftp://{}@{}:{}/{}", photo.path(), config.username(), config.host(), config.port(), targetPath);
                        successCount++;
                        processedCount++;
                        continue;
                    }
                    try {
                        // Ensure target directory exists
                        Path targetPathObj = Paths.get(targetPath);
                        Path targetDir = targetPathObj.getParent();
                        if (targetDir != null) {
                            sftpClient.mkdirs(targetDir.toString());
                        }
                        sftpClient.put(photo.path().toString(), targetPath);
                        successCount++;
                    } catch (IOException e) {
                        LOG.error("Cannot upload {}: {}", photo.name(), e.getMessage());
                        failureCount++;
                    }
                    processedCount++;
                }
            }
        } catch (IOException e) {
            LOG.error("SFTP connection error: {}", e.getMessage());
            failureCount += (photos.size() - processedCount);
        }
    }

    @NonNull
    private String getTargetPath(@NonNull Photo photo) {
        return targetRoot + "/" + resolvePath(photo) + "/" + photo.name();
    }
}
