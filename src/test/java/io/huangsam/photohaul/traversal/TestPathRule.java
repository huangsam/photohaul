package io.huangsam.photohaul.traversal;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRule {
    @Test
    void testIsValidExtensionTrue() {
        Path saladPath = getStaticResources().resolve("salad.jpg");
        assertTrue(PathRule.validExtensions().test(saladPath));
    }

    @Test
    void testIsValidExtensionFalse() {
        Path saladPath = getStaticResources().resolve("salad.foo");
        assertFalse(PathRule.validExtensions().test(saladPath));
    }

    @Test
    void testIsImageContentTrue() {
        Path schoolPath = getStaticResources().resolve("school.png");
        assertTrue(PathRule.isImageContent().test(schoolPath));
    }

    @Test
    void testIsImageContentFalse() {
        Path schoolPath = getStaticResources().resolve("school.foo");
        assertFalse(PathRule.isImageContent().test(schoolPath));
    }

    @Test
    void testIsMinimumBytesTrue() {
        Path bauerPath = getStaticResources().resolve("bauerlite.jpg");
        assertTrue(PathRule.minimumBytes(100L).test(bauerPath));
    }

    @Test
    void testIsMinimumBytesFalse() {
        Path bauerPath = getStaticResources().resolve("bauerlite.foo");
        assertFalse(PathRule.minimumBytes(100L).test(bauerPath));
    }
}
