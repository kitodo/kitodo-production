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

package org.kitodo.production.workflow.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.model.beans.KitodoScriptTask;
import org.kitodo.production.workflow.model.beans.KitodoTask;
import org.kitodo.production.workflow.model.beans.TaskInfo;

public class Converter {

    private Reader reader;

    /**
     * Constructor with diagram name as parameter. It sets up reader for xml file
     * with given name.
     *
     * @param diagramName
     *            as String
     * @throws IOException
     *             in case if file for given name doesn't exist
     */
    public Converter(String diagramName) throws IOException {
        reader = new Reader(diagramName);
    }

    /**
     * Constructor with diagram name as parameter. It sets up reader for xml with
     * given content.
     *
     * @param diagramXmlContent
     *            as InputStream
     * @throws IOException
     *             in case if input stream contains incorrect data
     */
    public Converter(InputStream diagramXmlContent) throws IOException {
        reader = new Reader(diagramXmlContent);
    }

    /**
     * Convert BPMN process (workflow) to list of Task beans.
     *
     * @return list of Task objects
     */
    public List<org.kitodo.data.database.beans.Task> convertWorkflowToTaskList()
            throws DAOException, WorkflowException {
        reader.readWorkflowTasks();

        Map<Task, TaskInfo> tasks = reader.getTasks();

        List<org.kitodo.data.database.beans.Task> taskBeans = new ArrayList<>();
        for (Map.Entry<Task, TaskInfo> entry : tasks.entrySet()) {
            taskBeans.add(getTask(entry.getKey(), entry.getValue()));
        }

        return taskBeans;
    }

    /**
     * Convert BPMN process (workflow) to template stored in database.
     */
    public void convertWorkflowToTemplate(Template template) throws DAOException, WorkflowException {
        reader.readWorkflowTasks();

        Map<Task, TaskInfo> tasks = reader.getTasks();

        for (Map.Entry<Task, TaskInfo> entry : tasks.entrySet()) {
            org.kitodo.data.database.beans.Task task = getTask(entry.getKey(), entry.getValue());
            task.setTemplate(template);
            template.getTasks().add(task);
        }
    }

    private org.kitodo.data.database.beans.Task getTask(Task workflowTask, TaskInfo taskInfo)
            throws DAOException, WorkflowException {
        org.kitodo.data.database.beans.Task task = new org.kitodo.data.database.beans.Task();
        KitodoTask kitodoTask = new KitodoTask(workflowTask);
        task.setWorkflowId(kitodoTask.getWorkflowId());
        task.setTitle(kitodoTask.getTitle());
        task.setOrdering(taskInfo.getOrdering());
        task.setPriority(kitodoTask.getPriority());
        task.setEditType(kitodoTask.getEditType());
        task.setProcessingStatus(kitodoTask.getProcessingStatus());
        task.setConcurrent(kitodoTask.isConcurrent());
        task.setLast(taskInfo.isLast());
        task.setBatchStep(kitodoTask.isBatchStep());
        task.setTypeAutomatic(kitodoTask.isTypeAutomatic());
        task.setTypeImagesRead(kitodoTask.isTypeImagesRead());
        task.setTypeImagesWrite(kitodoTask.isTypeImagesWrite());
        task.setTypeExportDMS(kitodoTask.isTypeExportDms());
        task.setTypeAcceptClose(kitodoTask.isTypeAcceptClose());
        task.setTypeCloseVerify(kitodoTask.isTypeCloseVerify());
        task.setWorkflowCondition(taskInfo.getCondition());

        try {
            String[] userRoleIds = kitodoTask.getUserRoles().split(",");
            for (int i = 0; i < userRoleIds.length; i++) {
                int userRoleId = Integer.parseInt(userRoleIds[i].trim());
                task.getRoles().add(ServiceManager.getRoleService().getById(userRoleId));
            }
        } catch (NullPointerException e) {
            throw new WorkflowException(Helper.getTranslation("workflowExceptionMissingRole",
                    Collections.singletonList(task.getTitle())));
        }

        if (workflowTask instanceof ScriptTask) {
            KitodoScriptTask kitodoScriptTask = new KitodoScriptTask((ScriptTask) workflowTask);
            task.setScriptName(kitodoScriptTask.getScriptName());
            task.setScriptPath(kitodoScriptTask.getScriptPath());
        }

        return task;
    }
}
