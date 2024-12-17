package io.huangsam.photohaul.traversal;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathWalker {
    @Test
    void testTraverseWithPhotos() {
        PhotoCollector photoCollector = new PhotoCollector();
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(PathRule.validExtensions()));
        PathWalker pathWalker = new PathWalker(getStaticResources(), pathRuleSet);
        pathWalker.traverse(photoCollector);
        assertFalse(photoCollector.getPhotos().isEmpty());
    }

    @Test
    void testTraverseWithNoPhotos() {
        PhotoCollector photoCollector = new PhotoCollector();
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(PathRule.minimumBytes(100_000_000L)));
        PathWalker pathWalker = new PathWalker(getStaticResources(), pathRuleSet);
        pathWalker.traverse(photoCollector);
        assertTrue(photoCollector.getPhotos().isEmpty());
    }
}
