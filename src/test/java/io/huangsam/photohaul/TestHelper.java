package io.huangsam.photohaul;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class TestHelper {
    private static final Path TEST_RESOURCES = Path.of("src/test/resources");

    @NotNull
    public static Path getStaticResources() {
        return TEST_RESOURCES.resolve("static");
    }

    @NotNull
    public static Path getTempResources() {
        return TEST_RESOURCES.resolve("temp");
    }
}
