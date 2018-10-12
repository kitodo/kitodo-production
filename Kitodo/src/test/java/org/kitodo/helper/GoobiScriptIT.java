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

package org.kitodo.helper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.services.ServiceManager;

public class GoobiScriptIT {

    private final ServiceManager serviceManager = new ServiceManager();

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
    public void shouldExecuteAddUserGroupScript() throws Exception {
        GoobiScript goobiScript = new GoobiScript();

        Task task = serviceManager.getTaskService().getById(3);
        int amountOfUserGroups = task.getUserGroups().size();

        String script = "action:addUserGroup \"steptitle:Testing and Blocking\" group:Random";
        List<Process> processes = new ArrayList<>();
        processes.add(serviceManager.getProcessService().getById(1));
        goobiScript.execute(processes, script);

        task = serviceManager.getTaskService().getById(3);
        assertEquals("User group was not correctly added to task!", amountOfUserGroups + 1, task.getUserGroups().size());
    }

    @Test
    public void shouldExecuteSetTaskStatusScript() throws Exception {
        MockDatabase.cleanDatabase();
        MockDatabase.insertProcessesFull();

        GoobiScript goobiScript = new GoobiScript();

        String script = "action:setStepStatus \"steptitle:Testing and Blocking\" status:3";
        List<Process> processes = new ArrayList<>();
        processes.add(serviceManager.getProcessService().getById(1));
        goobiScript.execute(processes, script);

        Task task = serviceManager.getTaskService().getById(3);
        assertEquals("Processing status was not correctly changed!", TaskStatus.DONE, task.getProcessingStatusEnum());
    }

    @Test
    public void shouldExecuteSwapTasksScript() throws Exception {
        MockDatabase.cleanDatabase();
        MockDatabase.insertProcessesFull();

        GoobiScript goobiScript = new GoobiScript();

        String script = "action:swapSteps swap1nr:1 \"swap1title:Blocking\" swap2nr:3 \"swap2title:Progress\"";
        List<Process> processes = new ArrayList<>();
        processes.add(serviceManager.getProcessService().getById(1));
        goobiScript.execute(processes, script);

        Task task = serviceManager.getTaskService().getById(2);
        assertEquals("Task was not swapped correctly!", Integer.valueOf(3), task.getOrdering());
        assertEquals("Task was not swapped correctly!", TaskStatus.INWORK, task.getProcessingStatusEnum());

        task = serviceManager.getTaskService().getById(4);
        assertEquals("Task was not swapped correctly!", Integer.valueOf(1), task.getOrdering());
        assertEquals("Task was not swapped correctly!", TaskStatus.OPEN, task.getProcessingStatusEnum());
    }

    @Test
    public void shouldExecuteAddTaskScript() throws Exception {
        GoobiScript goobiScript = new GoobiScript();

        String script = "action:addStep \"steptitle:Added\" number:4";
        List<Process> processes = new ArrayList<>();
        processes.add(serviceManager.getProcessService().getById(1));
        goobiScript.execute(processes, script);

        List<Task> tasks = serviceManager.getTaskService().getByQuery("FROM Task WHERE title = 'Added'");
        assertEquals("Task was not added!", 1, tasks.size());

        assertEquals("Task was added but with incorrect value!", Integer.valueOf(4), tasks.get(0).getOrdering());
    }

    @Test
    public void shouldExecuteDeleteTaskScript() throws Exception {
        GoobiScript goobiScript = new GoobiScript();

        String script = "action:deleteStep \"steptitle:Blocking\"";
        List<Process> processes = new ArrayList<>();
        processes.add(serviceManager.getProcessService().getById(1));
        goobiScript.execute(processes, script);

        List<Task> tasks = serviceManager.getTaskService().getByQuery("FROM Task WHERE title = 'Blocking'");
        assertEquals("Task was not removed!", 0, tasks.size());

        MockDatabase.cleanDatabase();
        MockDatabase.insertProcessesFull();
    }

    @Test
    public void shouldExecuteAddShellScriptToTaskScript() throws Exception {
        GoobiScript goobiScript = new GoobiScript();

        String script = "action:addShellScriptToStep \"steptitle:Testing and Blocking\" \"label:script\" \"script:/some/new/path\"";
        List<Process> processes = new ArrayList<>();
        processes.add(serviceManager.getProcessService().getById(1));
        goobiScript.execute(processes, script);

        Task task = serviceManager.getTaskService().getById(3);
        assertEquals("Script was not added to task - incorrect name!", "script", task.getScriptName());
        assertEquals("Script was not added to task - incorrect path!", "/some/new/path", task.getScriptPath());
    }

    @Test
    public void shouldExecuteSetPropertyTaskScript() throws Exception {
        GoobiScript goobiScript = new GoobiScript();

        String script = "action:setTaskProperty \"steptitle:Blocking\" property:validate value:true";
        List<Process> processes = new ArrayList<>();
        processes.add(serviceManager.getProcessService().getById(1));
        goobiScript.execute(processes, script);

        Task task = serviceManager.getTaskService().getById(2);
        assertTrue("Task property was not set!", task.isTypeCloseVerify());
    }

    @Test
    public void shouldNotExecuteSetPropertyTaskScript() throws Exception {
        GoobiScript goobiScript = new GoobiScript();

        String script = "action:setTaskProperty \"steptitle:Blocking\" property:validate value:invalid";
        List<Process> processes = new ArrayList<>();
        processes.add(serviceManager.getProcessService().getById(1));
        goobiScript.execute(processes, script);

        Task task = serviceManager.getTaskService().getById(2);
        assertFalse("Task property was set - default value is false!", task.isTypeCloseVerify());
    }
}
