package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PhotoRule {
    public static Predicate<Path> allowedExtensions(String... extensions) {
        return path -> {
            String pathName = path.toString().toLowerCase();
            return Stream.of(extensions).anyMatch(pathName::endsWith);
        };
    }

    public static Predicate<Path> isValidContent() {
        return path -> {
            try {
                return Files.probeContentType(path).startsWith("image/");
            } catch (IOException e) {
                return false;
            }
        };
    }

    public static Predicate<Path> minimumBytes(long minThreshold) {
        return path -> {
            try {
                return Files.size(path) >= minThreshold;
            } catch (IOException e) {
                return false;
            }
        };
    }
}
