package io.huangsam.photohaul.traversal;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class PathRule {
    private static final List<String> ALLOW_LIST = List.of(
            "jpg", "jpeg", "png", "gif", "cr2", "nef", "arw");

    @NotNull
    public static Predicate<Path> validExtensions() {
        return path -> {
            String pathName = path.toString().toLowerCase();
            return ALLOW_LIST.stream().anyMatch(pathName::endsWith);
        };
    }

    @NotNull
    public static Predicate<Path> isImageContent() {
        return path -> {
            try {
                String contentType = Files.probeContentType(path);
                return contentType != null && contentType.startsWith("image/");
            } catch (IOException e) {
                return false;
            }
        };
    }

    @NotNull
    public static Predicate<Path> minimumBytes(long minThreshold) {
        return path -> {
            try {
                return Files.size(path) >= minThreshold;
            } catch (IOException e) {
                return false;
            }
        };
    }

    @NotNull
    public static Predicate<Path> isPublic() {
        return path -> !path.getFileName().toString().startsWith(".");
    }
}
