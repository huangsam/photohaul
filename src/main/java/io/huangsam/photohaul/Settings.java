package io.huangsam.photohaul;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
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

    public String getValue(@NotNull String key) {
        return Objects.requireNonNull(properties.getProperty(key));
    }

    public Path getSourceRootPath() {
        return Paths.get(System.getProperty("user.home")).resolve(getValue("source.root"));
    }

    public Path getTargetRootPath() {
        return Paths.get(System.getProperty("user.home")).resolve(getValue("target.root"));
    }
}
