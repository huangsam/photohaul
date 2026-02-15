package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.model.Photo;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class PhotoCollector {
    private final ConcurrentHashMap<Path, Photo> photoIndex = new ConcurrentHashMap<>();

    /**
     * Get all collected photos.
     *
     * @return collection of photos
     */
    public @NonNull Collection<Photo> getPhotos() {
        return photoIndex.values();
    }

    /**
     * Add a photo to the collection.
     *
     * @param path path to the photo file
     */
    public void addPhoto(@NonNull Path path) {
        photoIndex.put(path, new Photo(path));
    }
}
