package io.huangsam.photohaul.migration.delta;

import io.huangsam.photohaul.migration.state.FileState;
import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * A batch of photos that need migration along with their file states.
 */
record MigrationBatch(@NonNull List<Photo> photosToMigrate, @NonNull List<FileState> fileStates) {
}
