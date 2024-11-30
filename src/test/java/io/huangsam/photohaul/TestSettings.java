package io.huangsam.photohaul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSettings {
    private static final Settings SETTINGS = new Settings("path-example.properties");

    @Test
    void testGetSourceRoot() {
        assertTrue(SETTINGS.getSourceRoot().endsWith("Dummy/Source"));
    }

    @Test
    void testGetTargetRoot() {
        assertTrue(SETTINGS.getTargetRoot().endsWith("Dummy/Target"));
    }
}
