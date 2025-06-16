package io.huangsam.photohaul;

import io.huangsam.photohaul.migration.MigratorMode;
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
    private static final String CONFIG_FILE_SYSTEM_PROPERTY = "photohaul.config";
    private static final String CONFIG_FILE_DEFAULT = "config.properties";

    private final Properties properties;

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
     * Constructs Settings by loading properties from a named resource file.
     *
     * @param name The name of the resource file.
     */
    public Settings(String name) {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(name)) {
            if (input == null) {
                LOG.error("Settings file '{}' not found in classpath.", name);
                throw new IllegalStateException("Required settings file not found: " + name);
            }
            properties.load(input);
        } catch (IOException e) {
            LOG.error("Error reading settings file '{}': {}", name, e.getMessage());
            throw new RuntimeException("Failed to load settings file: " + name, e);
        }
    }

    /**
     * Constructs Settings using an existing Properties object. Useful for testing or advanced scenarios.
     *
     * @param input An existing Properties object.
     */
    public Settings(Properties input) {
        this.properties = input;
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
     */
    public Path getSourcePath() {
        String relativeSourcePath = getValue("path.source"); // This will throw if missing
        return Paths.get(System.getProperty("user.home")).resolve(relativeSourcePath);
    }

    /**
     * Retrieves the MigratorMode from the {@code migrator.mode} property.
     *
     * @return The MigratorMode enum value.
     * @throws IllegalArgumentException if the value found is not a valid MigratorMode.
     */
    public MigratorMode getMigratorMode() {
        return MigratorMode.valueOf(getValue("migrator.mode")); // This will throw if missing or invalid enum value
    }
}
