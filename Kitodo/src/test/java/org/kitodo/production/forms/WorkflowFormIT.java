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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.DataEditorSettingService;
import org.kitodo.production.services.data.TaskService;

public class WorkflowFormIT {

    private final WorkflowEditView currentWorkflowForm = new WorkflowEditView();
    private static final TaskService taskService = ServiceManager.getTaskService();
    private static final DataEditorSettingService dataEditorSettingService = ServiceManager.getDataEditorSettingService();

    /**
     * Setup Database and start elasticsearch.
     * 
     * @throws Exception
     *             If databaseConnection failed.
     */
    @BeforeAll
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
    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    /**
     * Test the update of a workflow and the deletion of existing editor settings.
     *
     */
    @Test
    public void shouldUpdateTemplateTasksAndDeleteOnlyAffectedDataEditorSettings() throws Exception {
        //Get first template which already has template tasks assigned and assign it to the new workflow
        Template firstTemplate = ServiceManager.getTemplateService().getById(1);
        Workflow workflow = new Workflow("one_step_workflow");
        workflow.setTemplates(Collections.singletonList(firstTemplate));
        ServiceManager.getWorkflowService().save(workflow);
        firstTemplate.setWorkflow(workflow);
        ServiceManager.getTemplateService().save(firstTemplate);
        currentWorkflowForm.load(workflow.getId(), false);
        assertTrue(dataEditorSettingService.areDataEditorSettingsDefinedForWorkflow(workflow));

        //Get second template (without predefined tasks) and assign a task.
        //Assign data editor settings to this task
        Template secondTemplate = ServiceManager.getTemplateService().getById(2);
        Task templateTask = createAndSaveTemplateTask(TaskStatus.OPEN, 1,
                secondTemplate);
        createAndSaveDataEditorSetting(templateTask.getId());

        int numberOfTasksForFirstTemplateBeforeUpdate = firstTemplate.getTasks().size();
        assertEquals(5, numberOfTasksForFirstTemplateBeforeUpdate);
        List<DataEditorSetting> dataEditorSettingForTaskOfFirstTemplate = dataEditorSettingService.getByTaskId(
                firstTemplate.getTasks().getFirst().getId());
        List<DataEditorSetting> dataEditorSettingForTaskOfSecondTemplate = dataEditorSettingService
                .getByTaskId(secondTemplate.getTasks().getFirst().getId());
        assertEquals(1, dataEditorSettingForTaskOfFirstTemplate.size());
        assertEquals(1, dataEditorSettingForTaskOfSecondTemplate.size());
        List<DataEditorSetting> completeEditorSettingsBeforeUpdate = dataEditorSettingService.getAll();
        assertEquals(4, completeEditorSettingsBeforeUpdate.size());

        //Do the actual update of the affected template tasks
        currentWorkflowForm.updateTemplateTasks();

        firstTemplate = ServiceManager.getTemplateService().getById(1);
        assertFalse(dataEditorSettingService.areDataEditorSettingsDefinedForWorkflow(workflow));
        int numberOfTasksAfterUpdate = firstTemplate.getTasks().size();
        assertEquals(1, numberOfTasksAfterUpdate);
        dataEditorSettingForTaskOfFirstTemplate = dataEditorSettingService.getByTaskId(
                firstTemplate.getTasks().getFirst().getId());
        dataEditorSettingForTaskOfSecondTemplate = dataEditorSettingService
                .getByTaskId(secondTemplate.getTasks().getFirst().getId());
        assertEquals(0, dataEditorSettingForTaskOfFirstTemplate.size());
        assertEquals(1, dataEditorSettingForTaskOfSecondTemplate.size());
        List<DataEditorSetting> completeEditorSettingsAfterUpdate = dataEditorSettingService.getAll();
        assertEquals(1, completeEditorSettingsAfterUpdate.size());
        assertEquals(0.5f, dataEditorSettingForTaskOfSecondTemplate.getFirst().getStructureWidth(), 0);
        assertEquals(0.6f, dataEditorSettingForTaskOfSecondTemplate.getFirst().getMetadataWidth(), 0);
        assertEquals(0.6f, dataEditorSettingForTaskOfSecondTemplate.getFirst().getGalleryWidth(), 0);
    }

    private Task createAndSaveTemplateTask(TaskStatus taskStatus, int ordering, Template template) throws DAOException {
        Task task = new Task();
        task.setProcessingStatus(taskStatus);
        task.setEditType(TaskEditType.MANUAL_SINGLE);
        task.setOrdering(ordering);
        task.setTemplate(template);
        template.getTasks().add(task);
        taskService.save(task);
        return task;
    }

    private void createAndSaveDataEditorSetting(int templateTaskId) throws DAOException {
        DataEditorSetting dataEditorSetting = new DataEditorSetting();
        dataEditorSetting.setUserId(1);
        dataEditorSetting.setTaskId(templateTaskId);
        dataEditorSetting.setStructureWidth(0.5f);
        dataEditorSetting.setMetadataWidth(0.6f);
        dataEditorSetting.setGalleryWidth(0.6f);
        dataEditorSettingService.save(dataEditorSetting);
    }

}
