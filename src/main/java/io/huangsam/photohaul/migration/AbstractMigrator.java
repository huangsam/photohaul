package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.jspecify.annotations.NonNull;

/**
 * Base class for {@link Migrator} implementations to reduce boilerplate.
 */
public abstract class AbstractMigrator implements Migrator {
    protected final PhotoResolver photoResolver;
    protected final boolean dryRun;
    protected long successCount = 0L;
    protected long failureCount = 0L;

    protected AbstractMigrator(PhotoResolver photoResolver, boolean dryRun) {
        this.photoResolver = photoResolver;
        this.dryRun = dryRun;
    }

    @Override
    public long getSuccessCount() {
        return successCount;
    }

    @Override
    public long getFailureCount() {
        return failureCount;
    }

    @Override
    public void close() throws Exception {
        // Default no-op for most migrators
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
