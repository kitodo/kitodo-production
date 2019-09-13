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

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.WorkflowStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.migration.TaskComparator;
import org.kitodo.production.migration.TasksToWorkflowConverter;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

@Named("MigrationForm")
@ViewScoped
public class MigrationForm extends BaseForm {

    private static final Logger logger = LogManager.getLogger(MigrationForm.class);
    private List<Project> allProjects = new ArrayList<>();
    private List<Project> selectedProjects = new ArrayList<>();
    private List<Process> processList = new ArrayList<>();
    private boolean projectListShown;
    private boolean processListShown;
    Map<String, List<Process>> aggregatedProcesses = new HashMap<>();

    /**
     * Migrates the meta.xml for all processes in the database (if it's in the
     * old format).
     *
     * @throws DAOException
     *             if database access fails
     */
    public void migrateMetadata() throws DAOException {
        List<Process> processes = ServiceManager.getProcessService().getAll();
        FileService fileService = ServiceManager.getFileService();
        URI metadataFilePath;
        for (Process process : processes) {
            try {
                metadataFilePath = fileService.getMetadataFilePath(process, true, true);
                ServiceManager.getDataEditorService().readData(metadataFilePath);
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
    }

    /**
     * Shows all projects for migration.
     */
    public void showPossibleProjects() {
        try {
            allProjects = ServiceManager.getProjectService().getAll();
            projectListShown = true;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error during database access");
        }
    }

    /**
     * Shows all processes related to the selected projects.
     */
    public void showAggregatedProcesses() {
        processList.clear();
        aggregatedProcesses.clear();
        for (Project project : selectedProjects) {
            processList.addAll(project.getProcesses());
        }
        for (Process process : processList) {
            addToAggregatedProcesses(aggregatedProcesses, process);
        }
        processListShown = true;
    }

    private void addToAggregatedProcesses(Map<String, List<Process>> aggregatedProcesses, Process process) {
        for (String tasks : aggregatedProcesses.keySet()) {
            if (checkForTitle(tasks, process.getTasks())
                    && tasksAreEqual(aggregatedProcesses.get(tasks).get(0).getTasks(), process.getTasks())) {
                aggregatedProcesses.get(tasks).add(process);
                return;
            }
        }
        aggregatedProcesses.put(createTaskString(process.getTasks()), new ArrayList<>(Arrays.asList(process)));
    }

    boolean tasksAreEqual(List<Task> firstProcessTasks, List<Task> secondProcessTasks) {
        TaskComparator taskComparator = new TaskComparator();

        Iterator<Task> firstTaskIterator = firstProcessTasks.iterator();
        Iterator<Task> secondTaskIterator = secondProcessTasks.iterator();
        while (firstTaskIterator.hasNext()) {
            Task firstTask = firstTaskIterator.next();
            Task secondTask = secondTaskIterator.next();
            if (taskComparator.compare(firstTask, secondTask) != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean checkForTitle(String aggregatedTasks, List<Task> processTasks) {
        return aggregatedTasks.equals(createTaskString(processTasks));
    }

    private String createTaskString(List<Task> processTasks) {
        String taskString = "";
        for (Task processTask : processTasks) {
            taskString = taskString.concat(processTask.getTitle());
        }
        return taskString.replaceAll("\\s","");
    }

    /**
     * Get allProjects.
     *
     * @return value of allProjects
     */
    public List<Project> getAllProjects() {
        return allProjects;
    }

    /**
     * Set selectedProjects.
     *
     * @param selectedProjects
     *            as List of Project
     */
    public void setSelectedProjects(List<Project> selectedProjects) {
        this.selectedProjects = selectedProjects;
    }

    /**
     * Get projectListShown.
     *
     * @return value of projectListShown
     */
    public boolean isProjectListShown() {
        return projectListShown;
    }

    /**
     * Get selectedProjects.
     *
     * @return value of selectedProjects
     */
    public List<Project> getSelectedProjects() {
        return selectedProjects;
    }

    /**
     * Get processList.
     *
     * @return value of processList
     */
    public List<Process> getProcessList() {
        return processList;
    }

    /**
     * Get processListShown.
     *
     * @return value of processListShown
     */
    public boolean isProcessListShown() {
        return processListShown;
    }

    /**
     * Get aggregatedProcesses.
     *
     * @return value of aggregatedProcesses
     */
    public Map<String, List<Process>> getAggregatedProcesses() {
        return aggregatedProcesses;
    }

    /**
     * Set aggregatedProcesses.
     *
     * @param aggregatedProcesses
     *            as java.util.Map
     */
    public void setAggregatedProcesses(Map<String, List<Process>> aggregatedProcesses) {
        this.aggregatedProcesses = aggregatedProcesses;
    }

    /**
     * Get aggregatedTasks.
     *
     * @return keyset of aggregatedProcesses
     */
    public List<String> getAggregatedTasks() {
        return new ArrayList<>(aggregatedProcesses.keySet());
    }

    /**
     * Get numberOfProcesses.
     *
     * @return size of aggregatedProcesses
     */
    public int getNumberOfProcesses(String tasks) {
        return aggregatedProcesses.get(tasks).size();
    }

    public String convertTasksToWorkflow(String tasks) {

        try {
            if (workflowAlreadyExist(tasks)) {
                //show popup
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.TEMPLATE.getTranslationSingular() }, logger,
                e);
            return this.stayOnCurrentPage;
        }

        Process blueprintProcess = aggregatedProcesses.get(tasks).get(0);

        TasksToWorkflowConverter templateConverter = new TasksToWorkflowConverter();
        try {
            templateConverter.convertTasksToWorkflowFile(tasks, blueprintProcess.getTasks());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }

        Workflow workflow = new Workflow(tasks);
        workflow.setClient(blueprintProcess.getProject().getClient());
        workflow.setStatus(WorkflowStatus.DRAFT);
        workflow.getTemplates().add(null);

        try {
            ServiceManager.getWorkflowService().save(workflow);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.WORKFLOW.getTranslationSingular() }, logger,
                    e);
            return this.stayOnCurrentPage;
        }

        return MessageFormat.format(REDIRECT_PATH, "workflowEdit") + "&id=" + workflow.getId();
    }

    private boolean workflowAlreadyExist(String tasks) throws DAOException {
        List<Task> processTasks = aggregatedProcesses.get(tasks).get(0).getTasks();
        List<Template> allTemplates = ServiceManager.getTemplateService().getAll();
        for (Template template : allTemplates) {
            if (tasksAreEqual(template.getTasks(),processTasks)){
                return true;
            }
        }
        return false;

    }
}
