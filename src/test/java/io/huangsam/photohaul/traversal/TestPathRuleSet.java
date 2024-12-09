package io.huangsam.photohaul.traversal;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRuleSet {
    @Test
    void testMatchesNoPredicatePass() {
        Path samplePath = getStaticResources().resolve("sample.txt");
        PathRuleSet pathRuleSet = new PathRuleSet(List.of());
        assertTrue(pathRuleSet.matches(samplePath));
    }

    @Test
    void testMatchesOnePredicatePass() {
        Path samplePath = getStaticResources().resolve("sample.txt");
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(Files::isRegularFile));
        assertTrue(pathRuleSet.matches(samplePath));
    }

    @Test
    void testMatchesOnePredicateFail() {
        Path samplePath = getStaticResources().resolve("sample.txt");
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(PathRule.validExtensions()));
        assertFalse(pathRuleSet.matches(samplePath));
    }

    @Test
    void testDefaultRuleSetIsNotEmpty() {
        assertTrue(PathRuleSet.getDefault().size() > 0);
    }
}
