package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

public class SftpMigrator implements Migrator {
    private static final Logger LOG = getLogger(SftpMigrator.class);

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String targetRoot;
    private final PhotoResolver photoResolver;
    private final Supplier<SSHClient> sshClientSupplier;

    private long successCount = 0L;
    private long failureCount = 0L;

    public SftpMigrator(@NotNull String host, int port, @NotNull String username, @NotNull String password,
                       @NotNull String target, PhotoResolver resolver) {
        this(host, port, username, password, target, resolver, SSHClient::new);
    }

    // For testing
    SftpMigrator(@NotNull String host, int port, @NotNull String username, @NotNull String password,
                @NotNull String target, PhotoResolver resolver, Supplier<SSHClient> sshClientSupplier) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.targetRoot = target;
        this.photoResolver = resolver;
        this.sshClientSupplier = sshClientSupplier;
    }

    @Override
    public void migratePhotos(@NotNull java.util.Collection<Photo> photos) {
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

    @Override
    public long getSuccessCount() {
        return successCount;
    }

    @Override
    public long getFailureCount() {
        return failureCount;
    }

    @NotNull
    private String getTargetPath(Photo photo) {
        try {
            return targetRoot + "/" + photoResolver.resolveString(photo) + "/" + photo.name();
        } catch (ResolutionException e) {
            return targetRoot + "/Other/" + photo.name();
        }
    }
}
