package io.huangsam.photohaul.deduplication;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Strategy interface for photo deduplication at different levels.
 */
interface DeduplicationStrategy {
    /**
     * Process a group of photos and add unique ones to the context.
     *
     * @param photos  the photos to process
     * @param context the deduplication context
     * @param next    the next strategy to call for potential duplicates
     */
    void process(@NotNull List<Photo> photos, @NotNull DeduplicationContext context, @NotNull DeduplicationStrategy next);
}
