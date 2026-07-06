package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.model.PhotoMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPhotoResolver extends TestResolutionAbstract {
    @Test
    void testResolveListOnMakeYear() {
        List<String> resolvedList = getPhotoResolver().resolveList(getBauerPhoto());
        assertEquals(2, resolvedList.size());
        assertEquals("Canon", resolvedList.getFirst());
        assertEquals("2023", resolvedList.get(1));
    }

    @Test
    void testResolveStringOnMakeYearWithDefaultDelimiter() {
        String resolvedString = getPhotoResolver().resolveString(getBauerPhoto());
        assertEquals("Canon/2023", resolvedString);
    }

    @Test
    void testResolveStringOnMakeYearWithCustomDelimiter() {
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

    @Test
    void testResolverThrowsException() {
        PhotoResolver faultyResolver = new PhotoResolver(List.of(photo -> null));
        assertThrows(ResolutionException.class, () -> faultyResolver.resolveList(getBauerPhoto()));
    }

    @Test
    void testFromPatternValid() {
        PhotoResolver resolver = PhotoResolver.fromPattern("yearTaken/make/model/iso");
        assertEquals(4, resolver.size());

        List<String> resolved = resolver.resolveList(getBauerPhoto());
        assertEquals(4, resolved.size());
        assertEquals("2023", resolved.get(0)); // yearTaken
        assertEquals("Canon", resolved.get(1)); // make
        assertEquals("Canon EOS R6", resolved.get(2)); // model (from test resources)
        assertEquals("1250", resolved.get(3)); // iso
    }

    @Test
    void testFromPatternInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> PhotoResolver.fromPattern("yearTaken/invalidComponent"));
    }

    @Test
    void testFromPatternEmptyFallback() {
        PhotoResolver resolver = PhotoResolver.fromPattern("");
        assertEquals(1, resolver.size());
        List<String> resolved = resolver.resolveList(getBauerPhoto());
        assertEquals("2023", resolved.getFirst());
    }

    @Test
    void testFromSettings() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("folder.structure", "make/model");
        io.huangsam.photohaul.Settings settings = new io.huangsam.photohaul.Settings(props, java.nio.file.FileSystems.getDefault());

        PhotoResolver resolver = PhotoResolver.fromSettings(settings);
        assertEquals(2, resolver.size());

        List<String> resolved = resolver.resolveList(getBauerPhoto());
        assertEquals("Canon", resolved.getFirst());
        assertEquals("Canon EOS R6", resolved.get(1));
    }

    @Test
    void testFromPatternWithMetadataFallbacks() {
        // bauerlite has make (Canon) and taken year (2023)
        PhotoResolver resolver = PhotoResolver.fromPattern("make|yearModified/yearTaken|yearModified");
        List<String> resolved = resolver.resolveList(getBauerPhoto());
        assertEquals("Canon", resolved.getFirst());
        assertEquals("2023", resolved.get(1));

        // For an empty photo, should resolve to default fallback (Other)
        Photo emptyPhoto = new Photo(java.nio.file.Paths.get("dummy.jpg"), io.huangsam.photohaul.model.PhotoMetadata.EMPTY);
        List<String> emptyResolved = resolver.resolveList(emptyPhoto);
        assertEquals("Other", emptyResolved.getFirst());
        assertEquals("Other", emptyResolved.get(1));
    }

    @Test
    void testFromPatternWithLiteralFallbacks() {
        PhotoResolver resolver = PhotoResolver.fromPattern("make|Unknown/iso|999");
        
        // Bauer photo has make and iso
        List<String> resolved = resolver.resolveList(getBauerPhoto());
        assertEquals("Canon", resolved.getFirst());
        assertEquals("1250", resolved.get(1));

        // Empty photo should resolve to literals
        Photo emptyPhoto = new Photo(java.nio.file.Paths.get("dummy.jpg"), io.huangsam.photohaul.model.PhotoMetadata.EMPTY);
        List<String> emptyResolved = resolver.resolveList(emptyPhoto);
        assertEquals("Unknown", emptyResolved.getFirst());
        assertEquals("999", emptyResolved.get(1));
    }

    @Test
    void testFromPatternWithCustomDefaultFallback() {
        PhotoResolver resolver = PhotoResolver.fromPattern("make/iso", "Unsorted");
        Photo emptyPhoto = new Photo(java.nio.file.Paths.get("dummy.jpg"), io.huangsam.photohaul.model.PhotoMetadata.EMPTY);
        List<String> emptyResolved = resolver.resolveList(emptyPhoto);
        assertEquals("Unsorted", emptyResolved.getFirst());
        assertEquals("Unsorted", emptyResolved.get(1));
    }

    @Test
    void testFromPatternWithInvalidFallbackFirstThrows() {
        assertThrows(IllegalArgumentException.class, () -> PhotoResolver.fromPattern("Unknown|make"));
    }

    @Test
    void testResolveTagsPrimary() {
        PhotoMetadata metadata = new PhotoMetadata(null, null, null, null, null, null, null, null, "Wedding, Landscape; Bride");
        Photo photo = new Photo(java.nio.file.Paths.get("dummy.jpg"), () -> metadata);
        PhotoResolver resolver = PhotoResolver.fromPattern("tags");
        List<String> resolved = resolver.resolveList(photo);
        assertEquals("Wedding", resolved.getFirst());
    }

    @Test
    void testResolveTagsFallback() {
        PhotoResolver resolver = PhotoResolver.fromPattern("tags");
        Photo emptyPhoto = new Photo(java.nio.file.Paths.get("dummy.jpg"), io.huangsam.photohaul.model.PhotoMetadata.EMPTY);
        List<String> emptyResolved = resolver.resolveList(emptyPhoto);
        assertEquals("Other", emptyResolved.getFirst());
    }
}
