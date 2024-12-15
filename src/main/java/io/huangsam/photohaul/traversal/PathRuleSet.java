package io.huangsam.photohaul.traversal;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class PathRuleSet {
    private final List<Predicate<Path>> rules;

    @NotNull
    public static PathRuleSet getDefault() {
        return new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.isPublic(),
                PathRule.validExtensions().or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));
    }

    public PathRuleSet(List<Predicate<Path>> rules) {
        this.rules = rules;
    }

    public boolean matches(Path path) {
        return rules.stream().allMatch(rule -> rule.test(path));
    }

    public int size() {
        return rules.size();
    }
}
