package io.huangsam.photohaul.resolution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPhotoFunction extends TestResolutionAbstract {
    @Test
    void testYearTaken() {
        assertEquals("2023", PhotoFunction.yearTaken().apply(getBauerPhoto()));
    }

    @Test
    void testYearModified() {
        assertNotNull(PhotoFunction.yearModified().apply(getBauerPhoto()));
    }
}
