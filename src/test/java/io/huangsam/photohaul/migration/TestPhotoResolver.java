package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.huangsam.photohaul.migration.TestPhotoBase.BAUER_PHOTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPhotoResolver {
    public static final PhotoResolver RESOLVER = new PhotoResolver(
            List.of(Photo::make, PhotoFunction.yearTaken()));

    @Test
    void testResolveListOnMakeYear() {
        List<String> resolvedList = RESOLVER.resolveList(BAUER_PHOTO);
        assertEquals(2, resolvedList.size());
        assertEquals("Canon", resolvedList.get(0));
        assertEquals("2023", resolvedList.get(1));
    }

    @Test
    void testResolveStringOnMakeYearDefault() {
        String resolvedString = RESOLVER.resolveString(BAUER_PHOTO);
        assertEquals("Canon/2023", resolvedString);
    }

    @Test
    void testResolveStringOnMakeYearCustom() {
        String resolvedString = RESOLVER.resolveString(BAUER_PHOTO, " - ");
        assertEquals("Canon - 2023", resolvedString);
    }
}
