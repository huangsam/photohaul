package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.TestPathBase;
import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.traversal.PhotoPathBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPhotoResolver extends TestPathBase {
    private static Photo BAUER_PHOTO;

    @BeforeAll
    static void setUp() {
        Path bauerPath = getStaticResources().resolve("bauerlite.jpg");
        PhotoPathBuilder pb = new PhotoPathBuilder();
        pb.fillInfo(bauerPath);
        BAUER_PHOTO = pb.build();
    }

    @Test
    void testResolveListOnMakeThenYear() {
        PhotoResolver photoResolver = new PhotoResolver(List.of(Photo::make, PhotoFunction.yearTaken()));
        List<String> resolvedList = photoResolver.resolveList(BAUER_PHOTO);
        assertEquals(2, resolvedList.size());
        assertEquals("Canon", resolvedList.get(0));
        assertEquals("2023", resolvedList.get(1));
    }
}
