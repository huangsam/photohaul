package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;

public abstract class TestResolutionAbstract {
    private static final Photo BAUER_PHOTO = buildBauerPhoto();

    private static @NonNull Photo buildBauerPhoto() {
        Path photoPath = getStaticResources().resolve("bauerlite.jpg");
        return new Photo(photoPath);
    }

    @NonNull Photo getBauerPhoto() {
        return BAUER_PHOTO;
    }

    @NonNull PhotoResolver getPhotoResolver() {
        return new PhotoResolver(List.of(Photo::make, PhotoFunction.yearTaken()));
    }
}
