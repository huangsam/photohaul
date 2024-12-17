package io.huangsam.photohaul.resolution;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPhotoResolver extends TestResolutionAbstract {
    @Test
    void testResolveListOnMakeYear() {
        List<String> resolvedList = getPhotoResolver().resolveList(getBauerPhoto());
        assertEquals(2, resolvedList.size());
        assertEquals("Canon", resolvedList.get(0));
        assertEquals("2023", resolvedList.get(1));
    }

    @Test
    void testResolveStringOnMakeYearDefault() {
        String resolvedString = getPhotoResolver().resolveString(getBauerPhoto());
        assertEquals("Canon/2023", resolvedString);
    }

    @Test
    void testResolveStringOnMakeYearCustom() {
        String resolvedString = getPhotoResolver().resolveString(getBauerPhoto(), " - ");
        assertEquals("Canon - 2023", resolvedString);
    }

    @Test
    void testDefaultResolverIsNotEmpty() {
        PhotoResolver defaultResolver = PhotoResolver.getDefault();
        assertTrue(defaultResolver.size() > 0);
    }

    @Test
    void testEmptyResolverIsEmpty() {
        PhotoResolver emptyResolver = new PhotoResolver(List.of());
        List<String> resolvedList = emptyResolver.resolveList(getBauerPhoto());
        assertTrue(resolvedList.isEmpty());
    }
}
