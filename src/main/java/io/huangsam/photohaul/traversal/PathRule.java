package io.huangsam.photohaul.traversal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class PathRule {
    private static final List<String> ALLOW_LIST = List.of("jpg", "jpeg", "png", "cr2", "nef", "arw");

    public static Predicate<Path> validExtensions() {
        return path -> {
            String pathName = path.toString().toLowerCase();
            return ALLOW_LIST.stream().anyMatch(pathName::endsWith);
        };
    }

    public static Predicate<Path> isImageContent() {
        return path -> {
            try {
                return Files.probeContentType(path).startsWith("image/");
            } catch (IOException | NullPointerException e) {
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
