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

package org.kitodo.production.services.data;

import static org.kitodo.test.utils.TestConstants.TITLE_DOC_MAIN;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.test.utils.ProcessTestUtils;
import org.primefaces.model.DefaultTreeNode;

import javax.faces.model.SelectItem;

public class DataEditorServiceIT {

    private static final String EXPECTED_TITLE = "Otsar ha-kavod";
    private static final String TEST_PROCESS_TITLE = "DataEditorTestProcess";
    private static final String TEST_METADATA_FILE = "testmeta.xml";
    private static final String TEST_RULESET = "src/test/resources/rulesets/ruleset_test.xml";
    private static final String ENGLISH = "en";
    private static final String EDIT = "edit";
    private static final String CONTRIBUTOR_PERSON = "ContributorPerson";
    private int testProcessId = 0;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @After
    public void removeTestProcess() throws DAOException {
        if (testProcessId > 0) {
            ProcessTestUtils.removeTestProcess(testProcessId);
            testProcessId = 0;
        }
    }

    /**
     * Test retrieving title keys.
     */
    @Test
    public void shouldGetTitleKeys() {
        List<String> titleKeys = DataEditorService.getTitleKeys();
        Assert.assertTrue(String.format("List of title keys should contain '%s'", TITLE_DOC_MAIN),
                titleKeys.contains(TITLE_DOC_MAIN));
    }

    /**
     * Test retrieving title value.
     * @throws DAOException when adding test process fails.
     * @throws DataException when adding test process fails.
     * @throws IOException when adding test process fails.
     */
    @Test
    public void shouldGetTitleValue() throws DAOException, DataException, IOException {
        LogicalDivision logicalRoot = getLogicalRoot();
        Assert.assertEquals(String.format("Title value of logical root should be '%s'", EXPECTED_TITLE), EXPECTED_TITLE,
                DataEditorService.getTitleValue(logicalRoot, TITLE_DOC_MAIN));
    }

    /**
     * Test retrieving addable metadata for metadata group.
     * @throws DAOException when adding test process fails.
     * @throws DataException when adding test process fails.
     * @throws IOException when adding test process fails.
     */
    @Test
    public void shouldGetAddableMetadataForGroup() throws DAOException, DataException, IOException {
        LogicalDivision logicalRoot = getLogicalRoot();
        RulesetManagementInterface rulesetManagementInterface = ServiceManager.getRulesetManagementService()
                .getRulesetManagement();
        rulesetManagementInterface.load(new File(TEST_RULESET));
        List<Locale.LanguageRange> priorityList = Locale.LanguageRange.parse(ENGLISH);
        StructuralElementViewInterface divisionView = rulesetManagementInterface.getStructuralElementView(
                logicalRoot.getType(), EDIT, priorityList);
        ProcessFieldedMetadata processFieldedMetadata = new ProcessFieldedMetadata(logicalRoot, divisionView,
                rulesetManagementInterface);
        DefaultTreeNode treeNode = new DefaultTreeNode();
        treeNode.setData(processFieldedMetadata);
        Ruleset ruleset = ServiceManager.getRulesetService().getById(1);
        Assert.assertNotNull("Ruleset should not be null", ruleset);
        List<SelectItem> addableMetadata = DataEditorService.getAddableMetadataForGroup(ruleset, treeNode);
        Assert.assertFalse("List of addable metadata should not be empty", addableMetadata.isEmpty());
        Assert.assertTrue(String.format("List of addable metadata should contain '%s'", CONTRIBUTOR_PERSON),
                addableMetadata.stream().anyMatch(metadata -> CONTRIBUTOR_PERSON.equals(metadata.getValue())));
    }

    /**
     * Test retrieving addable metadata for logical structure element.
     * @throws IOException when adding test process fails.
     * @throws DAOException when adding test process fails.
     * @throws DataException when adding test process fails.
     */
    @Test
    public void shouldGetAddableMetadataForStructureElement() throws IOException, DAOException, DataException {
        LogicalDivision logicalRoot = getLogicalRoot();
        RulesetManagementInterface rulesetManagementInterface = ServiceManager.getRulesetManagementService()
                .getRulesetManagement();
        rulesetManagementInterface.load(new File(TEST_RULESET));
        List<Locale.LanguageRange> priorityList = Locale.LanguageRange.parse(ENGLISH);
        StructuralElementViewInterface divisionView = rulesetManagementInterface.getStructuralElementView(
                logicalRoot.getType(), EDIT, priorityList);
        Ruleset ruleset = ServiceManager.getRulesetService().getById(1);
        Assert.assertNotNull("Ruleset should not be null", ruleset);
        List<SelectItem> addableMetadata = DataEditorService.getAddableMetadataForStructureElement(divisionView,
                Collections.emptyList(), Collections.emptyList(), ruleset);
        Assert.assertFalse("List of addable metadata should not be empty", addableMetadata.isEmpty());
    }

    private Process addTestProcess() throws DAOException, DataException, IOException {
        testProcessId = MockDatabase.insertTestProcess(TEST_PROCESS_TITLE, 1, 1, 1);
        ProcessTestUtils.copyTestMetadataFile(testProcessId, TEST_METADATA_FILE);
        return ServiceManager.getProcessService().getById(testProcessId);
    }

    private LogicalDivision getLogicalRoot() throws DAOException, DataException, IOException {
        Process testProcess = addTestProcess();
        URI processUri = ServiceManager.getProcessService().getMetadataFileUri(testProcess);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(processUri);
        Assert.assertNotNull(workpiece);
        LogicalDivision logicalRoot = workpiece.getLogicalStructure();
        Assert.assertNotNull(logicalRoot);
        return logicalRoot;
    }
}
