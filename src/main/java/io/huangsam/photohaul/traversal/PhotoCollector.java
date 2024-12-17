package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.model.Photo;
import io.huangsam.photohaul.model.PhotoBuilder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class PhotoCollector {
    private final ConcurrentHashMap<Path, Photo> photoIndex = new ConcurrentHashMap<>();

    public Collection<Photo> getPhotos() {
        return photoIndex.values();
    }

    public void addPhoto(Path path) {
        PhotoBuilder pb = new PhotoBuilder();
        photoIndex.put(path, pb.fill(path).build());
    }
}
