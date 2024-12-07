package io.huangsam.photohaul.traversal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class is responsible for traversing a directory structure and filtering
 * files based on a given rule set. It utilizes Java's parallel streams to
 * efficiently process a large number of files.
 *
 * <p> Once a file matches the specified rules, it is passed to a provided
 * {@code PhotoPathVisitor} for further processing. This visitor can then
 * be used for actions like copying, moving, or analyzing the file.
 */
public class PathWalker {
    private static final Logger LOG = getLogger(PathWalker.class);

    private final Path sourceRoot;
    private final PathRuleSet pathRuleSet;

    public PathWalker(Path sourceRoot, PathRuleSet pathRuleSet) {
        this.sourceRoot = sourceRoot;
        this.pathRuleSet = pathRuleSet;
    }

    /**
     * Traverses source directory recursively, passing relevant files to
     * the path visitor for aggregation purposes.
     *
     * @param pathVisitor visitor to process matching files
     */
    public void traverse(@NotNull PhotoPathVisitor pathVisitor) {
        LOG.debug("Start traversal of {}", sourceRoot);
        try (Stream<Path> sourceStream = Files.walk(sourceRoot)) {
            sourceStream.parallel().filter(pathRuleSet::matches).forEach(pathVisitor::visitPhoto);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", sourceRoot, e.getMessage());
        }
    }
}
