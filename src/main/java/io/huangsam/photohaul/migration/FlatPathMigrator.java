package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FlatPathMigrator extends PathMigrator {
    public FlatPathMigrator(Path targetRoot) {
        super(targetRoot, StandardCopyOption.REPLACE_EXISTING);
    }

    public FlatPathMigrator(Path targetRoot, CopyOption copyOption) {
        super(targetRoot, copyOption);
    }

    @Override
    Path getTargetLocation(Photo photo) {
        return targetRoot;
    }
}
