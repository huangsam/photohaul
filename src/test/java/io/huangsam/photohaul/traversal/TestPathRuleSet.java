package io.huangsam.photohaul.traversal;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRuleSet {
    @Test
    void testMatchesWithOnePredicate() {
        PathRuleSet pathRules = new PathRuleSet(List.of(PathRule.validExtensions()));
        assertTrue(pathRules.matches(Path.of("/some.png")));
    }
}
