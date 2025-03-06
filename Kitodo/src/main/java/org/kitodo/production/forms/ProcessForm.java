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
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.PropertyType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.MediaNotFoundException;
import org.kitodo.production.controller.SecurityAccessController;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.filters.FilterMenu;
import org.kitodo.production.helper.CustomListColumnInitializer;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.process.ProcessValidator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.KitodoScriptService;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@Named("ProcessForm")
@SessionScoped
public class ProcessForm extends TemplateBaseForm {
    private static final Logger logger = LogManager.getLogger(ProcessForm.class);
    private Process process = new Process();
    private Task task = new Task();
    private Property templateProperty;
    private Property workpieceProperty;
    private String kitodoScriptSelection;
    private String newProcessTitle;
    private List<Property> properties;
    private List<Property> templates;
    private List<Property> workpieces;
    private Property property;
    private final FilterMenu filterMenu = new FilterMenu(this);
    private final transient FileService fileService = ServiceManager.getFileService();
    private final transient ProcessService processService = ServiceManager.getProcessService();
    private final transient WorkflowControllerService workflowControllerService = new WorkflowControllerService();
    private final String processEditPath = MessageFormat.format(REDIRECT_PATH, "processEdit");

    private String processEditReferer = DEFAULT_LINK;
    private String taskEditReferer = DEFAULT_LINK;
    private String errorMessage = "";

    private List<SelectItem> customColumns;

    private static final String CREATE_PROCESS_PATH = "/pages/processFromTemplate.jsf?faces-redirect=true";
    private static final String PROCESS_TABLE_VIEW_ID = "/pages/processes.xhtml";
    private static final String PROCESS_TABLE_ID = "processesTabView:processesForm:processesTable";
    private final Map<Integer, Boolean> assignedProcesses = new HashMap<>();

    @Inject
    private CustomListColumnInitializer initializer;

    /**
     * Constructor.
     */
    public ProcessForm() {
        super();
        ProcessService.emptyCache();
    }

    /**
     * Initialize SelectItems used for configuring displayed columns in process
     * list.
     */
    @PostConstruct
    public void init() {
        columns = new ArrayList<>();

        SelectItemGroup processColumnGroup;
        try {
            processColumnGroup = ServiceManager.getListColumnService()
                    .getListColumnsForListAsSelectItemGroup("process");
            columns.add(processColumnGroup);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }

        // Read process properties to display from configuration
        customColumns = new ArrayList<>();
        SelectItemGroup customColumnGroup = new SelectItemGroup(Helper.getTranslation("process"));
        customColumnGroup.setSelectItems(ServiceManager.getListColumnService().getAllCustomListColumns().stream()
                .map(listColumn -> new SelectItem(listColumn, listColumn.getTitle())).toArray(SelectItem[]::new));
        customColumns.add(customColumnGroup);

        selectedColumns =
                ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("process");
    }

    /**
     * Return list of process properties configured as custom list columns in kitodo
     * configuration.
     *
     * @return array of process property names
     */
    public String[] getProcessPropertyNames() {
        return initializer.getProcessProperties();
    }

    /**
     * Retrieve and return process property value of property with given name
     * 'propertyName' from given Process 'process'.
     *
     * @param process
     *            the Process object from which the property value is retrieved
     * @param propertyName
     *            name of the property for the property value is retrieved
     * @return property value if process has property with name 'propertyName',
     *         empty String otherwise
     */
    public static String getPropertyValue(Process process, String propertyName) {
        return ProcessService.getPropertyValue(process, propertyName);
    }

    /**
     * Calculate and return age of given process as a String.
     *
     * @param process
     *            Process object whose duration/age is calculated
     * @return process age of given process
     */
    public static String getProcessDuration(Process process) {
        return ProcessService.getProcessDuration(process);
    }

