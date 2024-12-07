package io.huangsam.photohaul.migration;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class GoogleDriveMigrator implements Migrator {
    private static final Logger LOG = getLogger(GoogleDriveMigrator.class);
    private static final String MIME_FOLDER = "application/vnd.google-apps.folder";

    private final String targetRoot;
    private final Drive driveService;
    private final PhotoResolver photoResolver;

    private long createdCount = 0L;
    private long existedCount = 0L;
    private long failureCount = 0L;

    public GoogleDriveMigrator(String target, Drive service, PhotoResolver resolver) {
        targetRoot = target;
        driveService = service;
        photoResolver = resolver;
    }

    @Override
    public void migratePhotos(@NotNull Collection<Photo> photos) {
        LOG.debug("Start Drive migration to {}", targetRoot);
        photos.forEach(photo -> {
            String targetPath = getTargetPath(photo);
            LOG.trace("Move {} to {}", photo.name(), targetPath);
            try {
                String folderId = createDriveFolder(targetRoot, targetPath);
                createDrivePhoto(folderId, photo);
            } catch (IOException | NullPointerException e) {
                LOG.error("Cannot move {}: {}", photo.name(), e.getMessage());
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

    private String getTargetPath(Photo photo) {
        try {
            return String.join("/", photoResolver.resolveList(photo));
        } catch (NullPointerException e) {
            return "Other";
        }
    }

    private String createDriveFolder(String targetRoot, String targetPath) throws IOException {
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

        File photoMetadata = new File();
        photoMetadata.setName(photo.name());
        photoMetadata.setParents(List.of(folderId));

        String contentType = Files.probeContentType(photo.path());
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
        String query = String.format("'%s' in parents and name = '%s'", folderId, fileName);
        FileList result = driveService.files().list().setQ(query).execute();
        List<File> fileList = result.getFiles();
        if (fileList.isEmpty()) {
            return null;
        }
        return fileList.get(0).getId();
    }
}
