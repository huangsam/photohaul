package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deduplication strategy based on full SHA-256 hash.
 */
class FullHashStrategy implements DeduplicationStrategy {
    private static final Logger LOG = getLogger(FullHashStrategy.class);

    @Override
    public void process(@NonNull List<Photo> photos, @NonNull DeduplicationContext context, @NonNull DeduplicationStrategy next) {
        for (Photo photo : photos) {
            processPhoto(photo, context);
        }
    }

    private void processPhoto(@NonNull Photo photo, @NonNull DeduplicationContext context) {
        try {
            String hash = calculateHash(photo);
            if (!context.contains(hash)) {
                context.addUnique(hash, photo);
                LOG.trace("Added unique photo: {} (hash: {})", photo.name(), hash);
            } else {
                context.markDuplicate();
                LOG.debug("Skipping duplicate: {} (hash: {})", photo.name(), hash);
            }
        } catch (IOException e) {
            LOG.warn("Cannot calculate hash for {}: {}, including as unique",
                    photo.name(), e.getMessage());
            context.addUnique(java.util.UUID.randomUUID().toString(), photo);
        }
    }

    private @NonNull String calculateHash(@NonNull Photo photo) throws IOException {
        return HashUtils.calculateHash(photo.path());
    }
}

