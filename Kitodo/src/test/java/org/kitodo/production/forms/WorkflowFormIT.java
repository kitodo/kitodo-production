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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
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
import org.mockito.ArgumentCaptor;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkflowFormIT {

    private WorkflowForm currentWorkflowForm = new WorkflowForm();
    private static final TaskService taskService = ServiceManager.getTaskService();
    private static final DataEditorSettingService dataEditorSettingService = ServiceManager.getDataEditorSettingService();
    private ExternalContext mockExternalContext;
    private Flash mockFlash;

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
        assertFalse(dataEditorSettingService.areDataEditorSettingsDefinedForWorkflow(workflow));
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
        assertEquals(0.5f, dataEditorSettingForTaskOfSecondTemplate.get(0).getStructureWidth(), 0);
        assertEquals(0.6f, dataEditorSettingForTaskOfSecondTemplate.get(0).getMetadataWidth(), 0);
        assertEquals(0.6f, dataEditorSettingForTaskOfSecondTemplate.get(0).getGalleryWidth(), 0);
    }

    @Test
    public void shouldDuplicateWorkflowAndStoreInFlash() throws Exception {
        // Mock ExternalContext and Flash
        mockExternalContext = mock(ExternalContext.class);
        mockFlash = mock(Flash.class);
        currentWorkflowForm.setExternalContext(mockExternalContext);

        when(mockExternalContext.getFlash()).thenReturn(mockFlash); // Ensure Flash scope is returned

        // Call the duplicate method
        String resultUrl = currentWorkflowForm.duplicate(1);

        // Ensure redirection to the edit page
        assertTrue(resultUrl.contains("&id=0"));

        // Capture arguments that were passed to Flash
        ArgumentCaptor<Workflow> workflowCaptor = ArgumentCaptor.forClass(Workflow.class);
        ArgumentCaptor<String> xmlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> svgCaptor = ArgumentCaptor.forClass(String.class);

        // Verify and capture actual method calls
        verify(mockFlash, times(1)).put(eq("duplicatedWorkflow"), workflowCaptor.capture());
        verify(mockFlash, times(1)).put(eq("xmlDiagram"), xmlCaptor.capture());
        verify(mockFlash, times(1)).put(eq("svgDiagram"), svgCaptor.capture());

        // Assert the captured workflow is not null and has expected values
        Workflow duplicatedWorkflow = workflowCaptor.getValue();
        String xmlWorkflow = xmlCaptor.getValue();
        assertNotNull(duplicatedWorkflow, "Duplicated workflow should not be null");
        assertEquals("gateway-test1", duplicatedWorkflow.getTitle().replaceFirst("_[^_]*$", ""),
                "Expected duplicated workflow title");

        String diagramPath = ConfigCore.getKitodoDiagramDirectory() + "gateway-test1" + ".bpmn20.xml";
        String data = FileUtils.readFileToString(new File(diagramPath), StandardCharsets.UTF_8);
        assertEquals(data, xmlWorkflow, "Expected duplicated workflow title");
        assertNotNull(xmlCaptor.getValue(), "XML Diagram should be stored");
        assertNotNull(svgCaptor.getValue(), "SVG Diagram should be stored");
    }

    private Task createAndSaveTemplateTask(TaskStatus taskStatus, int ordering, Template template) throws DataException {
        Task task = new Task();
        task.setProcessingStatus(taskStatus);
        task.setEditType(TaskEditType.MANUAL_SINGLE);
        task.setOrdering(ordering);
        task.setTemplate(template);
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
