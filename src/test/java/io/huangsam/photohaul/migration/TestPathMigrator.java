package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.resolution.PhotoResolver;
import io.huangsam.photohaul.resolution.ResolutionException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static io.huangsam.photohaul.TestHelper.getTempResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestPathMigrator extends TestMigrationAbstract {
    @Mock
    PhotoResolver photoResolverMock;

    @Test
    void testMigratePhotosDryRunAllSuccess() throws Exception {
        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = getPathMover(getTempResources(), photoResolverMock, PathMigrator.Action.DRY_RUN);
        run(migrator);

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosCopyAllSuccess() throws Exception {
        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = getPathMover(getTempResources(), photoResolverMock, PathMigrator.Action.COPY);
        run(migrator);

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosMoveAllFailure() throws Exception {
        when(photoResolverMock.resolveString(any(Photo.class))).thenReturn("some/path");

        Migrator migrator = getPathMover(getTempResources(), photoResolverMock, PathMigrator.Action.MOVE);
        run(migrator, List.of("foobar.jpg"));

        assertEquals(0, migrator.getSuccessCount());
        assertEquals(1, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @Test
    void testMigratePhotosWithResolutionException() throws Exception {
        when(photoResolverMock.resolveString(any(Photo.class))).thenThrow(new ResolutionException("Resolution failed"));

        Migrator migrator = getPathMover(getTempResources(), photoResolverMock, PathMigrator.Action.COPY);
        run(migrator);

        assertEquals(2, migrator.getSuccessCount());
        assertEquals(0, migrator.getFailureCount());

        migrator.close(); // No-op
    }

    @NotNull
    private static PathMigrator getPathMover(Path destination, PhotoResolver resolver, PathMigrator.Action action) {
        return new PathMigrator(destination, resolver, action);
    }
}
