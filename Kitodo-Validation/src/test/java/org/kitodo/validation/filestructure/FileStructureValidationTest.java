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


package org.kitodo.validation.filestructure;

import org.junit.jupiter.api.Test;
import org.kitodo.api.validation.ValidationResult;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileStructureValidationTest {

    private static final Path repositoryRoot = Paths.get("").toAbsolutePath().getParent();
    private static final String MODS_3_4_XSD = "/Kitodo/src/main/resources/schemata/mods-3-4.xsd";
    private static final String TEST_FILES_DIR = "src/test/resources/xmltestfiles/";
    private static final String MALFORMED_MODS_FILE = TEST_FILES_DIR + "mods-3-4-malformed.xml";
    private static final String INVALID_MODS_FILE = TEST_FILES_DIR + "mods-3-4-invalid.xml";
    private static final String VALID_MODS_FILE = TEST_FILES_DIR + "mods-3-4-valid.xml";
    private static final URI modsSchema = Paths.get(repositoryRoot + MODS_3_4_XSD).toUri();
    private final FileStructureValidation xmlValidation = new FileStructureValidation();

    @Test
    public void shouldSucceedToValidateValidXmlFile() throws SAXException, IOException {
        ValidationResult validationResult = xmlValidation.validate(Paths.get(VALID_MODS_FILE).toUri(), modsSchema);
        assertTrue(validationResult.getResultMessages().isEmpty(), "Validation should succeed with valid MODS XML file");
    }

    @Test
    public void shouldFailToValidateInvalidXmlFile() throws SAXException, IOException {
        ValidationResult validationResult = xmlValidation.validate(Paths.get(INVALID_MODS_FILE).toUri(), modsSchema);
        assertFalse(validationResult.getResultMessages().isEmpty(), "Validation should fail with invalid MODS XML file");
    }

    @Test
    public void shouldFailToValidateMalformedXmlFile() throws SAXException, IOException {
        ValidationResult validationResult = xmlValidation.validate(Paths.get(MALFORMED_MODS_FILE).toUri(), modsSchema);
        assertFalse(validationResult.getResultMessages().isEmpty(), "Validation should fail with malformed XML file");
    }

    @Test
    public void shouldSucceedToValidateValidXmlString() throws IOException, SAXException {
        String xmlContent = Files.readString(Paths.get(VALID_MODS_FILE));
        ValidationResult validationResult = xmlValidation.validate(xmlContent, modsSchema);
        assertTrue(validationResult.getResultMessages().isEmpty(), "Validation should succeed with valid MODS XML content string");
    }

    @Test
    public void shouldFailToValidateInvalidXmlString() throws IOException, SAXException {
        String xmlContent = Files.readString(Paths.get(INVALID_MODS_FILE));
        ValidationResult validationResult = xmlValidation.validate(xmlContent, modsSchema);
        assertFalse(validationResult.getResultMessages().isEmpty(), "Validation should fail with invalid MODS XML content string");
    }

    @Test
    public void shouldFailToValidateMalformedXmlString() throws IOException, SAXException {
        String xmlContent = Files.readString(Paths.get(MALFORMED_MODS_FILE));
        ValidationResult validationResult = xmlValidation.validate(xmlContent, modsSchema);
        assertFalse(validationResult.getResultMessages().isEmpty(), "Validation should fail with malformed XML file content string");
    }

    @Test
    public void shouldRejectDoctypeWithExternalEntity() throws IOException, SAXException {
        Path canary = Files.createTempFile("xxe-canary", ".txt");
        Files.writeString(canary, "XXE-CANARY-12345");
        // Permissive schema: without hardened parsing the entity would expand and
        // the document would validate successfully, so a non-empty result proves
        // the DOCTYPE was rejected.
        Path schema = createPermissiveSchema();
        try {
            String xmlContent = "<?xml version=\"1.0\"?>\n"
                    + "<!DOCTYPE root [ <!ENTITY xxe SYSTEM \"file://" + canary.toAbsolutePath() + "\"> ]>\n"
                    + "<root><value>&xxe;</value></root>";
            ValidationResult validationResult = xmlValidation.validate(xmlContent, schema.toUri());
            assertFalse(validationResult.getResultMessages().isEmpty(),
                    "Validation should reject a DOCTYPE declaration carrying an external entity");
            for (String message : validationResult.getResultMessages()) {
                assertFalse(message.contains("XXE-CANARY-12345"),
                        "External entity content must not be resolved or leaked");
            }
        } finally {
            Files.deleteIfExists(canary);
            Files.deleteIfExists(schema);
        }
    }

    @Test
    public void shouldRejectDoctypeWithInternalEntity() throws IOException, SAXException {
        Path schema = createPermissiveSchema();
        try {
            String xmlContent = "<?xml version=\"1.0\"?>\n"
                    + "<!DOCTYPE root [ <!ENTITY bomb \"XXE-CANARY-12345\"> ]>\n"
                    + "<root><value>&bomb;</value></root>";
            ValidationResult validationResult = xmlValidation.validate(xmlContent, schema.toUri());
            assertFalse(validationResult.getResultMessages().isEmpty(),
                    "Validation should reject a DOCTYPE declaration carrying an internal entity");
            for (String message : validationResult.getResultMessages()) {
                assertFalse(message.contains("XXE-CANARY-12345"),
                        "Internal entity content must not be resolved or leaked");
            }
        } finally {
            Files.deleteIfExists(schema);
        }
    }

    @Test
    public void shouldValidateXmlStringWithNonUtf8EncodingDeclaration() throws IOException, SAXException {
        Path schema = createPermissiveSchema();
        try {
            String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n"
                    + "<root><value>caf\u00e9</value></root>";
            ValidationResult validationResult = xmlValidation.validate(xmlContent, schema.toUri());
            assertTrue(validationResult.getResultMessages().isEmpty(),
                    "Character data must be preserved and must not be re-encoded before parsing");
        } finally {
            Files.deleteIfExists(schema);
        }
    }

    private Path createPermissiveSchema() throws IOException {
        Path schema = Files.createTempFile("xxe-permissive", ".xsd");
        Files.writeString(schema,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:element name=\"root\">\n"
                + "    <xs:complexType>\n"
                + "      <xs:sequence>\n"
                + "        <xs:element name=\"value\" type=\"xs:string\"/>\n"
                + "      </xs:sequence>\n"
                + "    </xs:complexType>\n"
                + "  </xs:element>\n"
                + "</xs:schema>\n");
        return schema;
    }
}
