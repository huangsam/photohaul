package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record PhotoResolver(List<Function<Photo, String>> photoFunctions) {
    /**
     * Get default photo resolver with year-based resolution.
     *
     * @return default photo resolver
     */
    @NotNull
    public static PhotoResolver getDefault() {
        return new PhotoResolver(List.of(PhotoFunction.yearTaken()));
    }

    /**
     * Resolve photo to a list of path components.
     *
     * @param photo photo to resolve
     * @return list of path components
     */
    public @NonNull List<String> resolveList(@NonNull Photo photo) {
        List<String> list = new ArrayList<>();
        for (Function<Photo, String> fn : photoFunctions) {
            String out = fn.apply(photo);
            if (out == null) {
                throw new ResolutionException("Got null while resolving " + photo.name());
            }
            list.add(out);
        }
        return list;
    }

    /**
     * Resolve photo to a string path with delimiter.
     *
     * @param photo photo to resolve
     * @param delimiter delimiter for joining components
     * @return resolved path string
     */
    public @NonNull String resolveString(@NonNull Photo photo, @NonNull String delimiter) {
        return String.join(delimiter, resolveList(photo));
    }

    /**
     * Resolve photo to a string path with default delimiter "/".
     *
     * @param photo photo to resolve
     * @return resolved path string
     */
    public @NonNull String resolveString(@NonNull Photo photo) {
        return resolveString(photo, "/");
    }

    /**
     * Get number of resolution functions.
     *
     * @return number of functions
     */
    public int size() {
        return photoFunctions.size();
    }
}
