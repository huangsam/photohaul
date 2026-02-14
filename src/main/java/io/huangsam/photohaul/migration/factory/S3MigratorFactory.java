package io.huangsam.photohaul.migration.factory;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.S3Migrator;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Factory for creating S3Migrator instances.
 */
class S3MigratorFactory implements MigratorFactoryStrategy {
    @Override
    public @NotNull Migrator create(@NotNull Settings settings, @NotNull PhotoResolver resolver) {
        String accessKey = settings.getValue("s3.accessKey");
        String secretKey = settings.getValue("s3.secretKey");
        String region = settings.getValue("s3.region", "us-east-1");
        String bucket = settings.getValue("s3.bucket");
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
        return new S3Migrator(bucket, resolver, s3Client);
    }
}
