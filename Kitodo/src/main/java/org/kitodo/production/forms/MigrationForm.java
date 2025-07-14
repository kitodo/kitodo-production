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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.WorkflowStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.tasks.HierarchyMigrationTask;
import org.kitodo.production.helper.tasks.MigrationTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.helper.tasks.UpdateInternalMetaInformationTask;
import org.kitodo.production.migration.NewspaperProcessesMigrator;
import org.kitodo.production.migration.TasksToWorkflowConverter;
import org.kitodo.production.security.AESUtil;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.migration.MigrationService;
import org.kitodo.production.workflow.model.Converter;
import org.primefaces.PrimeFaces;

@Named("MigrationForm")
@ApplicationScoped
public class MigrationForm extends BaseForm {

    private static final Logger logger = LogManager.getLogger(MigrationForm.class);
    private List<Project> allProjects = new ArrayList<>();
    private List<Project> selectedProjects = new ArrayList<>();
    private boolean projectListRendered;
    private boolean processListRendered;
    private final Map<String, List<Process>> aggregatedProcesses = new HashMap<>();
    private Workflow workflowToUse;
    private String currentTasks;
    private Map<Template, List<Process>> templatesToCreate = new HashMap<>();
    private Map<Template, Template> matchingTemplates = new HashMap<>();
    private final MigrationService migrationService = ServiceManager.getMigrationService();
    private boolean metadataRendered;
    private boolean workflowRendered;
    private boolean newspaperMigrationRendered = false;
    private Collection<Integer> newspaperBatchesSelectedItems = new ArrayList<>();
    private List<Batch> newspaperBatchesItems;
    private boolean ldapManagerPasswordsMigrationRendered = false;
    private boolean updateInternalMetaInformation = false;