    /**
     * Save process and redirect to list view.
     *
     * @return url to list view
     */
    public String save() {
        if (Objects.nonNull(process) && Objects.nonNull(newProcessTitle)) {
            if (!process.getTitle().equals(newProcessTitle)
                    && !renameAfterProcessTitleChanged()) {
                return this.stayOnCurrentPage;
            }

            try {
                ServiceManager.getProcessService().save(this.process);
                return processesPage;
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                    logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_INCOMPLETE_DATA, "processTitleEmpty");
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Create Child for given Process.
     * @param process the process to create a child for.
     * @return path to createProcessForm
     */
    public String createProcessAsChild(Process process) {
        if (Objects.nonNull(process.getTemplate()) && Objects.nonNull(process.getProject())) {
            return CREATE_PROCESS_PATH + "&templateId=" + process.getTemplate().getId() + "&projectId="
                    + process.getProject().getId() + "&parentId=" + process.getId();
        }
        return "processes";
    }

    /**
     * Get diagram image for current template.
     *
     * @return diagram image file
     */
    public InputStream getTasksDiagram() {
        Workflow workflow = this.process.getTemplate().getWorkflow();
        if (Objects.nonNull(workflow)) {
            return ServiceManager.getTemplateService().getTasksDiagram(workflow.getTitle());
        }
        return ServiceManager.getTemplateService().getTasksDiagram("");
    }

    /**
     * Get translation of task status title.
     *
     * @param taskStatusTitle
     *            'statusDone', 'statusLocked' and so on
     * @return translated message for given task status title
     */
    public String getTaskStatusTitle(String taskStatusTitle) {
        return Helper.getTranslation(taskStatusTitle);
    }

    /**
     * Remove content.
     *
     * @return String
     */
    public String deleteContent() {
        try {
            URI ocr = fileService.getOcrDirectory(this.process);
            if (fileService.fileExist(ocr)) {
                fileService.delete(ocr);
            }
            URI images = fileService.getImagesDirectory(this.process);
            if (fileService.fileExist(images)) {
                fileService.delete(images);
            }
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("errorDirectoryDeleting", new Object[] {Helper.getTranslation("metadata") }, logger,
                e);
        }

        Helper.setMessage("Content deleted");
        return this.stayOnCurrentPage;
    }

    private boolean renameAfterProcessTitleChanged() {
        String validateRegEx = ConfigCore.getParameterOrDefaultValue(ParameterCore.VALIDATE_PROCESS_TITLE_REGEX);
        if (!ProcessValidator.isProcessTitleCorrect(newProcessTitle)) {
            Helper.setErrorMessage("processTitleInvalid", new Object[] {validateRegEx });
            return false;
        } else {
            try {
                processService.renameProcess(this.process, this.newProcessTitle);
            } catch (IOException | RuntimeException e) {
                Helper.setErrorMessage("errorRenaming", new Object[] {Helper.getTranslation("directory") }, logger, e);
            }

            // remove Tiffwriter file
            ServiceManager.getKitodoScriptService().deleteTiffHeaderFile(List.of(process));
        }
        return true;
    }

    /**
     * Remove template properties.
     */
    public void deleteTemplateProperty() {
        this.templateProperty.getProcesses().clear();
        this.process.getTemplates().remove(this.templateProperty);
        loadTemplateProperties();
    }

    /**
     * Remove workpiece properties.
     */
    public void deleteWorkpieceProperty() {
        this.workpieceProperty.getProcesses().clear();
        this.process.getWorkpieces().remove(this.workpieceProperty);
        loadWorkpieceProperties();
    }

    /**
     * Create new template property.
     */
    public void createTemplateProperty() {
        if (Objects.isNull(this.templates)) {
            this.templates = new ArrayList<>();
        }
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.templates.add(newProperty);
        this.templateProperty = newProperty;
    }

    /**
     * Create new workpiece property.
     */
    public void createWorkpieceProperty() {
        if (Objects.isNull(this.workpieces)) {
            this.workpieces = new ArrayList<>();
        }
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.workpieces.add(newProperty);
        this.workpieceProperty = newProperty;
    }

    /**
     * Save template property.
     */
    public void saveTemplateProperty() {
        if (!this.process.getTemplates().contains(this.templateProperty)) {
            this.process.getTemplates().add(this.templateProperty);
        }
        loadTemplateProperties();
    }

    /**
     * Save workpiece property.
     */
    public void saveWorkpieceProperty() {
        if (!this.process.getWorkpieces().contains(this.workpieceProperty)) {
            this.process.getWorkpieces().add(this.workpieceProperty);
        }
        loadWorkpieceProperties();
    }

    /**
     * Save task and redirect to processEdit view.
     *
     * @return url to processEdit view
     */
    public String saveTaskAndRedirect() {
        super.saveTask(this.task);
        return processEditPath + "&id=" + (Objects.isNull(this.process.getId()) ? 0 : this.process.getId());
    }

    /**
     * Remove task.
     */
    public void removeTask() {
        this.process.getTasks().remove(this.task);

        List<Role> roles = this.task.getRoles();
        for (Role role : roles) {
            role.getTasks().remove(this.task);
        }
        ProcessService.deleteSymlinksFromUserHomes(this.task);
    }

    /**
     * Remove role from the task.
     *
     * @return stay on the same page
     */
    public String deleteRole() {
        String idParameter = Helper.getRequestParameter(ID_PARAMETER);
        if (Objects.nonNull(idParameter)) {
            try {
                int roleId = Integer.parseInt(idParameter);
                this.task.getRoles().removeIf(role -> role.getId().equals(roleId));
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Add role to the task.
     *
     * @return stay on the same page
     */
    public String addRole() {
        String idParameter = Helper.getRequestParameter(ID_PARAMETER);
        if (Objects.nonNull(idParameter)) {
            int roleId = 0;
            try {
                roleId = Integer.parseInt(idParameter);
                Role role = ServiceManager.getRoleService().getById(roleId);

                if (!this.task.getRoles().contains(role)) {
                    this.task.getRoles().add(role);
                }
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_DATABASE_READING,
                        new Object[] {ObjectType.ROLE.getTranslationSingular(), roleId }, logger, e);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Set up processing status selection.
     */
    public void setTaskStatusUpForSelection() {
        workflowControllerService.setTaskStatusUpForProcesses(getSelectedProcesses());
    }

    /**
     * Set down processing status selection.
     */
    public void setTaskStatusDownForSelection() {
        workflowControllerService.setTaskStatusDownForProcesses(getSelectedProcesses());
    }

    /**
     * Task status up.
     */
    public void setTaskStatusUp() throws DAOException, IOException {
        workflowControllerService.setTaskStatusUp(this.task);
        ProcessService.deleteSymlinksFromUserHomes(this.task);
        refreshParent();
    }

    /**
     * Task status down.
     */
    public void setTaskStatusDown() {
        workflowControllerService.setTaskStatusDown(this.task);
        ProcessService.deleteSymlinksFromUserHomes(this.task);
        refreshParent();
    }

    private void refreshParent() {
        try {
            if (Objects.nonNull(process.getParent())) {
                this.process.setParent(ServiceManager.getProcessService().getById(process.getParent().getId()));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                new Object[] {ObjectType.PROCESS.getTranslationSingular(), process.getParent().getId() }, logger, e);
        }
    }

    /**
     * Get process object.
     *
     * @return process object
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * Set process.
     *
     * @param process
     *            Process object
     */
    public void setProcess(Process process) {
        this.process = process;
        this.newProcessTitle = process.getTitle();
        loadProcessProperties();
        loadTemplateProperties();
        loadWorkpieceProperties();
    }

    /**
     * Get task object.
     *
     * @return Task object
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * Set task.
     *
     * @param task
     *            Task object
     */
    public void setTask(Task task) {
        this.task = task;
        this.task.setLocalizedTitle(ServiceManager.getTaskService().getLocalizedTitle(task.getTitle()));
    }

    public Property getTemplateProperty() {
        return this.templateProperty;
    }

    public void setTemplateProperty(Property templateProperty) {
        this.templateProperty = templateProperty;
    }

    public Property getWorkpieceProperty() {
        return this.workpieceProperty;
    }

    public void setWorkpieceProperty(Property workpieceProperty) {
        this.workpieceProperty = workpieceProperty;
    }

    /**
     * Execute Kitodo script for selected processes.
     */
    public void executeKitodoScriptSelection() {
        executeKitodoScriptForProcesses(getSelectedProcesses(), this.kitodoScriptSelection);
    }

    private void executeKitodoScriptForProcesses(List<Process> processes, String kitodoScript) {
        KitodoScriptService service = ServiceManager.getKitodoScriptService();
        try {
            service.execute(processes, kitodoScript);
        } catch (DAOException | IOException | InvalidImagesException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        } catch (MediaNotFoundException e) {
            Helper.setWarnMessage(e.getMessage());
        }
    }

    /**
     * Get kitodo script for selected results.
     *
     * @return kitodo script for selected results
     */
    public String getKitodoScriptSelection() {
        return this.kitodoScriptSelection;
    }

    /**
     * Set kitodo script for selected results.
     *
     * @param kitodoScriptSelection
     *            the kitodoScript
     */
    public void setKitodoScriptSelection(String kitodoScriptSelection) {
        this.kitodoScriptSelection = kitodoScriptSelection;
    }

    public String getNewProcessTitle() {
        return this.newProcessTitle;
    }

    public void setNewProcessTitle(String newProcessTitle) {
        this.newProcessTitle = newProcessTitle;
    }

    /**
     * Get property for process.
     *
     * @return property for process
     */
    public Property getProperty() {
        return this.property;
    }

    /**
     * Set property for process.
     *
     * @param property
     *            for process as Property object
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * Get list of properties for process.
     *
     * @return list of process properties
     */
    public List<Property> getProperties() {
        return this.properties;
    }

    /**
     * Set list of properties for process.
     *
     * @param properties
     *            for process as Property objects
     */
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Get list of templates for process.
     *
     * @return list of templates for process
     */
    public List<Property> getTemplates() {
        return this.templates;
    }

    /**
     * Set list of templates for process.
     *
     * @param templates
     *            for process as Property objects
     */
    public void setTemplates(List<Property> templates) {
        this.templates = templates;
    }

    /**
     * Get list of workpieces for process.
     *
     * @return list of workpieces for process
     */
    public List<Property> getWorkpieces() {
        return this.workpieces;
    }

    /**
     * Set list of workpieces for process.
     *
     * @param workpieces
     *            for process as Property objects
     */
    public void setWorkpieces(List<Property> workpieces) {
        this.workpieces = workpieces;
    }

    private void loadProcessProperties() {
        this.properties = this.process.getProperties();
    }

    private void loadTemplateProperties() {
        this.templates = this.process.getTemplates();
    }

    private void loadWorkpieceProperties() {
        this.workpieces = this.process.getWorkpieces();
    }

    /**
     * Create new property.
     */
    public void createNewProperty() {
        if (Objects.isNull(this.properties)) {
            this.properties = new ArrayList<>();
        }
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.properties.add(newProperty);
        this.property = newProperty;
    }

    /**
     * Save current property.
     */
    public void saveCurrentProperty() {
        if (!this.process.getProperties().contains(this.property)) {
            this.process.getProperties().add(this.property);
        }
        loadProcessProperties();
    }

    /**
     * Delete property.
     */
    public void deleteProperty() {
        this.property.getProcesses().clear();
        this.process.getProperties().remove(this.property);

        List<Property> propertiesToFilterTitle = this.process.getProperties();
        processService.removePropertiesWithEmptyTitle(propertiesToFilterTitle, this.process);
        loadProcessProperties();
    }

    /**
     * Duplicate property.
     */
    public void duplicateProperty() {
        Property newProperty = ServiceManager.getPropertyService().transfer(this.property);
        newProperty.getProcesses().add(this.process);
        this.process.getProperties().add(newProperty);
        loadProcessProperties();
    }

    /**
     * Get dockets for select list.
     *
     * @return list of dockets
     */
    public List<Docket> getDockets() {
        return ServiceManager.getDocketService().getAllForSelectedClient();
    }

    /**
     * Get list of projects.
     *
     * @return list of projects
     */
    public List<Project> getProjects() {
        return ServiceManager.getProjectService().getAllForSelectedClient();
    }

    /**
     * Get list of OCR-D workflows for select list.
     *
     * @return list of OCR-D workflows
     */
    public List<Pair<?, ?>> getOcrdWorkflows() {
        return ServiceManager.getOcrdWorkflowService().getOcrdWorkflows();
    }

    /**
     * Get the OCR-D workflow.
     *
     * @return Immutable key value pair
     */
    public Pair<?, ?> getOcrdWorkflow() {
        return ServiceManager.getOcrdWorkflowService().getOcrdWorkflow(process.getOcrdWorkflowId());
    }

    /**
     * Get the OCR-D workflow of process template.
     *
     * @return Immutable key value pair
     */
    public Pair<?, ?> getOcrdWorkflowOfTemplate() {
        return ServiceManager.getOcrdWorkflowService().getOcrdWorkflow(process.getTemplate().getOcrdWorkflowId());
    }

    /**
     * Set the OCR-D workflow.
     *
     * @param ocrdWorkflow
     *         The immutable key value pair
     */
    public void setOcrdWorkflow(Pair<?, ?> ocrdWorkflow) {
        String ocrdWorkflowId = StringUtils.EMPTY;
        if (Objects.nonNull(ocrdWorkflow)) {
            ocrdWorkflowId = ocrdWorkflow.getKey().toString();
        }
        process.setOcrdWorkflowId(ocrdWorkflowId);
    }

    /**
     * Get rulesets for select list.
     *
     * @return list of rulesets
     */
    public List<Ruleset> getRulesets() {
        return ServiceManager.getRulesetService().getAllForSelectedClient();
    }

    /**
     * Get task statuses for select list.
     *
     * @return array of task statuses
     */
    public TaskStatus[] getTaskStatuses() {
        return TaskStatus.values();
    }

    /**
     * Method being used as viewAction for process edit form. If the given
     * parameter 'id' is '0', the form for creating a new process will be
     * displayed.
     *
     * @param id
     *            ID of the process to load
     */
    public void load(int id) {
        SecurityAccessController securityAccessController = new SecurityAccessController();
        try {
            if (!securityAccessController.hasAuthorityToEditProcess(id)
                    && !securityAccessController.hasAuthorityToViewProcess(id)) {
                ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                context.redirect(DEFAULT_LINK);
            }
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), id },
                logger, e);
        }
        try {
            if (id != 0) {
                setProcess(ServiceManager.getProcessService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Method being used as viewAction for task form.
     */
    public void loadTask(int id) {
        SecurityAccessController securityAccessController = new SecurityAccessController();
        try {
            if (!securityAccessController.hasAuthorityToEditTask(id)) {
                ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                context.redirect(DEFAULT_LINK);
            }
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
                    logger, e);
        }
        try {
            if (id != 0) {
                setTask(ServiceManager.getTaskService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the task edit page.
     *
     * @param referer
     *            the referring view
     */
    public void setTaskEditReferer(String referer) {
        if (referer.equals("tasks") || referer.equals("processEdit?id=" + this.task.getProcess().getId())) {
            this.taskEditReferer = referer;
        } else {
            this.taskEditReferer = DEFAULT_LINK;
        }
    }

    /**
     * Get task edit page referring view.
     *
     * @return task eit page referring view
     */
    public String getTaskEditReferer() {
        return this.taskEditReferer;
    }

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the process edit page.
     *
     * @param referer
     *            the referring view
     */
    public void setProcessEditReferer(String referer) {
        if (!referer.isEmpty()) {
            if ("processes".equals(referer)) {
                this.processEditReferer = referer;
            } else if ("searchResult".equals(referer)) {
                this.processEditReferer = "searchResult.jsf";
            } else if (!referer.contains("taskEdit") || this.processEditReferer.isEmpty()) {
                this.processEditReferer = DEFAULT_LINK;
            }
        }
    }

    /**
     * Get process edit page referring view.
     *
     * @return process edit page referring view
     */
    public String getProcessEditReferer() {
        return this.processEditReferer;
    }

    /**
     * Changes the filter of the ProcessForm and reloads it.
     *
     * @param filter
     *            the filter to apply.
     * @return reloadpath of th page.
     */
    public String changeFilter(String filter) {
        filterMenu.parseFilters(filter);
        setFilter(filter);
        return filterList();
    }

    private String filterList() {
        this.selectedProcesses.clear();
        return processesPage;
    }

    @Override
    public void setFilter(String filter) {
        super.filter = filter;
        this.lazyBeanModel.setFilterString(filter);
    }

    /**
     * Returns a String containing titles of all current tasks of the given process, e.g. "OPEN" tasks and tasks
     * "INWORK".
     *
     * @param process
     *          process for which current task titles are returned
     * @return String containing titles of current tasks of given process
     */
    public String getCurrentTaskTitles(Process process) {
        return ServiceManager.getProcessService().createProgressTooltip(process);
    }

    /**
     * Get all parent processes recursively for the given process.
     *
     * @return List of Processes
     */
    public List<Process> getAllParentProcesses(int processId) {
        try {
            return ProcessService.getAllParentProcesses(ServiceManager.getProcessService().getById(processId));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), processId }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get number of direct child processes for the given process.
     *
     * @param processId
     *          process id for given process
     * @return number of child processes
     */
    public int getNumberOfChildProcesses(int processId) {
        try {
            return ServiceManager.getProcessService().getNumberOfChildren(processId);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), processId }, logger, e);
            return 0;
        }
    }

    /**
     * Return path to processes page.
     * @return path to processes page
     */
    public String getProcessesPage() {
        return this.processesPage;
    }

    /**
     * Returns the provided date as string in the format of "yyyy-MM-dd HH:mm:ss".
     * @param date the date to be converted
     * @return the converted date as string
     */
    public String convertProcessingDate(Date date) {
        return Helper.getDateAsFormattedString(date);
    }

    /**
     * Get all tasks of given process which should be visible to the user.
     * @param process process as Interface object
     * @return List of filtered tasks as Interface objects
     */
    public List<Task> getCurrentTasksForUser(Process process) {
        return ServiceManager.getProcessService().getCurrentTasksForUser(process,
            ServiceManager.getUserService().getCurrentUser());
    }

    /**
     * Gets the amount of processes for the current filter.
     * 
     * @return amount of processes
     */
    public String getAmount() throws DAOException {
        return Integer.toString(lazyBeanModel.getRowCount());
    }

    /**
     * Resets the process list multi view state such that the sort order and pagination is reset to their defaults.
     */
    public void resetProcessListMultiViewState() {
        if (Objects.nonNull(FacesContext.getCurrentInstance())) {
            // check whether there is a multi view state registered (to avoid warning log message in case there is not)
            Object mvs = PrimeFaces.current().multiViewState().get(PROCESS_TABLE_VIEW_ID, PROCESS_TABLE_ID, false, null);
            if (Objects.nonNull(mvs)) {
                // clear multi view state only if there is a state available
                PrimeFaces.current().multiViewState().clear(PROCESS_TABLE_VIEW_ID, PROCESS_TABLE_ID);
            }
        }
    }

    /**
     * Navigates to processes list and optionally resets table view state.
     *
     * @param resetTableViewState whether to reset table view state
     */
    public String navigateToProcessesList(boolean resetTableViewState) {
        if (resetTableViewState) {
            setFirstRow(0);
            resetProcessListMultiViewState();
        }
        return "/pages/processes?tabIndex=0&faces-redirect=true";
    }

    /**
     * Callback function triggered when a process is unselected in the data table.
     *
     * @param unselectEvent as UnUnselectEvent
     */
    public void onRowUnselect(UnselectEvent<?> unselectEvent) {
        if (allSelected) {
            excludedProcessIds.add(getProcessId(unselectEvent.getObject()));
        }
    }

    /**
     * Callback function triggered when a process is selected in the data table.
     *
     * @param selectEvent as SelectEvent
     */
    public void onRowSelect(SelectEvent<?> selectEvent) {
        if (allSelected) {
            excludedProcessIds.remove(getProcessId(selectEvent.getObject()));
            PrimeFaces.current().executeScript("PF('processesTable').selection=new Array('@all')");
            PrimeFaces.current().executeScript("$(PF('processesTable').selectionHolder).val('@all')");
        }
    }

    /**
     * Callback function triggered when all processes are selected or unselected in the data table.
     */
    public void selectAll() {
        setAllSelected(false);
    }

    private int getProcessId(Object process) {
        if (process instanceof Process) {
            return ((Process) process).getId();
        } else {
            return ((Process) process).getId();
        }
    }

    /**
     * Get filterMenu.
     *
     * @return value of filterMenu
     */
    public FilterMenu getFilterMenu() {
        return filterMenu;
    }

    /**
     * Rename media files of all selected processes.
     */
    public void renameMedia() {
        List<Process> processes = getSelectedProcesses();
        errorMessage = ServiceManager.getFileService().tooManyProcessesSelectedForMediaRenaming(processes.size());
        if (StringUtils.isBlank(errorMessage)) {
            PrimeFaces.current().executeScript("PF('renameMediaConfirmDialog').show();");
        } else {
            Ajax.update("errorDialog");
            PrimeFaces.current().executeScript("PF('errorDialog').show();");
        }
    }

    /**
     * Start renaming media files of selected processes.
     */
    public void startRenaming() {
        ServiceManager.getFileService().renameMedia(getSelectedProcesses());
        PrimeFaces.current().executeScript("PF('notifications').renderMessage({'summary':'"
                + Helper.getTranslation("renamingMediaFilesOfSelectedProcessesStarted")
                + "','severity':'info'})");
    }

    /**
     * Return media renaming confirmation message with number of processes affected.
     *
     * @return media renaming confirmation message
     */
    public String getMediaRenamingConfirmMessage() {
        return Helper.getTranslation("renameMediaForProcessesConfirmMessage",
                String.valueOf(getSelectedProcesses().size()));
    }

    /**
     * Get error message.
     * @return error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Check and return whether process with ID 'processId' belongs to a project assigned to the current user or not.
     * @param processId ID of process to check
     * @return whether process belongs to project assigned to current user or not
     */
    public boolean processInAssignedProject(int processId) {
        try {
            if (!assignedProcesses.containsKey(processId)) {
                assignedProcesses.put(processId, ImportService.processInAssignedProject(processId));
            }
            return assignedProcesses.get(processId);
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
        }
        return false;
    }

    /**
     * Determine whether the last comment should be displayed in the comments column.
     *
     * @return boolean
     */
    public boolean showLastComment() {
        return ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.SHOW_LAST_COMMENT);
    }
}
