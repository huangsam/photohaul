package io.huangsam.photohaul.visit;

import io.huangsam.photohaul.model.Photo;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

public class PhotoPathVisitor {
    private static final Logger LOG = getLogger(PhotoPathVisitor.class);

    private final ConcurrentHashMap<Path, Photo> photoIndex = new ConcurrentHashMap<>();

    public Collection<Photo> getPhotos() {
        return photoIndex.values();
    }

    public void visitPhoto(Path path) {
        LOG.trace("Visit photo {}", path.toString());
        PhotoPathBuilder pb = new PhotoPathBuilder();
        pb.fillInfo(path);
        photoIndex.put(path, pb.build());
    }
}
