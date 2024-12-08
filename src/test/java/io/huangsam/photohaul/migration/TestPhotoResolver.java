package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPhotoResolver extends TestPhotoBase {
    @Test
    void testResolveListOnMakeThenYear() {
        PhotoResolver photoResolver = new PhotoResolver(List.of(Photo::make, PhotoFunction.yearTaken()));
        List<String> resolvedList = photoResolver.resolveList(BAUER_PHOTO);
        assertEquals(2, resolvedList.size());
        assertEquals("Canon", resolvedList.get(0));
        assertEquals("2023", resolvedList.get(1));
    }
}
