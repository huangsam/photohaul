package io.huangsam.photohaul.resolution;

import io.huangsam.photohaul.model.Photo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PhotoResolver {
    private final List<Function<Photo, String>> photoFunctions;

    @NotNull
    public static PhotoResolver getDefault() {
        return new PhotoResolver(List.of(PhotoFunction.yearTaken()));
    }

    public PhotoResolver(List<Function<Photo, String>> photoFunctions) {
        this.photoFunctions = photoFunctions;
    }

    public List<String> resolveList(Photo photo) {
        List<String> list = new ArrayList<>();
        for (Function<Photo, String> fn : photoFunctions) {
            String out = fn.apply(photo);
            if (out == null) {
                throw new NullPointerException("Got null while resolving " + photo.name());
            }
            list.add(out);
        }
        return list;
    }

    public String resolveString(Photo photo, String delimiter) {
        return String.join(delimiter, resolveList(photo));
    }

    public String resolveString(Photo photo) {
        return resolveString(photo, "/");
    }

    public int size() {
        return photoFunctions.size();
    }
}
