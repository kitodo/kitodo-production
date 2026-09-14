/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMLSecurityTest {

    private static final String CANARY = "XXE-CANARY-SECRET";
    private static final String VALID_XML = "<document><element>Text</element></document>";

    @TempDir
    Path tempDir;

    @Test
    public void factoriesShouldBeNonNull() throws Exception {
        assertNotNull(XMLSecurity.newDocumentBuilderFactory());
        assertNotNull(XMLSecurity.newTransformerFactory());
        assertNotNull(XMLSecurity.newSchemaFactory());
        assertNotNull(XMLSecurity.newXmlInputFactory());
        assertNotNull(XMLSecurity.newSaxParserFactory());
    }

    @Test
    public void documentBuilderFactoryShouldNotResolveExternalEntities() throws Exception {
        File secret = createTestFile();
        String payload = xxePayload(secret);
        DocumentBuilderFactory factory = XMLSecurity.newDocumentBuilderFactory();
        SAXException exception = assertThrows(SAXException.class,
                () -> parseWithDocumentBuilder(factory, payload));
        assertFalse(exception.getMessage().contains(CANARY), "secret must not leak into the error");
    }

    @Test
    public void documentBuilderFactoryShouldParseValidXml() throws Exception {
        DocumentBuilderFactory factory = XMLSecurity.newDocumentBuilderFactory();
        assertDoesNotThrow(() -> parseWithDocumentBuilder(factory, VALID_XML));
    }

    @Test
    public void saxParserFactoryShouldNotResolveExternalEntities() throws Exception {
        File secret = createTestFile();
        String payload = xxePayload(secret);
        SAXParserFactory factory = XMLSecurity.newSaxParserFactory();
        XMLReader reader = factory.newSAXParser().getXMLReader();
        SAXException exception = assertThrows(SAXException.class,
                () -> reader.parse(new InputSource(toInputStream(payload))));
        assertFalse(exception.getMessage().contains(CANARY), "secret must not leak into the error");
    }

    @Test
    public void secureSourceShouldNotResolveExternalEntities() throws Exception {
        File secret = createTestFile();
        String payload = xxePayload(secret);
        SAXSource source = XMLSecurity.newSecureSource(toInputStream(payload));
        SAXException exception = assertThrows(SAXException.class,
                () -> source.getXMLReader().parse(source.getInputSource()));
        assertFalse(exception.getMessage().contains(CANARY), "secret must not leak into the error");
    }

    @Test
    public void secureSourceShouldParseValidXml() throws Exception {
        SAXSource source = XMLSecurity.newSecureSource(toInputStream(VALID_XML));
        assertDoesNotThrow(() -> source.getXMLReader().parse(source.getInputSource()));
    }

    @Test
    public void xmlInputFactoryShouldNotResolveExternalEntities() throws Exception {
        File secret = createTestFile();
        String payload = xxePayload(secret);
        XMLInputFactory factory = XMLSecurity.newXmlInputFactory();
        XMLStreamException exception = assertThrows(XMLStreamException.class,
                () -> readWithStax(factory, payload));
        assertFalse(String.valueOf(exception.getMessage()).contains(CANARY),
                "secret must not leak into the error");
    }

    @Test
    public void secureValidatorShouldValidateXml() throws Exception {
        Schema schema = XMLSecurity.newSchemaFactory().newSchema(new StreamSource(new StringReader(XSD)));
        Validator validator = XMLSecurity.newSecureValidator(schema);
        assertNotNull(validator);
        assertDoesNotThrow(() -> validator.validate(new StreamSource(new StringReader(
                "<root><value>hi</value></root>"))));
        assertThrows(SAXException.class, () -> validator.validate(new StreamSource(new StringReader(
                "<root><wrong>x</wrong></root>"))));
    }

    @Test
    public void secureValidatorShouldNotResolveExternalEntities() throws Exception {
        File secret = createTestFile();
        // Schema-valid root: an INSECURE validator (external entities enabled, DOCTYPE
        // allowed) would resolve &xxe; to the file content and validate successfully.
        // A hardened validator must reject the DOCTYPE instead of completing.
        String payload = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE root [ <!ENTITY xxe SYSTEM \"file://" + secret.getAbsolutePath() + "\"> ]>\n"
                + "<root><value>&xxe;</value></root>";
        Schema schema = XMLSecurity.newSchemaFactory().newSchema(new StreamSource(new StringReader(XSD)));
        Validator validator = XMLSecurity.newSecureValidator(schema);
        SAXException exception = assertThrows(SAXException.class,
                () -> validator.validate(new StreamSource(new StringReader(payload))));
        assertFalse(exception.getMessage().contains(CANARY), "secret must not leak into the error");
    }

    @Test
    public void secureValidatorShouldRejectInternalEntityExpansion() throws Exception {
        // An internally declared entity (no SYSTEM/file) cannot be stopped by
        // ACCESS_EXTERNAL_DTD; only rejecting the DOCTYPE declaration prevents its
        // expansion. The root is schema-valid so an insecure validator would complete.
        String payload = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE root [ <!ENTITY bomb \"" + CANARY + "\"> ]>\n"
                + "<root><value>&bomb;</value></root>";
        Schema schema = XMLSecurity.newSchemaFactory().newSchema(new StreamSource(new StringReader(XSD)));
        Validator validator = XMLSecurity.newSecureValidator(schema);
        SAXException exception = assertThrows(SAXException.class,
                () -> validator.validate(new StreamSource(new StringReader(payload))));
        assertFalse(exception.getMessage().contains(CANARY), "secret must not leak into the error");
    }

    @Test
    public void schemaFactoryShouldStillAllowLocalSchemaImports() throws Exception {
        // The bundled XSDs (e.g. mods-3-4.xsd) use local xs:import. Restricting
        // external schema access to the "file" protocol must keep those resolving
        // while still denying the network; a blanket deny would break them.
        Path dep = tempDir.resolve("dep.xsd");
        Path main = tempDir.resolve("main.xsd");
        Files.writeString(dep, DEP_XSD);
        Files.writeString(main, MAIN_XSD);
        assertDoesNotThrow(() -> XMLSecurity.newSchemaFactory()
                .newSchema(new StreamSource(main.toFile())),
                "local xs:import must still resolve under ACCESS_EXTERNAL_SCHEMA=file");
    }

    private void parseWithDocumentBuilder(DocumentBuilderFactory factory, String xml)
            throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
    }

    private String readWithStax(XMLInputFactory factory, String xml) throws XMLStreamException {
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        while (reader.hasNext()) {
            reader.next();
        }
        return "";
    }

    private InputStream toInputStream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private String xxePayload(File secret) {
        return "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \"file://" + secret.getAbsolutePath() + "\"> ]>\n"
                + "<foo>&xxe;</foo>";
    }

    private File createTestFile() throws IOException {
        Path secretPath = Files.createTempFile(tempDir, "xxe-canary", ".txt");
        File secret = secretPath.toFile();
        secret.deleteOnExit();
        Files.writeString(secret.toPath(), CANARY);
        return secret;
    }

    private static final String XSD =
            "<?xml version=\"1.0\"?>\n"
                    + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + "  <xs:element name=\"root\">\n"
                    + "    <xs:complexType>\n"
                    + "      <xs:sequence>\n"
                    + "        <xs:element name=\"value\" type=\"xs:string\"/>\n"
                    + "      </xs:sequence>\n"
                    + "    </xs:complexType>\n"
                    + "  </xs:element>\n"
                    + "</xs:schema>";

    private static final String DEP_XSD =
            "<?xml version=\"1.0\"?>\n"
                    + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" "
                    + "targetNamespace=\"http://example.com/dep\" elementFormDefault=\"qualified\">\n"
                    + "  <xs:element name=\"dep\" type=\"xs:string\"/>\n"
                    + "</xs:schema>";

    private static final String MAIN_XSD =
            "<?xml version=\"1.0\"?>\n"
                    + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" "
                    + "targetNamespace=\"http://example.com/main\" elementFormDefault=\"qualified\">\n"
                    + "  <xs:import namespace=\"http://example.com/dep\" schemaLocation=\"dep.xsd\"/>\n"
                    + "  <xs:element name=\"root\" type=\"xs:string\"/>\n"
                    + "</xs:schema>";
}
