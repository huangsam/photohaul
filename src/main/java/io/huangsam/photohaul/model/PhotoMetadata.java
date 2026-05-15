package io.huangsam.photohaul.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Structured metadata for a photo.
 *
 * @param takenAt      The date and time the photo was taken.
 * @param make         The camera make.
 * @param model        The camera model.
 * @param focalLength  The focal length used.
 * @param shutterSpeed The shutter speed used.
 * @param aperture     The aperture used.
 * @param flash        The flash setting used.
 * @param iso          The ISO speed rating.
 */
public record PhotoMetadata(
    LocalDateTime takenAt,
    String make,
    String model,
    String focalLength,
    String shutterSpeed,
    String aperture,
    String flash,
    String iso
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    public static final PhotoMetadata EMPTY = new PhotoMetadata(null, null, null, null, null, null, null, null);

    /**
     * Format the takenAt date as a string.
     *
     * @return formatted date string or null if takenAt is null
     */
    public String formatTakenAt() {
        return (takenAt == null) ? null : takenAt.format(DATE_FORMATTER);
    }
}
