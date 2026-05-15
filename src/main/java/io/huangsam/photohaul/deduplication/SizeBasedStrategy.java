package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deduplication strategy based on file size.
 * Photos with different sizes cannot be duplicates.
 */
class SizeBasedStrategy implements DeduplicationStrategy {
    private static final Logger LOG = getLogger(SizeBasedStrategy.class);

    @Override
    public void process(@NonNull List<Photo> photos, @NonNull DeduplicationContext context, @NonNull DeduplicationStrategy next) {
        if (photos.size() == 1) {
            addUniquePhoto(photos.getFirst(), context);
        } else {
            next.process(photos, context, null); // Delegate to next level
        }
    }

    private void addUniquePhoto(@NonNull Photo photo, @NonNull DeduplicationContext context) {
        try {
            long size = Files.size(photo.path());
            String key = "size_" + size;
            context.addUnique(key, photo);
            LOG.trace("Added unique photo by size: {} (size: {})", photo.name(), size);
        } catch (IOException e) {
            context.addUnique(java.util.UUID.randomUUID().toString(), photo);
        }
    }
}
