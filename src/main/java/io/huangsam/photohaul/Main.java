package io.huangsam.photohaul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Main {
    private static final Logger LOG = getLogger(Main.class);

    public static void main(String[] args) {
        PhotoVisitor visitor = new PhotoVisitor();

        PathRuleSet pathRules = new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.allowedExtensions("jpg", "jpeg", "png").or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));

        visitPhotos(getSourcePath(), visitor, pathRules);

        migratePhotos(visitor);
    }

    private static void visitPhotos(Path path, PhotoVisitor visitor, PathRuleSet pathRules) {
        LOG.info("Start traversal of {}", path);
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream.parallel().filter(pathRules::matches).forEach(visitor::visitPhoto);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", path, e.getMessage());
        }
        LOG.info("Finish traversal of {}", path);
    }

    private static void migratePhotos(PhotoVisitor visitor) {
        Map<Path, Photo> photoIndex = visitor.getPhotoIndex();
        visitor.getPhotoIndex().forEach((path, photo) -> {
            String photoName = photo.name();
            LocalDate photoDate = photo.date();
            if (photoDate != null) {
                Integer photoYear = photoDate.getYear();
                String photoMonth = photoDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                LOG.debug("Move {} over to {}/{}/{}", photoName, getDestinationPath(), photoYear, photoMonth);
            }
        });
        LOG.info("Processed {} photos", photoIndex.size());
    }

    private static Path getSourcePath() {
        return Paths.get(System.getProperty("user.home") + '/' + "Pictures/Camera PNG");
    }

    private static Path getDestinationPath() {
        return Paths.get(System.getProperty("user.home") + '/' + "Pictures/Camera FIN");
    }
}
