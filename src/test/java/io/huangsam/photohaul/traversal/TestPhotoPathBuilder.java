package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.model.Photo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPhotoPathBuilder extends TestPathBase {
    @Test
    void testFillInfoAndBuild() {
        String expected = "bauerlite.jpg";
        Path bauerPhoto = getStaticResources().resolve(expected);

        PhotoPathBuilder pathBuilder = new PhotoPathBuilder();
        pathBuilder.fillInfo(bauerPhoto);
        Photo photo = pathBuilder.build();

        assertEquals(bauerPhoto, photo.path());
        assertEquals(expected, photo.name());
        assertNotNull(photo.make());
        assertNotNull(photo.focalLength());
        assertNotNull(photo.shutterSpeed());
        assertNotNull(photo.aperture());
    }
}
