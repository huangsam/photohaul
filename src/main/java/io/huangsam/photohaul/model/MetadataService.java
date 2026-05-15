package io.huangsam.photohaul.model;

import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Service for managing photo metadata extraction.
 */
public class MetadataService {
    private final MetadataExtractor extractor;

    public MetadataService() {
        this(new ExifMetadataExtractor());
    }

    public MetadataService(@NonNull MetadataExtractor extractor) {
        this.extractor = extractor;
    }

    /**
     * Create a supplier for lazy metadata extraction.
     *
     * @param path The path to the photo file.
     * @return A supplier that will extract metadata when called.
     */
    public @NonNull Supplier<PhotoMetadata> getSupplier(@NonNull Path path) {
        return () -> extractor.extract(path);
    }
}
