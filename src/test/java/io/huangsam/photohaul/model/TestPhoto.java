package io.huangsam.photohaul.model;

import org.jetbrains.annotations.NotNull;
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

    @NotNull
    private static Photo getPhoto(@NonNull String pathName) {
        return new Photo(Path.of(pathName));
    }
}
