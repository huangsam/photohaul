package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.Nullable;

import java.nio.file.CopyOption;
import java.nio.file.Path;

public class FlatPathMigrator extends PathMigrator {
    public FlatPathMigrator(Path targetRoot, CopyOption copyOption) {
        super(targetRoot, copyOption);
    }

    @Override
    @Nullable Path getTargetLocation(Photo photo) {
        return targetRoot;
    }
}
