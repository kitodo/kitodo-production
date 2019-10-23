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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
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
import org.kitodo.production.helper.tasks.MigrationTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.migration.TasksToWorkflowConverter;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.migration.MigrationService;
import org.primefaces.PrimeFaces;

@Named("MigrationForm")
@ApplicationScoped
public class MigrationForm extends BaseForm {

    private static final Logger logger = LogManager.getLogger(MigrationForm.class);
    private List<Project> allProjects = new ArrayList<>();
    private List<Project> selectedProjects = new ArrayList<>();
    private boolean projectListShown;
    private boolean processListShown;
    private Map<String, List<Process>> aggregatedProcesses = new HashMap<>();
    private Workflow workflowToUse;
    private String currentTasks;
    private Map<Template, List<Process>> templatesToCreate = new HashMap<>();
    private Map<Template, Template> matchingTemplates = new HashMap<>();
    private MigrationService migrationService = ServiceManager.getMigrationService();
    private boolean metadataShown;
    private boolean workflowShown;

    /**
     * Migrates the meta.xml for all processes in the database (if it's in the
     * old format).
     *
     */
    public void migrateMetadata() {
        try {
            allProjects = ServiceManager.getProjectService().getAll();
            projectListShown = true;
            metadataShown = true;
            workflowShown = false;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error during database access", e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Shows all projects for migration.
     */
    public void showPossibleProjects() {
        try {
            allProjects = ServiceManager.getProjectService().getAll();
            projectListShown = true;
            workflowShown = true;
            metadataShown = false;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error during database access", e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Shows all processes related to the selected projects.
     */
    public void showAggregatedProcesses() {
        List<Process> processList = new ArrayList<>();
        aggregatedProcesses.clear();
        for (Project project : selectedProjects) {
            processList.addAll(project.getProcesses());
        }
        for (Process process : processList) {
            if (Objects.isNull(process.getTemplate())) {
                addToAggregatedProcesses(aggregatedProcesses, process);
            }
        }
        processListShown = true;
    }

    /**
     * Method for migrating the metadata. This is done when the user clicks the
     * button to migrate metadata under the projects selection.
     */
    public void convertMetadata() {
        for (Project project : selectedProjects) {
            TaskManager.addTask(new MigrationTask(project));
        }
        projectListShown = false;
    }

    private void addToAggregatedProcesses(Map<String, List<Process>> aggregatedProcesses, Process process) {
        List<Task> processTasks = process.getTasks();
        processTasks.sort(Comparator.comparingInt(Task::getOrdering));
        for (String tasks : aggregatedProcesses.keySet()) {
            List<Task> aggregatedTasks = aggregatedProcesses.get(tasks).get(0).getTasks();
            aggregatedTasks.sort(Comparator.comparingInt(Task::getOrdering));
            if (checkForTitle(tasks, processTasks) && migrationService
                    .tasksAreEqual(aggregatedTasks, processTasks)) {
                aggregatedProcesses.get(tasks).add(process);
                return;
            }
        }
        aggregatedProcesses.put(migrationService.createTaskString(processTasks),
            new ArrayList<>(Arrays.asList(process)));
    }

    private boolean checkForTitle(String aggregatedTasks, List<Task> processTasks) {
        return aggregatedTasks.equals(migrationService.createTaskString(processTasks));
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
     * Returns whether the switch for starting the metadata migration should be
     * displayed.
     *
     * @return whether the switch for starting the metadata migration should be
     *         displayed
     */
    public boolean isMetadataShown() {
        return metadataShown;
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
     * Returns whether the switch for creating workflows should be displayed.
     *
     * @return whether the switch for creating workflows should be displayed
     */
    public boolean isWorkflowShown() {
        return workflowShown;
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
     * Get processListShown.
     *
     * @return value of processListShown
     */
    public boolean isProcessListShown() {
        return processListShown;
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

    /**
     * Uses the aggregated processes to create a new Workflow.
     *
     * @param tasks
     *            the list of tasks found in the projects
     * @return a navigation path
     */
    public String convertTasksToWorkflow(String tasks) {
        currentTasks = tasks;

        try {
            if (workflowAlreadyExist()) {
                PrimeFaces.current().executeScript("PF('confirmWorkflowPopup').show();");
                return this.stayOnCurrentPage;
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.TEMPLATE.getTranslationSingular() }, logger,
                e);
            return this.stayOnCurrentPage;
        }
        return createNewWorkflow();
    }

    private boolean workflowAlreadyExist() throws DAOException {
        List<Task> processTasks = aggregatedProcesses.get(currentTasks).get(0).getTasks();
        List<Template> allTemplates = ServiceManager.getTemplateService().getAll();
        for (Template template : allTemplates) {
            if (migrationService.tasksAreEqual(template.getTasks(), processTasks)) {
                workflowToUse = template.getWorkflow();
                return true;
            }
        }
        return false;

    }

    /**
     * Use an existing Workflow instead of creating a new one.
     */
    public void useExistingWorkflow() {
        setRedirectFromWorkflow(workflowToUse.getId());
    }

    /**
     * Creates a new Workflow from the aggregated processes.
     *
     * @return a navigation path.
     */
    public String createNewWorkflow() {

        Process blueprintProcess = aggregatedProcesses.get(currentTasks).get(0);
        TasksToWorkflowConverter templateConverter = new TasksToWorkflowConverter();
        List<Task> processTasks = blueprintProcess.getTasks();
        processTasks.sort(Comparator.comparingInt(Task::getOrdering));

        try {
            templateConverter.convertTasksToWorkflowFile(currentTasks, processTasks);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }

        workflowToUse = new Workflow(currentTasks);
        workflowToUse.setClient(blueprintProcess.getProject().getClient());
        workflowToUse.setStatus(WorkflowStatus.DRAFT);
        workflowToUse.getTemplates().add(null);

        try {
            ServiceManager.getWorkflowService().save(workflowToUse);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.WORKFLOW.getTranslationSingular() }, logger,
                e);
            return this.stayOnCurrentPage;
        }

        return MessageFormat.format(REDIRECT_PATH, "workflowEdit") + "&id=" + workflowToUse.getId() + "&migration=true";
    }

    /**
     * When the navigation to the migration form is coming from a workflow
     * creation the URL contains a WorkflowId.
     *
     * @param workflowId
     *            the id of the created Workflow
     */
    public void setRedirectFromWorkflow(Integer workflowId) {
        if (Objects.nonNull(workflowId) && workflowId != 0) {
            // showPopup for Template
            try {
                createTemplates();
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.TEMPLATE.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    private void createTemplates() throws DAOException {
        templatesToCreate = migrationService.createTemplatesForProcesses(aggregatedProcesses.get(currentTasks),
            workflowToUse);
        matchingTemplates.clear();
        matchingTemplates = migrationService.getMatchingTemplates(templatesToCreate.keySet());
        PrimeFaces.current().executeScript("PF('createTemplatePopup').show();");
    }

    /**
     * Get templatesToCreate.
     *
     * @return value of templatesToCreate
     */
    public Set<Template> getTemplatesToCreate() {
        return templatesToCreate.keySet();
    }

    /**
     * Gets a matching template from matchingTemplates.
     *
     * @param template
     *            the template to match.
     * @return the matching template
     */
    public Template getMatchingTemplate(Template template) {
        return matchingTemplates.get(template);
    }

    /**
     * Uses the existing template to add processes to.
     *
     * @param template
     *            The template to which's matching template the processes should
     *            be added
     * @param existingTemplate
     *            the template to add the processes to
     */
    public void useExistingTemplate(Template template, Template existingTemplate) {
        List<Process> processesToAddToTemplate = templatesToCreate.get(template);
        try {
            migrationService.addProcessesToTemplate(existingTemplate, processesToAddToTemplate);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                e);
        }
        templatesToCreate.remove(template);
    }

    /**
     * Creates a new template.
     *
     * @param template
     *            The template to create.
     */
    public void createNewTemplate(Template template) {
        List<Process> processesToAddToTemplate = templatesToCreate.get(template);
        try {
            ServiceManager.getTemplateService().save(template);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TEMPLATE.getTranslationSingular() }, logger,
                e);
        }
        try {
            migrationService.addProcessesToTemplate(template, processesToAddToTemplate);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                e);
        }
        templatesToCreate.remove(template);
    }
}
