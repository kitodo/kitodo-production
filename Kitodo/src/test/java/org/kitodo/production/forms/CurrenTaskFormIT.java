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

package org.kitodo.production.forms;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.WorkflowCondition;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;

public class CurrenTaskFormIT {

    private CurrentTaskForm currentTaskForm = new CurrentTaskForm();
    private static final TaskService taskService = ServiceManager.getTaskService();

    /**
     * Setup Database and start elasticsearch.
     * 
     * @throws Exception
     *             If databaseConnection failed.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    /**
     * Cleanup the database and stop elasticsearch.
     *
     * @throws Exception
     *             if elasticsearch could not been stopped.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    /**
     * Test the automatic closing of tasks of type typeAcceptClose.
     * 
     */
    @Test
    public void testCloseTaskOnAccept() throws DataException, DAOException {
        Process process = new Process();
        process.setProcessBaseUri(URI.create("5"));

        ServiceManager.getProcessService().save(process);
        Task taskTypeAcceptClose = createAndSaveTask(TaskStatus.OPEN, 1, process, null, true);
        Task followingTask = createAndSaveTask(TaskStatus.LOCKED, 2, process, null, true);
        currentTaskForm.setTaskById(taskTypeAcceptClose.getId());
        currentTaskForm.takeOverTask();
        Task taskTypeAcceptCloseUpdated = taskService.getById(taskTypeAcceptClose.getId());
        Task followingTaskUpdated = taskService.getById(followingTask.getId());

        assertEquals("Task of type typeAcceptClose was closed!", TaskStatus.DONE,
            taskTypeAcceptCloseUpdated.getProcessingStatus());
        assertEquals("Following task is open!", TaskStatus.OPEN, followingTaskUpdated.getProcessingStatus());

    }

    private Task createAndSaveTask(TaskStatus taskStatus, int ordering, Process process,
            WorkflowCondition workflowCondition, Boolean typeAcceptClose) throws DataException {
        Task task = new Task();
        task.setProcessingStatus(taskStatus);
        task.setEditType(TaskEditType.MANUAL_SINGLE);
        task.setOrdering(ordering);
        task.setProcess(process);
        task.setWorkflowCondition(workflowCondition);
        task.setTypeAcceptClose(typeAcceptClose);
        taskService.save(task);
        return task;
    }

}
