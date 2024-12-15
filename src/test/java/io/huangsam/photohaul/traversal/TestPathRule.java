package io.huangsam.photohaul.traversal;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRule {
    private static final Path REAL_PATH = getStaticResources().resolve("bauerlite.jpg");
    private static final Path FAKE_PATH = getStaticResources().resolve("bauerlite.foo");

    @Test
    void testIsValidExtensionWithRealIsTrue() {
        assertTrue(PathRule.validExtensions().test(REAL_PATH));
    }

    @Test
    void testIsValidExtensionWithFakeIsFalse() {
        assertFalse(PathRule.validExtensions().test(FAKE_PATH));
    }

    @Test
    void testIsImageContentWithRealIsTrue() {
        assertTrue(PathRule.isImageContent().test(REAL_PATH));
    }

    @Test
    void testIsImageContentWithFakeIsFalse() {
        assertFalse(PathRule.isImageContent().test(FAKE_PATH));
    }

    @Test
    void testIsMinimumBytesWithRealIsTrue() {
        assertTrue(PathRule.minimumBytes(100L).test(REAL_PATH));
    }

    @Test
    void testIsMinimumBytesWithFakeIsFalse() {
        assertFalse(PathRule.minimumBytes(100L).test(FAKE_PATH));
    }

    @Test
    void testIsPublicWithRealIsTrue() {
        assertTrue(PathRule.isPublic().test(REAL_PATH));
    }

    @Test
    void testIsPublicWithFakeIsTrue() {
        assertTrue(PathRule.isPublic().test(FAKE_PATH));
    }
}
