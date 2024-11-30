package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.TestPathBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathRule extends TestPathBase {
    @Test
    void testIsValidExtension() {
        Path saladPhoto = getStaticResources().resolve("salad.jpg");
        assertTrue(PathRule.validExtensions().test(saladPhoto));
    }

    @Test
    void testIsImageContent() {
        Path schoolPhoto = getStaticResources().resolve("school.png");
        assertTrue(PathRule.isImageContent().test(schoolPhoto));
    }

    @Test
    void testIsMinimumBytes() {
        Path bauerPhoto = getStaticResources().resolve("bauerlite.jpg");
        assertTrue(PathRule.minimumBytes(100L).test(bauerPhoto));
    }
}
