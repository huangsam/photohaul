package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

public class FtpMigrator implements Migrator {
    private static final Logger LOG = getLogger(FtpMigrator.class);

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String targetRoot;
    private final PhotoResolver photoResolver;
    private final Supplier<FTPClient> ftpClientSupplier;

    private long successCount = 0L;
    private long failureCount = 0L;

    public FtpMigrator(@NotNull String host, int port, @NotNull String username, @NotNull String password,
                       @NotNull String target, PhotoResolver resolver) {
        this(host, port, username, password, target, resolver, FTPClient::new);
    }

    // For testing
    FtpMigrator(@NotNull String host, int port, @NotNull String username, @NotNull String password,
                @NotNull String target, PhotoResolver resolver, Supplier<FTPClient> ftpClientSupplier) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.targetRoot = target;
        this.photoResolver = resolver;
        this.ftpClientSupplier = ftpClientSupplier;
    }

    @Override
    public void migratePhotos(@NotNull java.util.Collection<Photo> photos) {
        LOG.debug("Start FTP migration to {}@{}:{}", username, host, port);
        FTPClient ftpClient = ftpClientSupplier.get();
        try {
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            for (Photo photo : photos) {
                String targetPath = getTargetPath(photo);
                LOG.trace("Upload {} to {}", photo.name(), targetPath);
                try (InputStream in = Files.newInputStream(photo.path())) {
                    if (ftpClient.storeFile(targetPath, in)) {
                        successCount++;
                    } else {
                        LOG.error("Failed to upload {}", photo.name());
                        failureCount++;
                    }
                } catch (IOException e) {
                    LOG.error("Cannot upload {}: {}", photo.name(), e.getMessage());
                    failureCount++;
                }
            }
        } catch (IOException e) {
            LOG.error("FTP connection error: {}", e.getMessage());
            failureCount += photos.size();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                LOG.warn("Error disconnecting FTP: {}", e.getMessage());
            }
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
