package io.huangsam.photohaul.migrate;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class CameraPhotoMigrator extends PhotoMigrator {
    public CameraPhotoMigrator(Path targetRoot) {
        super(targetRoot);
    }

    @Override
    Path getTargetLocation(Photo photo) {
        LocalDateTime takenTime = photo.takenAt();
        if (takenTime != null) {
            return getTargetPath(String.valueOf(takenTime.getYear()));
        }
        return getFallbackPath();
    }
}
