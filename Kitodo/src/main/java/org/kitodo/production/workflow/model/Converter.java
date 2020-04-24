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
import java.util.Objects;

import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.WorkflowCondition;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.model.beans.KitodoScriptTask;
import org.kitodo.production.workflow.model.beans.KitodoTask;
import org.kitodo.production.workflow.model.beans.TaskInfo;

public class Converter {

    private final Reader reader;

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
     * Convert BPMN process (workflow) to template stored in database.
     */
    public void convertWorkflowToTemplate(Template template) throws DAOException, WorkflowException {
        List<org.kitodo.data.database.beans.Task> validatedTasks = validateWorkflowTaskList();

        for (org.kitodo.data.database.beans.Task validatedTask : validatedTasks) {
            if (Objects.nonNull(validatedTask.getWorkflowCondition())) {
                ServiceManager.getWorkflowConditionService().saveToDatabase(validatedTask.getWorkflowCondition());
            }
            validatedTask.setTemplate(template);
            template.getTasks().add(validatedTask);
        }
    }

    /**
     * Validate BPMN process (workflow) list of Task beans.
     *
     * @return list of Task objects
     */
    public List<org.kitodo.data.database.beans.Task> validateWorkflowTaskList() throws WorkflowException {
        reader.readWorkflowTasks();

        Map<Task, TaskInfo> tasks = reader.getTasks();

        List<org.kitodo.data.database.beans.Task> taskBeans = new ArrayList<>();
        for (Map.Entry<Task, TaskInfo> entry : tasks.entrySet()) {
            taskBeans.add(getTask(entry.getKey(), entry.getValue()));
        }

        return taskBeans;
    }

    private org.kitodo.data.database.beans.Task getTask(Task workflowTask, TaskInfo taskInfo)
            throws WorkflowException {
        org.kitodo.data.database.beans.Task task = new org.kitodo.data.database.beans.Task();
        KitodoTask kitodoTask = new KitodoTask(workflowTask);
        task.setWorkflowId(kitodoTask.getWorkflowId());
        task.setTitle(kitodoTask.getTitle());
        task.setOrdering(taskInfo.getOrdering());
        task.setEditType(TaskEditType.getTypeFromValue(kitodoTask.getEditType()));
        task.setProcessingStatus(TaskStatus.getStatusFromValue(kitodoTask.getProcessingStatus()));
        task.setConcurrent(kitodoTask.isConcurrent());
        task.setLast(taskInfo.isLast());
        task.setBatchStep(kitodoTask.isBatchStep());
        task.setRepeatOnCorrection(kitodoTask.isRepeatOnCorrection());
        task.setTypeMetadata(kitodoTask.isTypeMetadata());
        task.setSeparateStructure(kitodoTask.isSeparateStructure());
        task.setTypeAutomatic(kitodoTask.isTypeAutomatic());
        task.setTypeImagesRead(kitodoTask.isTypeImagesRead());
        task.setTypeImagesWrite(kitodoTask.isTypeImagesWrite());
        task.setTypeGenerateImages(kitodoTask.isTypeGenerateImages());
        task.setTypeValidateImages(kitodoTask.isTypeValidateImages());
        task.setTypeExportDMS(kitodoTask.isTypeExportDms());
        task.setTypeAcceptClose(kitodoTask.isTypeAcceptClose());
        task.setTypeCloseVerify(kitodoTask.isTypeCloseVerify());

        if (Objects.nonNull(kitodoTask.getConditionType()) && Objects.nonNull(kitodoTask.getConditionValue())) {
            task.setWorkflowCondition(new WorkflowCondition(kitodoTask.getConditionType(), kitodoTask.getConditionValue()));
        }

        try {
            String[] userRoleIds = kitodoTask.getUserRoles().split(",");
            for (String userRoleString : userRoleIds) {
                int userRoleId = Integer.parseInt(userRoleString.trim());
                try {
                    task.getRoles().add(ServiceManager.getRoleService().getById(userRoleId));
                } catch (DAOException e) {
                    throw new WorkflowException(Helper.getTranslation("workflowExceptionRoleNotFound",
                            Collections.singletonList(task.getTitle())));
                }
            }
        } catch (NullPointerException e) {
            throw new WorkflowException(Helper.getTranslation("workflowExceptionMissingRoleAssignment",
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
