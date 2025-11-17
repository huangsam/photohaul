package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.Path;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;

public abstract class TestResolutionAbstract {
    private static final Photo BAUER_PHOTO = buildBauerPhoto();

    private static Photo buildBauerPhoto() {
        Path photoPath = getStaticResources().resolve("bauerlite.jpg");
        return new Photo(photoPath);
    }

    Photo getBauerPhoto() {
        return BAUER_PHOTO;
    }

    PhotoResolver getPhotoResolver() {
        return new PhotoResolver(List.of(Photo::make, PhotoFunction.yearTaken()));
    }
}
