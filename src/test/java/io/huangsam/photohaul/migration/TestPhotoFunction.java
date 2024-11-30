package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.traversal.PhotoPathBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPhotoFunction extends TestPathBase {
    private static Photo BAUER_PHOTO;

    @BeforeAll
    static void setUp() {
        Path bauerPath = getStaticResources().resolve("bauerlite.jpg");
        PhotoPathBuilder pb = new PhotoPathBuilder();
        pb.fillInfo(bauerPath);
        BAUER_PHOTO = pb.build();
    }

    @Test
    void testYearTaken() {
        assertEquals("2023", PhotoFunction.yearTaken().apply(BAUER_PHOTO));
    }

    @Test
    void testYearModified() {
        assertNotNull(PhotoFunction.yearModified().apply(BAUER_PHOTO));
    }
}
