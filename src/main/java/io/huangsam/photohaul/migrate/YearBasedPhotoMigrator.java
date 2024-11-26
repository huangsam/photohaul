package io.huangsam.photohaul.migrate;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.Path;
import java.time.LocalDate;

public class YearBasedPhotoMigrator extends PhotoMigrator {
    public YearBasedPhotoMigrator(Path targetRoot) {
        super(targetRoot);
    }

    @Override
    Path getTargetLocation(Photo photo) {
        LocalDate photoDate = photo.date();
        if (photoDate == null) {
            return targetRoot;
        }
        String photoYear = String.valueOf(photoDate.getYear());
        return targetRoot.resolve(photoYear);
    }
}
