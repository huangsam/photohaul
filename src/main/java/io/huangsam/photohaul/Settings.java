package io.huangsam.photohaul;

import io.huangsam.photohaul.migration.MigratorMode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public record Settings(Properties properties) {
    private static final Logger LOG = getLogger(Settings.class);
    private static final String CONFIG_FILE_SYSTEM_PROPERTY = "photohaul.config";
    private static final String CONFIG_FILE_DEFAULT = "config.properties";

    /**
     * Provides the default Settings instance, attempting to load from a system property
     * or falling back to a default file name.
     *
     * @return A Settings instance.
     * @throws IllegalArgumentException if the value is not defined.
     */
    @NotNull
    public static Settings getDefault() {
        String configFileName = System.getProperty(CONFIG_FILE_SYSTEM_PROPERTY);
        if (configFileName == null || configFileName.isEmpty()) {
            configFileName = CONFIG_FILE_DEFAULT;
        }
        LOG.info("Use config file from {}: {}", CONFIG_FILE_SYSTEM_PROPERTY, configFileName);
        return new Settings(configFileName);
    }

    /**
     * Constructs Settings by loading properties from a classpath resource name, with a filesystem fallback.
     *
     * Loading order:
     * 1) Try classpath resource via ClassLoader#getResourceAsStream(name)
     * 2) If not found, try reading from the filesystem path specified by {@code name}
     *
     * @param name The classpath resource name or filesystem path.
     * @throws IllegalStateException if the settings file is not found in either location.
     * @throws RuntimeException      if the settings file cannot be parsed.
     */
    public Settings(String name) {
        this(new Properties());
        boolean fromClasspath = true;
        Path fsPath = null;

        // Resolve input stream from classpath first, then filesystem fallback
        InputStream resolved = getClass().getClassLoader().getResourceAsStream(name);
        if (resolved == null) {
            fromClasspath = false;
            fsPath = Paths.get(name);
            try {
                if (Files.exists(fsPath)) {
                    resolved = Files.newInputStream(fsPath);
                }
            } catch (IOException e) {
                LOG.error("Error opening settings file from filesystem '{}': {}", name, e.getMessage());
                throw new RuntimeException("Failed to open settings file: " + name, e);
            }
        }

        // Load properties from the resolved input stream
        try (InputStream input = resolved) {
            if (input == null) {
                LOG.error("Settings file '{}' not found in classpath or filesystem.", name);
                throw new IllegalStateException("Required settings file not found: " + name);
            }

            properties.load(input);

            if (fromClasspath) {
                LOG.info("Loaded settings from classpath: {}", name);
            } else {
                LOG.info("Loaded settings from filesystem: {}", fsPath.toAbsolutePath());
            }
        } catch (IOException e) {
            LOG.error("Error reading settings file '{}': {}", name, e.getMessage());
            throw new RuntimeException("Failed to load settings file: " + name, e);
        }
    }

    /**
     * Retrieves a mandatory string value from settings. Throws NullPointerException if key is not found.
     *
     * @param key The key to look up.
     * @return The string value associated with the key.
     * @throws NullPointerException if the key is not found.
     */
    public String getValue(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            LOG.error("Mandatory settings key '{}' not found.", key);
            throw new NullPointerException("Settings key '" + key + "' is missing.");
        }
        return value;
    }

    /**
     * Retrieves a string value from settings, with a fallback default.
     *
     * @param key   The key to look up.
     * @param other The default value to return if the key is not found.
     * @return The string value, or the default if not found.
     */
    public String getValue(String key, String other) {
        String value = properties.getProperty(key);
        return (value == null) ? other : value;
    }

    /**
     * Constructs the full source path by resolving a path from the "path.source" property
     * against the user's home directory.
     *
     * @return The resolved source path.
     * @throws IllegalArgumentException if "path.source" is missing.
     */
    public Path getSourcePath() {
        String relativeSourcePath = getValue("path.source");
        return Paths.get(System.getProperty("user.home")).resolve(relativeSourcePath);
    }

    /**
     * Retrieves the MigratorMode from the {@code migrator.mode} property.
     *
     * @return The MigratorMode enum value.
     * @throws IllegalArgumentException if "migrator.mode" is missing or invalid.
     */
    public MigratorMode getMigratorMode() {
        return MigratorMode.valueOf(getValue("migrator.mode"));
    }
}
