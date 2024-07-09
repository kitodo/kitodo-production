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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.forms.createprocess.CreateProcessForm;
import org.kitodo.production.helper.TempProcess;
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
    public void shouldCreateNewProcess() throws Exception {
        CreateProcessForm underTest = new CreateProcessForm();
        underTest.getProcessDataTab().setDocType("Monograph");
        Process newProcess = new Process();
        Workpiece newWorkPiece = new Workpiece();
        TempProcess tempProcess = new TempProcess(newProcess, newWorkPiece);
        underTest.setProcesses(new LinkedList<>(Collections.singletonList(tempProcess)));
        Project project = ServiceManager.getProjectService().getById(1);
        underTest.getMainProcess().setProject(project);
        underTest.getMainProcess().setRuleset(ServiceManager.getRulesetService().getById(1));
        underTest.getMainProcess().setTitle("title");

        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }
        long before = processService.count();
        underTest.createNewProcess();
        project.getProcesses().add(newProcess);
        ServiceManager.getProjectService().save(project);
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }
        long after = processService.count();
        assertEquals("No process was created!", before + 1, after);

        // clean up database, index and file system
        Integer processId = newProcess.getId();
        processService.deleteProcess(newProcess);
        fileService.delete(URI.create(processId.toString()));
    }

    /**
     * tests creation of processes without workflow.
     */
    @Test
    public void shouldCreateNewProcessWithoutWorkflow() throws Exception {
        CreateProcessForm underTest = new CreateProcessForm();
        underTest.getProcessDataTab().setDocType("MultiVolumeWork");
        Process newProcess = new Process();
        Workpiece newWorkPiece = new Workpiece();
        TempProcess tempProcess = new TempProcess(newProcess, newWorkPiece);
        underTest.setProcesses(new LinkedList<>(Collections.singletonList(tempProcess)));
        Project project = ServiceManager.getProjectService().getById(1);
        underTest.getMainProcess().setProject(project);
        underTest.getMainProcess().setRuleset(ServiceManager.getRulesetService().getById(1));
        underTest.getMainProcess().setTitle("title");

        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        ExecutionPermission.setExecutePermission(script);
        long before = processService.count();
        underTest.createNewProcess();
        project.getProcesses().add(newProcess);
        ServiceManager.getProjectService().save(project);
        ExecutionPermission.setNoExecutePermission(script);
        long after = processService.count();
        assertEquals("No process was created!", before + 1, after);

        assertTrue("Process should not have tasks", newProcess.getTasks().isEmpty());
        assertNull("process should not have sortHelperStatus", newProcess.getSortHelperStatus());

        // clean up database, index and file system
        Integer processId = newProcess.getId();
        processService.deleteProcess(newProcess);
        fileService.delete(URI.create(processId.toString()));
    }
}
