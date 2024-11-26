package io.huangsam.photohaul;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings {
    private final Properties properties;

    public Settings() {
        properties = new Properties();
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getSourcePath() {
        String targetPath = properties.getProperty("source.path");
        return Paths.get(System.getProperty("user.home")).resolve(targetPath);
    }

    public Path getTargetPath() {
        String targetPath = properties.getProperty("target.path");
        return Paths.get(System.getProperty("user.home")).resolve(targetPath);
    }
}
