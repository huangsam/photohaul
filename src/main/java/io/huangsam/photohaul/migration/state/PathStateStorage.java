package io.huangsam.photohaul.migration.state;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * StateFileStorage implementation for local filesystem.
 *
 * <p> Stores the state file at the specified root path on the local filesystem.
 */
public class PathStateStorage implements StateFileStorage {
    private final @NonNull Path rootPath;

    /**
     * Creates a PathStateStorage for the given root path.
     *
     * @param rootPath the root directory where state files will be stored
     */
    public PathStateStorage(@NotNull Path rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public @Nullable String readStateFile(@NonNull String fileName) throws IOException {
        Path statePath = rootPath.resolve(fileName);
        if (!Files.exists(statePath)) {
            return null;
        }
        return Files.readString(statePath, StandardCharsets.UTF_8);
    }

    @Override
    public void writeStateFile(@NonNull String fileName, @NonNull String content) throws IOException {
        Files.createDirectories(rootPath);
        Path statePath = rootPath.resolve(fileName);
        Files.writeString(statePath, content, StandardCharsets.UTF_8);
    }
}
