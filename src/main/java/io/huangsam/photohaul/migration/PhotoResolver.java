package io.huangsam.photohaul.migration;

import io.huangsam.photohaul.model.Photo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PhotoResolver {
    private final List<Function<Photo, String>> photoFunctions;

    public PhotoResolver(List<Function<Photo, String>> photoFunctions) {
        this.photoFunctions = photoFunctions;
    }

    public List<String> resolveList(Photo photo) {
        List<String> list = new ArrayList<>();
        for (Function<Photo, String> fn : photoFunctions) {
            String out = fn.apply(photo);
            if (out == null) {
                throw new NullPointerException("Got null output during pattern resolution");
            }
            list.add(out);
        }
        return list;
    }
}
