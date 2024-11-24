package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        String homeDirectory = System.getProperty("user.home");
        Main.printPhotos(Paths.get(homeDirectory + "/Pictures"));
    }

    private static void printPhotos(Path path) {
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream.filter(Files::isRegularFile).filter(Main::isPhoto).forEach(System.out::println);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static boolean isPhoto(Path path) {
        try {
            return Files.probeContentType(path).startsWith("image/");
        } catch (IOException e) {
            String pathName = path.toString();
            return Stream.of("jpg", "png", "svg").anyMatch(pathName::endsWith);
        }
    }
}
