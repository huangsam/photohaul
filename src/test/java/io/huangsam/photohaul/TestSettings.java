package io.huangsam.photohaul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSettings {
    @Test
    void testGetSourcePath() {
        Settings settings = Settings.getDefault();
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
}
