package io.huangsam.photohaul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSettings {
    private static final Settings SETTINGS = new Settings("path-example.properties");

    @Test
    void testGetSourceRootPath() {
        assertTrue(SETTINGS.getSourceRootPath().endsWith("Dummy/Source"));
    }

    @Test
    void testGetTargetRootPath() {
        assertTrue(SETTINGS.getTargetRootPath().endsWith("Dummy/Target"));
    }
}
