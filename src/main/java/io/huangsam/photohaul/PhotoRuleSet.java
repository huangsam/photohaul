package io.huangsam.photohaul;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class PhotoRuleSet {
    private final List<Predicate<Path>> rules;

    public PhotoRuleSet(List<Predicate<Path>> rules) {
        this.rules = rules;
    }

    public boolean matches(Path path) {
        return rules.stream().allMatch(rule -> rule.test(path));
    }
}
