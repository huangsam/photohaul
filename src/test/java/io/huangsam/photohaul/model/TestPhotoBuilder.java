package io.huangsam.photohaul.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPhotoBuilder {
    @Test
    void testFillInfoAndBuild() {
        String expected = "bauerlite.jpg";
        Path bauerPath = getStaticResources().resolve(expected);

        PhotoBuilder pb = new PhotoBuilder();
        pb.fillInfo(bauerPath);
        Photo photo = pb.build();

        assertEquals(bauerPath, photo.path());
        assertEquals(expected, photo.name());
        assertNotNull(photo.make());
        assertNotNull(photo.focalLength());
        assertNotNull(photo.shutterSpeed());
        assertNotNull(photo.aperture());
    }
}
