package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.TestPathBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathTraversal extends TestPathBase {
    @Test
    void testTraverseWithPhotos() {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(PathRule.validExtensions()));
        PathTraversal pathTraversal = new PathTraversal(getStaticResources(), pathRuleSet);
        pathTraversal.traverse(pathVisitor);
        assertFalse(pathVisitor.getPhotos().isEmpty());
    }

    @Test
    void testTraverseWithNoPhotos() {
        PhotoPathVisitor pathVisitor = new PhotoPathVisitor();
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(PathRule.minimumBytes(100_000_000L)));
        PathTraversal pathTraversal = new PathTraversal(getStaticResources(), pathRuleSet);
        pathTraversal.traverse(pathVisitor);
        assertTrue(pathVisitor.getPhotos().isEmpty());
    }
}
