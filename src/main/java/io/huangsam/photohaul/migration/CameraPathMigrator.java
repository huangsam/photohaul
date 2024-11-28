package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class CameraPathMigrator extends PathMigrator {
    public CameraPathMigrator(Path targetRoot, CopyOption copyOption) {
        super(targetRoot, copyOption);
    }

    @Override
    Path getTargetLocation(Photo photo) {
        LocalDateTime takenTime = photo.takenAt();
        if (takenTime != null) {
            return targetRoot.resolve(String.valueOf(takenTime.getYear()));
        }
        return fallback();
    }
}
