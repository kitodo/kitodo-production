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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
    public void shouldChooseParentProcess() {
        TitleRecordLinkTab testedTitleRecordLinkTab = new TitleRecordLinkTab(null);
        testedTitleRecordLinkTab.setChosenParentProcess("1");
        testedTitleRecordLinkTab.chooseParentProcess();
        assertFalse("titleRecordProcess is null!", Objects.isNull(testedTitleRecordLinkTab.getTitleRecordProcess()));
        assertEquals("titleRecordProcess has wrong ID!", (Integer) 1,
            testedTitleRecordLinkTab.getTitleRecordProcess().getId());
    }

    @Test
    @Ignore
    public void shouldSearchForParentProcesses() throws Exception {
        CreateProcessForm createProcessForm = new CreateProcessForm();
        createProcessForm.setProject(ServiceManager.getProjectService().getById(1));
        createProcessForm.setTemplate(ServiceManager.getTemplateService().getById(1));
        TitleRecordLinkTab testedTitleRecordLinkTab = new TitleRecordLinkTab(null);
        testedTitleRecordLinkTab.setSearchQuery("HierarchyParent");
        testedTitleRecordLinkTab.searchForParentProcesses();

        assertEquals("Wrong number of possibleParentProcesses found!", 1,
            testedTitleRecordLinkTab.getPossibleParentProcesses().size());
        assertEquals("Wrong possibleParentProcesses found!", "4",
            testedTitleRecordLinkTab.getPossibleParentProcesses().get(0).getValue());
    }
}
