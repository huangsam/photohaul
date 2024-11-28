package io.huangsam.photohaul.traversal;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class PathTraversal {
    private static final Logger LOG = getLogger(PathTraversal.class);

    private final Path sourcePath;
    private final PathRuleSet pathRuleSet;

    public PathTraversal(Path path, PathRuleSet pathRuleSet) {
        this.sourcePath = path;
        this.pathRuleSet = pathRuleSet;
    }

    public void traverse(PhotoPathVisitor pathVisitor) {
        try (Stream<Path> sourceStream = Files.walk(sourcePath)) {
            sourceStream.parallel().filter(pathRuleSet::matches).forEach(pathVisitor::visitPhoto);
        } catch (IOException e) {
            LOG.error("Abort traversal of {}: {}", sourcePath, e.getMessage());
        }
    }
}
