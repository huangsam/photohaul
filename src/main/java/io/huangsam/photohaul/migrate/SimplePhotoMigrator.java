package io.huangsam.photohaul.migrate;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Year;
import java.time.ZoneId;

public class SimplePhotoMigrator extends PhotoMigrator {
    public SimplePhotoMigrator(Path targetRoot) {
        super(targetRoot);
    }

    @Override
    Path getTargetLocation(Photo photo) {
        FileTime creationTime = photo.createdAt();
        if (creationTime != null) {
            return targetRoot.resolve(parseYear(creationTime).toString());
        }
        FileTime modifiedTime = photo.modifiedAt();
        if (modifiedTime != null) {
            return targetRoot.resolve(parseYear(modifiedTime).toString());
        }
        return fallback();
    }

    private Year parseYear(FileTime time) {
        return Year.from(time.toInstant().atZone(ZoneId.systemDefault()));
    }
}
