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

package org.kitodo.production.forms.copyprocess;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.forms.createprocess.CreateProcessForm;
import org.kitodo.production.forms.createprocess.TitleRecordLinkTab;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.test.utils.ProcessTestUtils;

public class TitleRecordLinkTabIT {
    private static final ProcessService processService = ServiceManager.getProcessService();
    private static final String META_XML = "testParentProcessMeta.xml";
    private static final String firstProcess = "First process";
    private static int parentProcessId = -1;

    /**
     * Is running before the class runs.
     */
    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        Map<String, Integer> hierarchyProcesses = MockDatabase.insertProcessesForHierarchyTests();
        parentProcessId = hierarchyProcesses.get(MockDatabase.HIERARCHY_PARENT);
        ProcessTestUtils.copyTestMetadataFile(parentProcessId, META_XML);
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
    @AfterAll
    public static void cleanDatabase() throws Exception {
        ProcessTestUtils.removeTestProcess(parentProcessId);
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldChooseParentProcess() {
        TitleRecordLinkTab testedTitleRecordLinkTab = new TitleRecordLinkTab(null);
        testedTitleRecordLinkTab.setChosenParentProcess("1");
        testedTitleRecordLinkTab.chooseParentProcess();
        assertFalse(Objects.isNull(testedTitleRecordLinkTab.getTitleRecordProcess()),
            "titleRecordProcess is null!");
        assertEquals((Integer) 1, testedTitleRecordLinkTab.getTitleRecordProcess().getId(),
            "titleRecordProcess has wrong ID!");
    }

    @Test
    public void shouldSearchForParentProcesses() throws Exception {
        CreateProcessForm createProcessForm = new CreateProcessForm();
        createProcessForm.setProject(ServiceManager.getProjectService().getById(1));
        createProcessForm.setTemplate(ServiceManager.getTemplateService().getById(1));
        TitleRecordLinkTab testedTitleRecordLinkTab = new TitleRecordLinkTab(createProcessForm);
        testedTitleRecordLinkTab.setSearchQuery("HierarchyParent");
        testedTitleRecordLinkTab.searchForParentProcesses();

        assertEquals(1, testedTitleRecordLinkTab.getPossibleParentProcesses().size(),
            "Wrong number of possibleParentProcesses found!");
        assertEquals("4", testedTitleRecordLinkTab.getPossibleParentProcesses().getFirst().getValue(),
            "Wrong possibleParentProcesses found!");
    }

    /**
     * Verify that user can only link to parent processes of unassigned projects when he has the corresponding
     * permission/authority.
     * @throws DAOException when loading test objects from database fails
     */
    @Test
    public void shouldPreventLinkingToParentProcessOfUnassignedProject() throws DAOException {
        Project firstProject = ServiceManager.getProjectService().getById(1);
        SecurityUserDetails user = ServiceManager.getUserService().getAuthenticatedUser();
        user.getProjects().remove(firstProject);

        TitleRecordLinkTab testedTitleRecordLinkTab = searchForHierarchyParent();

        assertEquals(1, testedTitleRecordLinkTab.getPossibleParentProcesses().size(),
            "Wrong number of potential parent processes found!");
        assertTrue(testedTitleRecordLinkTab.getPossibleParentProcesses().getFirst().isDisabled(),
            "Process of unassigned project should be deactivated in TitleRecordLinkTab!");

        // re-add first project to user
        user.getProjects().add(firstProject);
    }

    private TitleRecordLinkTab searchForHierarchyParent() throws DAOException {
        CreateProcessForm createProcessForm = new CreateProcessForm();
        createProcessForm.setProject(ServiceManager.getProjectService().getById(2));
        createProcessForm.setTemplate(ServiceManager.getTemplateService().getById(1));
        TitleRecordLinkTab testedTitleRecordLinkTab = new TitleRecordLinkTab(createProcessForm);
        testedTitleRecordLinkTab.setSearchQuery("HierarchyParent");
        testedTitleRecordLinkTab.searchForParentProcesses();
        return testedTitleRecordLinkTab;
    }
}
