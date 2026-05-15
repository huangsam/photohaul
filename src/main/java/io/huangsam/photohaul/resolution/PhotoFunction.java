package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

public class PhotoFunction {
    /**
     * Create function to extract aperture metadata.
     *
     * @return function for aperture
     */
    @NonNull
    public static Function<Photo, String> aperture() {
        return Photo::aperture;
    }

    /**
     * Create function to extract flash metadata.
     *
     * @return function for flash
     */
    @NonNull
    public static Function<Photo, String> flash() {
        return Photo::flash;
    }

    /**
     * Create function to extract focal length metadata.
     *
     * @return function for focal length
     */
    @NonNull
    public static Function<Photo, String> focalLength() {
        return Photo::focalLength;
    }

    /**
     * Create function to extract camera make metadata.
     *
     * @return function for make
     */
    @NonNull
    public static Function<Photo, String> make() {
        return Photo::make;
    }

    /**
     * Create function to extract camera model metadata.
     *
     * @return function for model
     */
    @NonNull
    public static Function<Photo, String> model() {
        return Photo::model;
    }

    /**
     * Create function to extract shutter speed metadata.
     *
     * @return function for shutter speed
     */
    @NonNull
    public static Function<Photo, String> shutterSpeed() {
        return Photo::shutterSpeed;
    }

    /**
     * Create function to extract year from modified time.
     *
     * @return function for year modified
     */
    @NonNull
    public static Function<Photo, String> yearModified() {
        return photo -> {
            FileTime modifiedTime = photo.modifiedAt();
            return (modifiedTime == null)
                    ? null
                    : String.valueOf(modifiedTime.toInstant().atZone(ZoneId.systemDefault()).getYear());
        };
    }

    /**
     * Create function to extract year from taken time.
     *
     * @return function for year taken
     */
    @NonNull
    public static Function<Photo, String> yearTaken() {
        return photo -> {
            LocalDateTime takenTime = photo.takenAt();
            return (takenTime == null)
                    ? null
                    : String.valueOf(takenTime.getYear());
        };
    }
}
