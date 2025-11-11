package io.huangsam.photohaul;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSettings {
    @Test
    void testGetSourcePath() {
        Settings settings = new Settings("path-example.properties");
        assertTrue(settings.getSourcePath().endsWith("Dummy/Source"));
    }

    @Test
    void testGetValueIsValid() {
        Settings settings = new Settings("dbx-example.properties");
        assertTrue(settings.getValue("dbx.target").startsWith("/"));
    }

    @Test
    void testGetValueIsMissing() {
        Settings settings = new Settings("drive-example.properties");
        assertThrows(NullPointerException.class, () -> settings.getValue("foo.bar"));
    }

    @Test
    void testGetValueWithFallback() {
        Settings settings = new Settings("path-example.properties");
        String expected = "foo";
        assertEquals(expected, settings.getValue("foo.bar", expected));
    }

    @Test
    void testSettingsFromProperties() {
        Properties properties = new Properties();
        properties.setProperty("hello.message", "world");
        Settings settings = new Settings(properties);
        assertEquals("world", settings.getValue("hello.message"));
    }

    @Test
    void testGetDefaultConfig() {
        assertNotNull(Settings.getDefault());
    }

    @Test
    void testLoadFromFilesystem(@TempDir Path tmp) throws IOException {
        // Arrange: create a temp properties file
        Path props = tmp.resolve("custom.properties");
        Files.writeString(props, "hello=filesystem\n");

        // Act: load using absolute filesystem path
        Settings settings = new Settings(props.toString());

        // Assert
        assertEquals("filesystem", settings.getValue("hello"));
    }

    @Test
    void testMissingSettingsThrows() {
        assertThrows(IllegalStateException.class, () -> new Settings("__definitely_not_here__.properties"));
    }
}
