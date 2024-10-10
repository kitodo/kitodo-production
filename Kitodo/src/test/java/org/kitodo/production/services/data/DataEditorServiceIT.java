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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kitodo.test.utils.TestConstants.TITLE_DOC_MAIN;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.model.SelectItem;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

public class DataEditorServiceIT {

    private static final String EXPECTED_TITLE = "Otsar ha-kavod";
    private static final String TEST_PROCESS_TITLE = "DataEditorTestProcess";
    private static final String TEST_METADATA_FILE = "testmeta.xml";
    private static final String TEST_RULESET = "src/test/resources/rulesets/ruleset_test.xml";
    private static final String ENGLISH = "en";
    private static final String EDIT = "edit";
    private static final String CONTRIBUTOR_PERSON = "ContributorPerson";
    private int testProcessId = 0;

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @AfterEach
    public void removeTestProcess() throws DAOException {
        if (testProcessId > 0) {
            ProcessTestUtils.removeTestProcess(testProcessId);
            testProcessId = 0;
        }
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
        assertEquals(EXPECTED_TITLE, DataEditorService.getTitleValue(logicalRoot, TITLE_DOC_MAIN),
                String.format("Title value of logical root should be '%s'", EXPECTED_TITLE));
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
        assertNotNull(ruleset, "Ruleset should not be null");
        List<SelectItem> addableMetadata = DataEditorService.getAddableMetadataForGroup(ruleset, treeNode);
        assertFalse(addableMetadata.isEmpty(), "List of addable metadata should not be empty");
        assertTrue(addableMetadata
                        .stream().anyMatch(metadata -> CONTRIBUTOR_PERSON.equals(metadata.getValue())),
                String.format("List of addable metadata should contain '%s'", CONTRIBUTOR_PERSON));
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
        assertNotNull(ruleset, "Ruleset should not be null");
        List<SelectItem> addableMetadata = DataEditorService.getAddableMetadataForStructureElement(divisionView,
                Collections.emptyList(), Collections.emptyList(), ruleset);
        assertFalse(addableMetadata.isEmpty(), "List of addable metadata should not be empty");
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
        assertNotNull(workpiece);
        LogicalDivision logicalRoot = workpiece.getLogicalStructure();
        assertNotNull(logicalRoot);
        return logicalRoot;
    }
}
