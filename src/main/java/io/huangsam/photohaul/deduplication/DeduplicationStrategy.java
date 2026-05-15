package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Strategy interface for photo deduplication at different levels.
 */
interface DeduplicationStrategy {
    /**
     * Process a group of photos and add unique ones to the context.
     *
     * <p>This method implements a callback-based strategy chain (Chain of Responsibility).
     * If a strategy identifies a photo as unique, it adds it to the {@code context} directly.
     * If it cannot uniquely identify photos (i.e. they are potential duplicates at this level),
     * it delegates the group to the {@code next} strategy in the chain for further inspection.
     *
     * @param photos  the photos to process
     * @param context the deduplication context to store unique photos and duplicates
     * @param next    the next strategy to call for potential duplicates
     */
    void process(@NonNull List<Photo> photos, @NonNull DeduplicationContext context, @NonNull DeduplicationStrategy next);
}
