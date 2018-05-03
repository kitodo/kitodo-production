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

public class WorkflowControllerServiceTest {

    private static final WorkflowControllerService workflowControllerService = new ServiceManager().getWorkflowControllerService();

    @BeforeClass
    public static void prepareUser() {
        User user = new User();
        workflowControllerService.setUser(user);
    }

    @Test
    public void shouldSetTaskStatusUp() throws Exception {
        Task task = new Task();
        task.setProcessingStatusEnum(TaskStatus.OPEN);
        task = workflowControllerService.setTaskStatusUp(task);
        assertEquals("Task status was not set up!", TaskStatus.INWORK, task.getProcessingStatusEnum());
    }

    @Test
    public void shouldSetTaskStatusDown() {
        Task task = new Task();
        task.setProcessingStatusEnum(TaskStatus.OPEN);
        task = workflowControllerService.setTaskStatusDown(task);
        assertEquals("Task status was not set down!", TaskStatus.LOCKED, task.getProcessingStatusEnum());
    }
}
