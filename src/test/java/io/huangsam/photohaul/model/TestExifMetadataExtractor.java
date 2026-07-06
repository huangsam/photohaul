package io.huangsam.photohaul.model;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.huangsam.photohaul.TestHelper.getStaticResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestExifMetadataExtractor {

    @Test
    void testExtractXmpSidecarReplaceExt(@TempDir @NonNull Path tempDir) throws IOException {
        Path imageFile = tempDir.resolve("bauerlite.jpg");
        Files.copy(getStaticResources().resolve("bauerlite.jpg"), imageFile);

        // Sidecar 1: bauerlite.xmp (replace extension)
        Path xmpFile = tempDir.resolve("bauerlite.xmp");
        String xmpContent = """
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
             <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/">
               <dc:subject>
                <rdf:Bag>
                 <rdf:li>wedding</rdf:li>
                 <rdf:li>bride</rdf:li>
                </rdf:Bag>
               </dc:subject>
              </rdf:Description>
             </rdf:RDF>
            </x:xmpmeta>
            """;
        Files.write(xmpFile, xmpContent.getBytes());

        ExifMetadataExtractor extractor = new ExifMetadataExtractor();
        PhotoMetadata metadata = extractor.extract(imageFile);

        assertNotNull(metadata);
        assertEquals("wedding, bride", metadata.tags());
    }

    @Test
    void testExtractXmpSidecarAppendExt(@TempDir @NonNull Path tempDir) throws IOException {
        Path imageFile = tempDir.resolve("bauerlite.jpg");
        Files.copy(getStaticResources().resolve("bauerlite.jpg"), imageFile);

        // Sidecar 2: bauerlite.jpg.xmp (append extension)
        Path xmpFile = tempDir.resolve("bauerlite.jpg.xmp");
        String xmpContent = """
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
             <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/">
               <dc:subject>
                <rdf:Bag>
                 <rdf:li>nature</rdf:li>
                </rdf:Bag>
               </dc:subject>
              </rdf:Description>
             </rdf:RDF>
            </x:xmpmeta>
            """;
        Files.write(xmpFile, xmpContent.getBytes());

        ExifMetadataExtractor extractor = new ExifMetadataExtractor();
        PhotoMetadata metadata = extractor.extract(imageFile);

        assertNotNull(metadata);
        assertEquals("nature", metadata.tags());
    }

    @Test
    void testExtractXmpMalformedXml(@TempDir @NonNull Path tempDir) throws IOException {
        Path imageFile = tempDir.resolve("bauerlite.jpg");
        Files.copy(getStaticResources().resolve("bauerlite.jpg"), imageFile);

        Path xmpFile = tempDir.resolve("bauerlite.xmp");
        Files.write(xmpFile, "malformed xml <dc:subject>".getBytes());

        ExifMetadataExtractor extractor = new ExifMetadataExtractor();
        PhotoMetadata metadata = extractor.extract(imageFile);

        assertNotNull(metadata);
        assertNull(metadata.tags());
    }
}
