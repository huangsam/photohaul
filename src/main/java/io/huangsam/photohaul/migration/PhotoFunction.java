package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

public class PhotoFunction {
    public static Function<Photo, String> yearTaken() {
        return photo -> {
            LocalDateTime takenTime = photo.takenAt();
            return (takenTime == null) ? null : String.valueOf(takenTime.getYear());
        };
    }

    public static Function<Photo, String> yearModified() {
        return photo -> {
            FileTime modifiedTime = photo.modifiedAt();
            return (modifiedTime == null)
                    ? null : String.valueOf(modifiedTime.toInstant().atZone(ZoneId.systemDefault()).getYear());
        };
    }
}
