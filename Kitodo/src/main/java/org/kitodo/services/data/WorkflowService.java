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

package org.kitodo.services.data;

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import javax.json.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.WorkflowDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.WorkflowType;
import org.kitodo.data.elasticsearch.index.type.enums.WorkflowTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.TaskDTO;
import org.kitodo.dto.WorkflowDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class WorkflowService extends SearchService<Workflow, WorkflowDTO, WorkflowDAO> {

    private static WorkflowService instance = null;
    private static final Logger logger = LogManager.getLogger(WorkflowService.class);
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Private constructor with Searcher and Indexer assigning.
     */
    private WorkflowService() {
        super(new WorkflowDAO(), new WorkflowType(), new Indexer<>(Workflow.class), new Searcher(Workflow.class));
    }

    /**
     * Return singleton variable of type WorkflowService.
     *
     * @return unique instance of WorkflowService
     */
    public static WorkflowService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (WorkflowService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new WorkflowService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Workflow");
    }

    @Override
    public WorkflowDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject workflowJSONObject = jsonObject.getJsonObject("_source");
        workflowDTO.setTitle(WorkflowTypeField.TITLE.getStringValue(workflowJSONObject));
        workflowDTO.setFileName(WorkflowTypeField.FILE_NAME.getStringValue(workflowJSONObject));
        workflowDTO.setReady(WorkflowTypeField.READY.getBooleanValue(workflowJSONObject));
        workflowDTO.setActive(WorkflowTypeField.ACTIVE.getBooleanValue(workflowJSONObject));

        if (!related) {
            workflowDTO.setTasks(convertRelatedJSONObjectToDTO(workflowJSONObject, WorkflowTypeField.TASKS.getKey(),
                    serviceManager.getTaskService()));
        }
        return workflowDTO;
    }

    /**
     * Method saves or removes tasks related to modified workflow.
     *
     * @param workflow
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Workflow workflow)
            throws CustomResponseException, DAOException, DataException, IOException {
        manageTaskDependenciesForIndex(workflow);
    }

    /**
     * Check IndexAction flag for workflow object. If DELETE remove all tasks from
     * index, if other call saveOrRemoveTaskInIndex() method.
     *
     * @param workflow
     *            object
     */
    private void manageTaskDependenciesForIndex(Workflow workflow)
            throws CustomResponseException, DAOException, IOException, DataException {
        if (workflow.getIndexAction().equals(IndexAction.DELETE)) {
            for (Task task : workflow.getTasks()) {
                serviceManager.getTaskService().removeFromIndex(task, false);
            }
        } else {
            serviceManager.getTaskService().saveOrRemoveTasksInIndex(workflow);
        }
    }

    /**
     * Duplicate the given workflow.
     *
     * @param baseWorkflow
     *            to copy
     * @return the duplicated Workflow
     */
    public Workflow duplicateWorkflow(Workflow baseWorkflow) {
        Workflow duplicatedWorkflow = new Workflow();

        // Workflow _title_ should explicitly _not_ be duplicated!
        duplicatedWorkflow.setFileName(baseWorkflow.getFileName() + "_Copy");
        duplicatedWorkflow.setActive(baseWorkflow.isActive());
        duplicatedWorkflow.setReady(false);

        return duplicatedWorkflow;
    }

    /**
     * Get diagram image for current template.
     *
     * @return diagram image file
     */
    public InputStream getTasksDiagram(String fileName) {
        if (Objects.nonNull(fileName) && !fileName.equals("")) {
            File tasksDiagram = new File(ConfigCore.getKitodoDiagramDirectory(), fileName + ".svg");
            try {
                return new FileInputStream(tasksDiagram);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
                return getEmptyInputStream();
            }
        }
        return getEmptyInputStream();
    }

    private InputStream getEmptyInputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return -1;
            }
        };
    }

    /**
     * Check whether the template contains tasks that are not assigned to a user or
     * user group.
     *
     * @param tasks
     *            list of tasks for testing
     * @return true or false
     */
    public boolean hasCompleteTasks(List<Task> tasks) {
        TaskService taskService = serviceManager.getTaskService();
        if (tasks.isEmpty()) {
            return false;
        }
        for (Task task : tasks) {
            if (taskService.getUserGroupsSize(task) == 0 && taskService.getUsersSize(task) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether the template contains tasks that are not assigned to a user or
     * user group.
     *
     * @param tasks
     *            list of tasks for testing
     * @return true or false
     */
    public boolean hasCompleteTasksDTO(List<TaskDTO> tasks) {
        if (tasks.isEmpty()) {
            return false;
        }
        for (TaskDTO task : tasks) {
            if (task.getUserGroupsSize() == 0 && task.getUsersSize() == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get workflows for given title and file name.
     * 
     * @param title
     *            as String
     * @param file
     *            as String
     * @return list of Workflow objects, desired is that only 1 or 0 workflows are
     *         returned
     */
    public List<Workflow> getWorkflowsForTitleAndFile(String title, String file) {
        return dao.getByTitleAndFile(title, file);
    }

    /**
     * Get available workflows - available means that workflow is active and ready.
     *
     * @return list of available Workflow objects
     */
    public List<Workflow> getAvailableWorkflows() {
        return dao.getAvailableWorkflows();
    }
}
