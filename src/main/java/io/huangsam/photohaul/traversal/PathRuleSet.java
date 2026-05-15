package io.huangsam.photohaul.traversal;

import org.jspecify.annotations.NonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public record PathRuleSet(List<Predicate<Path>> rules) {
    @NonNull
    public static PathRuleSet getDefault() {
        return new PathRuleSet(List.of(
                Files::isRegularFile,
                PathRule.isPublic(),
                PathRule.validExtensions().or(PathRule.isImageContent()),
                PathRule.minimumBytes(100L)));
    }

    public boolean matches(Path path) {
        return rules.stream().allMatch(rule -> rule.test(path));
    }

    public int size() {
        return rules.size();
    }
}
