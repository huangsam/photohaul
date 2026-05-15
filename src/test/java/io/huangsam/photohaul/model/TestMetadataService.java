package io.huangsam.photohaul.model;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMetadataService {
    @Test
    void testGetSupplier(@TempDir @NonNull Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "fake content".getBytes());

        MetadataService service = new MetadataService();
        Supplier<PhotoMetadata> supplier = service.getSupplier(testFile);

        assertNotNull(supplier);
        PhotoMetadata metadata = supplier.get();
        assertNotNull(metadata);
        // ExifMetadataExtractor returns EMPTY for non-image files or errors
        assertEquals(PhotoMetadata.EMPTY, metadata);
    }

    @Test
    void testPhotoLazyLoadingWithService(@TempDir @NonNull Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "fake content".getBytes());

        MetadataService service = new MetadataService();
        Photo photo = new Photo(testFile, service.getSupplier(testFile));

        // Metadata should not be loaded yet
        // (We can't easily check this without a mock, but we can verify it works)
        assertNotNull(photo.metadata());
        assertEquals(PhotoMetadata.EMPTY, photo.metadata());
    }
}
