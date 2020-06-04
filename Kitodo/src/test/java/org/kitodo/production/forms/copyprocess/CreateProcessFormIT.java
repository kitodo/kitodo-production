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

import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.production.forms.createprocess.CreateProcessForm;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;

/**
 * Tests for ProcessService class.
 */
public class CreateProcessFormIT {

    private static FileService fileService = new FileService();
    private static final ProcessService processService = ServiceManager.getProcessService();

    private static final String firstProcess = "First process";

    private static final String KITODO_PROJECTS_TEST_XML = "kitodo_projects_test.xml";

    /**
     * Is running before the class runs.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessesForHierarchyTests();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        await().untilTrue(new AtomicBoolean(Objects.nonNull(processService.findByTitle(firstProcess))));
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
    public void shouldCreateNewProcess() throws Exception {
        new File(KITODO_PROJECTS_TEST_XML).renameTo(KitodoConfigFile.PROJECT_CONFIGURATION.getFile());
        CreateProcessForm underTest = new CreateProcessForm();
        underTest.getProcessDataTab().setDocType("Monograph");
        underTest.prepareProcess(1,1,null);
        underTest.initializeProcesses();
        underTest.getMainProcess().setProject(ServiceManager.getProjectService().getById(1));
        underTest.getMainProcess().setRuleset(ServiceManager.getRulesetService().getById(1));
        underTest.getMainProcess().setTitle("title");

        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        ExecutionPermission.setExecutePermission(script);
        long before = processService.count();
        underTest.createNewProcess();
        ExecutionPermission.setNoExecutePermission(script);
        long after = processService.count();
        assertEquals("No process was created!", before + 1, after);
        assertEquals("no Tasks were created", 5, underTest.getMainProcess().getTasks().size());

        // clean up database, index and file system
        Integer processId = underTest.getMainProcess().getId();
        processService.remove(processId);
        fileService.delete(URI.create(processId.toString()));
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().renameTo(new File(KITODO_PROJECTS_TEST_XML));
    }
    
    @Test
    public void shouldCreateProcessWithoutTasks() throws Exception {
        new File(KITODO_PROJECTS_TEST_XML).renameTo(KitodoConfigFile.PROJECT_CONFIGURATION.getFile());
        CreateProcessForm underTest = new CreateProcessForm();
        underTest.prepareProcess(1,1,null);
        underTest.initializeProcesses();
        underTest.getProcessDataTab().setDocType("MultiVolumeWork");
        underTest.getMainProcess().setProject(ServiceManager.getProjectService().getById(1));
        underTest.getMainProcess().setRuleset(ServiceManager.getRulesetService().getById(1));
        underTest.getMainProcess().setTemplate(ServiceManager.getTemplateService().getById(1));
        underTest.getMainProcess().setTitle("title");

        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        //ExecutionPermission.setExecutePermission(script);
        underTest.createNewProcess();
        //ExecutionPermission.setNoExecutePermission(script);
        assertEquals("There are tasks assigned to a process with doctype 'noWorkflow'", 0, underTest.getMainProcess().getTasks().size());

        // clean up database, index and file system
        Integer processId = underTest.getMainProcess().getId();
        processService.remove(processId);
        fileService.delete(URI.create(processId.toString()));
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().renameTo(new File(KITODO_PROJECTS_TEST_XML));
    }
}
