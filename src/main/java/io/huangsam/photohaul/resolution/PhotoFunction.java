package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

public class PhotoFunction {
    @NotNull
    public static Function<Photo, String> aperture() {
        return Photo::aperture;
    }

    @NotNull
    public static Function<Photo, String> flash() {
        return Photo::flash;
    }

    @NotNull
    public static Function<Photo, String> focalLength() {
        return Photo::focalLength;
    }

    @NotNull
    public static Function<Photo, String> make() {
        return Photo::make;
    }

    @NotNull
    public static Function<Photo, String> model() {
        return Photo::model;
    }

    @NotNull
    public static Function<Photo, String> shutterSpeed() {
        return Photo::shutterSpeed;
    }

    @NotNull
    public static Function<Photo, String> yearModified() {
        return photo -> {
            FileTime modifiedTime = photo.modifiedAt();
            return (modifiedTime == null)
                    ? null
                    : String.valueOf(modifiedTime.toInstant().atZone(ZoneId.systemDefault()).getYear());
        };
    }

    @NotNull
    public static Function<Photo, String> yearTaken() {
        return photo -> {
            LocalDateTime takenTime = photo.takenAt();
            return (takenTime == null)
                    ? null
                    : String.valueOf(takenTime.getYear());
        };
    }
}
