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

package org.kitodo.production.forms.process;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.MediaNotFoundException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.filters.FilterMenu;
import org.kitodo.production.helper.CustomListColumnInitializer;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.KitodoScriptService;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.kitodo.utils.Stopwatch;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.xml.sax.SAXException;

@Named("ProcessListView")
@ViewScoped
public class ProcessListView extends ProcessListBaseView {

    private static final Logger logger = LogManager.getLogger(ProcessListView.class);

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "processes.jsf") + "&tab=processTab";

    private Process process = new Process();
    
    private String kitodoScriptSelection;
        
    private final FilterMenu filterMenu = new FilterMenu(this);
    private final transient FileService fileService = ServiceManager.getFileService();
    private final transient WorkflowControllerService workflowControllerService = new WorkflowControllerService();

    private String errorMessage = "";

    private List<SelectItem> customColumns;

    private static final String CREATE_PROCESS_PATH = "/pages/processFromTemplate.jsf?faces-redirect=true";

    private final Map<Integer, Boolean> assignedProcesses = new HashMap<>();
    private String settingImportConfigurationResultMessage;
    private boolean importConfigurationsSetSuccessfully = false;

    @Inject
    private CustomListColumnInitializer initializer;

    /**
     * Constructor.
     */
    public ProcessListView() {
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

        sortBy = SortMeta.builder().field("id").order(SortOrder.DESCENDING).build();
        stopwatch.stop();
    }

    /**
     * Navigates to processes list.
     */
    public static String getViewPath() {
        return VIEW_PATH;
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
        Stopwatch stopwatch = new Stopwatch(ProcessListView.class, process, "getPropertyValue", "propertyName",
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
        Stopwatch stopwatch = new Stopwatch(ProcessListView.class, process, "getProcessDuration");
        return stopwatch.stop(ProcessService.getProcessDuration(process));
    }

    /**
     * Save process and redirect to list view.
     *
     * @return url to list view
     */
    public String save() {
        Stopwatch stopwatch = new Stopwatch(this, "save");

        try {
            ServiceManager.getProcessService().save(this.process);

            return ProcessListView.getViewPath();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                logger, e);
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
        stopwatch.stop();
    }

    /**
     * Execute Kitodo script for selected processes.
     */
    public void executeKitodoScriptSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "executeKitodoScriptSelection");
        executeKitodoScriptForProcesses(getSelectedProcesses(), this.kitodoScriptSelection);
        // Clear selection if deleteProcess was executed
        if (Objects.nonNull(kitodoScriptSelection) && kitodoScriptSelection.startsWith("action:deleteProcess")) {
            this.selectedProcesses.clear();
        }
        stopwatch.stop();
    }

    private void executeKitodoScriptForProcesses(List<Process> processes, String kitodoScript) {
        KitodoScriptService service = ServiceManager.getKitodoScriptService();
        try {
            service.execute(processes, kitodoScript);
        } catch (DAOException | IOException | InvalidImagesException | SAXException | FileStructureValidationException e) {
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
     * Changes the filter of the ProcessListView and reloads it.
     *
     * @param filter
     *            the filter to apply.
     * @return reloadpath of th page.
     */
    public String changeFilter(String filter) {
        final Stopwatch stopwatch = new Stopwatch(this, "changeFilter");
        filterMenu.parseFilters(filter);
        setFilter(filter);
        this.selectedProcesses.clear();
        return stopwatch.stop(VIEW_PATH + "&" + getCombinedListOptions());
    }

    @Override
    public void setFilter(String filter) {
        final Stopwatch stopwatch = new Stopwatch(this, "setFilter", "filter", filter);
        super.filter = filter;
        this.lazyBeanModel.setFilterString(filter);
        String script = "kitodo.updateQueryParameter('filter', '" + filter.replace("&", "%26") +  "');";
        PrimeFaces.current().executeScript(script);
        stopwatch.stop();
    }

    /**
     * Get all parent processes recursively for the given process.
     *
     * @return List of Processes
     */
    public List<Process> getAllParentProcesses(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process.getId(), "getAllParentProcesses");
        return stopwatch.stop(ProcessService.getAllParentProcesses(process));
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
    public Integer getAmount() {
        return Objects.isNull(lazyBeanModel) ? 0 : lazyBeanModel.getRowCount();
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

    /**
     * Set filter based on the URL query parameter "filter", which can be any string.
     * 
     * @param encodedFilter the filter as URL query parameter to be set as new filter
     */
    public void setFilterFromTemplate(String encodedFilter) {
        if (Objects.nonNull(encodedFilter) && !encodedFilter.isEmpty()) {
            String decodedFilter = encodedFilter.replace("%26", "&");
            this.filterMenu.parseFilters(decodedFilter);
            this.setFilter(decodedFilter);
        }
    }

    /**
     * Declare the allowed sort fields for sanitizing the query parameter "sortField".
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        return Set.of(
            "id", "title", "progressCombined", "lastEditingUser", "processingBeginLastTask", 
            "processingEndLastTask", "correctionCommentStatus", "project.title", "creationDate"
        );
    }
}
