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

package org.kitodo.services.workflow;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.services.ServiceManager;

import static org.junit.Assert.assertEquals;

public class WorkflowServiceTest {

    private static final WorkflowService workflowService = new ServiceManager().getWorkflowService();

    @BeforeClass
    public static void prepareUser() {
        User user = new User();
        workflowService.setUser(user);
    }

    @Test
    public void shouldSetTaskStatusUp() throws Exception {
        Task task = new Task();
        task.setProcessingStatusEnum(TaskStatus.OPEN);
        task = workflowService.setTaskStatusUp(task);
        assertEquals("Task status was not set up!", TaskStatus.INWORK, task.getProcessingStatusEnum());
    }

    @Test
    public void shouldSetTaskStatusDown() {
        Task task = new Task();
        task.setProcessingStatusEnum(TaskStatus.OPEN);
        task = workflowService.setTaskStatusDown(task);
        assertEquals("Task status was not set down!", TaskStatus.LOCKED, task.getProcessingStatusEnum());
    }
}
