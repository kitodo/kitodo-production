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

package org.kitodo.production.metadata;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.forms.dataeditor.DataEditorForm;
import org.kitodo.production.metadata.InsertionPosition;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;


public class MetadataEditorIT {
    private static final ProcessService processService = ServiceManager.getProcessService();

    private static final String firstProcess = "First process";

    /**
     * Is running before the class runs.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessesForHierarchyTests();
        MockDatabase.setUpAwaitility();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !processService.findByTitle(firstProcess).isEmpty();
        });
    }

    /**
     * Is running after the class has run.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldAddLink() throws Exception {
        File metaXmlFile = new File("src/test/resources/metadata/4/meta.xml");
        List<String> metaXmlContentBefore = FileUtils.readLines(metaXmlFile, StandardCharsets.UTF_8);

        MetadataEditor.addLink(ServiceManager.getProcessService().getById(4), "0", 7);

        assertTrue("The link was not added correctly!",
            isInternalMetsLink(FileUtils.readLines(metaXmlFile, StandardCharsets.UTF_8).get(37), 7));

        FileUtils.writeLines(metaXmlFile, StandardCharsets.UTF_8.toString(), metaXmlContentBefore);
        FileUtils.deleteQuietly(new File("src/test/resources/metadata/4/meta.xml.1"));
    }

    @Test
    public void shouldAddMultipleStructuresWithoutMetadata() throws Exception {

        File metaXmlFile = new File("src/test/resources/metadata/2/meta.xml");
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metaXmlFile.toURI());

        int oldNrLogicalDivisions = workpiece.getAllLogicalDivisions().size();
        int addedDivisions = 2;
        int newNrDivisions = oldNrLogicalDivisions + addedDivisions;

        MetadataEditor.addMultipleStructures(addedDivisions, "section", workpiece, workpiece.getLogicalStructure(),
            InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT);

        List<LogicalDivision> logicalDivisions = workpiece.getAllLogicalDivisions();
        assertTrue("Metadata should be empty", logicalDivisions.get(newNrDivisions - 1).getMetadata().isEmpty());
    }

    @Test
    public void shouldAddMultipleStructuresWithMetadataGroup() throws Exception {

        RulesetManagementInterface ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        ruleset.load(new File("src/test/resources/rulesets/ruleset_test.xml"));
        StructuralElementViewInterface divisionView = ruleset.getStructuralElementView("Monograph", "edit",
            Locale.LanguageRange.parse("en"));
        String metadataKey = "TitleDocMain";

        File metaXmlFile = new File("src/test/resources/metadata/2/meta.xml");
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metaXmlFile.toURI());

        int oldNrLogicalDivisions = workpiece.getAllLogicalDivisions().size();
        int addedDivisions = 2;
        int newNrDivisions = oldNrLogicalDivisions + addedDivisions;

        MetadataViewInterface mvi = divisionView.getAllowedMetadata().stream()
                .filter(metaDatum -> metaDatum.getId().equals(metadataKey)).findFirst().orElse(null);

        MetadataEditor.addMultipleStructuresWithMetadata(addedDivisions, "Monograph", workpiece,
            workpiece.getLogicalStructure(), InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT, mvi, "value");
        LogicalDivision newSectionOne = workpiece.getAllLogicalDivisions().get(newNrDivisions - 2);
        List<Metadata> metadataListOne = new ArrayList<Metadata>(newSection1.getMetadata());
        LogicalDivision newSectionTwo = workpiece.getAllLogicalDivisions().get(newNrDivisions - 1);
        List<Metadata> metadataListTwo = new ArrayList<Metadata>(newSection2.getMetadata());
        Metadata metadatumOne = metadataListOne.get(0);
        Metadata metadatumTwo = metadataListTwo.get(0);

        assertTrue("Metadata should be of type MetadataEntry", metadatumOne instanceof MetadataEntry);
        assertTrue("Metadata value was incorrectly added", ((MetadataEntry) metadatumOne).getValue().equals("value 1")
                && ((MetadataEntry) metadatumTwo).getValue().equals("value 2"));
    }

    @Test
    public void shouldAddMultipleStructuresWithMetadataEntry() throws Exception {
        RulesetManagementInterface ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        ruleset.load(new File("src/test/resources/rulesets/ruleset_test.xml"));
        StructuralElementViewInterface divisionView = ruleset.getStructuralElementView("Monograph", "edit",
            Locale.LanguageRange.parse("en"));
        String metadataKey = "Person";

        File metaXmlFile = new File("src/test/resources/metadata/2/meta.xml");
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metaXmlFile.toURI());

        int oldNrLogicalDivisions = workpiece.getAllLogicalDivisions().size();
        int addedDivisions = 2;
        int newNrDivisions = oldNrLogicalDivisions + addedDivisions;

        MetadataViewInterface mvi = divisionView.getAllowedMetadata().stream()
                .filter(metaDatum -> metaDatum.getId().equals(metadataKey)).findFirst().orElse(null);

        MetadataEditor.addMultipleStructuresWithMetadata(addedDivisions, "Monograph", workpiece,
            workpiece.getLogicalStructure(), InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT, mvi, "value");
        LogicalDivision newSectionOne = workpiece.getAllLogicalDivisions().get(newNrDivisions - 2);
        List<Metadata> metadataListOne = new ArrayList<Metadata>(newSection1.getMetadata());
        LogicalDivision newSectionTwo = workpiece.getAllLogicalDivisions().get(newNrDivisions - 1);
        List<Metadata> metadataListTwo = new ArrayList<Metadata>(newSection2.getMetadata());
        Metadata metadatumOne = metadataListOne.get(0);
        Metadata metadatumTwo = metadataListTwo.get(0);

        assertTrue("Metadata should be of type MetadataGroup",
            metadatumOne instanceof MetadataGroup && metadatumTwo instanceof MetadataGroup);
        assertTrue("Metadata value was incorrectly added",
            metadatumOne.getKey().equals("Person") && metadatumTwo.getKey().equals("Person"));
    }

    private boolean isInternalMetsLink(String lineOfMets, int recordNumber) {
        // Order of <mptr> attributes varies
        boolean isInternalMetsLink = lineOfMets.contains("mptr ") && lineOfMets.contains("LOCTYPE=\"OTHER\"")
                && lineOfMets.contains("OTHERLOCTYPE=\"Kitodo.Production\"")
                && lineOfMets.contains("href=\"database://?process.id=" + recordNumber + "\"");
        return isInternalMetsLink;
    }
}
