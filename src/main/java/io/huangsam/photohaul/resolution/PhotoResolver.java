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
        map.put("tags", PhotoFunction.tags());
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
        return fromPattern(settings.getFolderStructure(), settings.getFolderFallback());
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
        return fromPattern(pattern, "Other");
    }

    /**
     * Parse folder structure pattern and construct a photo resolver with a custom default fallback.
     *
     * @param pattern         pattern representing nested components separated by slash (e.g. "yearTaken|yearModified/make|Unknown")
     * @param defaultFallback fallback folder name if all options in a component chain resolve to null/blank
     * @return parsed photo resolver
     * @throws IllegalArgumentException if the first option in a component fallback chain is not a valid metadata key
     */
    @NonNull
    public static PhotoResolver fromPattern(@NonNull String pattern, @NonNull String defaultFallback) {
        String[] parts = pattern.split("/");
        List<Function<Photo, String>> functions = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            functions.add(parseComponentChain(trimmed, defaultFallback));
        }
        if (functions.isEmpty()) {
            functions.add(photo -> {
                String res = PhotoFunction.yearTaken().apply(photo);
                return (res != null && !res.isBlank()) ? res : defaultFallback;
            });
        }
        return new PhotoResolver(functions);
    }

    private static @NonNull Function<Photo, String> parseComponentChain(@NonNull String componentPart, @NonNull String defaultFallback) {
        String[] subParts = componentPart.split("\\|");
        List<Function<Photo, String>> chain = new ArrayList<>();

        // The first sub-part must be a valid metadata key
        String firstSub = subParts[0].trim();
        Function<Photo, String> firstFn = COMPONENT_MAP.get(firstSub);
        if (firstFn == null) {
            throw new IllegalArgumentException("Unsupported folder structure component: " + firstSub);
        }
        chain.add(firstFn);

        // Subsequent sub-parts can be metadata keys or literal fallback strings
        for (int i = 1; i < subParts.length; i++) {
            String sub = subParts[i].trim();
            if (sub.isEmpty()) {
                continue;
            }
            Function<Photo, String> fn = COMPONENT_MAP.get(sub);
            if (fn != null) {
                chain.add(fn);
            } else {
                chain.add(photo -> sub);
            }
        }

        return photo -> {
            for (Function<Photo, String> fn : chain) {
                String res = fn.apply(photo);
                if (res != null && !res.isBlank()) {
                    return res;
                }
            }
            return defaultFallback;
        };
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
