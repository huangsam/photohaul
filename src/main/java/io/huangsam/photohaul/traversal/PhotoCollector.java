package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class PhotoCollector {
    private final ConcurrentHashMap<Path, Photo> photoIndex = new ConcurrentHashMap<>();

    public @NonNull Collection<Photo> getPhotos() {
        return photoIndex.values();
    }

    public void addPhoto(@NonNull Path path) {
        photoIndex.put(path, new Photo(path));
    }
}
