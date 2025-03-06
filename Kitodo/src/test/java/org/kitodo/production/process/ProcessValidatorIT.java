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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;

public class ProcessValidatorIT {

    private static final String NON_EXISTENT = "NonExistentTitle";

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void contentShouldBeValid() throws Exception {
        boolean valid = ProcessValidator.isContentValid(NON_EXISTENT, createProcessDetailsList(), true);
        assertTrue(valid, "Process content is invalid!");
    }

    @Test
    public void contentShouldBeInvalidTitle() throws Exception {
        boolean valid = ProcessValidator.isContentValid("First process", createProcessDetailsList(), true);
        assertFalse(valid, "Process content is valid - title should be invalid!");
    }

    @Disabled("find ou values for which it fails")
    @Test
    public void contentShouldBeInvalidAdditionalFields() throws Exception {
        boolean valid = ProcessValidator.isContentValid(NON_EXISTENT, createProcessDetailsList(), true);
        assertTrue(valid, "Process content is valid - additional fields should be invalid!");
    }

    @Test
    public void processTitleShouldBeCorrect() {
        boolean valid = ProcessValidator.isProcessTitleCorrect(NON_EXISTENT);
        assertTrue(valid, "Process title is invalid!");
    }

    @Test
    public void processTitleShouldBeIncorrectWhiteSpaces() {
        boolean valid = ProcessValidator.isProcessTitleCorrect("First process");
        assertFalse(valid, "Process content is valid - title should be invalid!");
    }

    @Test
    public void processTitleShouldBeIncorrectNotUnique() {
        boolean valid = ProcessValidator.isProcessTitleCorrect("DBConnectionTest");
        assertFalse(valid, "Process content is valid - title should be invalid!");
    }

    @Test
    public void propertyShouldExist() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle("Korrektur notwendig");
        boolean exists = ProcessValidator.existsProperty(process.getProperties(), property);
        assertTrue(exists, "Property doesn't exist!");
    }

    @Test
    public void propertyShouldNotExist() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        Property property = new Property();
        property.setTitle("Korrektur");
        boolean exists = ProcessValidator.existsProperty(process.getProperties(), property);
        assertFalse(exists, "Property exists!");
    }

    private List<ProcessDetail> createProcessDetailsList() throws IOException {
        Workpiece workpiece = new Workpiece();
        workpiece.getLogicalStructure().setType("Monograph");
        RulesetManagementInterface rulesetManagement = ServiceManager.getRulesetManagementService().getRulesetManagement();
        rulesetManagement.load(new File("src/test/resources/rulesets/monograph.xml"));
        StructuralElementViewInterface monograph = rulesetManagement.getStructuralElementView(
                "Monograph", "", Locale.LanguageRange.parse("en"));
        ProcessFieldedMetadata processDetails = new ProcessFieldedMetadata(workpiece.getLogicalStructure(), monograph,
                rulesetManagement);
        for (ProcessDetail detail : processDetails.getRows()) {
            switch (detail.getMetadataID()) {
                case "TitleDocMain":
                case "TitleDocMainShort":
                    ImportService.setProcessDetailValue(detail, "Test");
                    break;
                case "TSL_ATS":
                    ImportService.setProcessDetailValue(detail, " ");
                    break;
                case "CatalogIDSource":
                case "CatalogIDDigital":
                    ImportService.setProcessDetailValue(detail, "123");
                    break;
                case "Person":
                    for (ProcessDetail personMetadataRow : ((ProcessFieldedMetadata) detail).getRows()) {
                        switch (personMetadataRow.getMetadataID()) {
                            case "Role":
                            case "LastName":
                                ImportService.setProcessDetailValue(personMetadataRow, "Author");
                                break;
                            case "FirstName":
                                ImportService.setProcessDetailValue(personMetadataRow, "Test");
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return processDetails.getRows();
    }
}
