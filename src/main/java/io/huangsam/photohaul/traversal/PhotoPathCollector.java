package io.huangsam.photohaul.traversal;

import io.huangsam.photohaul.model.Photo;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class PhotoPathCollector {
    private final ConcurrentHashMap<Path, Photo> photoIndex = new ConcurrentHashMap<>();

    public Collection<Photo> getPhotos() {
        return photoIndex.values();
    }

    public void addPhoto(Path path) {
        PhotoPathBuilder pb = new PhotoPathBuilder();
        pb.fillInfo(path);
        photoIndex.put(path, pb.build());
    }
}
