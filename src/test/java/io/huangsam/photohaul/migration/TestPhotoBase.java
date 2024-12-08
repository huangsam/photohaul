package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.traversal.PhotoPathBuilder;
import org.junit.jupiter.api.BeforeAll;

import java.nio.file.Path;

import static io.huangsam.photohaul.TestHelper.getStaticResources;

public abstract class TestPhotoBase {
    static Photo BAUER_PHOTO;

    @BeforeAll
    static void setUp() {
        Path bauerPath = getStaticResources().resolve("bauerlite.jpg");
        PhotoPathBuilder pb = new PhotoPathBuilder();
        pb.fillInfo(bauerPath);
        BAUER_PHOTO = pb.build();
    }
}
