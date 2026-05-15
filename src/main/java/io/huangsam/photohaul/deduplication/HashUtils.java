package io.huangsam.photohaul.deduplication;

import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

/**
 * Utility class for hashing operations.
 */
public final class HashUtils {
    private HashUtils() {
        // Prevent instantiation
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return The hexadecimal string representation.
     */
    public static @NonNull String bytesToHex(byte @NonNull [] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Calculates the SHA-256 hash of a file.
     *
     * @param path The path to the file.
     * @return The hexadecimal string representation of the hash.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static @NonNull String calculateHash(@NonNull Path path) throws java.io.IOException {
        return calculateHash(path, -1);
    }

    /**
     * Calculates the SHA-256 hash of a file, optionally limiting the number of bytes read.
     *
     * @param path  The path to the file.
     * @param limit The maximum number of bytes to read. If negative, the entire file is read.
     * @return The hexadecimal string representation of the hash.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static @NonNull String calculateHash(@NonNull Path path, int limit) throws java.io.IOException {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            try (java.io.InputStream inputStream = java.nio.file.Files.newInputStream(path)) {
                byte[] buffer = new byte[65536]; // 64KB
                int bytesRead;
                int totalRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (limit >= 0 && totalRead + bytesRead > limit) {
                        digest.update(buffer, 0, limit - totalRead);
                        break;
                    }
                    digest.update(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    if (limit >= 0 && totalRead >= limit) {
                        break;
                    }
                }
            }
            return bytesToHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
