package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.TestPathBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRuleSet extends TestPathBase {
    private static final Path CURRENT_TEXT = getCurrentResources().resolve("sample.txt");

    @Test
    void testMatchesNoPredicatePass() {
        PathRuleSet pathRules = new PathRuleSet(List.of());
        assertTrue(pathRules.matches(CURRENT_TEXT));
    }

    @Test
    void testMatchesOnePredicatePass() {
        PathRuleSet pathRules = new PathRuleSet(List.of(Files::isRegularFile));
        assertTrue(pathRules.matches(CURRENT_TEXT));
    }

    @Test
    void testMatchesOnePredicateFail() {
        PathRuleSet pathRules = new PathRuleSet(List.of(PathRule.validExtensions()));
        assertFalse(pathRules.matches(CURRENT_TEXT));
    }
}
