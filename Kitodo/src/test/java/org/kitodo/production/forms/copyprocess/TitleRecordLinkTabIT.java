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

import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.forms.createprocess.CreateProcessForm;
import org.kitodo.production.forms.createprocess.TitleRecordLinkTab;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;

public class TitleRecordLinkTabIT {
    private static final ProcessService processService = ServiceManager.getProcessService();

    private static final String firstProcess = "First process";

    /**
     * Is running before the class runs.
     */
    @BeforeAll
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
    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldChooseParentProcess() {
        TitleRecordLinkTab testedTitleRecordLinkTab = new TitleRecordLinkTab(null);
        testedTitleRecordLinkTab.setChosenParentProcess("1");
        testedTitleRecordLinkTab.chooseParentProcess();
        assertFalse(Objects.isNull(testedTitleRecordLinkTab.getTitleRecordProcess()), "titleRecordProcess is null!");
        assertEquals((Integer) 1, testedTitleRecordLinkTab.getTitleRecordProcess().getId(), "titleRecordProcess has wrong ID!");
    }

    @Test
    @Disabled
    public void shouldSearchForParentProcesses() throws Exception {
        CreateProcessForm createProcessForm = new CreateProcessForm();
        createProcessForm.setProject(ServiceManager.getProjectService().getById(1));
        createProcessForm.setTemplate(ServiceManager.getTemplateService().getById(1));
        TitleRecordLinkTab testedTitleRecordLinkTab = new TitleRecordLinkTab(null);
        testedTitleRecordLinkTab.setSearchQuery("HierarchyParent");
        testedTitleRecordLinkTab.searchForParentProcesses();

        assertEquals(1, testedTitleRecordLinkTab.getPossibleParentProcesses().size(), "Wrong number of possibleParentProcesses found!");
        assertEquals("4", testedTitleRecordLinkTab.getPossibleParentProcesses().get(0).getValue(), "Wrong possibleParentProcesses found!");
    }
}
