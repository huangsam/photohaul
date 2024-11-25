package io.huangsam.photohaul;

import org.jetbrains.annotations.Nullable;

public record Photo(
        String name,
        @Nullable String dateTime,
        @Nullable String make,
        @Nullable String model,
        @Nullable String focalLength,
        @Nullable String shutterSpeed,
        @Nullable String aperture,
        @Nullable String flash
) {
}
