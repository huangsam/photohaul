package io.huangsam.photohaul.migration.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestPathStateStorage {

    @Test
    void testReadStateFileReturnsNullWhenNotExists(@TempDir Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String result = storage.readStateFile("nonexistent.json");
        assertNull(result);
    }

    @Test
    void testReadStateFileReturnsContent(@TempDir Path tempDir) throws IOException {
        String content = "{\"test\": \"data\"}";
        Files.writeString(tempDir.resolve("state.json"), content);

        PathStateStorage storage = new PathStateStorage(tempDir);
        String result = storage.readStateFile("state.json");
        assertEquals(content, result);
    }

    @Test
    void testWriteStateFileCreatesFile(@TempDir Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String content = "{\"test\": \"data\"}";

        storage.writeStateFile("state.json", content);

        String result = Files.readString(tempDir.resolve("state.json"));
        assertEquals(content, result);
    }

    @Test
    void testWriteStateFileCreatesDirectories(@TempDir Path tempDir) throws IOException {
        Path nestedDir = tempDir.resolve("nested/dir");
        PathStateStorage storage = new PathStateStorage(nestedDir);
        String content = "{\"test\": \"data\"}";

        storage.writeStateFile("state.json", content);

        String result = Files.readString(nestedDir.resolve("state.json"));
        assertEquals(content, result);
    }

    @Test
    void testWriteAndReadRoundTrip(@TempDir Path tempDir) throws IOException {
        PathStateStorage storage = new PathStateStorage(tempDir);
        String content = "{\"path\":\"/photo.jpg\",\"size\":1024,\"lastModifiedMillis\":1700000000000}";

        storage.writeStateFile(".photohaul_state.json", content);
        String result = storage.readStateFile(".photohaul_state.json");

        assertEquals(content, result);
    }
}
