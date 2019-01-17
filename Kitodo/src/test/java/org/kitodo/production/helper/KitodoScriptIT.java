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

package org.kitodo.production.helper;

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
import org.kitodo.production.services.ServiceManager;

public class KitodoScriptIT {

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
    public void shouldExecuteAddRoleScript() throws Exception {
        KitodoScript kitodoScript = new KitodoScript();

        Task task = ServiceManager.getTaskService().getById(8);
        int amountOfRoles = task.getRoles().size();

        String script = "action:addRole \"steptitle:Progress\" role:General";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        task = ServiceManager.getTaskService().getById(8);
        assertEquals("Role was not correctly added to task!", amountOfRoles + 1, task.getRoles().size());
    }

    @Test
    public void shouldExecuteSetTaskStatusScript() throws Exception {
        MockDatabase.cleanDatabase();
        MockDatabase.insertProcessesFull();

        KitodoScript kitodoScript = new KitodoScript();

        String script = "action:setStepStatus \"steptitle:Progress\" status:3";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(8);
        assertEquals("Processing status was not correctly changed!", TaskStatus.DONE, task.getProcessingStatusEnum());
    }

    @Test
    public void shouldExecuteAddShellScriptToTaskScript() throws Exception {
        KitodoScript kitodoScript = new KitodoScript();

        String script = "action:addShellScriptToStep \"steptitle:Progress\" \"label:script\" \"script:/some/new/path\"";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(8);
        assertEquals("Script was not added to task - incorrect name!", "script", task.getScriptName());
        assertEquals("Script was not added to task - incorrect path!", "/some/new/path", task.getScriptPath());
    }

    @Test
    public void shouldExecuteSetPropertyTaskScript() throws Exception {
        KitodoScript kitodoScript = new KitodoScript();

        String script = "action:setTaskProperty \"steptitle:Closed\" property:validate value:true";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(7);
        assertTrue("Task property was not set!", task.isTypeCloseVerify());
    }

    @Test
    public void shouldNotExecuteSetPropertyTaskScript() throws Exception {
        KitodoScript kitodoScript = new KitodoScript();

        String script = "action:setTaskProperty \"steptitle:Closed\" property:validate value:invalid";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(7);
        assertFalse("Task property was set - default value is false!", task.isTypeCloseVerify());
    }
}
