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

package org.kitodo.production.services.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.TreeDeleter;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.services.ServiceManager;

public class KitodoScriptServiceIT {

    private static final File scriptCreateDirMeta = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));

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
    public void shouldCreateProcessFolders() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(scriptCreateDirMeta);
        }

        Process process = ServiceManager.getProcessService().getById(1);
        process.setTitle("FirstProcess");
        ServiceManager.getFileService().createProcessLocation(process);

        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "1");
        File max = new File(processHome, "jpgs/max");
        max.delete();

        KitodoScriptService kitodoScript = new KitodoScriptService();

        String script = "action:createFolders";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        kitodoScript.execute(processes, script);

        assertTrue(max + ": There is no such directory!", max.isDirectory());

        TreeDeleter.deltree(processHome);
    }

    @Test
    public void shouldExecuteAddRoleScript() throws Exception {
        KitodoScriptService kitodoScript = new KitodoScriptService();

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

        KitodoScriptService kitodoScript = new KitodoScriptService();

        String script = "action:setStepStatus \"steptitle:Progress\" status:3";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(8);
        assertEquals("Processing status was not correctly changed!", TaskStatus.DONE, task.getProcessingStatus());
    }

    @Test
    public void shouldExecuteAddShellScriptToTaskScript() throws Exception {
        KitodoScriptService kitodoScript = new KitodoScriptService();

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
        KitodoScriptService kitodoScript = new KitodoScriptService();

        String script = "action:setTaskProperty \"steptitle:Closed\" property:validate value:true";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(7);
        assertTrue("Task property was not set!", task.isTypeCloseVerify());
    }

    @Test
    public void shouldNotExecuteSetPropertyTaskScript() throws Exception {
        KitodoScriptService kitodoScript = new KitodoScriptService();

        String script = "action:setTaskProperty \"steptitle:Closed\" property:validate value:invalid";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(7);
        assertFalse("Task property was set - default value is false!", task.isTypeCloseVerify());
    }

    @Test
    public void shouldGenerateDerivativeImages() throws Exception {

        // Delete created and still running taskmanager tasks from other test suites because
        // this test is assuming that there are no other taskmanager tasks running!
        TaskManager.stopAndDeleteAllTasks();

        Folder generatorSource = new Folder();
        generatorSource.setMimeType("image/tiff");
        generatorSource.setPath("images/(processtitle)_media");
        Process processTwo = ServiceManager.getProcessService().getById(2);
        Project project = processTwo.getProject();
        generatorSource.setProject(project);
        ServiceManager.getFolderService().saveToDatabase(generatorSource);
        project.setGeneratorSource(generatorSource);
        ServiceManager.getProjectService().save(project);
        List<Process> processes = new ArrayList<>();
        processTwo.setTitle("SecondProcess");
        processes.add(processTwo);

        new KitodoScriptService().execute(processes,
            "action:generateImages \"folders:jpgs/max,jpgs/thumbs\" images:all");
        EmptyTask taskImageGeneratorThread = TaskManager.getTaskList().get(0);
        while (taskImageGeneratorThread.isStartable() || taskImageGeneratorThread.isStoppable()) {
            Thread.sleep(400);
        }
        TaskManager.stopAndDeleteAllTasks();

        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "2");
        File maxJpg = new File(processHome, "jpgs/max/00000001.jpg");
        assertTrue(maxJpg.exists());
        File thumbsJpg = new File(processHome, "jpgs/thumbs/00000001.jpg");
        assertTrue(thumbsJpg.exists());

        maxJpg.delete();
        thumbsJpg.delete();
    }
}
