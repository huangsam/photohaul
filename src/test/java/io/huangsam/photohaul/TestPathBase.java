package io.huangsam.photohaul;

import java.nio.file.Path;

public class TestPathBase {
    protected static Path getStaticResources() {
        return Path.of("src/test/resources/static");
    }
}
