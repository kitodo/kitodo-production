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

package org.kitodo.production.services.dataeditor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.production.services.ServiceManager;

public class DataEditorServiceTest {

    private final DataEditorService dataEditorService = ServiceManager.getDataEditorService();
    private static byte[] testMetaOldFormat;
    private static final String pathOfOldMetaFormat = "src/test/resources/testmetaOldFormat.xml";
    private static final String metadataFilesDir = "./src/test/resources/metadata/metadataFiles/";
    private static final String EXPECTED_ENTRY_STRING = "Test-Titel";
    private static final String EXPECTED_GROUP_STRING = "JohnDoeAuthor";

    @BeforeEach
    public void saveFile() throws IOException {
        File file = new File(metadataFilesDir + "testmetaOldFormat.xml");
        testMetaOldFormat = IOUtils.toByteArray(file.toURI());
    }

    @AfterEach
    public void revertFile() throws IOException {
        IOUtils.write( testMetaOldFormat, Files.newOutputStream(Paths.get(pathOfOldMetaFormat)));
    }

    @Test
    public void shouldReadMetadata() {
        assertDoesNotThrow(() -> dataEditorService.readData(Paths.get(metadataFilesDir + "testmeta.xml").toUri()));
    }

    @Test
    public void shouldReadOldMetadata() {
        assertDoesNotThrow(() -> dataEditorService.readData(Paths.get(metadataFilesDir + "testmetaOldFormat.xml").toUri()));
    }

    @Test
    public void shouldNotReadMetadataOfNotExistingFile() {
        assertThrows(IOException.class, () -> dataEditorService.readData(Paths.get("notExisting.xml").toUri()));
    }

    @Test
    public void shouldConvertMetadataToString() {
        MetadataEntry metadataEntry = new MetadataEntry();
        metadataEntry.setKey("TitleMainDoc");
        metadataEntry.setValue(EXPECTED_ENTRY_STRING);
        assertEquals(EXPECTED_ENTRY_STRING, DataEditorService.metadataToString(metadataEntry));
        MetadataGroup metadataGroup = new MetadataGroup();
        metadataGroup.setKey("Person");
        MetadataEntry firstNameEntry = new MetadataEntry();
        firstNameEntry.setKey("FirstName");
        firstNameEntry.setValue("John");
        MetadataEntry lastNameEntry = new MetadataEntry();
        lastNameEntry.setKey("LastName");
        lastNameEntry.setValue("Doe");
        MetadataEntry roleEntry = new MetadataEntry();
        roleEntry.setKey("Role");
        roleEntry.setValue("Author");
        metadataGroup.getMetadata().add(firstNameEntry);
        metadataGroup.getMetadata().add(lastNameEntry);
        metadataGroup.getMetadata().add(roleEntry);
        assertEquals(EXPECTED_GROUP_STRING, DataEditorService.metadataToString(metadataGroup));
    }
}
