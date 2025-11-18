package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class S3Migrator implements Migrator {
    private static final Logger LOG = getLogger(S3Migrator.class);

    private final String bucketName;
    private final PhotoResolver photoResolver;
    private final S3Client s3Client;

    private long successCount = 0L;
    private long failureCount = 0L;

    public S3Migrator(@NotNull String bucket, PhotoResolver resolver, S3Client client) {
        bucketName = bucket;
        photoResolver = resolver;
        s3Client = client;
    }

    @Override
    public void migratePhotos(@NotNull Collection<Photo> photos) {
        LOG.debug("Start S3 migration to bucket {}", bucketName);
        photos.forEach(photo -> {
            String key = getTargetKey(photo);
            LOG.trace("Upload {} to s3://{}/{}", photo.name(), bucketName, key);
            try {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.putObject(request, photo.path());
                successCount++;
            } catch (Exception e) {
                LOG.error("Cannot upload {}: {}", photo.name(), e.getMessage());
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

    @Override
    public void close() throws Exception {
        s3Client.close();
    }

    @NotNull
    private String getTargetKey(Photo photo) {
        try {
            return photoResolver.resolveString(photo) + "/" + photo.name();
        } catch (ResolutionException e) {
            return "Other/" + photo.name();
        }
    }
}
