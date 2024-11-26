package io.huangsam.photohaul.visit;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class PathRuleSet {
    private final List<Predicate<Path>> rules;

    public PathRuleSet(List<Predicate<Path>> rules) {
        this.rules = rules;
    }

    public boolean matches(Path path) {
        return rules.stream().allMatch(rule -> rule.test(path));
    }
}