    /**
     * Migrates the meta.xml for all processes in the database (if it's in the
     * old format).
     *
     */
    public void migrateMetadata() {
        try {
            loadProjects();
            projectListRendered = true;
            metadataRendered = true;
            workflowRendered = false;
            newspaperMigrationRendered = false;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error during database access", e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Shows all projects for migration.
     */
    public void showPossibleProjects() {
        try {
            loadProjects();
            projectListRendered = true;
            workflowRendered = true;
            metadataRendered = false;
            newspaperMigrationRendered = false;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error during database access", e.getLocalizedMessage(), logger, e);
        }
    }

    private void loadProjects() throws DAOException {
        allProjects = ServiceManager.getProjectService().getAll()
                .stream().sorted(Comparator.comparing(Project::getTitle)).collect(Collectors.toList());
    }

    /**
     * Shows all processes related to the selected projects.
     */
    public void showAggregatedProcesses() {
        List<Process> processList = new ArrayList<>();
        aggregatedProcesses.clear();
        for (Project project : selectedProjects) {
            logger.trace("Listing processes from project \"{}\"...", project.getTitle());
            processList.addAll(project.getProcesses());
        }
        int numberOfProcesses = processList.size();
        long lastSystemSecond = System.nanoTime() / 1_000_000_000;
        for (int currentProcess = 0; currentProcess < processList.size(); currentProcess++) {
            Process process = processList.get(currentProcess);
            if (logger.isTraceEnabled()) {
                long currentSystemSecond = System.nanoTime() / 1_000_000_000;
                if (currentSystemSecond != lastSystemSecond) {
                    lastSystemSecond = currentSystemSecond;
                    logger.trace("Analyzing process {}/{} ({}% done)", currentProcess, numberOfProcesses,
                        100 * currentProcess / numberOfProcesses);
                }
            }
            if (Objects.isNull(process.getTemplate())) {
                addToAggregatedProcesses(aggregatedProcesses, process);
            }
        }
        processListRendered = true;
    }

    /**
     * Method for migrating hierarchical processes. This is done when the user
     * clicks the button to migrate hierarchical processes under the projects
     * selection.
     */
    public void convertHierarchys() {
        TaskManager.addTask(new HierarchyMigrationTask(selectedProjects));
        projectListRendered = false;
    }

    /**
     * Method for migrating the metadata. This is done when the user clicks the
     * button to migrate metadata under the projects selection.
     */
    public void convertMetadata() {
        for (Project project : selectedProjects) {
            TaskManager.addTask(new MigrationTask(project));
        }
        projectListRendered = false;
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
            new ArrayList<>(Collections.singletonList(process)));
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
    public boolean isMetadataRendered() {
        return metadataRendered;
    }

    /**
     * Get projectListRendered.
     *
     * @return value of projectListRendered
     */
    public boolean isProjectListRendered() {
        return projectListRendered;
    }

    /**
     * Returns whether the switch for creating workflows should be displayed.
     *
     * @return whether the switch for creating workflows should be displayed
     */
    public boolean isWorkflowRendered() {
        return workflowRendered;
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
     * Get processListRendered.
     *
     * @return value of processListRendered
     */
    public boolean isProcessListRendered() {
        return processListRendered;
    }

    /**
     * Get aggregatedTasks. Sorts them in descending order by count,
     * alphabetically for the same count.
     *
     * @return sorted keyset of aggregatedProcesses
     */
    public List<String> getAggregatedTasks() {
        ArrayList<String> aggregatedTasks = new ArrayList<>(aggregatedProcesses.keySet());
        aggregatedTasks.sort((one, another) -> {
            int oneSize = aggregatedProcesses.get(one).size();
            int anotherSize = aggregatedProcesses.get(another).size();
            return oneSize == anotherSize ? one.compareTo(another) : anotherSize - oneSize;
        });
        return aggregatedTasks;
    }

    /**
     * Get tasksWithoutHashCode.
     *
     * @return tasks without hash code
     */
    public String tasksWithoutHashCode(String tasks) {
        return tasks.substring(0, tasks.lastIndexOf(MigrationService.SEPARATOR));
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
     */
    public void convertTasksToWorkflow(String tasks) {
        currentTasks = tasks;
        PrimeFaces.current().executeScript("PF('confirmWorkflowPopup').show();");
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
        String workflowTitle = "ChangeME_" + Helper.generateRandomString(3);

        try {
            templateConverter.convertTasksToWorkflowFile(workflowTitle, processTasks);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }

        workflowToUse = new Workflow(workflowTitle);
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
                workflowToUse = ServiceManager.getWorkflowService().getById(workflowId);
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
        if (migrationService.isTitleValid(template)) {
            try {
                Converter converter = new Converter(template.getWorkflow().getTitle());
                converter.convertWorkflowToTemplate(template);
            } catch (IOException | DAOException | WorkflowException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.PROCESS.getTranslationSingular()},
                        logger, e);
            }

            List<Process> processesToAddToTemplate = templatesToCreate.get(template);
            try {
                ServiceManager.getTemplateService().save(template);
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.TEMPLATE.getTranslationSingular()}, logger,
                        e);
            }
            try {
                migrationService.addProcessesToTemplate(template, processesToAddToTemplate);
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.PROCESS.getTranslationSingular()}, logger,
                        e);
            }
            templatesToCreate.remove(template);
        }
    }

    /**
     * Gets all workflows, possible to use in migration.
     * @return A list of workflows.
     */
    public List<Workflow> getAllWorkflows() {
        return ServiceManager.getWorkflowService().getAllActiveWorkflows();
    }

    /**
     * Get workflowToUse.
     *
     * @return value of workflowToUse
     */
    public Workflow getWorkflowToUse() {
        return workflowToUse;
    }

    /**
     * Set workflowToUse.
     *
     * @param workflowToUse as org.kitodo.data.database.beans.Workflow
     */
    public void setWorkflowToUse(Workflow workflowToUse) {
        this.workflowToUse = workflowToUse;
    }

    /**
     * Action performed when the migrateNewspaperBatches button is clicked.
     */
    public void showPossibleBatches() {
        newspaperMigrationRendered = true;
        projectListRendered = false;
    }

    /**
     * Returns whether the newspaperMigration panel group is rendered.
     *
     * @return whether the newspaperMigration panel group is rendered
     */

    public boolean isNewspaperMigrationRendered() {
        return newspaperMigrationRendered;
    }

    /**
     * Returns the selected items of the newspaperBatches select box.
     *
     * @return the selected items of the newspaperBatches select box
     */
    public Collection<Integer> getNewspaperBatchesSelectedItems() {
        return newspaperBatchesSelectedItems;
    }

    /**
     * Sets the selected items of the newspaperBatches select box.
     *
     * @param selectedItems
     *            elected items of the newspaperBatches select box to set
     */
    public void setNewspaperBatchesSelectedItems(Collection<Integer> selectedItems) {
        newspaperBatchesSelectedItems = selectedItems;
    }

    /**
     * Returns the items of the newspaperBatches select box.
     *
     * @return the items of the newspaperBatches select box
     */
    public List<Batch> getNewspaperBatchesItems() {
        if (Objects.isNull(newspaperBatchesItems)) {
            try {
                newspaperBatchesItems = NewspaperProcessesMigrator.getNewspaperBatches();
            } catch (DAOException | IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                return Collections.emptyList();
            }
        }
        return newspaperBatchesItems;
    }

    /**
     * Action performed when the startNewspaperMigration button is clicked.
     */
    public void startNewspaperMigration() {
        try {
            for (Integer batchId : newspaperBatchesSelectedItems) {
                NewspaperProcessesMigrator.initializeMigration(batchId);
            }
            newspaperMigrationRendered = false;
            newspaperBatchesSelectedItems = new ArrayList<>();
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Action performed when the cancelNewspaperMigration button is clicked.
     */
    public void hideNewspaperMigration() {
        newspaperMigrationRendered = false;
    }

    /**
     * Action performed when the migrateLdapManagerPasswords button is clicked.
     */
    public void showLdapManagerPasswordsMigration() {
        ldapManagerPasswordsMigrationRendered = true;
    }

    /**
     * Returns whether the ldapManagerPasswordsMigration panel group is rendered.
     *
     * @return whether the ldapManagerPasswordsMigration panel group is rendered
     */
    public boolean isLdapManagerPasswordsMigrationRendered() {
        return ldapManagerPasswordsMigrationRendered;
    }

    /**
     * Action performed when the startLdapManagerPasswordsMigration button is
     * clicked.
     */
    public void startLdapManagerPasswordsMigration() {

        String securitySecret = ConfigCore.getParameterOrDefaultValue(ParameterCore.SECURITY_SECRET_LDAPMANAGERPASSWORD);

        if (StringUtils.isBlank(securitySecret)) {
            Helper.setErrorMessage(
                "The security.secret.ldapManagerPassword parameter was not configured in kitodo_config.properties file.");
            return;
        }

        List<LdapServer> ldapServers = getLdapServers();
        ldapServers.parallelStream().forEach(ldapServer -> {
            String managerPassword = ldapServer.getManagerPassword();
            if (StringUtils.isNotBlank(managerPassword) && !AESUtil.isEncrypted(managerPassword)) {
                try {
                    ldapServer.setManagerPassword(AESUtil.encrypt(managerPassword, securitySecret));
                    ServiceManager.getLdapServerService().saveToDatabase(ldapServer);
                } catch (DAOException | NoSuchPaddingException | NoSuchAlgorithmException
                        | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException
                        | IllegalBlockSizeException | InvalidKeySpecException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                }
            }
        });

        Helper.setMessage("All uncrypted LDAP Manager passwords were successfully encrypted.");
    }

    /**
     * Action performed when the cancelLdapManagerPasswordMigration button is
     * clicked.
     */
    public void hideLdapManagerPasswordsMigrationRendered() {
        ldapManagerPasswordsMigrationRendered = false;
    }

    /**
     * Gets all ldap servers.
     *
     * @return list of LdapServer objects.
     */
    public List<LdapServer> getLdapServers() {
        try {
            return ServiceManager.getLdapServerService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {Helper.getTranslation("ldapServers") }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Show projects for updating the project processes internal meta information.
     */
    public void showProjectsForUpdatingInternalMetaInformation() {
        try {
            loadProjects();
            updateInternalMetaInformation = true;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error during database access", e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Should update internal meta information be displayed or not.
     * @return Is update internal meta information content displayed or not.
     */
    public boolean isUpdateInternalMetaInformation() {
        return updateInternalMetaInformation;
    }

    /**
     * Action performed when the cancelUpdateInternalMetaInformation button is clicked.
     */
    public void hideUpdateInternalMetaInformation() {
        selectedProjects.clear();
        updateInternalMetaInformation = false;
    }

    /**
     * Start updating internal meta information in separat tasks.
     */
    public void startUpdateInternalMetaInformation() {
        for (Project project : selectedProjects) {
            TaskManager.addTask(new UpdateInternalMetaInformationTask(project));
        }
        updateInternalMetaInformation = false;
        selectedProjects.clear();
    }
}
