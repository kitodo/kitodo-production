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
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileStructureValidationTest {

    private static final String TEST_RESOURCES_DIR = "src/test/resources/";
    private static final String TEST_XSD_DIR = TEST_RESOURCES_DIR + "xmlschemadefinitions/";
    private static final String MODS_3_4_XSD = TEST_XSD_DIR + "mods-3-4.xsd";
    private static final String TEST_FILES_DIR = TEST_RESOURCES_DIR + "xmltestfiles/";
    private static final String MALFORMED_MODS_FILE = TEST_FILES_DIR + "mods-3-4-malformed.xml";
    private static final String INVALID_MODS_FILE = TEST_FILES_DIR + "mods-3-4-invalid.xml";
    private static final String VALID_MODS_FILE = TEST_FILES_DIR + "mods-3-4-valid.xml";
    private static final URI modsSchema = Paths.get(MODS_3_4_XSD).toUri();
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
        assertFalse(validationResult.getResultMessages().isEmpty(), "Validation should fail with malformed XML content string");
    }

}
