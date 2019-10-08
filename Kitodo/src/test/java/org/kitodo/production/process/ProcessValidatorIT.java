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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.production.forms.createprocess.AdditionalDetailsTab;
import org.kitodo.production.forms.createprocess.AdditionalDetailsTableRow;
import org.kitodo.production.forms.createprocess.FieldedAdditionalDetailsTableRow;
import org.kitodo.production.services.ServiceManager;

public class ProcessValidatorIT {

    private static final String NON_EXISTENT = "NonExistentTitle";

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void contentShouldBeValid() throws Exception {
        boolean valid = ProcessValidator.isContentValid(NON_EXISTENT, createAdditionalDetailsRows(), true);
        assertTrue("Process content is invalid!", valid);
    }

    @Test
    public void contentShouldBeInvalidTitle() throws Exception {
        boolean valid = ProcessValidator.isContentValid("First process", createAdditionalDetailsRows(), true);
        assertFalse("Process content is valid - title should be invalid!", valid);
    }

    @Ignore("find ou values for which it fails")
    @Test
    public void contentShouldBeInvalidAdditionalFields() throws Exception {
        boolean valid = ProcessValidator.isContentValid(NON_EXISTENT, createAdditionalDetailsRows(), true);
        assertTrue("Process content is valid - additional fields should be invalid!", valid);
    }

    @Test
    public void processTitleShouldBeCorrect() {
        boolean valid = ProcessValidator.isProcessTitleCorrect(NON_EXISTENT);
        assertTrue("Process title is invalid!", valid);
    }

    @Test
    public void processTitleShouldBeIncorrectWhiteSpaces() {
        boolean valid = ProcessValidator.isProcessTitleCorrect("First process");
        assertFalse("Process content is valid - title should be invalid!", valid);
    }

    @Test
    public void processTitleShouldBeIncorrectNotUnique() {
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

    private List<AdditionalDetailsTableRow> createAdditionalDetailsRows() throws IOException {
        Workpiece workpiece = new Workpiece();
        workpiece.getRootElement().setType("Monograph");
        RulesetManagementInterface rulesetManagementInterface = ServiceManager.getRulesetManagementService().getRulesetManagement();
        rulesetManagementInterface.load(new File("src/test/resources/rulesets/monograph.xml"));
        StructuralElementViewInterface monograph = rulesetManagementInterface.getStructuralElementView(
                "Monograph", "", Locale.LanguageRange.parse("en"));
        FieldedAdditionalDetailsTableRow additionalDetailsTable = new FieldedAdditionalDetailsTableRow(
                null, workpiece.getRootElement(), monograph);
        for (AdditionalDetailsTableRow row : additionalDetailsTable.getRows()) {
            switch (row.getMetadataID()) {
                case "TitleDocMain":
                case "TitleDocMainShort":
                    AdditionalDetailsTab.setAdditionalDetailsRow(row, "Test");
                    break;
                case "TSL_ATS":
                    AdditionalDetailsTab.setAdditionalDetailsRow(row, " ");
                    break;
                case "CatalogIDSource":
                case "CatalogIDDigital":
                    AdditionalDetailsTab.setAdditionalDetailsRow(row, "123");
                    break;
                case "Person":
                    for (AdditionalDetailsTableRow personMetadataRow : ((FieldedAdditionalDetailsTableRow) row).getRows()) {
                        switch (personMetadataRow.getMetadataID()) {
                            case "Role":
                            case "LastName":
                                AdditionalDetailsTab.setAdditionalDetailsRow(personMetadataRow, "Author");
                                break;
                            case "FirstName":
                                AdditionalDetailsTab.setAdditionalDetailsRow(personMetadataRow, "Test");
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
        return additionalDetailsTable.getRows();
    }
}
