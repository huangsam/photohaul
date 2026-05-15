package io.huangsam.photohaul.model;

import java.time.LocalDateTime;

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
 */
public record PhotoMetadata(
    LocalDateTime takenAt,
    String make,
    String model,
    String focalLength,
    String shutterSpeed,
    String aperture,
    String flash
) {
    public static final PhotoMetadata EMPTY = new PhotoMetadata(null, null, null, null, null, null, null);
}
