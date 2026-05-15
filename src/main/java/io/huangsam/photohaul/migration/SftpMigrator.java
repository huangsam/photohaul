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

    private final @NonNull String host;
    private final int port;
    private final @NonNull String username;
    private final @NonNull String password;
    private final @NonNull String targetRoot;
    private final Supplier<SSHClient> sshClientSupplier;

    public SftpMigrator(@NonNull String host, int port, @NonNull String username, @NonNull String password,
                       @NonNull String target, PhotoResolver resolver) {
        this(host, port, username, password, target, resolver, SSHClient::new);
    }

    // For testing
    SftpMigrator(@NonNull String host, int port, @NonNull String username, @NonNull String password,
                @NonNull String target, PhotoResolver resolver, Supplier<SSHClient> sshClientSupplier) {
        super(resolver);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.targetRoot = target;
        this.sshClientSupplier = sshClientSupplier;
    }

    @Override
    public void migratePhotos(java.util.@NonNull Collection<Photo> photos) {
        LOG.debug("Start SFTP migration to {}@{}:{}", username, host, port);
        int processedCount = 0;
        try (SSHClient sshClient = sshClientSupplier.get()) {
            sshClient.loadKnownHosts();
            sshClient.connect(host, port);
            sshClient.authPassword(username, password);

            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                for (Photo photo : photos) {
                    String targetPath = getTargetPath(photo);
                    LOG.trace("Upload {} to {}", photo.name(), targetPath);
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
