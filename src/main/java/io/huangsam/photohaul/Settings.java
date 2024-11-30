package io.huangsam.photohaul;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings {
    private final Properties properties;

    public Settings(String name) {
        properties = new Properties();
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(name)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getSourceRoot() {
        String sourceRoot = properties.getProperty("source.root");
        if (sourceRoot == null) {
            throw new IllegalArgumentException("Missing source root");
        }
        return Paths.get(System.getProperty("user.home")).resolve(sourceRoot);
    }

    public Path getTargetRoot() {
        String targetRoot = properties.getProperty("target.root");
        if (targetRoot == null) {
            throw new IllegalArgumentException("Missing target root");
        }
        return Paths.get(System.getProperty("user.home")).resolve(targetRoot);
    }
}
