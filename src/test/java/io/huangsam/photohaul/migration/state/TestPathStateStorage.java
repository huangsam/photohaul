package io.huangsam.photohaul.migration.state;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathStateStorage {
    @Test
    void testReadStateFileReturnsNullWhenNotExists(@TempDir @NonNull Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String result = storage.readStateFile("nonexistent.json");
        assertNull(result);
    }

    @Test
    void testReadStateFileReturnsContent(@TempDir @NonNull Path tempDir) throws IOException {
        String content = "{\"test\": \"data\"}";
        Files.writeString(tempDir.resolve("state.json"), content);

        PathStateStorage storage = new PathStateStorage(tempDir);
        String result = storage.readStateFile("state.json");
        assertEquals(content, result);
    }

    @Test
    void testWriteStateFileCreatesFile(@TempDir @NonNull Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String content = "{\"test\": \"data\"}";

        storage.writeStateFile("state.json", content);

        String result = Files.readString(tempDir.resolve("state.json"));
        assertEquals(content, result);
    }

    @Test
    void testWriteStateFileCreatesDirectories(@TempDir @NonNull Path tempDir) throws IOException {
        Path nestedDir = tempDir.resolve("nested/dir");
        PathStateStorage storage = new PathStateStorage(nestedDir);
        String content = "{\"test\": \"data\"}";

        storage.writeStateFile("state.json", content);

        String result = Files.readString(nestedDir.resolve("state.json"));
        assertEquals(content, result);
    }

    @Test
    void testWriteAndReadRoundTrip(@TempDir @NonNull Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String content = "{\"path\":\"/photo.jpg\",\"size\":1024,\"lastModifiedMillis\":1700000000000}";

        storage.writeStateFile(".photohaul_state.json", content);
        String result = storage.readStateFile(".photohaul_state.json");

        assertEquals(content, result);
    }

    @Test
    void testReadStateFileWithUTF8Content(@TempDir @NonNull Path tempDir) throws IOException {
        String content = "{\"path\":\"/photos/日本語.jpg\",\"size\":1024}";
        Files.writeString(tempDir.resolve("state.json"), content);

        PathStateStorage storage = new PathStateStorage(tempDir);
        String result = storage.readStateFile("state.json");
        assertEquals(content, result);
    }

    @Test
    void testWriteStateFileWithUTF8Content(@TempDir @NonNull Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String content = "{\"path\":\"/photos/日本語.jpg\",\"size\":1024}";

        storage.writeStateFile("state.json", content);

        String result = Files.readString(tempDir.resolve("state.json"));
        assertEquals(content, result);
    }

    @Test
    void testWriteStateFileOverwritesExisting(@TempDir @NonNull Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String originalContent = "{\"original\": \"data\"}";
        String newContent = "{\"new\": \"data\"}";

        storage.writeStateFile("state.json", originalContent);
        storage.writeStateFile("state.json", newContent);

        String result = storage.readStateFile("state.json");
        assertEquals(newContent, result);
    }

    @Test
    void testReadStateFileWithEmptyFile(@TempDir @NonNull Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("empty.json"), "");

        PathStateStorage storage = new PathStateStorage(tempDir);
        String result = storage.readStateFile("empty.json");
        assertEquals("", result);
    }

    @Test
    void testWriteStateFileWithEmptyContent(@TempDir @NonNull Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);

        storage.writeStateFile("empty.json", "");

        String result = Files.readString(tempDir.resolve("empty.json"));
        assertEquals("", result);
    }

    @Test
    void testReadStateFileWithMultilineContent(@TempDir @NonNull Path tempDir) throws IOException {
        String content = "{\n  \"path\": \"/photo.jpg\",\n  \"size\": 1024\n}";
        Files.writeString(tempDir.resolve("state.json"), content);

        PathStateStorage storage = new PathStateStorage(tempDir);
        String result = storage.readStateFile("state.json");
        assertEquals(content, result);
    }

    @Test
    void testWriteStateFileWithMultilineContent(@TempDir @NonNull Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String content = "{\n  \"path\": \"/photo.jpg\",\n  \"size\": 1024\n}";

        storage.writeStateFile("state.json", content);

        String result = Files.readString(tempDir.resolve("state.json"));
        assertEquals(content, result);
    }

    @Test
    void testWriteStateFileWithDeeplyNestedDirectories(@TempDir @NonNull Path tempDir) throws IOException {
        Path deepPath = tempDir.resolve("a/b/c/d/e/f");
        PathStateStorage storage = new PathStateStorage(deepPath);
        String content = "{\"test\": \"deep\"}";

        storage.writeStateFile("state.json", content);

        assertTrue(Files.exists(deepPath.resolve("state.json")));
        assertEquals(content, Files.readString(deepPath.resolve("state.json")));
    }
}
