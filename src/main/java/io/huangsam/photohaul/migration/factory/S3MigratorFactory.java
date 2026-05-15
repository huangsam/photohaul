package io.huangsam.photohaul.migration.factory;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.migration.Migrator;
import io.huangsam.photohaul.migration.S3Migrator;
import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jspecify.annotations.NonNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Factory for creating S3Migrator instances.
 */
public class S3MigratorFactory implements MigratorFactoryStrategy {
    @Override
    public @NonNull Migrator create(@NonNull Settings settings, @NonNull PhotoResolver resolver) {
        String accessKey = settings.getValue("s3.accessKey");
        String secretKey = settings.getValue("s3.secretKey");
        String region = settings.getValue("s3.region", "us-east-1");
        String bucket = settings.getValue("s3.bucket");
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        S3Client client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        return new S3Migrator(bucket, resolver, client, settings.isDryRun());
    }
}
