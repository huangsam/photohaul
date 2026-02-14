package io.huangsam.photohaul.migration.delta;

import io.huangsam.photohaul.migration.state.FileState;
import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A batch of photos that need migration along with their file states.
 */
record MigrationBatch(@NotNull List<Photo> photosToMigrate, @NotNull List<FileState> fileStates) {
}
