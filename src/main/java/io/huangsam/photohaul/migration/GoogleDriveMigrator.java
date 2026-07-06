package io.huangsam.photohaul.migration;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

public class GoogleDriveMigrator extends AbstractMigrator {
    private static final Logger LOG = getLogger(GoogleDriveMigrator.class);
    private static final String MIME_FOLDER = "application/vnd.google-apps.folder";

    private final String targetRoot;
    private final Drive driveService;
    private final HttpTransport httpTransport;
    private final Map<String, String> folderCache = new ConcurrentHashMap<>();

    public GoogleDriveMigrator(String target, PhotoResolver resolver, Drive service, HttpTransport transport, boolean dryRun) {
        this(target, resolver, service, transport, dryRun, 1);
    }

    public GoogleDriveMigrator(String target, PhotoResolver resolver, Drive service, HttpTransport transport, boolean dryRun, int threadCount) {
        super(resolver, dryRun, threadCount);
        targetRoot = target;
        driveService = service;
        httpTransport = transport;
    }

    @Override
    public void migratePhotos(@NonNull Collection<Photo> photos) {
        LOG.debug("Start Drive migration to {}", targetRoot);
        runMigration(photos, photo -> {
            String targetPath = getTargetPath(photo);
            Path sidecarLocal = photo.getSidecarPath();
            String sidecarName = (sidecarLocal != null) ? sidecarLocal.getFileName().toString() : null;

            LOG.trace("Move {} to {}", photo.name(), targetPath);
            if (dryRun) {
                LOG.info("Dry-run {} to Google Drive path: {}/{}", photo.path(), targetPath, photo.name());
                if (sidecarLocal != null) {
                    LOG.info("Dry-run sidecar {} to Google Drive path: {}/{}", sidecarLocal, targetPath, sidecarName);
                }
                successfulPhotos.add(photo.path().toString());
                successCount.incrementAndGet();
                return;
            }
            try {
                String folderId = createDriveFolder(targetPath);
                createDrivePhoto(folderId, photo);
                if (sidecarLocal != null) {
                    createDriveSidecar(folderId, sidecarLocal);
                }
                successfulPhotos.add(photo.path().toString());
                successCount.incrementAndGet();
            } catch (IOException | NullPointerException e) {
                LOG.error("Cannot move {}: {}", photo.name(), e.getMessage());
                failureCount.incrementAndGet();
            }
        });
    }

    @Override
    public void close() throws Exception {
        httpTransport.shutdown();
    }

    private @NonNull String getTargetPath(Photo photo) {
        return resolvePath(photo);
    }

    private synchronized String createDriveFolder(@NonNull String targetPath) throws IOException {
        if (targetPath.isEmpty()) {
            return targetRoot;
        }

        String[] parts = targetPath.split("/");
        String currentParentId = targetRoot;
        StringBuilder pathBuilder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (pathBuilder.length() > 0) {
                pathBuilder.append("/");
            }
            pathBuilder.append(part);
            String pathKey = pathBuilder.toString();

            String cachedId = folderCache.get(pathKey);
            if (cachedId != null) {
                currentParentId = cachedId;
                continue;
            }

            String existingId = getExistingId(currentParentId, part);
            if (existingId != null) {
                currentParentId = existingId;
            } else {
                File folderMetadata = new File();
                folderMetadata.setName(part);
                folderMetadata.setMimeType(MIME_FOLDER);
                folderMetadata.setParents(List.of(currentParentId));

                File folderSuccess = driveService.files().create(folderMetadata)
                        .setFields("id")
                        .execute();
                String newFolderId = folderSuccess.getId();
                if (newFolderId == null) {
                    throw new IOException("Failed to create Google Drive folder: " + part);
                }
                currentParentId = newFolderId;
                LOG.trace("Folder created: {} with ID: {}", part, currentParentId);
            }
            folderCache.put(pathKey, currentParentId);
        }

        return currentParentId;
    }

    private void createDrivePhoto(@NonNull String folderId, @NonNull Photo photo) throws IOException {
        String existingId = getExistingId(folderId, photo.name());
        if (existingId != null) {
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
    }

    private void createDriveSidecar(@NonNull String folderId, @NonNull Path sidecarPath) throws IOException {
        String fileName = sidecarPath.getFileName().toString();
        String existingId = getExistingId(folderId, fileName);
        if (existingId != null) {
            return;
        }

        String contentType = "application/x-xmp";

        File sidecarMetadata = new File();
        sidecarMetadata.setName(fileName);
        sidecarMetadata.setParents(List.of(folderId));

        java.io.File sidecarFile = new java.io.File(sidecarPath.toString());
        FileContent sidecarContent = new FileContent(contentType, sidecarFile);

        File sidecarSuccess = driveService.files().create(sidecarMetadata, sidecarContent)
                .setFields("id")
                .execute();

        LOG.trace("Sidecar created: {}", sidecarSuccess.getId());
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
