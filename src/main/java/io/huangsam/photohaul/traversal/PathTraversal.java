package io.huangsam.photohaul.traversal;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class PathTraversal {
    private static final Logger LOG = getLogger(PathTraversal.class);

    private final Path sourceRoot;
    private final PathRuleSet pathRuleSet;

    public PathTraversal(Path sourceRoot, PathRuleSet pathRuleSet) {
        this.sourceRoot = sourceRoot;
        this.pathRuleSet = pathRuleSet;
    }

    public void traverse(PhotoPathVisitor pathVisitor) {
        LOG.debug("Start traversal of {}", sourceRoot);
        try (Stream<Path> sourceStream = Files.walk(sourceRoot)) {
            sourceStream.parallel().filter(pathRuleSet::matches).forEach(pathVisitor::visitPhoto);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", sourceRoot, e.getMessage());
        }
    }
}
