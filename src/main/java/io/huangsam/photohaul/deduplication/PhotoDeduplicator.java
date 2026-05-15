package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles deduplication of photos using SHA-256 hashing.
 *
 * <p>This class identifies duplicate photos by computing their SHA-256 hash
 * and keeps only the first occurrence of each unique file.
 */
public class PhotoDeduplicator {
    private static final Logger LOG = getLogger(PhotoDeduplicator.class);

    /**
     * Deduplicate a collection of photos based on their SHA-256 hash.
     *
     * <p>For each photo, calculate its SHA-256 hash. If multiple photos have
     * the same hash, only the first occurrence is kept. The order of photos
     * in the input collection determines which photo is kept.
     *
     * <p><b>Architecture:</b>
     * This uses a nested Chain of Responsibility pattern. Photos are initially grouped by size.
     * Each size group is passed through a chain of strategies: Size -> Partial Hash -> Full Hash.
     * The nested lambdas act as callbacks, allowing a strategy to cleanly delegate sub-groups
     * of potential duplicates to the next, more computationally expensive tier.
     *
     * @param photos the photos to deduplicate
     * @return a collection of unique photos
     */
    @NonNull
    public Collection<Photo> deduplicate(@NonNull Collection<Photo> photos) {
        Map<Long, List<Photo>> photosBySize = groupBySize(photos);
        DeduplicationContext context = new DeduplicationContext();

        DeduplicationStrategy fullHash = new FullHashStrategy();
        DeduplicationStrategy partialHash = new PartialHashStrategy();
        DeduplicationStrategy sizeBased = new SizeBasedStrategy();

        for (List<Photo> sizeGroup : photosBySize.values()) {
            sizeBased.process(sizeGroup, context, (group, ctx, next) ->
                partialHash.process(group, ctx, (g, c, n) ->
                    fullHash.process(g, c, null)
                )
            );
        }

        LOG.info("Deduplication complete: {} unique photos, {} duplicates removed",
                context.getUniquePhotos().size(), context.getDuplicateCount());
        return context.getUniquePhotos();
    }

    private @NonNull Map<Long, List<Photo>> groupBySize(@NonNull Collection<Photo> photos) {
        return photos.stream()
            .collect(Collectors.groupingBy(this::safeGetFileSize, LinkedHashMap::new, Collectors.toList()));
    }

    private @NonNull Long safeGetFileSize(@NonNull Photo photo) {
        try {
            return getFileSize(photo);
        } catch (IOException e) {
            return -1L;
        }
    }

    private long getFileSize(@NonNull Photo photo) throws IOException {
        return Files.size(photo.path());
    }
}
