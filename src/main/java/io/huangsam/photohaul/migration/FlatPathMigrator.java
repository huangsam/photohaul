package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.CopyOption;
import java.nio.file.Path;

public class FlatPathMigrator extends PathMigrator {
    public FlatPathMigrator(Path targetRoot, CopyOption copyOption) {
        super(targetRoot, copyOption);
    }

    @Override
    Path getTargetLocation(Photo photo) {
        return targetRoot;
    }
}
