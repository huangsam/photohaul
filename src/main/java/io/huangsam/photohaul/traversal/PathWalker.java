package io.huangsam.photohaul.traversal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class is responsible for traversing a source directory and filtering
 * files based on a given rule set.
 */
public record PathWalker(Path sourceRoot, PathRuleSet pathRuleSet) {
    private static final Logger LOG = getLogger(PathWalker.class);

    /**
     * Traverse source directory recursively, passing relevant files to
     * the photo collector for aggregation purposes.
     *
     * @param photoCollector collector to process matching files
     */
    public void traverse(@NotNull PhotoCollector photoCollector) {
        LOG.debug("Start traversal of {}", sourceRoot);
        try (Stream<Path> sourceStream = Files.walk(sourceRoot)) {
            sourceStream.parallel().filter(pathRuleSet::matches).forEach(photoCollector::addPhoto);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", sourceRoot, e.getMessage());
        }
    }
}
