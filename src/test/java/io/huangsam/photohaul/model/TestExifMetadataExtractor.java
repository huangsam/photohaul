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

    @Test
    void testExtractNonImageFile(@TempDir @NonNull Path tempDir) throws IOException {
        Path txtFile = tempDir.resolve("sample.txt");
        Files.write(txtFile, "plain text".getBytes());

        ExifMetadataExtractor extractor = new ExifMetadataExtractor();
        PhotoMetadata metadata = extractor.extract(txtFile);

        assertEquals(PhotoMetadata.EMPTY, metadata);
    }

    @Test
    void testPrivateParseDateTime() throws Exception {
        ExifMetadataExtractor extractor = new ExifMetadataExtractor();
        java.lang.reflect.Method method = ExifMetadataExtractor.class.getDeclaredMethod("parseDateTime", String.class);
        method.setAccessible(true);

        // 1. null string input
        Object resultNull = method.invoke(extractor, (String) null);
        assertNull(resultNull);

        // 2. malformed string input (causes exception)
        Object resultMalformed = method.invoke(extractor, "invalid-date-format");
        assertNull(resultMalformed);

        // 3. valid format
        Object resultValid = method.invoke(extractor, "2026:07:06 12:34:56");
        assertNotNull(resultValid);
    }

    @Test
    void testPrivateIsSubjectNode() throws Exception {
        ExifMetadataExtractor extractor = new ExifMetadataExtractor();
        java.lang.reflect.Method method = ExifMetadataExtractor.class.getDeclaredMethod("isSubjectNode", org.w3c.dom.Node.class);
        method.setAccessible(true);

        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document doc = db.newDocument();

        // 1. Node with null parent
        org.w3c.dom.Element liNode1 = doc.createElement("li");
        assertEquals(false, method.invoke(extractor, liNode1));

        // 2. Node where parent localName is null or not Bag/Seq/Alt
        org.w3c.dom.Element parentNotBag = doc.createElement("NotBag");
        parentNotBag.appendChild(liNode1);
        assertEquals(false, method.invoke(extractor, liNode1));

        // 3. Node where grandparent is null
        org.w3c.dom.Element bagNode = doc.createElement("Bag");
        org.w3c.dom.Element liNode2 = doc.createElement("li");
        bagNode.appendChild(liNode2);
        // bagNode has no parent, so grandparent is null
        assertEquals(false, method.invoke(extractor, liNode2));

        // 4. Node where grandparent localName is not subject
        org.w3c.dom.Element grandParentNotSubject = doc.createElement("NotSubject");
        grandParentNotSubject.appendChild(bagNode);
        assertEquals(false, method.invoke(extractor, liNode2));
    }
}
