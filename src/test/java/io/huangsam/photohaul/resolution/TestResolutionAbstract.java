package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.traversal.PhotoPathBuilder;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getStaticResources;

public abstract class TestResolutionAbstract {
    private static final Photo BAUER_PHOTO = buildBauerPhoto();

    private static Photo buildBauerPhoto() {
        Path photoPath = getStaticResources().resolve("bauerlite.jpg");
        PhotoPathBuilder pb = new PhotoPathBuilder();
        pb.fillInfo(photoPath);
        return pb.build();
    }

    static Photo getBauerPhoto() {
        return BAUER_PHOTO;
    }

    @NotNull
    static PhotoResolver getPhotoResolver() {
        return new PhotoResolver(List.of(Photo::make, PhotoFunction.yearTaken()));
    }
}
