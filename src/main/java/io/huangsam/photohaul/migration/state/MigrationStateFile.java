package io.huangsam.photohaul.migration.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Manages the migration state file for delta migration.
 *
 * <p> The state file is a JSON file (e.g., .photohaul_state.json) stored at the
 * migration destination. It records the path, size, and last modified timestamp
 * of every successfully processed file, enabling efficient delta migrations by
 * skipping unchanged files.
 *
 * <p> This class provides an abstraction over the state file operations using
 * the {@link StateFileStorage} interface, allowing different storage backends
 * (local filesystem, S3, Dropbox, etc.) to be used.
 */
public class MigrationStateFile {
    private static final Logger LOG = getLogger(MigrationStateFile.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STATE_MAP_TYPE = new TypeToken<Map<String, FileState>>() {}.getType();

    public static final String DEFAULT_STATE_FILE_NAME = ".photohaul_state.json";

    private final StateFileStorage storage;
    private final String stateFileName;
    private final Map<String, FileState> state;

    /**
     * Creates a MigrationStateFile with the default state file name.
     *
     * @param storage the storage backend for reading/writing state
     */
    public MigrationStateFile(@NotNull StateFileStorage storage) {
        this(storage, DEFAULT_STATE_FILE_NAME);
    }

    /**
     * Creates a MigrationStateFile with a custom state file name.
     *
     * @param storage      the storage backend for reading/writing state
     * @param stateFileName the name of the state file
     */
    public MigrationStateFile(@NotNull StateFileStorage storage, @NotNull String stateFileName) {
        this.storage = storage;
        this.stateFileName = stateFileName;
        this.state = new HashMap<>();
    }

    /**
     * Load state from the storage backend.
     *
     * <p> If the state file doesn't exist, cannot be read, or contains malformed JSON,
     * the state will be empty and migration will proceed with all files.
     */
    public void load() {
        try {
            String content = storage.readStateFile(stateFileName);
            if (content != null && !content.isBlank()) {
                Map<String, FileState> loaded = GSON.fromJson(content, STATE_MAP_TYPE);
                if (loaded != null) {
                    state.clear();
                    state.putAll(loaded);
                    LOG.debug("Loaded {} file states from {}", state.size(), stateFileName);
                }
            }
        } catch (IOException e) {
            LOG.warn("Could not load state file {}: {}", stateFileName, e.getMessage());
        } catch (JsonSyntaxException e) {
            LOG.warn("State file {} contains malformed JSON, proceeding with empty state: {}",
                    stateFileName, e.getMessage());
        }
    }

    /**
     * Save state to the storage backend.
     *
     * @throws IOException if the state cannot be saved
     */
    public void save() throws IOException {
        String content = GSON.toJson(state, STATE_MAP_TYPE);
        storage.writeStateFile(stateFileName, content);
        LOG.debug("Saved {} file states to {}", state.size(), stateFileName);
    }

    /**
     * Check if a file needs migration based on its current state.
     *
     * @param currentState the current state of the file
     * @return true if the file is new or modified since last migration
     */
    public boolean needsMigration(@NotNull FileState currentState) {
        FileState previousState = state.get(currentState.path());
        if (previousState == null) {
            return true; // New file
        }
        return !previousState.matches(currentState);
    }

    /**
     * Record a successful file migration.
     *
     * @param fileState the state of the successfully migrated file
     */
    public void recordMigration(@NotNull FileState fileState) {
        state.put(fileState.path(), fileState);
    }

    /**
     * Get the number of files in the state.
     *
     * @return the number of tracked files
     */
    public int size() {
        return state.size();
    }

    /**
     * Get the state file name.
     *
     * @return the state file name
     */
    public String getStateFileName() {
        return stateFileName;
    }
}
