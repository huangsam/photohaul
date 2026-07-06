package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
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
        this(config, target, resolver, SSHClient::new, dryRun, 1);
    }

    public SftpMigrator(@NonNull Config config,
                       @NonNull String target, PhotoResolver resolver, boolean dryRun, int threadCount) {
        this(config, target, resolver, SSHClient::new, dryRun, threadCount);
    }

    // For testing
    SftpMigrator(@NonNull Config config,
                @NonNull String target, PhotoResolver resolver, Supplier<SSHClient> sshClientSupplier, boolean dryRun) {
        this(config, target, resolver, sshClientSupplier, dryRun, 1);
    }

    // For testing with threadCount
    SftpMigrator(@NonNull Config config,
                @NonNull String target, PhotoResolver resolver, Supplier<SSHClient> sshClientSupplier, boolean dryRun, int threadCount) {
        super(resolver, dryRun, threadCount);
        this.config = config;
        this.targetRoot = target;
        this.sshClientSupplier = sshClientSupplier;
    }

    @Override
    public void migratePhotos(java.util.@NonNull Collection<Photo> photos) {
        LOG.debug("Start SFTP migration to {}@{}:{}", config.username(), config.host(), config.port());
        java.util.concurrent.atomic.AtomicInteger processedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        try (SSHClient sshClient = sshClientSupplier.get()) {
            sshClient.loadKnownHosts();
            sshClient.connect(config.host(), config.port());
            sshClient.authPassword(config.username(), config.password());

            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                runMigration(photos, photo -> {
                    String targetPath = getTargetPath(photo);
                    LOG.trace("Upload {} to {}", photo.name(), targetPath);
                    if (dryRun) {
                        LOG.info("Dry-run {} to sftp://{}@{}:{}/{}", photo.path(), config.username(), config.host(), config.port(), targetPath);
                        successfulPhotos.add(photo.path().toString());
                        successCount.incrementAndGet();
                        processedCount.incrementAndGet();
                        return;
                    }
                    try {
                        synchronized (sftpClient) {
                            // Ensure target directory exists
                            int lastSlash = targetPath.lastIndexOf('/');
                            if (lastSlash > 0) {
                                String targetDir = targetPath.substring(0, lastSlash);
                                sftpClient.mkdirs(targetDir);
                            }
                            sftpClient.put(photo.path().toString(), targetPath);
                        }
                        successfulPhotos.add(photo.path().toString());
                        successCount.incrementAndGet();
                    } catch (IOException e) {
                        LOG.error("Cannot upload {}: {}", photo.name(), e.getMessage());
                        failureCount.incrementAndGet();
                    }
                    processedCount.incrementAndGet();
                });
            }
        } catch (IOException e) {
            LOG.error("SFTP connection error: {}", e.getMessage());
            failureCount.addAndGet(photos.size() - processedCount.get());
        }
    }

    @NonNull
    private String getTargetPath(@NonNull Photo photo) {
        return targetRoot + "/" + resolvePath(photo) + "/" + photo.name();
    }
}
