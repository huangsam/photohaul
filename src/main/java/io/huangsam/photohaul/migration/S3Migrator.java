package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class S3Migrator extends AbstractMigrator {
    private static final Logger LOG = getLogger(S3Migrator.class);

    private final @NonNull String bucketName;
    private final S3Client s3Client;

    public S3Migrator(@NonNull String bucket, PhotoResolver resolver, S3Client client, boolean dryRun) {
        super(resolver, dryRun);
        bucketName = bucket;
        s3Client = client;
    }

    @Override
    public void migratePhotos(@NonNull Collection<Photo> photos) {
        LOG.debug("Start S3 migration to bucket {}", bucketName);
        photos.forEach(photo -> {
            String key = getTargetKey(photo);
            LOG.trace("Upload {} to s3://{}/{}", photo.name(), bucketName, key);
            if (dryRun) {
                LOG.info("Dry-run {} to s3://{}/{}", photo.path(), bucketName, key);
                successCount++;
                return;
            }
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
    public void close() throws Exception {
        s3Client.close();
    }

    @NonNull
    private String getTargetKey(@NonNull Photo photo) {
        return resolvePath(photo) + "/" + photo.name();
    }
}
