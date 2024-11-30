package io.huangsam.photohaul;

import java.nio.file.Path;

public class TestPathBase {
    private static final Path TEST_RESOURCES = Path.of("src/test/resources");

    protected static Path getStaticResources() {
        return TEST_RESOURCES.resolve("static");
    }

    protected static Path getTempResources() {
        return TEST_RESOURCES.resolve("temp");
    }
}
