package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.resolution.PhotoResolver;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getTempResources;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPathMigrator extends TestMigrationAbstract {
    @Test
    void testMigratePhotosDryRunAllSuccess() throws Exception {
        Migrator migrator = getPathMover(getTempResources(), PathMigrator.Action.DRY_RUN);
        run(migrator);

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosCopyAllSuccess() throws Exception {
        Migrator migrator = getPathMover(getTempResources(), PathMigrator.Action.COPY);
        run(migrator);

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosMoveAllFailure() throws Exception {
        Migrator migrator = getPathMover(getTempResources(), PathMigrator.Action.MOVE);
        run(migrator, List.of("foobar.jpg"));

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(1, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @NotNull
    private static PathMigrator getPathMover(Path destination, PathMigrator.Action action) {
        return new PathMigrator(destination, new PhotoResolver(List.of()), action);
    }
}
