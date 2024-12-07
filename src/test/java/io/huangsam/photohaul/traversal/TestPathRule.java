package io.huangsam.photohaul.traversal;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRule {
    @Test
    void testIsValidExtension() {
        Path saladPath = getStaticResources().resolve("salad.jpg");
        assertTrue(PathRule.validExtensions().test(saladPath));
    }

    @Test
    void testIsImageContent() {
        Path schoolPath = getStaticResources().resolve("school.png");
        assertTrue(PathRule.isImageContent().test(schoolPath));
    }

    @Test
    void testIsMinimumBytes() {
        Path bauerPath = getStaticResources().resolve("bauerlite.jpg");
        assertTrue(PathRule.minimumBytes(100L).test(bauerPath));
    }
}
