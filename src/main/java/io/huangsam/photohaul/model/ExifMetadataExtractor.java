package io.huangsam.photohaul.model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts metadata using the metadata-extractor library.
 */
public class ExifMetadataExtractor implements MetadataExtractor {
    private static final String TAKEN_KEY_ORIGINAL = "Date/Time Original";
    private static final String TAKEN_KEY_DIGITIZED = "Date/Time Digitized";
    private static final String TAKEN_KEY_BASE = "Date/Time";

    private static final String MAKE_KEY = "Make";
    private static final String MODEL_KEY = "Model";
    private static final String FOCAL_LENGTH_KEY = "Focal Length";
    private static final String SHUTTER_SPEED_KEY = "Shutter Speed Value";
    private static final String APERTURE_KEY = "Aperture Value";
    private static final String FLASH_KEY = "Flash";
    private static final String ISO_KEY = "ISO Speed Ratings";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    @Override
    public @NonNull PhotoMetadata extract(@NonNull Path path) {
        Map<String, String> tags = new HashMap<>();
        try (InputStream input = Files.newInputStream(path)) {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    tags.put(tag.getTagName(), tag.getDescription());
                }
            }
        } catch (IOException | ImageProcessingException e) {
            return PhotoMetadata.EMPTY;
        }

        String takenAtStr = tags.get(TAKEN_KEY_ORIGINAL);
        if (takenAtStr == null) {
            takenAtStr = tags.get(TAKEN_KEY_DIGITIZED);
        }
        if (takenAtStr == null) {
            takenAtStr = tags.get(TAKEN_KEY_BASE);
        }

        return new PhotoMetadata(
            parseDateTime(takenAtStr),
            tags.get(MAKE_KEY),
            tags.get(MODEL_KEY),
            tags.get(FOCAL_LENGTH_KEY),
            tags.get(SHUTTER_SPEED_KEY),
            tags.get(APERTURE_KEY),
            tags.get(FLASH_KEY),
            tags.get(ISO_KEY),
            resolveTags(path, tags)
        );
    }

    private @Nullable String resolveTags(@NonNull Path path, @NonNull Map<String, String> tags) {
        // First try to load from sidecar XMP
        String tagsVal = null;
        Path sidecarPath = getSidecarPath(path);
        if (sidecarPath != null) {
            tagsVal = parseXmpTags(sidecarPath);
        }

        // If not found in sidecar, check embedded tags
        if (tagsVal == null || tagsVal.isBlank()) {
            tagsVal = tags.get("Keywords");
            if (tagsVal == null || tagsVal.isBlank()) {
                tagsVal = tags.get("Subject");
            }
        }
        return tagsVal;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private @Nullable Path getSidecarPath(@NonNull Path photoPath) {
        Path parent = photoPath.getParent();
        String fileName = photoPath.getFileName().toString();

        // 1. replace extension
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            String nameWithoutExt = fileName.substring(0, lastDot) + ".xmp";
            Path p1 = (parent != null) ? parent.resolve(nameWithoutExt) : photoPath.getFileSystem().getPath(nameWithoutExt);
            if (Files.exists(p1)) {
                return p1;
            }
        }

        // 2. append extension
        String nameWithXmp = fileName + ".xmp";
        Path p2 = (parent != null) ? parent.resolve(nameWithXmp) : photoPath.getFileSystem().getPath(nameWithXmp);
        if (Files.exists(p2)) {
            return p2;
        }
        return null;
    }

    private @Nullable String parseXmpTags(@NonNull Path xmpPath) {
        try (InputStream in = Files.newInputStream(xmpPath)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            NodeList list = doc.getElementsByTagNameNS("*", "li");
            List<String> keywords = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (isSubjectNode(node)) {
                    keywords.add(node.getTextContent().trim());
                }
            }
            if (!keywords.isEmpty()) {
                return String.join(", ", keywords);
            }
        } catch (Exception e) {
            // Ignore XML parsing errors and return null
        }
        return null;
    }

    private boolean isSubjectNode(@NonNull Node node) {
        Node parent = node.getParentNode();
        if (parent == null) {
            return false;
        }
        String parentLocal = parent.getLocalName();
        if (parentLocal == null || (!"Bag".equals(parentLocal) && !"Seq".equals(parentLocal) && !"Alt".equals(parentLocal))) {
            return false;
        }
        Node grandparent = parent.getParentNode();
        if (grandparent == null) {
            return false;
        }
        String grandparentLocal = grandparent.getLocalName();
        return "subject".equals(grandparentLocal);
    }
}
