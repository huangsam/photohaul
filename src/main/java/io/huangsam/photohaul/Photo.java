package io.huangsam.photohaul;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Photo(
        Path path,
        @Nullable String dateTime,
        @Nullable String make,
        @Nullable String model,
        @Nullable String focalLength,
        @Nullable String shutterSpeed,
        @Nullable String aperture,
        @Nullable String flash
) {
    public String name() {
        return path.getFileName().toString();
    }

    public @Nullable LocalDate date() {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
        return localDateTime.toLocalDate();
    }
}
