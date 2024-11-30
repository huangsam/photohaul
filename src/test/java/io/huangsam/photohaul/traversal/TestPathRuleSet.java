package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.TestPathBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRuleSet extends TestPathBase {
    @Test
    void testMatchesNoPredicatePass() {
        Path sampleText = getStaticResources().resolve("sample.txt");
        PathRuleSet pathRuleSet = new PathRuleSet(List.of());
        assertTrue(pathRuleSet.matches(sampleText));
    }

    @Test
    void testMatchesOnePredicatePass() {
        Path sampleText = getStaticResources().resolve("sample.txt");
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(Files::isRegularFile));
        assertTrue(pathRuleSet.matches(sampleText));
    }

    @Test
    void testMatchesOnePredicateFail() {
        Path sampleText = getStaticResources().resolve("sample.txt");
        PathRuleSet pathRuleSet = new PathRuleSet(List.of(PathRule.validExtensions()));
        assertFalse(pathRuleSet.matches(sampleText));
    }
}
