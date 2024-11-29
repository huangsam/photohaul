package io.huangsam.photohaul.migration.path;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.Nullable;

import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class CameraPathMigrator extends PathMigrator {
    public CameraPathMigrator(Path targetRoot, CopyOption copyOption) {
        super(targetRoot, copyOption);
    }

    @Override
    @Nullable Path getTargetLocation(Photo photo) {
        LocalDateTime takenTime = photo.takenAt();
        if (takenTime != null) {
            return targetRoot.resolve(String.valueOf(takenTime.getYear()));
        }
        return null;
    }
}
