package io.huangsam.photohaul.migration;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class GoogleDriveMigrator implements Migrator {
    private static final Logger LOG = getLogger(GoogleDriveMigrator.class);

    private final String targetRoot;
    private final Drive driveService;
    private final PhotoResolver photoResolver;

    private long successCount = 0L;
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
                createDriveFolder(targetPath);
                createDrivePhoto(targetPath, photo);
                successCount++;
            } catch (IOException | NullPointerException e) {
                LOG.error("Cannot move {}: {}", photo.name(), e.getMessage());
                failureCount++;
            }
        });
    }

    @Override
    public long getSuccessCount() {
        return successCount;
    }

    @Override
    public long getFailureCount() {
        return failureCount;
    }

    private String getTargetPath(Photo photo) {
        try {
            StringBuilder result = new StringBuilder(targetRoot);
            for (String out : photoResolver.resolveList(photo)) {
                result.append("/").append(out);
            }
            return result.toString();
        } catch (NullPointerException e) {
            return targetRoot + "/Other";
        }
    }

    private void createDriveFolder(String targetPath) throws IOException {
        String query = '\'' + targetPath + '\'' + " in parents and mimeType='application/vnd.google-apps.folder'";
        FileList result = driveService.files().list().setQ(query).execute();
        if (result.getFiles().isEmpty()) {
            return;
        }

        File folderMetadata = new File();
        folderMetadata.setName(targetPath);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        File folderSuccess = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();

        LOG.trace("Folder created: {}", folderSuccess.getId());
    }

    private void createDrivePhoto(String targetPath, @NotNull Photo photo) throws IOException {
        File photoMetadata = new File();
        photoMetadata.setName(targetPath + "/" + photo.name());

        String contentType = Files.probeContentType(photo.path());
        java.io.File photoFile = new java.io.File(photo.path().toString());
        FileContent photoContent = new FileContent(contentType, photoFile);

        File photoSuccess = driveService.files().create(photoMetadata, photoContent)
                .setFields("id")
                .execute();

        LOG.trace("Photo created: {}", photoSuccess.getId());
    }
}
