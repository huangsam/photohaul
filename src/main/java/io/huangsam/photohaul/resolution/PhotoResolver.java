package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.Settings;
import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record PhotoResolver(List<Function<Photo, String>> photoFunctions) {
    private static final Map<String, Function<Photo, String>> COMPONENT_MAP;

    static {
        Map<String, Function<Photo, String>> map = new HashMap<>();
        map.put("yearTaken", PhotoFunction.yearTaken());
        map.put("yearModified", PhotoFunction.yearModified());
        map.put("make", PhotoFunction.make());
        map.put("model", PhotoFunction.model());
        map.put("focalLength", PhotoFunction.focalLength());
        map.put("shutterSpeed", PhotoFunction.shutterSpeed());
        map.put("aperture", PhotoFunction.aperture());
        map.put("flash", PhotoFunction.flash());
        map.put("iso", PhotoFunction.iso());
        COMPONENT_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Get default photo resolver with year-based resolution.
     *
     * @return default photo resolver
     */
    @NonNull
    public static PhotoResolver getDefault() {
        return new PhotoResolver(List.of(PhotoFunction.yearTaken()));
    }

    /**
     * Create a photo resolver from Settings.
     *
     * @param settings settings instance
     * @return photo resolver matching settings configuration
     */
    @NonNull
    public static PhotoResolver fromSettings(@NonNull Settings settings) {
        return fromPattern(settings.getFolderStructure());
    }

    /**
     * Parse folder structure pattern and construct a photo resolver.
     *
     * @param pattern pattern representing nested components separated by slash (e.g. "yearTaken/make")
     * @return parsed photo resolver
     * @throws IllegalArgumentException if pattern contains unsupported components
     */
    @NonNull
    public static PhotoResolver fromPattern(@NonNull String pattern) {
        String[] parts = pattern.split("/");
        List<Function<Photo, String>> functions = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Function<Photo, String> fn = COMPONENT_MAP.get(trimmed);
            if (fn == null) {
                throw new IllegalArgumentException("Unsupported folder structure component: " + trimmed);
            }
            functions.add(fn);
        }
        if (functions.isEmpty()) {
            functions.add(PhotoFunction.yearTaken());
        }
        return new PhotoResolver(functions);
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
