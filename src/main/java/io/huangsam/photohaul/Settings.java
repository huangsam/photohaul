package io.huangsam.photohaul;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class Settings {
    private static final Logger LOG = getLogger(Settings.class);

    public Path getSourcePath() {
        Properties properties = loadProperties();
        String targetPath = properties.getProperty("source.path");
        return Paths.get(System.getProperty("user.home")).resolve(targetPath);
    }

    public Path getTargetPath() {
        Properties properties = loadProperties();
        String targetPath = properties.getProperty("target.path");
        return Paths.get(System.getProperty("user.home")).resolve(targetPath);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            LOG.error("Error loading properties file: {}", e.getMessage());
        }
        return properties;
    }
}
