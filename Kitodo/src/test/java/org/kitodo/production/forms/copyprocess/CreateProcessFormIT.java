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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.exceptions.ProcessGenerationException;
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
    private Process createdProcess;

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

    @AfterEach
    public void cleanUpAfterEach() throws Exception {
        if (createdProcess != null && createdProcess.getId() != null) {
            processService.remove(createdProcess.getId());
            fileService.delete(URI.create(createdProcess.getId().toString()));
        }
        createdProcess = null;
        setScriptPermissions(false);
    }

    // Helper to create and initialize a CreateProcessForm with common properties
    private CreateProcessForm setupCreateProcessForm(String docType) throws Exception {
        CreateProcessForm form = new CreateProcessForm();
        form.getProcessDataTab().setDocType(docType);

        Process process = new Process();
        Workpiece workPiece = new Workpiece();
        TempProcess tempProcess = new TempProcess(process, workPiece);
        form.setProcesses(new LinkedList<>(Collections.singletonList(tempProcess)));

        form.getMainProcess().setProject(ServiceManager.getProjectService().getById(1));
        form.getMainProcess().setRuleset(ServiceManager.getRulesetService().getById(1));

        form.updateRulesetAndDocType(tempProcess.getProcess().getRuleset());
        return form;
    }

    // Helper to manage script permissions
    private void setScriptPermissions(boolean enable) throws Exception {
        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        if (!SystemUtils.IS_OS_WINDOWS) {
            if (enable) {
                ExecutionPermission.setExecutePermission(script);
            } else {
                ExecutionPermission.setNoExecutePermission(script);
            }
        }
    }

    @Test
    public void shouldCreateNewProcess() throws Exception {
        CreateProcessForm underTest = setupCreateProcessForm("Monograph");
        underTest.getMainProcess().setTitle("title");
        setScriptPermissions(true);
        long before = processService.count();
        underTest.createNewProcess();
        setScriptPermissions(false);

        long after = processService.count();
        createdProcess = underTest.getMainProcess();
        assertEquals(before + 1, after, "No process was created!");
    }

    @Test
    public void shouldCreateNewProcessWithoutWorkflow() throws Exception {
        CreateProcessForm underTest = setupCreateProcessForm("MultiVolumeWork");
        underTest.getMainProcess().setTitle("title");
        setScriptPermissions(true);
        long before = processService.count();
        underTest.createNewProcess();
        setScriptPermissions(false);
        long after = processService.count();
        createdProcess = underTest.getMainProcess();
        assertEquals(before + 1, after, "No process was created!");
        assertTrue(underTest.getMainProcess().getTasks().isEmpty(), "Process should not have tasks");
        assertNull(underTest.getMainProcess().getSortHelperStatus(), "Process should not have sortHelperStatus");
    }

    @Test
    public void shouldThrowExceptionForInvalidTitle() throws Exception {
        // Attempt to create a process with an invalid title
        CreateProcessForm underTest = setupCreateProcessForm("Monograph");
        underTest.getMainProcess().setTitle("title with whitespaces");
        long before = processService.count();
        assertThrows(ProcessGenerationException.class, underTest::createProcessHierarchy,
                "Expected a ProcessGenerationException to be thrown for an invalid title, but it was not.");
        long after = processService.count();
        // Ensure no process was created
        assertEquals(before, after, "A process with an invalid title was created!");
    }


    @Test
    public void shouldNotAllowDuplicateProcessTitles() throws Exception {
        assertDuplicateTitleNotAllowed(1, 1, false);
    }

    @Test
    public void shouldNotAllowProcessTitlesInProjectsTheUserDoesNotBelongTo() throws Exception {
        assertDuplicateTitleNotAllowed(2, 1, true);
    }

    private void assertDuplicateTitleNotAllowed(int firstProjectId, int secondProjectId, boolean switchUserContext) throws Exception {
        // First process creation
        CreateProcessForm underTest = setupCreateProcessForm("Monograph");
        underTest.getMainProcess().setTitle("title");
        underTest.getMainProcess().setProject(ServiceManager.getProjectService().getById(firstProjectId));

        setScriptPermissions(true);
        long before = processService.count();
        underTest.createProcessHierarchy();
        setScriptPermissions(false);

        long after = processService.count();
        createdProcess = underTest.getMainProcess();
        assertEquals(before + 1, after, "First process creation failed. No process was created!");

        // Switch user context to check with a user which does not has access to project 2
        if (switchUserContext) {
            User userTwo = ServiceManager.getUserService().getById(2);
            SecurityTestUtils.addUserDataToSecurityContext(userTwo, 1);
            // Assert that the user 2 is NOT associated with project 2
            assertFalse(ServiceManager.getProjectService()
                    .getById(firstProjectId)
                    .getUsers()
                    .contains(userTwo), "User 2 should not have access to project 2");
        }
        // Second process creation with duplicate title
        CreateProcessForm underTestTwo = setupCreateProcessForm("Monograph");
        underTestTwo.getMainProcess().setTitle("title");
        underTestTwo.getMainProcess().setProject(ServiceManager.getProjectService().getById(secondProjectId));

        long beforeDuplicate = processService.count();
        assertThrows(ProcessGenerationException.class, underTestTwo::createProcessHierarchy,
                "Expected a ProcessGenerationException to be thrown for duplicate title, but it was not.");
        long afterDuplicate = processService.count();
        assertEquals(beforeDuplicate, afterDuplicate, "A duplicate process with the same title was created!");
    }
}
