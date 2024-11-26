package io.huangsam.photohaul.visit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRuleSet {
    @Test
    void testMatchesWithNoPredicates() {
        PathRuleSet pathRules = new PathRuleSet(List.of());
        assertTrue(pathRules.matches(Path.of("/some.png")));
    }

    @Test
    void testMatchesWithOnePredicate() {
        PathRuleSet pathRules = new PathRuleSet(List.of(PathRule.allowedExtensions("png")));
        assertTrue(pathRules.matches(Path.of("/some.png")));
    }
}
