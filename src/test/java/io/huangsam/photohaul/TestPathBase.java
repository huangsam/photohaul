package io.huangsam.photohaul;

import java.nio.file.Path;

public class TestPathBase {
    protected static Path getCurrentResources() {
        return Path.of("src/test/resources/current");
    }

    protected static Path getNextResources() {
        return Path.of("src/test/resources/next");
    }
}
