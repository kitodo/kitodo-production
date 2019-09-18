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
import org.kitodo.production.migration.TaskComparator;
import org.kitodo.production.migration.TasksToWorkflowConverter;
import org.kitodo.production.migration.TemplateComparator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;
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
        List<Process> processList = new ArrayList<>();
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
        return taskString.replaceAll("\\s", "");
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
     * Uses the aggregatet processes to create a new Workflow.
     * @param tasks the list of tasks found in the projects
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
            if (tasksAreEqual(template.getTasks(), processTasks)) {
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
     * @return a navigation path.
     */
    public String createNewWorkflow() {

        Process blueprintProcess = aggregatedProcesses.get(currentTasks).get(0);

        TasksToWorkflowConverter templateConverter = new TasksToWorkflowConverter();
        try {
            templateConverter.convertTasksToWorkflowFile(currentTasks, blueprintProcess.getTasks());
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
     * When the navigation to the migration form is coming from a workflow creation the URL contains an WorkflowId.
     * @param workflowId the id of the created Workflow
     */
    public void setRedirectFromWorkflow(Integer workflowId) {
        if (Objects.nonNull(workflowId)) {
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
        templatesToCreate = createTemplatesForProcesses();
        matchingTemplates = getMatchingTemplates(templatesToCreate.keySet());
        PrimeFaces.current().executeScript("PF('createTemplatePopup').show();");
    }

    private Map<Template, List<Process>> createTemplatesForProcesses() {
        List<Process> processesToMigrate = aggregatedProcesses.get(currentTasks);
        Map<Template, List<Process>> newTemplates = new HashMap<>();
        for (Process process : processesToMigrate) {
            if (!templateListContainsTemplate(newTemplates, process)) {
                Template template = new Template();
                template.setDocket(process.getDocket());
                template.setRuleset(process.getRuleset());
                template.setWorkflow(workflowToUse);
                template.setClient(process.getProject().getClient());
                newTemplates.put(template, new ArrayList<>(Arrays.asList(process)));
            }
        }
        return newTemplates;
    }

    private boolean templateListContainsTemplate(Map<Template, List<Process>> newTemplates, Process process) {
        for (Template template : newTemplates.keySet()) {
            if (template.getRuleset().equals(process.getRuleset()) && template.getDocket().equals(process.getDocket())
                    && template.getWorkflow().equals(workflowToUse)) {
                newTemplates.get(template).add(process);
                return true;
            }
        }

        return false;
    }

    private Map<Template, Template> getMatchingTemplates(Set<Template> templatesToCreate) throws DAOException {
        TemplateComparator templateComparator = new TemplateComparator();
        matchingTemplates.clear();
        List<Template> existingTemplates = ServiceManager.getTemplateService().getAll();

        for (Template templateToCreate : templatesToCreate) {
            for (Template existingTemplate : existingTemplates) {
                if (templateComparator.compare(templateToCreate, existingTemplate) == 0) {
                    matchingTemplates.put(templateToCreate, existingTemplate);
                }
            }
        }
        return matchingTemplates;
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
     * @param template The template to which's matching template the processes should be added
     * @param existingTemplate the template to add the processes to
     */
    public void useExistingTemplate(Template template, Template existingTemplate) {
        List<Process> processesToAddToTemplate = templatesToCreate.get(template);
        try {
            addProcessesToTemplate(existingTemplate, processesToAddToTemplate);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
        }
        templatesToCreate.remove(template);
    }

    /**
     * Creates a new template.
     * @param template The template to create.
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
            addProcessesToTemplate(template, processesToAddToTemplate);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
        }
        templatesToCreate.remove(template);
    }

    private void addProcessesToTemplate(Template template, List<Process> processesToAddToTemplate) throws DataException {
        for (Process process : processesToAddToTemplate) {
            process.setTemplate(template);
            ServiceManager.getProcessService().save(process);
        }
    }

}
