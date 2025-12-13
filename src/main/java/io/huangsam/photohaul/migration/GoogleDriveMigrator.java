package io.huangsam.photohaul.migration;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class GoogleDriveMigrator implements Migrator {
    private static final Logger LOG = getLogger(GoogleDriveMigrator.class);
    private static final String MIME_FOLDER = "application/vnd.google-apps.folder";

    private final String targetRoot;
    private final PhotoResolver photoResolver;
    private final Drive driveService;
    private final HttpTransport httpTransport;

    private long createdCount = 0L;
    private long existedCount = 0L;
    private long failureCount = 0L;

    public GoogleDriveMigrator(String target, PhotoResolver resolver, Drive service, HttpTransport transport) {
        targetRoot = target;
        photoResolver = resolver;
        driveService = service;
        httpTransport = transport;
    }

    @Override
    public void migratePhotos(@NotNull Collection<Photo> photos) {
        LOG.debug("Start Drive migration to {}", targetRoot);
        photos.forEach(photo -> {
            String targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            try {
                String folderId = createDriveFolder(targetPath);
                createDrivePhoto(folderId, photo);
            } catch (IOException e) {
                LOG.error("Cannot move {}: {}", photo.name(), e.getMessage());
                failureCount++;
            } catch (NullPointerException e) {
                LOG.error("Unexpected null for {}", photo.name());
                failureCount++;
            }
        });
    }

    @Override
    public long getSuccessCount() {
        return createdCount + existedCount;
    }

    @Override
    public long getFailureCount() {
        return failureCount;
    }

    @Override
    public void close() throws Exception {
        httpTransport.shutdown();
    }

    private String getTargetPath(Photo photo) {
        try {
            return photoResolver.resolveString(photo);
        } catch (ResolutionException e) {
            return "Other";
        }
    }

    private String createDriveFolder(String targetPath) throws IOException {
        String existingId = getExistingId(targetRoot, targetPath);
        if (existingId != null) {
            return existingId;
        }
        if (targetPath.isEmpty()) {
            return targetRoot;
        }

        File folderMetadata = new File();
        folderMetadata.setName(targetPath);
        folderMetadata.setMimeType(MIME_FOLDER);
        folderMetadata.setParents(List.of(targetRoot));

        File folderSuccess = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();

        String folderId = folderSuccess.getId();

        LOG.trace("Folder created: {}", folderId);

        return folderId;
    }

    private void createDrivePhoto(String folderId, @NotNull Photo photo) throws IOException {
        String existingId = getExistingId(folderId, photo.name());
        if (existingId != null) {
            existedCount++;
            return;
        }

        String contentType = Files.probeContentType(photo.path());
        if (contentType == null) {
            throw new IOException("Missing MIME type: " + photo.path());
        }

        File photoMetadata = new File();
        photoMetadata.setName(photo.name());
        photoMetadata.setParents(List.of(folderId));

        java.io.File photoFile = new java.io.File(photo.path().toString());
        FileContent photoContent = new FileContent(contentType, photoFile);

        File photoSuccess = driveService.files().create(photoMetadata, photoContent)
                .setFields("id")
                .execute();

        LOG.trace("Photo created: {}", photoSuccess.getId());

        createdCount++;
    }

    @Nullable
    private String getExistingId(String folderId, String fileName) throws IOException {
        Objects.requireNonNull(folderId);
        String query = String.format("'%s' in parents and name = '%s'", folderId, fileName);
        FileList result = driveService.files().list().setQ(query).execute();
        List<File> fileList = result.getFiles();
        if (fileList.isEmpty()) {
            return null;
        }
        return fileList.getFirst().getId();
    }
}
