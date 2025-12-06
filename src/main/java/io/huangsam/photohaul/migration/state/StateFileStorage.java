package io.huangsam.photohaul.migration.state;

import java.io.IOException;

/**
 * Interface for reading and writing migration state files.
 *
 * <p> Implementations of this interface provide storage-specific operations
 * for managing the migration state file at different destinations
 * (local filesystem, S3, Dropbox, Google Drive, SFTP).
 */
public interface StateFileStorage {
    /**
     * Read the content of the state file.
     *
     * @param fileName the name of the state file
     * @return the content of the state file, or null if it doesn't exist
     * @throws IOException if an I/O error occurs during reading
     */
    String readStateFile(String fileName) throws IOException;

    /**
     * Write content to the state file.
     *
     * @param fileName the name of the state file
     * @param content  the content to write
     * @throws IOException if an I/O error occurs during writing
     */
    void writeStateFile(String fileName, String content) throws IOException;
}
