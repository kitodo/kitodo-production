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

package org.kitodo.production.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.production.helper.AdditionalField;
import org.kitodo.production.services.ServiceManager;

public class ProcessValidatorIT {

    private static final String NON_EXISTENT = "NonExistentTitle";

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void contentShouldBeValid() throws Exception {
        boolean valid = ProcessValidator.isContentValid(NON_EXISTENT, createAdditionalFields(),
            Arrays.asList("Digi1", "Digi2"), createStandardFields(), true);
        assertTrue("Process content is invalid!", valid);
    }

    @Test
    public void contentShouldBeInvalidTitle() throws Exception {
        boolean valid = ProcessValidator.isContentValid("First process", createAdditionalFields(),
            Arrays.asList("Digi1", "Digi2"), createStandardFields(), true);
        assertFalse("Process content is valid - title should be invalid!", valid);
    }

    @Test
    public void contentShouldBeInvalidCollections() throws Exception {
        boolean valid = ProcessValidator.isContentValid(NON_EXISTENT, createAdditionalFields(), Collections.emptyList(),
            createStandardFields(), true);
        assertFalse("Process content is valid - collections should be invalid!", valid);
    }

    @Ignore("find ou values for which it fails")
    @Test
    public void contentShouldBeInvalidAdditionalFields() throws Exception {
        boolean valid = ProcessValidator.isContentValid(NON_EXISTENT, createAdditionalFields(),
            Arrays.asList("Digi1", "Digi2"), createStandardFields(), true);
        assertTrue("Process content is valid - additional fields should be invalid!", valid);
    }

    @Test
    public void processTitleShouldBeCorrect() throws Exception {
        boolean valid = ProcessValidator.isProcessTitleCorrect(NON_EXISTENT);
        assertTrue("Process title is invalid!", valid);
    }

    @Test
    public void processTitleShouldBeIncorrectWhiteSpaces() throws Exception {
        boolean valid = ProcessValidator.isProcessTitleCorrect("First process");
        assertFalse("Process content is valid - title should be invalid!", valid);
    }

    @Test
    public void processTitleShouldBeIncorrectNotUnique() throws Exception {
        boolean valid = ProcessValidator.isProcessTitleCorrect("DBConnectionTest");
        assertFalse("Process content is valid - title should be invalid!", valid);
    }

    @Test
    public void propertyShouldExist() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle("Korrektur notwendig");
        boolean exists = ProcessValidator.existsProperty(process.getProperties(), property);
        assertTrue("Property doesn't exist!", exists);
    }

    @Test
    public void propertyShouldNotExist() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle("Korrektur");
        boolean exists = ProcessValidator.existsProperty(process.getProperties(), property);
        assertFalse("Property exists!", exists);
    }

    private List<AdditionalField> createAdditionalFields() {
        List<AdditionalField> additionalFields = new ArrayList<>();
        additionalFields.add(createAdditionalField("Artist", "", ""));
        additionalFields.add(createAdditionalField("Schrifttyp", "", ""));
        additionalFields.add(createAdditionalField("Titel", "Test", "TitleDocMain"));
        additionalFields.add(createAdditionalField("Titel (Sortierung)", "Test", "TitleDocMainShort"));
        additionalFields.add(createAdditionalField("Autoren", "Test Author", "ListOfCreators"));
        additionalFields.add(createAdditionalField("ATS", "", "TSL_ATS"));
        additionalFields.add(createAdditionalField("TSL", "", "TSL_ATS"));
        additionalFields.add(createAdditionalField("PPN analog a-Satz", "123", "CatalogIDSource"));
        additionalFields.add(createAdditionalField("PPN digital a-Satz", "123", "CatalogIDDigital"));
        return additionalFields;
    }

    private AdditionalField createAdditionalField(String title, String value, String metadata) {
        AdditionalField additionalField = new AdditionalField("monograph");
        additionalField.setTitle(title);
        additionalField.setValue(value);
        additionalField.setMetadata(metadata);
        additionalField.setIsdoctype("monograph");
        return additionalField;
    }

    private Map<String, Boolean> createStandardFields() {
        Map<String, Boolean> standardFields = new HashMap<>();
        standardFields.put("collections", true);
        standardFields.put("doctype", true);
        standardFields.put("regelsatz", true);
        standardFields.put("images", true);
        return standardFields;
    }
}
