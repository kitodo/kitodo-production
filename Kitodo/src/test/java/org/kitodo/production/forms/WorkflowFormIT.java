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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.DataEditorSettingService;
import org.kitodo.production.services.data.TaskService;

public class WorkflowFormIT {

    private WorkflowForm currentWorkflowForm = new WorkflowForm();
    private static final TaskService taskService = ServiceManager.getTaskService();
    private static final DataEditorSettingService dataEditorSettingService = ServiceManager.getDataEditorSettingService();

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
        MockDatabase.setUpAwaitility();
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
     * Test the update of a workflow and the deletion of existing editor settings.
     *
     */
    @Test
    public void shouldUpdateTemplateTasksAndDeleteOnlyAffectedDataEditorSettings() throws DAOException, DataException,
            WorkflowException, IOException {
        //Get first template which already has template tasks assigned and assign it to the new workflow
        Template firstTemplate = ServiceManager.getTemplateService().getById(1);
        Workflow workflow = new Workflow("one_step_workflow");
        workflow.setTemplates(Arrays.asList(firstTemplate));
        ServiceManager.getWorkflowService().save(workflow);
        firstTemplate.setWorkflow(workflow);
        ServiceManager.getTemplateService().save(firstTemplate);
        currentWorkflowForm.load(workflow.getId());
        assertEquals(true, dataEditorSettingService.areDataEditorSettingsDefinedForWorkflow(workflow));

        //Get second template (without predefined tasks) and assign a task.
        //Assign data editor settings to this task
        Template secondTemplate = ServiceManager.getTemplateService().getById(2);
        Task templateTask = createAndSaveTemplateTask(TaskStatus.OPEN, 1,
                secondTemplate);
        createAndSaveDataEditorSetting(templateTask.getId());

        int numberOfTasksForFirstTemplateBeforeUpdate = firstTemplate.getTasks().size();
        assertEquals(5, numberOfTasksForFirstTemplateBeforeUpdate);
        List<DataEditorSetting> dataEditorSettingForTaskOfFirstTemplate = dataEditorSettingService.getByTaskId(
                firstTemplate.getTasks().get(0).getId());
        List<DataEditorSetting> dataEditorSettingForTaskOfSecondTemplate = dataEditorSettingService
                .getByTaskId(secondTemplate.getTasks().get(0).getId());
        assertEquals(1, dataEditorSettingForTaskOfFirstTemplate.size());
        assertEquals(1, dataEditorSettingForTaskOfSecondTemplate.size());
        List<DataEditorSetting> completeEditorSettingsBeforeUpdate = dataEditorSettingService.getAll();
        assertEquals(4, completeEditorSettingsBeforeUpdate.size());

        //Do the actual update of the affected template tasks
        currentWorkflowForm.updateTemplateTasks();

        firstTemplate = ServiceManager.getTemplateService().getById(1);
        assertEquals(false, dataEditorSettingService.areDataEditorSettingsDefinedForWorkflow(workflow));
        int numberOfTasksAfterUpdate = firstTemplate.getTasks().size();
        assertEquals(numberOfTasksAfterUpdate, 1);
        dataEditorSettingForTaskOfFirstTemplate = dataEditorSettingService.getByTaskId(
                firstTemplate.getTasks().get(0).getId());
        dataEditorSettingForTaskOfSecondTemplate = dataEditorSettingService
                .getByTaskId(secondTemplate.getTasks().get(0).getId());
        assertEquals(0, dataEditorSettingForTaskOfFirstTemplate.size());
        assertEquals(1, dataEditorSettingForTaskOfSecondTemplate.size());
        List<DataEditorSetting> completeEditorSettingsAfterUpdate = dataEditorSettingService.getAll();
        assertEquals(1, completeEditorSettingsAfterUpdate.size());
        assertEquals(0.5f, dataEditorSettingForTaskOfSecondTemplate.get(0).getStructureWidth(),0);
        assertEquals(0.6f, dataEditorSettingForTaskOfSecondTemplate.get(0).getMetadataWidth(),0);
        assertEquals(0.6f, dataEditorSettingForTaskOfSecondTemplate.get(0).getGalleryWidth(),0);
    }

    private Task createAndSaveTemplateTask(TaskStatus taskStatus, int ordering, Template template) throws DataException {
        Task task = new Task();
        task.setProcessingStatus(taskStatus);
        task.setEditType(TaskEditType.MANUAL_SINGLE);
        task.setOrdering(ordering);
        task.setTemplate(template);
        template.getTasks().add(task);
        taskService.save(task);
        return task;
    }

    private void createAndSaveDataEditorSetting(int templateTaskId) throws DataException, DAOException {
        DataEditorSetting dataEditorSetting = new DataEditorSetting();
        dataEditorSetting.setUserId(1);
        dataEditorSetting.setTaskId(templateTaskId);
        dataEditorSetting.setStructureWidth(0.5f);
        dataEditorSetting.setMetadataWidth(0.6f);
        dataEditorSetting.setGalleryWidth(0.6f);
        dataEditorSettingService.saveToDatabase(dataEditorSetting);
    }

}
