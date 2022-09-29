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
import org.kitodo.production.services.data.WorkflowService;

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
    public void testUpdateWorkflow() throws DAOException, DataException, WorkflowException, IOException {
        Workflow workflow = new Workflow("one_step_workflow");
        currentWorkflowForm.setWorkflow(workflow);

        Template firstTemplate = ServiceManager.getTemplateService().getById(1);
        workflow.setTemplates(Arrays.asList(firstTemplate));
        WorkflowService workflowService = ServiceManager.getWorkflowService();
        workflowService.save(workflow);

        //Instantiate DataEditor settings for a task of the second template which should not be deleted
        Task templateTask = createAndSaveTemplateTask(TaskStatus.OPEN, 1,
                ServiceManager.getTemplateService().getById(2));
        createAndSaveDataEditorSetting(templateTask.getId());

        int numberOfTasksBeforeUpdate = firstTemplate.getTasks().size();
        List<DataEditorSetting> dataEditorSettingForFirstTaskBeforeUpdate = dataEditorSettingService.getByTaskId(
                firstTemplate.getTasks().get(0).getId());
        List<DataEditorSetting> completeEditorSettingsBeforeUpdate = dataEditorSettingService.getAll();
        assertEquals(5, numberOfTasksBeforeUpdate);
        assertEquals(1, dataEditorSettingForFirstTaskBeforeUpdate.size());
        assertEquals(4, completeEditorSettingsBeforeUpdate.size());

        currentWorkflowForm.updateTemplateTasks();

        firstTemplate = ServiceManager.getTemplateService().getById(1);
        int numberOfTasksAfterUpdate = firstTemplate.getTasks().size();
        List<DataEditorSetting> dataEditorSettingForFirstTaskAfterUpdate = dataEditorSettingService.getByTaskId(
                firstTemplate.getTasks().get(0).getId());
        List<DataEditorSetting> completeEditorSettingsAfterUpdate = dataEditorSettingService.getAll();
        assertEquals(numberOfTasksAfterUpdate, 1);
        assertEquals(0, dataEditorSettingForFirstTaskAfterUpdate.size());
        assertEquals(1, completeEditorSettingsAfterUpdate.size());
    }

    private Task createAndSaveTemplateTask(TaskStatus taskStatus, int ordering, Template template) throws DataException {
        Task task = new Task();
        task.setProcessingStatus(taskStatus);
        task.setEditType(TaskEditType.MANUAL_SINGLE);
        task.setOrdering(ordering);
        taskService.save(task);
        return task;
    }

    private void createAndSaveDataEditorSetting(int templateTaskId) throws DataException, DAOException {
        DataEditorSetting dataEditorSetting = new DataEditorSetting();
        dataEditorSetting.setUserId(1);
        dataEditorSetting.setTaskId(templateTaskId);
        dataEditorSetting.setStructureWidth(0.2f);
        dataEditorSetting.setMetadataWidth(0.4f);
        dataEditorSetting.setGalleryWidth(0.4f);
        dataEditorSettingService.saveToDatabase(dataEditorSetting);
    }

}
