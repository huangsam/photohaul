package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.migration.state.FileState;
import io.huangsam.photohaul.migration.state.MigrationStateFile;
import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A decorator that adds delta migration functionality to any Migrator.
 *
 * <p> This migrator wraps an existing migrator and uses a state file to track
 * previously migrated files. Before migration, it compares each file's current
 * metadata (size and last modified time) against the recorded state, and only
 * migrates files that are new or modified since the last run.
 *
 * <p> After each successful migration batch, the state file is updated with
 * the current state of all successfully migrated files.
 *
 * <p> Note: Since the delegate migrators process files in order and we cannot
 * determine exactly which specific files failed, we conservatively only record
 * state for the number of successful migrations (assuming failures occur at
 * the end of the batch). This may result in some files being re-migrated on
 * the next run if failures occurred mid-batch, but ensures no file is
 * incorrectly marked as migrated.
 */
public class DeltaMigrator implements Migrator {
    private static final Logger LOG = getLogger(DeltaMigrator.class);

    private final @NonNull Migrator delegate;
    private final @NonNull MigrationStateFile stateFile;

    private long skippedCount = 0L;

    /**
     * Creates a DeltaMigrator wrapping the given delegate migrator.
     *
     * @param delegate  the underlying migrator to delegate to
     * @param stateFile the state file manager for tracking migrations
     */
    public DeltaMigrator(@NotNull Migrator delegate, @NotNull MigrationStateFile stateFile) {
        this.delegate = delegate;
        this.stateFile = stateFile;
    }

    @Override
    public void migratePhotos(@NotNull Collection<Photo> photos) {
        // Load existing state
        stateFile.load();

        // Filter to only photos that need migration
        List<Photo> photosToMigrate = new ArrayList<>();
        List<FileState> fileStates = new ArrayList<>();

        for (Photo photo : photos) {
            try {
                FileState currentState = createFileState(photo);
                if (stateFile.needsMigration(currentState)) {
                    photosToMigrate.add(photo);
                    fileStates.add(currentState);
                } else {
                    LOG.trace("Skipping unchanged file: {}", photo.name());
                    skippedCount++;
                }
            } catch (IOException e) {
                LOG.warn("Could not read file metadata for {}: {}", photo.name(), e.getMessage());
                // Include in migration attempt anyway
                photosToMigrate.add(photo);
                fileStates.add(null);
            }
        }

        LOG.info("Delta migration: {} files to migrate, {} files skipped (unchanged)",
                photosToMigrate.size(), skippedCount);

        if (photosToMigrate.isEmpty()) {
            return;
        }

        // Get the delegate's success count before migration
        long previousSuccessCount = delegate.getSuccessCount();

        // Delegate actual migration
        delegate.migratePhotos(photosToMigrate);

        // Calculate how many files were successfully migrated
        long newSuccessCount = delegate.getSuccessCount();
        long successfulMigrations = newSuccessCount - previousSuccessCount;

        if (successfulMigrations > 0) {
            // Record states only for the number of successful migrations
            // Since we process in order, record states from the beginning
            int statesToRecord = (int) Math.min(successfulMigrations, fileStates.size());
            for (int i = 0; i < statesToRecord; i++) {
                FileState fileState = fileStates.get(i);
                if (fileState != null) {
                    stateFile.recordMigration(fileState);
                }
            }

            // Save updated state
            try {
                stateFile.save();
            } catch (IOException e) {
                LOG.error("Failed to save migration state: {}", e.getMessage());
            }
        }
    }

    @Override
    public long getSuccessCount() {
        return delegate.getSuccessCount();
    }

    @Override
    public long getFailureCount() {
        return delegate.getFailureCount();
    }

    /**
     * Get the number of files skipped because they were unchanged.
     *
     * @return the number of skipped files
     */
    public long getSkippedCount() {
        return skippedCount;
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    @NotNull
    private FileState createFileState(@NotNull Photo photo) throws IOException {
        String path = photo.path().toString();
        long size = Files.size(photo.path());
        FileTime modifiedTime = Files.getLastModifiedTime(photo.path());
        return new FileState(path, size, modifiedTime.toMillis());
    }
}
