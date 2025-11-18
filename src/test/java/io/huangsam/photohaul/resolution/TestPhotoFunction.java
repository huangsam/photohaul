package io.huangsam.photohaul.resolution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPhotoFunction extends TestResolutionAbstract {
    @Test
    void testAperture() {
        assertNotNull(PhotoFunction.aperture().apply(getBauerPhoto()));
    }

    @Test
    void testFlash() {
        assertNotNull(PhotoFunction.flash().apply(getBauerPhoto()));
    }

    @Test
    void testFocalLength() {
        assertNotNull(PhotoFunction.focalLength().apply(getBauerPhoto()));
    }

    @Test
    void testMake() {
        assertNotNull(PhotoFunction.make().apply(getBauerPhoto()));
    }

    @Test
    void testModel() {
        assertNotNull(PhotoFunction.model().apply(getBauerPhoto()));
    }

    @Test
    void testShutterSpeed() {
        assertNotNull(PhotoFunction.shutterSpeed().apply(getBauerPhoto()));
    }

    @Test
    void testYearModified() {
        assertNotNull(PhotoFunction.yearModified().apply(getBauerPhoto()));
    }

    @Test
    void testYearTaken() {
        assertEquals("2023", PhotoFunction.yearTaken().apply(getBauerPhoto()));
    }
}
