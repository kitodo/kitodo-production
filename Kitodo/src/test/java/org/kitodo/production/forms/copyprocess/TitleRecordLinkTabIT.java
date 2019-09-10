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

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.production.forms.createprocess.TitleRecordLinkTab;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;

public class TitleRecordLinkTabIT {
    private static FileService fileService = new FileService();
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
        fileService.createDirectory(URI.create(""), "1");
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        if (System.getProperty("java.class.path").contains("eclipse")) {
            while (Objects.isNull(processService.findByTitle(firstProcess))) {
                Thread.sleep(50);
            }
        } else {
            await().untilTrue(new AtomicBoolean(Objects.nonNull(processService.findByTitle(firstProcess))));
        }
    }

    /**
     * Is running after the class has run.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        fileService.delete(URI.create("1"));
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
        ProzesskopieForm prozesskopieForm = new ProzesskopieForm();
        prozesskopieForm.project = ServiceManager.getProjectService().getById(1);
        prozesskopieForm.template = ServiceManager.getTemplateService().getById(1);
        TitleRecordLinkTab testedTitleRecordLinkTab = new TitleRecordLinkTab(null);
        testedTitleRecordLinkTab.setSearchQuery("HierarchyParent");
        testedTitleRecordLinkTab.searchForParentProcesses();

        assertEquals("Wrong number of possibleParentProcesses found!", 1,
            testedTitleRecordLinkTab.getPossibleParentProcesses().size());
        assertEquals("Wrong possibleParentProcesses found!", "4",
            testedTitleRecordLinkTab.getPossibleParentProcesses().get(0).getValue());
    }
}
