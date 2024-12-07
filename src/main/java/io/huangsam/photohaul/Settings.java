package io.huangsam.photohaul;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class Settings {
    private static final Logger LOG = getLogger(Settings.class);

    private final Properties properties;

    public Settings(String name) {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(name)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValue(@NotNull String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            LOG.error("Cannot find setting {}", key);
            throw new NullPointerException();
        }
        return value;
    }

    public String getValue(@NotNull String key, String other) {
        String value = properties.getProperty(key);
        return (value == null) ? other : value;
    }

    public Path getSourceRootPath() {
        return Paths.get(System.getProperty("user.home")).resolve(getValue("source.root"));
    }

    public Path getTargetRootPath() {
        return Paths.get(System.getProperty("user.home")).resolve(getValue("target.root"));
    }
}
