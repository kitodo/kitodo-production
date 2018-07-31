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

package org.kitodo.services.data;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.dto.WorkflowDTO;
import org.kitodo.services.ServiceManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkflowServiceIT {

    private WorkflowService workflowService = new ServiceManager().getWorkflowService();

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

    @Test
    public void shouldGetWorkflow() throws Exception {
        Workflow workflow = workflowService.getById(1);
        boolean condition = workflow.getTitle().equals("say-hello") && workflow.getFileName().equals("test");
        assertTrue("Workflow was not found in database!", condition);

        assertEquals("Workflow was found but tasks were not inserted!", 1, workflow.getTasks().size());
    }

    @Test
    public void shouldFindWorkflow() throws Exception {
        WorkflowDTO workflow = workflowService.findById(1);
        boolean condition = workflow.getTitle().equals("say-hello") && workflow.getFileName().equals("test");
        assertTrue("Workflow was not found in database!", condition);

        assertEquals("Workflow was found but tasks were not inserted!", 1, workflow.getTasks().size());
    }

    @Test
    public void shouldFindAllWorkflows() throws Exception {
        List<WorkflowDTO> workflows = workflowService.findAll();
        assertEquals("Workflows were not found in database!", 3, workflows.size());
    }

    @Test
    public void shouldGetWorkflowsForTitleAndFile() {
        List<Workflow> workflows = workflowService.getWorkflowsForTitleAndFile("say-hello", "test");
        assertEquals("Workflows were not found in database!", 1, workflows.size());
    }

    @Test
    public void shouldGetAvailableWorkflows() {
        List<Workflow> workflows = workflowService.getAvailableWorkflows();
        assertEquals("Workflows were not found in database!", 1, workflows.size());
    }

    @Test
    public void shouldHasCompleteTasks() throws Exception {
        Workflow workflow = workflowService.getById(1);
        boolean condition = workflowService.hasCompleteTasks(workflow.getTasks());
        assertTrue("Workflow doesn't have complete tasks!", condition);

        workflow = workflowService.getById(3);
        condition = workflowService.hasCompleteTasks(workflow.getTasks());
        assertFalse("Workflow has complete tasks!", condition);
    }

    @Test
    public void shouldHasCompleteDTOTasks() throws Exception {
        WorkflowDTO workflowDTO = workflowService.findById(1);
        boolean condition = workflowService.hasCompleteTasksDTO(workflowDTO.getTasks());
        assertTrue("Workflow DTO doesn't have complete tasks!", condition);

        workflowDTO = workflowService.findById(3);
        condition = workflowService.hasCompleteTasksDTO(workflowDTO.getTasks());
        assertFalse("Workflow DTO has complete tasks!", condition);
    }
}
