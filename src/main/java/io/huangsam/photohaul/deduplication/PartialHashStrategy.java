package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deduplication strategy based on partial SHA-256 hash (first 1KB).
 */
class PartialHashStrategy implements DeduplicationStrategy {
    private static final Logger LOG = getLogger(PartialHashStrategy.class);

    @Override
    public void process(@NonNull List<Photo> photos, @NonNull DeduplicationContext context, @NonNull DeduplicationStrategy next) {
        Map<String, List<Photo>> photosByPartialHash = groupByPartialHash(photos);

        for (List<Photo> group : photosByPartialHash.values()) {
            processPartialHashGroup(group, context, next);
        }
    }

    private Map<String, List<Photo>> groupByPartialHash(@NonNull List<Photo> photos) {
        return photos.stream()
            .collect(Collectors.groupingBy(this::safeCalculatePartialHash,
                         LinkedHashMap::new, Collectors.toList()));
    }

    private void processPartialHashGroup(@NonNull List<Photo> group, @NonNull DeduplicationContext context, @NonNull DeduplicationStrategy next) {
        if (group.size() == 1) {
            addUniquePhoto(group.getFirst(), context);
        } else {
            next.process(group, context, null); // Call next level
        }
    }

    private void addUniquePhoto(@NonNull Photo photo, @NonNull DeduplicationContext context) {
        try {
            String hash = calculatePartialHash(photo);
            context.addUnique(hash, photo);
            LOG.trace("Added unique photo by partial hash: {}", photo.name());
        } catch (IOException e) {
            context.addUnique(java.util.UUID.randomUUID().toString(), photo);
        }
    }

    private @NonNull String safeCalculatePartialHash(@NonNull Photo photo) {
        try {
            return calculatePartialHash(photo);
        } catch (IOException e) {
            return "error_" + java.util.UUID.randomUUID();
        }
    }

    private @NonNull String calculatePartialHash(@NonNull Photo photo) throws IOException {
        return HashUtils.calculateHash(photo.path(), 1024);
    }
}

