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
import java.util.Collections;
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
import org.kitodo.data.database.beans.ImportConfiguration;
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
import org.kitodo.production.services.data.FilterService;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.kitodo.utils.Stopwatch;
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
    private String settingImportConfigurationResultMessage;
    private boolean importConfigurationsSetSuccessfully = false;

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
        final Stopwatch stopwatch = new Stopwatch(this, "init");
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
        stopwatch.stop();
    }

    /**
     * Return list of process properties configured as custom list columns in kitodo
     * configuration.
     *
     * @return array of process property names
     */
    public String[] getProcessPropertyNames() {
        Stopwatch stopwatch = new Stopwatch(this, "getProcessPropertyNames");
        return stopwatch.stop(initializer.getProcessProperties());
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
        Stopwatch stopwatch = new Stopwatch(ProcessForm.class, process, "getPropertyValue", "propertyName",
                propertyName);
        return stopwatch.stop(ProcessService.getPropertyValue(process, propertyName));
    }

    /**
     * Calculate and return age of given process as a String.
     *
     * @param process
     *            Process object whose duration/age is calculated
     * @return process age of given process
     */
    public static String getProcessDuration(Process process) {
        Stopwatch stopwatch = new Stopwatch(ProcessForm.class, process, "getProcessDuration");
        return stopwatch.stop(ProcessService.getProcessDuration(process));
    }

    /**
     * Save process and redirect to list view.
     *
     * @return url to list view
     */
    public String save() {
        Stopwatch stopwatch = new Stopwatch(this, "save");
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
        return stopwatch.stop(this.stayOnCurrentPage);
    }

    /**
     * Create Child for given Process.
     * @param process the process to create a child for.
     * @return path to createProcessForm
     */
    public String createProcessAsChild(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "createProcessAsChild");
        if (Objects.nonNull(process.getTemplate()) && Objects.nonNull(process.getProject())) {
            return stopwatch.stop(CREATE_PROCESS_PATH + "&templateId=" + process.getTemplate().getId() + "&projectId="
                    + process.getProject().getId() + "&parentId=" + process.getId());
        }
        return stopwatch.stop("processes");
    }

    /**
     * Get diagram image for current template.
     *
     * @return diagram image file
     */
    public InputStream getTasksDiagram() {
        Stopwatch stopwatch = new Stopwatch(this, "getTasksDiagram");
        Workflow workflow = this.process.getTemplate().getWorkflow();
        if (Objects.nonNull(workflow)) {
            return ServiceManager.getTemplateService().getTasksDiagram(workflow.getTitle());
        }
        return stopwatch.stop(ServiceManager.getTemplateService().getTasksDiagram(""));
    }

    /**
     * Get translation of task status title.
     *
     * @param taskStatusTitle
     *            'statusDone', 'statusLocked' and so on
     * @return translated message for given task status title
     */
    public String getTaskStatusTitle(String taskStatusTitle) {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskStatusTitle", "taskStatusTitle", taskStatusTitle);
        return stopwatch.stop(Helper.getTranslation(taskStatusTitle));
    }

    /**
     * Remove content.
     *
     * @return String
     */
    public String deleteContent() {
        Stopwatch stopwatch = new Stopwatch(this, "deleteContent");
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
        return stopwatch.stop(this.stayOnCurrentPage);
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
        final Stopwatch stopwatch = new Stopwatch(this, "deleteTemplateProperty");
        this.templateProperty.getProcesses().clear();
        this.process.getTemplates().remove(this.templateProperty);
        loadTemplateProperties();
        stopwatch.stop();
    }

    /**
     * Remove workpiece properties.
     */
    public void deleteWorkpieceProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "deleteWorkpieceProperty");
        this.workpieceProperty.getProcesses().clear();
        this.process.getWorkpieces().remove(this.workpieceProperty);
        loadWorkpieceProperties();
        stopwatch.stop();
    }

    /**
     * Create new template property.
     */
    public void createTemplateProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "createTemplateProperty");
        if (Objects.isNull(this.templates)) {
            this.templates = new ArrayList<>();
        }
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.templates.add(newProperty);
        this.templateProperty = newProperty;
        stopwatch.stop();
    }

    /**
     * Create new workpiece property.
     */
    public void createWorkpieceProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "createWorkpieceProperty");
        if (Objects.isNull(this.workpieces)) {
            this.workpieces = new ArrayList<>();
        }
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.workpieces.add(newProperty);
        this.workpieceProperty = newProperty;
        stopwatch.stop();
    }

    /**
     * Save template property.
     */
    public void saveTemplateProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "saveTemplateProperty");
        if (!this.process.getTemplates().contains(this.templateProperty)) {
            this.process.getTemplates().add(this.templateProperty);
        }
        loadTemplateProperties();
        stopwatch.stop();
    }

    /**
     * Save workpiece property.
     */
    public void saveWorkpieceProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "saveWorkpieceProperty");
        if (!this.process.getWorkpieces().contains(this.workpieceProperty)) {
            this.process.getWorkpieces().add(this.workpieceProperty);
        }
        loadWorkpieceProperties();
        stopwatch.stop();
    }

    /**
     * Save task and redirect to processEdit view.
     *
     * @return url to processEdit view
     */
    public String saveTaskAndRedirect() {
        Stopwatch stopwatch = new Stopwatch(this, "saveTaskAndRedirect");
        saveTask(this.task);
        try {
            this.process = ServiceManager.getProcessService().getById(this.process.getId());
            ServiceManager.getProcessService().save(this.process);
            return stopwatch.stop(processEditPath + "&id=" + (Objects.isNull(this.process.getId()) ? 0 : this.process.getId()));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.PROCESS.getTranslationSingular()},
                    logger, e);
        }
        return stopwatch.stop(this.stayOnCurrentPage);
    }

    /**
     * Remove task.
     */
    public void removeTask() {
        final Stopwatch stopwatch = new Stopwatch(this, "removeTask");
        this.process.getTasks().remove(this.task);

        List<Role> roles = this.task.getRoles();
        for (Role role : roles) {
            role.getTasks().remove(this.task);
        }
        ProcessService.deleteSymlinksFromUserHomes(this.task);
        stopwatch.stop();
    }

    /**
     * Remove role from the task.
     *
     * @return stay on the same page
     */
    public String deleteRole() {
        Stopwatch stopwatch = new Stopwatch(this, "deleteRole");
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
        return stopwatch.stop(this.stayOnCurrentPage);
    }

    /**
     * Add role to the task.
     *
     * @return stay on the same page
     */
    public String addRole() {
        Stopwatch stopwatch = new Stopwatch(this, "addRole");
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
        return stopwatch.stop(this.stayOnCurrentPage);
    }

    /**
     * Set up processing status selection.
     */
    public void setTaskStatusUpForSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusUpForSelection");
        workflowControllerService.setTaskStatusUpForProcesses(getSelectedProcesses());
        stopwatch.stop();
    }

    /**
     * Set down processing status selection.
     */
    public void setTaskStatusDownForSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusDownForSelection");
        workflowControllerService.setTaskStatusDownForProcesses(getSelectedProcesses());
        stopwatch.stop();
    }

    /**
     * Task status up.
     */
    public void setTaskStatusUp() throws DAOException, IOException {
        final Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusUp");
        workflowControllerService.setTaskStatusUp(this.task);
        processService.refresh(this.process);
        ProcessService.deleteSymlinksFromUserHomes(this.task);
        refreshParent();
        stopwatch.stop();
    }

    /**
     * Task status down.
     */
    public void setTaskStatusDown() {
        final Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusDown");
        workflowControllerService.setTaskStatusDown(this.task);
        ProcessService.deleteSymlinksFromUserHomes(this.task);
        refreshParent();
        stopwatch.stop();
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
        Stopwatch stopwatch = new Stopwatch(this, "getProcess");
        return stopwatch.stop(this.process);
    }

    /**
     * Set process.
     *
     * @param process
     *            Process object
     */
    public void setProcess(Process process) {
        final Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "setProcess");
        this.process = process;
        this.newProcessTitle = process.getTitle();
        loadProcessProperties();
        loadTemplateProperties();
        loadWorkpieceProperties();
        stopwatch.stop();
    }

    /**
     * Get task object.
     *
     * @return Task object
     */
    public Task getTask() {
        Stopwatch stopwatch = new Stopwatch(this, "getTask");
        return stopwatch.stop(this.task);
    }

    /**
     * Set task.
     *
     * @param task
     *            Task object
     */
    public void setTask(Task task) {
        Stopwatch stopwatch = new Stopwatch(this, "setTask", "task", Objects.toString(task));
        this.task = task;
        this.task.setLocalizedTitle(ServiceManager.getTaskService().getLocalizedTitle(task.getTitle()));
        stopwatch.stop();
    }

    public Property getTemplateProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "getTemplateProperty");
        return stopwatch.stop(this.templateProperty);
    }

    /**
     * Sets the template property.
     * 
     * @param templateProperty
     *            template property to set
     */
    public void setTemplateProperty(Property templateProperty) {
        Stopwatch stopwatch = new Stopwatch(this, "setTemplateProperty", "templateProperty", Objects.toString(
            templateProperty));
        this.templateProperty = templateProperty;
        stopwatch.stop();
    }

    public Property getWorkpieceProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "getWorkpieceProperty");
        return stopwatch.stop(this.workpieceProperty);
    }

    /**
     * Sets the workpiece property.
     * 
     * @param workpieceProperty
     *            workpiece property to set
     */
    public void setWorkpieceProperty(Property workpieceProperty) {
        Stopwatch stopwatch = new Stopwatch(this, "setWorkpieceProperty", "workpieceProperty", Objects.toString(
            workpieceProperty));
        this.workpieceProperty = workpieceProperty;
        stopwatch.stop();
    }

    /**
     * Execute Kitodo script for selected processes.
     */
    public void executeKitodoScriptSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "executeKitodoScriptSelection");
        executeKitodoScriptForProcesses(getSelectedProcesses(), this.kitodoScriptSelection);
        stopwatch.stop();
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
        Stopwatch stopwatch = new Stopwatch(this, "getKitodoScriptSelection");
        return stopwatch.stop(this.kitodoScriptSelection);
    }

    /**
     * Set kitodo script for selected results.
     *
     * @param kitodoScriptSelection
     *            the kitodoScript
     */
    public void setKitodoScriptSelection(String kitodoScriptSelection) {
        Stopwatch stopwatch = new Stopwatch(this, "setKitodoScriptSelection", "kitodoScriptSelection",
                kitodoScriptSelection);
        this.kitodoScriptSelection = kitodoScriptSelection;
        stopwatch.stop();
    }

    public String getNewProcessTitle() {
        Stopwatch stopwatch = new Stopwatch(this, "getNewProcessTitle");
        return stopwatch.stop(this.newProcessTitle);
    }

    /**
     * Sets a new process title.
     * 
     * @param newProcessTitle
     *            new process title to set
     */
    public void setNewProcessTitle(String newProcessTitle) {
        Stopwatch stopwatch = new Stopwatch(this, "setNewProcessTitle", "newProcessTitle", newProcessTitle);
        this.newProcessTitle = newProcessTitle;
        stopwatch.stop();
    }

    /**
     * Get property for process.
     *
     * @return property for process
     */
    public Property getProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "getProperty");
        return stopwatch.stop(this.property);
    }

    /**
     * Set property for process.
     *
     * @param property
     *            for process as Property object
     */
    public void setProperty(Property property) {
        Stopwatch stopwatch = new Stopwatch(this, "setProperty", "property", Objects.toString(property));
        this.property = property;
        stopwatch.stop();
    }

    /**
     * Get list of properties for process.
     *
     * @return list of process properties
     */
    public List<Property> getProperties() {
        Stopwatch stopwatch = new Stopwatch(this, "getProperties");
        return stopwatch.stop(this.properties);
    }

    /**
     * Set list of properties for process.
     *
     * @param properties
     *            for process as Property objects
     */
    public void setProperties(List<Property> properties) {
        Stopwatch stopwatch = new Stopwatch(this, "setProperties", "properties", Objects.toString(properties));
        this.properties = properties;
        stopwatch.stop();
    }

    /**
     * Get list of templates for process.
     *
     * @return list of templates for process
     */
    public List<Property> getTemplates() {
        Stopwatch stopwatch = new Stopwatch(this, "getTemplates");
        return stopwatch.stop(this.templates);
    }

    /**
     * Set list of templates for process.
     *
     * @param templates
     *            for process as Property objects
     */
    public void setTemplates(List<Property> templates) {
        Stopwatch stopwatch = new Stopwatch(this, "setTemplates", "templates", Objects.toString(templates));
        this.templates = templates;
        stopwatch.stop();
    }

    /**
     * Get list of workpieces for process.
     *
     * @return list of workpieces for process
     */
    public List<Property> getWorkpieces() {
        Stopwatch stopwatch = new Stopwatch(this, "getWorkpieces");
        return stopwatch.stop(this.workpieces);
    }

    /**
     * Set list of workpieces for process.
     *
     * @param workpieces
     *            for process as Property objects
     */
    public void setWorkpieces(List<Property> workpieces) {
        Stopwatch stopwatch = new Stopwatch(this, "setWorkpieces", "workpieces", Objects.toString(workpieces));
        this.workpieces = workpieces;
        stopwatch.stop();
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
        final Stopwatch stopwatch = new Stopwatch(this, "createNewProperty");
        if (Objects.isNull(this.properties)) {
            this.properties = new ArrayList<>();
        }
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.properties.add(newProperty);
        this.property = newProperty;
        stopwatch.stop();
    }

    /**
     * Save current property.
     */
    public void saveCurrentProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "saveCurrentProperty");
        if (!this.process.getProperties().contains(this.property)) {
            this.process.getProperties().add(this.property);
        }
        loadProcessProperties();
        stopwatch.stop();
    }

    /**
     * Delete property.
     */
    public void deleteProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "deleteProperty");
        this.property.getProcesses().clear();
        this.process.getProperties().remove(this.property);

        List<Property> propertiesToFilterTitle = this.process.getProperties();
        processService.removePropertiesWithEmptyTitle(propertiesToFilterTitle, this.process);
        loadProcessProperties();
        stopwatch.stop();
    }

    /**
     * Duplicate property.
     */
    public void duplicateProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "duplicateProperty");
        Property newProperty = ServiceManager.getPropertyService().transfer(this.property);
        newProperty.getProcesses().add(this.process);
        this.process.getProperties().add(newProperty);
        loadProcessProperties();
        stopwatch.stop();
    }

    /**
     * Get dockets for select list.
     *
     * @return list of dockets
     */
    public List<Docket> getDockets() {
        Stopwatch stopwatch = new Stopwatch(this, "getDockets");
        return stopwatch.stop(ServiceManager.getDocketService().getAllForSelectedClient());
    }

    /**
     * Get list of projects.
     *
     * @return list of projects
     */
    public List<Project> getProjects() {
        Stopwatch stopwatch = new Stopwatch(this, "getProjects");
        return stopwatch.stop(ServiceManager.getProjectService().getAllForSelectedClient());
    }

    /**
     * Get list of OCR-D workflows for select list.
     *
     * @return list of OCR-D workflows
     */
    public List<Pair<?, ?>> getOcrdWorkflows() {
        Stopwatch stopwatch = new Stopwatch(this, "getOcrdWorkflows");
        return stopwatch.stop(ServiceManager.getOcrdWorkflowService().getOcrdWorkflows());
    }

    /**
     * Get the OCR-D workflow.
     *
     * @return Immutable key value pair
     */
    public Pair<?, ?> getOcrdWorkflow() {
        Stopwatch stopwatch = new Stopwatch(this, "getOcrdWorkflow");
        return stopwatch.stop(ServiceManager.getOcrdWorkflowService().getOcrdWorkflow(process.getOcrdWorkflowId()));
    }

    /**
     * Get the OCR-D workflow of process template.
     *
     * @return Immutable key value pair
     */
    public Pair<?, ?> getOcrdWorkflowOfTemplate() {
        Stopwatch stopwatch = new Stopwatch(this, "getOcrdWorkflowOfTemplate");
        return stopwatch.stop(ServiceManager.getOcrdWorkflowService().getOcrdWorkflow(process.getTemplate()
                .getOcrdWorkflowId()));
    }

    /**
     * Set the OCR-D workflow.
     *
     * @param ocrdWorkflow
     *         The immutable key value pair
     */
    public void setOcrdWorkflow(Pair<?, ?> ocrdWorkflow) {
        Stopwatch stopwatch = new Stopwatch(this, "setOcrdWorkflow");
        String ocrdWorkflowId = StringUtils.EMPTY;
        if (Objects.nonNull(ocrdWorkflow)) {
            ocrdWorkflowId = ocrdWorkflow.getKey().toString();
        }
        process.setOcrdWorkflowId(ocrdWorkflowId);
        stopwatch.stop();
    }

    /**
     * Get rulesets for select list.
     *
     * @return list of rulesets
     */
    public List<Ruleset> getRulesets() {
        Stopwatch stopwatch = new Stopwatch(this, "getRulesets");
        return stopwatch.stop(ServiceManager.getRulesetService().getAllForSelectedClient());
    }

    /**
     * Get list of all import configurations.
     *
     * @return list of all import configurations.
     */
    public List<ImportConfiguration> getImportConfigurations() {
        Stopwatch stopwatch = new Stopwatch(this, "getImportConfigurations");
        try {
            return stopwatch.stop(ServiceManager.getImportConfigurationService().getAll());
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            return stopwatch.stop(Collections.emptyList());
        }
    }

    /**
     * Get task statuses for select list.
     *
     * @return array of task statuses
     */
    public TaskStatus[] getTaskStatuses() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskStatuses");
        return stopwatch.stop(TaskStatus.values());
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
        Stopwatch stopwatch = new Stopwatch(this, "load");
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
        stopwatch.stop();
    }

    /**
     * Method being used as viewAction for task form.
     */
    public void loadTask(int id) {
        Stopwatch stopwatch = new Stopwatch(this, "loadTask");
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
        stopwatch.stop();
    }

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the task edit page.
     *
     * @param referer
     *            the referring view
     */
    public void setTaskEditReferer(String referer) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskEditReferer", "referer", referer);
        if (referer.equals("tasks") || referer.equals("processEdit?id=" + this.task.getProcess().getId())) {
            this.taskEditReferer = referer;
        } else {
            this.taskEditReferer = DEFAULT_LINK;
        }
        stopwatch.stop();
    }

    /**
     * Get task edit page referring view.
     *
     * @return task eit page referring view
     */
    public String getTaskEditReferer() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskEditReferer");
        return stopwatch.stop(this.taskEditReferer);
    }

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the process edit page.
     *
     * @param referer
     *            the referring view
     */
    public void setProcessEditReferer(String referer) {
        Stopwatch stopwatch = new Stopwatch(this, "setProcessEditReferer", "referer", referer);
        if (!referer.isEmpty()) {
            if ("processes".equals(referer)) {
                this.processEditReferer = referer;
            } else if (!referer.contains("taskEdit") || this.processEditReferer.isEmpty()) {
                this.processEditReferer = DEFAULT_LINK;
            }
        }
        stopwatch.stop();
    }

    /**
     * Get process edit page referring view.
     *
     * @return process edit page referring view
     */
    public String getProcessEditReferer() {
        Stopwatch stopwatch = new Stopwatch(this, "getProcessEditReferer");
        return stopwatch.stop(this.processEditReferer);
    }

    /**
     * Changes the filter of the ProcessForm and reloads it.
     *
     * @param filter
     *            the filter to apply.
     * @return reloadpath of th page.
     */
    public String changeFilter(String filter) {
        Stopwatch stopwatch = new Stopwatch(this, "changeFilter");
        filterMenu.parseFilters(filter);
        setFilter(filter);
        return stopwatch.stop(filterList());
    }

    // $$$$
    private String filterList() {
        this.selectedProcesses.clear();
        return processesPage;
    }

    @Override
    public void setFilter(String filter) {
        Stopwatch stopwatch = new Stopwatch(this, "setFilter", "filter", filter);
        super.filter = filter;
        this.lazyBeanModel.setFilterString(filter);
        stopwatch.stop();
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
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "getCurrentTaskTitles");
        return stopwatch.stop(ServiceManager.getProcessService().createProgressTooltip(process));

    }

    /**
     * Get all parent processes recursively for the given process.
     *
     * @return List of Processes
     */
    public List<Process> getAllParentProcesses(int processId) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), processId, "getAllParentProcesses");
        try {
            return stopwatch.stop(ProcessService.getAllParentProcesses(ServiceManager.getProcessService().getById(
                processId)));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), processId }, logger, e);
            return stopwatch.stop(new ArrayList<>());
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
        Stopwatch stopwatch = new Stopwatch(this.getClass(), processId, "getNumberOfChildProcesses");
        try {
            return stopwatch.stop(ServiceManager.getProcessService().getNumberOfChildren(processId));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), processId }, logger, e);
            return stopwatch.stop(0);
        }
    }

    /**
     * Return path to processes page.
     * @return path to processes page
     */
    public String getProcessesPage() {
        Stopwatch stopwatch = new Stopwatch(this, "getProcessesPage");
        return stopwatch.stop(this.processesPage);
    }

    /**
     * Returns the provided date as string in the format of "yyyy-MM-dd HH:mm:ss".
     * @param date the date to be converted
     * @return the converted date as string
     */
    public String convertProcessingDate(Date date) {
        Stopwatch stopwatch = new Stopwatch(this, "convertProcessingDate");
        return stopwatch.stop(Helper.getDateAsFormattedString(date));
    }

    /**
     * Get all tasks of given process which should be visible to the user.
     * @param process process as Interface object
     * @return List of filtered tasks as Interface objects
     */
    public List<Task> getCurrentTasksForUser(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "getCurrentTasksForUser");
        return stopwatch.stop(ServiceManager.getProcessService().getCurrentTasksForUser(process, ServiceManager
                .getUserService().getCurrentUser()));
    }

    /**
     * Gets the amount of processes for the current filter.
     * 
     * @return amount of processes
     */
    public String getAmount() throws DAOException {
        Stopwatch stopwatch = new Stopwatch(this, "getAmount");
        HashMap<String, String> filterMap = new HashMap<>();
        if (!StringUtils.isBlank(this.filter)) {
            filterMap.put(FilterService.FILTER_STRING, this.filter);
        }
        return stopwatch.stop(ServiceManager.getProcessService().countResults(filterMap,
                isShowClosedProcesses(), isShowInactiveProjects()).toString());
    }

    /**
     * Resets the process list multi view state such that the sort order and pagination is reset to their defaults.
     */
    public void resetProcessListMultiViewState() {
        Stopwatch stopwatch = new Stopwatch(this, "resetProcessListMultiViewState");
        if (Objects.nonNull(FacesContext.getCurrentInstance())) {
            // check whether there is a multi view state registered (to avoid warning log message in case there is not)
            Object mvs = PrimeFaces.current().multiViewState().get(PROCESS_TABLE_VIEW_ID, PROCESS_TABLE_ID, false, null);
            if (Objects.nonNull(mvs)) {
                // clear multi view state only if there is a state available
                PrimeFaces.current().multiViewState().clear(PROCESS_TABLE_VIEW_ID, PROCESS_TABLE_ID);
            }
        }
        stopwatch.stop();
    }

    /**
     * Navigates to processes list and optionally resets table view state.
     *
     * @param resetTableViewState whether to reset table view state
     */
    public String navigateToProcessesList(boolean resetTableViewState) {
        Stopwatch stopwatch = new Stopwatch(this, "navigateToProcessesList", "resetTableViewState", Boolean.toString(
            resetTableViewState));
        if (resetTableViewState) {
            setFirstRow(0);
            resetProcessListMultiViewState();
        }
        return stopwatch.stop("/pages/processes?tabIndex=0&faces-redirect=true");
    }

    /**
     * Callback function triggered when a process is unselected in the data table.
     *
     * @param unselectEvent as UnUnselectEvent
     */
    public void onRowUnselect(UnselectEvent<?> unselectEvent) {
        Stopwatch stopwatch = new Stopwatch(this, "onRowUnselect");
        if (allSelected) {
            excludedProcessIds.add(((Process) unselectEvent.getObject()).getId());
        }
        stopwatch.stop();
    }

    /**
     * Callback function triggered when a process is selected in the data table.
     *
     * @param selectEvent as SelectEvent
     */
    public void onRowSelect(SelectEvent<?> selectEvent) {
        Stopwatch stopwatch = new Stopwatch(this, "onRowSelect");
        if (allSelected) {
            excludedProcessIds.remove(((Process) selectEvent.getObject()).getId());
            PrimeFaces.current().executeScript("PF('processesTable').selection=new Array('@all')");
            PrimeFaces.current().executeScript("$(PF('processesTable').selectionHolder).val('@all')");
        }
        stopwatch.stop();
    }

    /**
     * Callback function triggered when all processes are selected or unselected in the data table.
     */
    public void selectAll() {
        Stopwatch stopwatch = new Stopwatch(this, "selectAll");
        setAllSelected(false);
        stopwatch.stop();
    }

    /**
     * Get filterMenu.
     *
     * @return value of filterMenu
     */
    public FilterMenu getFilterMenu() {
        Stopwatch stopwatch = new Stopwatch(this, "getFilterMenu");
        return stopwatch.stop(filterMenu);
    }

    /**
     * Rename media files of all selected processes.
     */
    public void renameMedia() {
        Stopwatch stopwatch = new Stopwatch(this, "renameMedia");
        List<Process> processes = getSelectedProcesses();
        errorMessage = ServiceManager.getFileService().tooManyProcessesSelectedForMediaRenaming(processes.size());
        if (StringUtils.isBlank(errorMessage)) {
            PrimeFaces.current().executeScript("PF('renameMediaConfirmDialog').show();");
        } else {
            Ajax.update("errorDialog");
            PrimeFaces.current().executeScript("PF('errorDialog').show();");
        }
        stopwatch.stop();
    }

    /**
     * Start renaming media files of selected processes.
     */
    public void startRenaming() {
        Stopwatch stopwatch = new Stopwatch(this, "startRenaming");
        ServiceManager.getFileService().renameMedia(getSelectedProcesses());
        PrimeFaces.current().executeScript("PF('notifications').renderMessage({'summary':'"
                + Helper.getTranslation("renamingMediaFilesOfSelectedProcessesStarted")
                + "','severity':'info'})");
        stopwatch.stop();
    }

    /**
     * Return media renaming confirmation message with number of processes affected.
     *
     * @return media renaming confirmation message
     */
    public String getMediaRenamingConfirmMessage() {
        Stopwatch stopwatch = new Stopwatch(this, "getMediaRenamingConfirmMessage");
        return stopwatch.stop(Helper.getTranslation("renameMediaForProcessesConfirmMessage", String.valueOf(
            getSelectedProcesses().size())));
    }

    /**
     * Get error message.
     * @return error message
     */
    public String getErrorMessage() {
        Stopwatch stopwatch = new Stopwatch(this, "getErrorMessage");
        return stopwatch.stop(errorMessage);
    }

    /**
     * Check and return whether process with ID 'processId' belongs to a project assigned to the current user or not.
     * @param processId ID of process to check
     * @return whether process belongs to project assigned to current user or not
     */
    public boolean processInAssignedProject(int processId) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), processId, "processInAssignedProject");
        try {
            if (!assignedProcesses.containsKey(processId)) {
                assignedProcesses.put(processId, ImportService.processInAssignedProject(processId));
            }
            return stopwatch.stop(assignedProcesses.get(processId));
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
        }
        return stopwatch.stop(false);
    }

    /**
     * Determine whether the last comment should be displayed in the comments column.
     *
     * @return boolean
     */
    public boolean showLastComment() {
        Stopwatch stopwatch = new Stopwatch(this, "showLastComment");
        return stopwatch.stop(ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.SHOW_LAST_COMMENT));
    }

    /**
     * Display dialog to set import configuration for selected processes.
     */
    public void setImportConfiguration() {
        Stopwatch stopwatch = new Stopwatch(this, "setImportConfiguration");
        PrimeFaces.current().executeScript("PF('selectImportConfigurationDialog').show();");
        stopwatch.stop();
    }

    /**
     * Assign import configuration with given ID 'importConfigurationId' to all selected processes.
     *
     * @param importConfigurationId ID of import configuration to assign to selected processes
     */
    public void startSettingImportConfigurations(int importConfigurationId) {
        final Stopwatch stopwatch = new Stopwatch(this, "startSettingImportConfigurations");
        PrimeFaces.current().executeScript("PF('selectImportConfigurationDialog').hide();");
        try {
            String configName = ServiceManager.getProcessService().setImportConfigurationForMultipleProcesses(
                    getSelectedProcesses(), importConfigurationId);
            settingImportConfigurationResultMessage = Helper.getTranslation("setImportConfigurationSuccessfulDescription",
                configName, String.valueOf(selectedProcesses.size()));
            importConfigurationsSetSuccessfully = true;
        } catch (DAOException e) {
            settingImportConfigurationResultMessage = e.getLocalizedMessage();
            importConfigurationsSetSuccessfully = false;
        }
        Ajax.update("importConfigurationsSelectedDialog");
        PrimeFaces.current().executeScript("PF('importConfigurationsSelectedDialog').show();");
        stopwatch.stop();
    }

    /**
     * Get value of 'settingImportConfigurationResultMessage'.
     *
     * @return value of 'settingImportConfigurationResultMessage'
     */
    public String getSettingImportConfigurationResultMessage() {
        Stopwatch stopwatch = new Stopwatch(this, "getSettingImportConfigurationResultMessage");
        return stopwatch.stop(settingImportConfigurationResultMessage);
    }

    /**
     * Get value of 'importConfigurationsSetSuccessfully'.
     *
     * @return value of 'importConfigurationsSetSuccessfully'
     */
    public boolean isImportConfigurationsSetSuccessfully() {
        Stopwatch stopwatch = new Stopwatch(this, "isImportConfigurationsSetSuccessfully");
        return stopwatch.stop(importConfigurationsSetSuccessfully);
    }
}
