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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

public class WorkflowServiceIT {

    private final WorkflowService workflowService = ServiceManager.getWorkflowService();

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

    @Test
    public void shouldGetWorkflow() throws Exception {
        Workflow workflow = workflowService.getById(1);
        boolean condition = workflow.getTitle().equals("test");
        assertTrue(condition, "Workflow was not found in database!");
    }

    @Test
    public void shouldGetAllWorkflows() throws Exception {
        List<Workflow> workflows = workflowService.getAll();
        assertEquals(3, workflows.size(), "Workflows were not found in database!");
    }

    @Test
    public void shouldGetAvailableWorkflows() throws Exception {
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);

        List<Workflow> workflows = workflowService.getAvailableWorkflows();
        assertEquals(1, workflows.size(), "Workflows were not found in database!");

        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldNotSaveNewWorkflowWithExistingTitle() {
        Workflow workflow = new Workflow("test");
        String expectedExceptionMessage = Helper.getTranslation("duplicateWorkflowTitle", "test");
        DAOException dataException = assertThrows(DAOException.class,
                () -> workflowService.saveWorkflow(workflow),
            "Expected DAOException to be thrown when saving a new workflow with an existing title");
        assertEquals(expectedExceptionMessage, dataException.getMessage());
    }
}
