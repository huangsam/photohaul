package io.huangsam.photohaul;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.LocalDate;

public class YearBasedPhotoMigrator extends PhotoMigrator {
    public YearBasedPhotoMigrator(Path targetRoot) {
        super(targetRoot);
    }

    @Override
    @NotNull Path getTargetLocation(Photo photo) {
        LocalDate photoDate = photo.date();
        if (photoDate == null) {
            return targetRoot;
        }
        String photoYear = String.valueOf(photoDate.getYear());
        return targetRoot.resolve(photoYear);
    }
}
