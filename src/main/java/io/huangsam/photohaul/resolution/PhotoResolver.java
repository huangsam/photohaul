package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record PhotoResolver(List<Function<Photo, String>> photoFunctions) {
    @NotNull
    public static PhotoResolver getDefault() {
        return new PhotoResolver(List.of(PhotoFunction.yearTaken()));
    }

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

    public @NonNull String resolveString(@NonNull Photo photo, @NonNull String delimiter) {
        return String.join(delimiter, resolveList(photo));
    }

    public @NonNull String resolveString(@NonNull Photo photo) {
        return resolveString(photo, "/");
    }

    public int size() {
        return photoFunctions.size();
    }
}
