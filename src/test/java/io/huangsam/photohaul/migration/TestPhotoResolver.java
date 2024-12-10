package io.huangsam.photohaul.migration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPhotoResolver extends TestPhotoBase {
    @Test
    void testResolveListOnMakeYear() {
        List<String> resolvedList = getPhotoResolver().resolveList(BAUER_PHOTO);
        assertEquals(2, resolvedList.size());
        assertEquals("Canon", resolvedList.get(0));
        assertEquals("2023", resolvedList.get(1));
    }

    @Test
    void testResolveStringOnMakeYearDefault() {
        String resolvedString = getPhotoResolver().resolveString(BAUER_PHOTO);
        assertEquals("Canon/2023", resolvedString);
    }

    @Test
    void testResolveStringOnMakeYearCustom() {
        String resolvedString = getPhotoResolver().resolveString(BAUER_PHOTO, " - ");
        assertEquals("Canon - 2023", resolvedString);
    }

    @Test
    void testDefaultResolverIsNotEmpty() {
        assertTrue(PhotoResolver.getDefault().size() > 0);
    }
}
