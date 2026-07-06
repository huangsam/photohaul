package io.huangsam.photohaul.deduplication;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestHashUtils {

    // --- bytesToHex ---

    @Test
    void testBytesToHexEmptyArray() {
        assertEquals("", HashUtils.bytesToHex(new byte[]{}));
    }

    @Test
    void testBytesToHexAllZeros() {
        // Each zero byte should produce "00", not "0"
        assertEquals("000000", HashUtils.bytesToHex(new byte[]{0, 0, 0}));
    }

    @Test
    void testBytesToHexPaddingForLowValues() {
        // 0x0f should be "0f", not "f"
        assertEquals("0f", HashUtils.bytesToHex(new byte[]{0x0f}));
    }

    @Test
    void testBytesToHexHighValues() {
        assertEquals("ff", HashUtils.bytesToHex(new byte[]{(byte) 0xff}));
        assertEquals("deadbe", HashUtils.bytesToHex(new byte[]{(byte) 0xde, (byte) 0xad, (byte) 0xbe}));
    }

    @Test
    void testBytesToHexLengthIsDoubled() {
        byte[] input = new byte[16];
        assertEquals(32, HashUtils.bytesToHex(input).length());
    }

    // --- calculateHash (full file) ---

    @Test
    void testCalculateHashIsConsistentForSameContent(@TempDir @NonNull Path tempDir) throws IOException {
        Path file = tempDir.resolve("photo.jpg");
        Files.write(file, "some content".getBytes());

        String hash1 = HashUtils.calculateHash(file);
        String hash2 = HashUtils.calculateHash(file);

        assertEquals(hash1, hash2);
    }

    @Test
    void testCalculateHashDiffersForDifferentContent(@TempDir @NonNull Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("a.jpg");
        Path file2 = tempDir.resolve("b.jpg");
        Files.write(file1, "content A".getBytes());
        Files.write(file2, "content B".getBytes());

        assertNotEquals(HashUtils.calculateHash(file1), HashUtils.calculateHash(file2));
    }

    @Test
    void testCalculateHashSameForIdenticalFiles(@TempDir @NonNull Path tempDir) throws IOException {
        byte[] content = "identical content".getBytes();
        Path file1 = tempDir.resolve("x.jpg");
        Path file2 = tempDir.resolve("y.jpg");
        Files.write(file1, content);
        Files.write(file2, content);

        assertEquals(HashUtils.calculateHash(file1), HashUtils.calculateHash(file2));
    }

    @Test
    void testCalculateHashEmptyFile(@TempDir @NonNull Path tempDir) throws IOException {
        Path file = tempDir.resolve("empty.jpg");
        Files.write(file, new byte[]{});

        // SHA-256 of empty input is a known constant
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                HashUtils.calculateHash(file));
    }

    @Test
    void testCalculateHashThrowsForNonExistentFile() {
        Path missing = Path.of("does_not_exist.jpg");
        assertThrows(IOException.class, () -> HashUtils.calculateHash(missing));
    }

    // --- calculateHash with limit ---

    @Test
    void testCalculateHashWithLimitSmallerThanFile(@TempDir @NonNull Path tempDir) throws IOException {
        // Write 100 bytes; hash with limit=10 should differ from full hash
        byte[] content = new byte[100];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) i;
        }
        Path file = tempDir.resolve("data.jpg");
        Files.write(file, content);

        String fullHash = HashUtils.calculateHash(file, -1);
        String partialHash = HashUtils.calculateHash(file, 10);

        assertNotEquals(fullHash, partialHash);
    }

    @Test
    void testCalculateHashWithLimitLargerThanFile(@TempDir @NonNull Path tempDir) throws IOException {
        // When limit exceeds file size the result should equal the full hash
        byte[] content = "short".getBytes();
        Path file = tempDir.resolve("short.jpg");
        Files.write(file, content);

        String fullHash = HashUtils.calculateHash(file, -1);
        String limitHash = HashUtils.calculateHash(file, 10_000);

        assertEquals(fullHash, limitHash);
    }

    @Test
    void testCalculateHashWithLimitZero(@TempDir @NonNull Path tempDir) throws IOException {
        // Limit of 0 means read no bytes — same as hashing an empty input
        Path file = tempDir.resolve("photo.jpg");
        Files.write(file, "some data".getBytes());

        String limitZeroHash = HashUtils.calculateHash(file, 0);
        String emptyHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        assertEquals(emptyHash, limitZeroHash);
    }

    @Test
    void testCalculateHashWithNegativeLimitEqualsFullHash(@TempDir @NonNull Path tempDir) throws IOException {
        Path file = tempDir.resolve("photo.jpg");
        Files.write(file, "test content".getBytes());

        assertEquals(HashUtils.calculateHash(file), HashUtils.calculateHash(file, -1));
    }

    @Test
    void testCalculateHashWithLimitIsConsistentAcrossIdenticalFiles(@TempDir @NonNull Path tempDir) throws IOException {
        byte[] content = new byte[2048];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }
        Path file1 = tempDir.resolve("p1.jpg");
        Path file2 = tempDir.resolve("p2.jpg");
        Files.write(file1, content);
        Files.write(file2, content);

        assertEquals(HashUtils.calculateHash(file1, 1024), HashUtils.calculateHash(file2, 1024));
    }
}
