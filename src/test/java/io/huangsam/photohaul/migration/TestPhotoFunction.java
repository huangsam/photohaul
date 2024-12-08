package io.huangsam.photohaul.migration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPhotoFunction extends TestPhotoBase {
    @Test
    void testYearTaken() {
        assertEquals("2023", PhotoFunction.yearTaken().apply(BAUER_PHOTO));
    }

    @Test
    void testYearModified() {
        assertNotNull(PhotoFunction.yearModified().apply(BAUER_PHOTO));
    }
}
