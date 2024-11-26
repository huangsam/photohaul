package io.huangsam.photohaul.migrate;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.Path;
import java.time.LocalDate;

public class YearPhotoMigrator extends PhotoMigrator {
    public YearPhotoMigrator(Path targetRoot) {
        super(targetRoot);
    }

    @Override
    Path getTargetLocation(Photo photo) {
        LocalDate photoDate = photo.date();
        if (photoDate == null) {
            return targetRoot.resolve("Other");
        }
        String photoYear = String.valueOf(photoDate.getYear());
        return targetRoot.resolve(photoYear);
    }
}
