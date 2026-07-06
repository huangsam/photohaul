package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Base class for {@link Migrator} implementations to reduce boilerplate.
 */
public abstract class AbstractMigrator implements Migrator {
    private static final Logger LOG = getLogger(AbstractMigrator.class);

    protected final PhotoResolver photoResolver;
    protected final boolean dryRun;
    protected final int threadCount;
    protected final AtomicLong successCount = new AtomicLong(0L);
    protected final AtomicLong failureCount = new AtomicLong(0L);
    protected final Set<String> successfulPhotos = ConcurrentHashMap.newKeySet();

    protected AbstractMigrator(PhotoResolver photoResolver, boolean dryRun) {
        this(photoResolver, dryRun, 1);
    }

    protected AbstractMigrator(PhotoResolver photoResolver, boolean dryRun, int threadCount) {
        this.photoResolver = photoResolver;
        this.dryRun = dryRun;
        this.threadCount = threadCount;
    }

    @Override
    public long getSuccessCount() {
        return successCount.get();
    }

    @Override
    public long getFailureCount() {
        return failureCount.get();
    }

    /**
     * Get the set of successfully migrated photo paths.
     *
     * @return set of successful photo path strings
     */
    public @NonNull Set<String> getSuccessfulPhotos() {
        return successfulPhotos;
    }

    @Override
    public void close() throws Exception {
        // Default no-op for most migrators
    }

    /**
     * Helper to run photo migrations concurrently or sequentially depending on threadCount configuration.
     *
     * @param photos          the collection of photos to process
     * @param migrationAction the action to perform for each photo
     */
    protected void runMigration(@NonNull Collection<Photo> photos, @NonNull Consumer<Photo> migrationAction) {
        if (threadCount <= 1) {
            photos.forEach(migrationAction);
        } else {
            ForkJoinPool customThreadPool = new ForkJoinPool(threadCount);
            try {
                customThreadPool.submit(() -> photos.parallelStream().forEach(migrationAction)).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Parallel migration was interrupted", e);
            } catch (Exception e) {
                throw new RuntimeException("Parallel migration failed", e);
            } finally {
                customThreadPool.shutdown();
            }
        }
    }

    /**
     * Resolves the target directory/path for a photo using the configured resolver.
     *
     * @param photo the photo to resolve
     * @return the resolved path string, or "Other" if resolution fails
     */
    @NonNull
    protected String resolvePath(@NonNull Photo photo) {
        try {
            return photoResolver.resolveString(photo);
        } catch (ResolutionException e) {
            return "Other";
        }
    }
}

