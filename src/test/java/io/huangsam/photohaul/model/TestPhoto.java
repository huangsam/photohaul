package io.huangsam.photohaul.model;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestPhoto {
    private static final Photo FAKE_PHOTO = getPhoto("someFolder/foobar.jpg");
    private static final Photo REAL_PHOTO = getPhoto("src/test/resources/static/bauerlite.jpg");

    @Test
    void testRealPhotoNameIsBauer() {
        assertEquals("bauerlite.jpg", REAL_PHOTO.name());
    }

    @Test
    void testRealPhotoModifiedAtIsNotNull() {
        assertNotNull(REAL_PHOTO.modifiedAt());
    }

    @Test
    void testFakePhotoNameIsFoobar() {
        assertEquals("foobar.jpg", FAKE_PHOTO.name());
    }

    @Test
    void testFakePhotoModifiedAtIsNull() {
        assertNull(FAKE_PHOTO.modifiedAt());
    }

    @Test
    void testPhotoEqualsAndHashCode() {
        Photo photo1 = getPhoto("someFolder/foobar.jpg");
        Photo photo2 = getPhoto("someFolder/foobar.jpg");
        Photo photo3 = getPhoto("someFolder/diff.jpg");

        assertEquals(photo1, photo1);
        assertEquals(photo1, photo2);
        assertEquals(photo1.hashCode(), photo2.hashCode());
        
        // Test not equals
        org.junit.jupiter.api.Assertions.assertNotEquals(photo1, photo3);
        org.junit.jupiter.api.Assertions.assertNotEquals(photo1, null);
        org.junit.jupiter.api.Assertions.assertNotEquals(photo1, "string");
    }

    @Test
    void testPhotoToString() {
        Photo photo = getPhoto("someFolder/foobar.jpg");
        assertEquals("Photo{path=someFolder/foobar.jpg}", photo.toString());
    }

    @Test
    void testPhotoMetadataDelegates() {
        MetadataService service = new MetadataService();
        Path path = Path.of("src/test/resources/static/bauerlite.jpg");
        Photo photoWithMeta = new Photo(path, service.getSupplier(path));
        assertNotNull(photoWithMeta.metadata());
        assertNotNull(photoWithMeta.taken());
        assertEquals("Canon", photoWithMeta.make());
    }

    @NonNull
    private static Photo getPhoto(@NonNull String pathName) {
        return new Photo(Path.of(pathName));
    }
}
