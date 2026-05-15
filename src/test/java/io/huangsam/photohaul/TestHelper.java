package io.huangsam.photohaul;

import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public final class TestHelper {
    private static final Path TEST_RESOURCES = Path.of("src/test/resources");

    @NonNull
    public static Path getStaticResources() {
        return TEST_RESOURCES.resolve("static");
    }

    @NonNull
    public static Path getTempResources() {
        return TEST_RESOURCES.resolve("temp");
    }
}
