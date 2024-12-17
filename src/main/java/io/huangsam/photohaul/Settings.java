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

    public static Settings getDefault() {
        return new Settings("config.properties");
    }

    public Settings(String name) {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(name)) {
            properties.load(input);
        } catch (IOException e) {
            LOG.error("Cannot find settings file {}", name);
        }
    }

    public Settings(Properties input) {
        properties = input;
    }

    public String getValue(@NotNull String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            LOG.error("Cannot find settings key {}", key);
            throw new NullPointerException();
        }
        return value;
    }

    public String getValue(@NotNull String key, String other) {
        String value = properties.getProperty(key);
        return (value == null) ? other : value;
    }

    public Path getSourcePath() {
        return Paths.get(System.getProperty("user.home")).resolve(getValue("path.source"));
    }
}
