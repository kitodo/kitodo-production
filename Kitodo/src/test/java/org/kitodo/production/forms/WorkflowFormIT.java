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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.DataEditorSettingService;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.data.WorkflowService;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WorkflowFormIT {

    private WorkflowForm currentWorkflowForm = new WorkflowForm();

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
     * Test
     *
     */
    @Test
    public void testUpdateWorkflow() throws DAOException, DataException, WorkflowException, IOException {
        Workflow workflow = new Workflow("one_step_workflow");
        currentWorkflowForm.setWorkflow(workflow);

        Template firstTemplate = ServiceManager.getTemplateService().getById(1);
        workflow.setTemplates(Arrays.asList(ServiceManager.getTemplateService().getById(1)));
        WorkflowService workflowService = ServiceManager.getWorkflowService();
        workflowService.save(workflow);

        DataEditorSettingService dataEditorSettingService = ServiceManager.getDataEditorSettingService();

        int numberOfTasksBeforeUpdate = firstTemplate.getTasks().size();
        List<DataEditorSetting> dataEditorSettingListBeforeUpdate = dataEditorSettingService.getByTaskId(firstTemplate.getTasks().get(0).getId());
        assertEquals(dataEditorSettingListBeforeUpdate.size(), 1);
        assertEquals(numberOfTasksBeforeUpdate, 5);

        currentWorkflowForm.updateTemplateTasks();

        firstTemplate = ServiceManager.getTemplateService().getById(1);
        int numberOfTasksAfterUpdate = firstTemplate.getTasks().size();
        assertEquals(numberOfTasksAfterUpdate, 1);
        List<DataEditorSetting> dataEditorSettingListAfterUpdate = dataEditorSettingService.getByTaskId(firstTemplate.getTasks().get(0).getId());
        assertEquals(dataEditorSettingListAfterUpdate.size(), 0);
    }

}
