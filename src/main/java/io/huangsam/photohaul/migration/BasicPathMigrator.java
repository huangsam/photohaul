package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.Nullable;

import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;

public class BasicPathMigrator extends PathMigrator {
    public BasicPathMigrator(Path targetRoot, CopyOption copyOption) {
        super(targetRoot, copyOption);
    }

    @Override
    @Nullable Path getTargetLocation(Photo photo) {
        FileTime creationTime = photo.createdAt();
        if (creationTime != null) {
            return targetRoot.resolve(parseYear(creationTime));
        }
        FileTime modifiedTime = photo.modifiedAt();
        if (modifiedTime != null) {
            return targetRoot.resolve(parseYear(modifiedTime));
        }
        return null;
    }

    private String parseYear(FileTime time) {
        return String.valueOf(time.toInstant().atZone(ZoneId.systemDefault()).getYear());
    }
}
