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

package org.kitodo.production.forms.task;

import static java.util.Map.entry;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.filters.FilterMenu;
import org.kitodo.production.forms.BaseListView;
import org.kitodo.production.helper.CustomListColumnInitializer;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WebDav;
import org.kitodo.production.model.LazyTaskModel;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.kitodo.utils.Stopwatch;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.xml.sax.SAXException;

@Named("TaskListView")
@ViewScoped
public class TaskListView extends BaseListView {

    private static final Logger logger = LogManager.getLogger(TaskListView.class);
    private static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "tasks");
    
    private final WorkflowControllerService workflowControllerService = new WorkflowControllerService();

    private List<Task> selectedTasks = new ArrayList<>();
    private final WebDav myDav = new WebDav();
    
    private List<String> taskFilters;
    private List<String> selectedTaskFilters;
    private final FilterMenu filterMenu = new FilterMenu(this);

    private List<TaskStatus> taskStatus;
    private List<TaskStatus> selectedTaskStatus;

    private static final String AUTOMATIC_TASKS_FILTER = "automaticTasks";
    private static final String CORRECTION_TASKS_FILTER = "correctionTasks";
    private static final String OTHER_USERS_TASKS_FILTER = "otherUsersTasks";

    @Inject
    private CustomListColumnInitializer initializer;

    @Inject
    private TaskListViewSessionState sessionState;

    /**
     * Constructor.
     */
    public TaskListView() {
        super();
        Stopwatch stopwatch = new Stopwatch(this, "TaskListView");
        super.setLazyBeanModel(new LazyTaskModel(ServiceManager.getTaskService()));
        stopwatch.stop();
    }

    /**
     * Initialize the list of displayed list columns.
     */
    @PostConstruct
    public void init() {
        final Stopwatch stopwatch = new Stopwatch(this, "init");
        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("task"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("task");

        taskFilters = new LinkedList<>();
        taskFilters.add(AUTOMATIC_TASKS_FILTER);
        taskFilters.add(CORRECTION_TASKS_FILTER);
        taskFilters.add(OTHER_USERS_TASKS_FILTER);

        selectedTaskFilters = new LinkedList<>();
        selectedTaskFilters.add(CORRECTION_TASKS_FILTER);
        selectedTaskFilters.add(OTHER_USERS_TASKS_FILTER);

        taskStatus = new LinkedList<>();
        taskStatus.add(TaskStatus.OPEN);
        taskStatus.add(TaskStatus.INWORK);

        selectedTaskStatus = new LinkedList<>();
        selectedTaskStatus.add(TaskStatus.OPEN);
        selectedTaskStatus.add(TaskStatus.INWORK);

        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
        stopwatch.stop();
    }

    /**
     * Returns the URL to the task list.
     * 
     * @return view path to task list
     */
    public static String getViewPath() {
        return VIEW_PATH; 
    }

    /**
     * Returns the URL to the task list with a given filter string.
     * 
     * @param filter the filter string
     * @return view path to task list with specific filter
     */
    public static String getViewPath(String filter) {
        return VIEW_PATH + "&filter=" + filter.replace("&", "%26"); 
    }

    /**
     * Take over task by user which calls this method.
     *
     * @return page
     */
    public String takeOverTask(Task task, String referrer) {
        Stopwatch stopwatch = new Stopwatch(this, "takeOverTask");
        if (task.getProcessingStatus() != TaskStatus.OPEN) {
            Helper.setErrorMessage("stepInWorkError");
            return stopwatch.stop(this.stayOnCurrentPage);
        } else {
            try {
                if (task.isTypeAcceptClose()) {
                    this.workflowControllerService.close(task);
                    return stopwatch.stop(reload());
                } else {
                    this.workflowControllerService.assignTaskToUser(task);
                    ServiceManager.getTaskService().save(task);
                }
            } catch (DAOException | IOException | SAXException | FileStructureValidationException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger,
                    e);
            }
        }
        return stopwatch.stop(TaskWorkView.getViewPath(task, referrer, getCombinedListOptions()));
    }

    /**
     * Returns the view path to navigate to the "work on" task view.
     *
     * @param task the task to work on
     * @param referrer the referrer page (e.g. "desktop" or "tasks") to navigate back to in case the task is closed
     * @return the view path
     */
    public String workOnTask(Task task, String referrer) {
        Stopwatch stopwatch = new Stopwatch(this, "workOnTask");
        return stopwatch.stop(TaskWorkView.getViewPath(task, referrer, getCombinedListOptions()));
    }

    /**
     * Take over batch of tasks - all tasks assigned to the same batch with the same
     * title.
     *
     * @return page for edit one task, page for edit many or stay on the same page
     */
    public String takeOverBatchTasks(Task task, String referrer) {
        Stopwatch stopwatch = new Stopwatch(this, "takeOverBatchTasks");
        String taskTitle = task.getTitle();
        List<Batch> batches = task.getProcess().getBatches();

        if (batches.isEmpty()) {
            return stopwatch.stop(takeOverTask(task, referrer));
        } else if (batches.size() == 1) {
            Integer batchId = batches.getFirst().getId();
            List<Task> currentTasksOfBatch = ServiceManager.getTaskService().getCurrentTasksOfBatch(taskTitle, batchId);
            if (currentTasksOfBatch.isEmpty()) {
                return stopwatch.stop(this.stayOnCurrentPage);
            } else if (currentTasksOfBatch.size() == 1) {
                return stopwatch.stop(takeOverTask(task, referrer));
            } else {
                for (Task t : currentTasksOfBatch) {
                    processTask(t);
                }

                return stopwatch.stop(TaskBatchEditView.getViewPath(task));
            }
        } else {
            Helper.setErrorMessage("multipleBatchesAssigned");
            return stopwatch.stop(this.stayOnCurrentPage);
        }
    }

    /**
     * Update task which are available to take.
     *
     * @param task
     *            which is part of the batch
     */
    private void processTask(Task task) {
        if (task.getProcessingStatus().equals(TaskStatus.OPEN)) {
            task.setProcessingStatus(TaskStatus.INWORK);
            task.setEditType(TaskEditType.MANUAL_MULTI);
            task.setProcessingTime(new Date());
            User user = getUser();
            ServiceManager.getTaskService().replaceProcessingUser(task, user);
            if (Objects.isNull(task.getProcessingBegin())) {
                task.setProcessingBegin(new Date());
            }

            if (task.isTypeImagesRead() || task.isTypeImagesWrite()) {
                try {
                    URI imagesOrigDirectory = ServiceManager.getProcessService().getImagesOriginDirectory(false,
                        task.getProcess());
                    if (!ServiceManager.getFileService().fileExist(imagesOrigDirectory)) {
                        Helper.setErrorMessage("errorDirectoryNotFound", new Object[] {imagesOrigDirectory });
                    }
                } catch (Exception e) {
                    Helper.setErrorMessage("errorDirectoryRetrieve", new Object[] {"image" }, logger, e);
                }
                task.setProcessingTime(new Date());
                this.myDav.downloadToHome(task.getProcess(), !task.isTypeImagesWrite());
            }
        }

        try {
            ServiceManager.getTaskService().save(task);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Edit batch of tasks - all tasks assigned to the same batch with the same
     * title.
     *
     * @return page for edit one task, page for edit many or stay on the same page
     */
    public String editBatchTasks(Task task, String referrer) {
        Stopwatch stopwatch = new Stopwatch(this, "editBatchTasks");
        String taskTitle = task.getTitle();
        List<Batch> batches = task.getProcess().getBatches();

        if (batches.isEmpty()) {
            return stopwatch.stop(TaskWorkView.getViewPath(task, referrer, getCombinedListOptions()));
        } else if (batches.size() == 1) {
            Integer batchId = batches.getFirst().getId();
            List<Task> currentTasksOfBatch = ServiceManager.getTaskService().getCurrentTasksOfBatch(taskTitle, batchId);
            if (currentTasksOfBatch.isEmpty()) {
                return stopwatch.stop(this.stayOnCurrentPage);
            } else if (currentTasksOfBatch.size() == 1) {
                return stopwatch.stop(TaskWorkView.getViewPath(task, referrer, getCombinedListOptions()));
            } else {
                return stopwatch.stop(TaskBatchEditView.getViewPath(task));
            }
        } else {
            Helper.setErrorMessage("multipleBatchesAssigned");
            return stopwatch.stop(this.stayOnCurrentPage);
        }
    }

    /**
     * Get list of selected Tasks.
     *
     * @return List of selected Tasks
     */
    public List<Task> getSelectedTasks() {
        Stopwatch stopwatch = new Stopwatch(this, "getSelectedTasks");
        return stopwatch.stop(this.selectedTasks);
    }

    /**
     * Set selected tasks: Set tasks in old list to false and set new list to true.
     *
     * @param selectedTasks
     *            provided by data table
     */
    public void setSelectedTasks(List<Task> selectedTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setSelectedTasks", "selectedTasks", Objects.toString(selectedTasks));
        this.selectedTasks = selectedTasks;
        stopwatch.stop();
    }

    /**
     * Sets the task status constraint.
     * 
     * @param taskStatus
     *            Status of the tasks to be displayed. If empty, all tasks are
     *            displayed; otherwise, only those in one of the given statuses
     *            are displayed. Must not be {@code null}.
     */
    public void setTaskStatusRestriction(List<TaskStatus> taskStatus) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusRestriction", "taskStatus", Objects.toString(
            taskStatus));
        ((LazyTaskModel)this.lazyBeanModel).setTaskStatusRestriction(taskStatus);
        String script = "kitodo.updateQueryParameter('taskStatus', '" + encodeTaskStatusAsQueryParameter(taskStatus) +  "');";
        PrimeFaces.current().executeScript(script);
        stopwatch.stop();
    }

    /**
     * Set shown only tasks owned by currently logged user.
     *
     * @param onlyOwnTasks
     *            as boolean
     */
    public void setOnlyOwnTasks(boolean onlyOwnTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setOnlyOwnTasks", "onlyOwnTasks", Boolean.toString(onlyOwnTasks));
        ((LazyTaskModel)this.lazyBeanModel).setOnlyOwnTasks(onlyOwnTasks);
        stopwatch.stop();
    }

    /**
     * Get task filters.
     *
     * @return task filters
     */
    public List<String> getTaskFilters() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskFilters");
        return stopwatch.stop(this.taskFilters);
    }

    /**
     * Set task filters.
     *
     * @param filters task filters
     */
    public void setTaskFilters(List<String> filters) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskFilters", "filters", Objects.toString(filters));
        this.taskFilters = filters;
        stopwatch.stop();
    }

    /**
     * Get selected task filters.
     *
     * @return selected task filters
     */
    public List<String> getSelectedTaskFilters() {
        Stopwatch stopwatch = new Stopwatch(this, "getSelectedTaskFilters");
        return stopwatch.stop(this.selectedTaskFilters);
    }

    /**
     * Set selected task filters.
     *
     * @param selectedFilters selected task filters
     */
    public void setSelectedTaskFilters(List<String> selectedFilters) {
        Stopwatch stopwatch = new Stopwatch(this, "setSelectedTaskFilters", "selectedFilters", Objects.toString(
            selectedFilters));
        this.selectedTaskFilters = selectedFilters;
        String script = "kitodo.updateQueryParameter('taskFilter', '" + encodeTaskFilterAsQueryParameter(selectedFilters) +  "');";
        PrimeFaces.current().executeScript(script);
        stopwatch.stop();
    }

    /**
     * Event listener for task filter changed event.
     */
    public void taskFiltersChanged() {
        final Stopwatch stopwatch = new Stopwatch(this, "taskFiltersChanged");
        this.setShowAutomaticTasks(this.selectedTaskFilters.contains(AUTOMATIC_TASKS_FILTER));
        this.setHideCorrectionTasks(!this.selectedTaskFilters.contains(CORRECTION_TASKS_FILTER));
        this.setOnlyOwnTasks(!this.selectedTaskFilters.contains(OTHER_USERS_TASKS_FILTER));
        stopwatch.stop();
    }

    /**
     * Get task status.
     *
     * @return task status
     */
    public List<TaskStatus> getTaskStatus() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskStatus");
        return stopwatch.stop(this.taskStatus);
    }

    /**
     * Set task status.
     *
     * @param status task status
     */
    public void setTaskStatus(List<TaskStatus> status) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskStatus", "status", Objects.toString(status));
        this.taskStatus = status;
        stopwatch.stop();
    }

    /**
     * Get selected task status.
     *
     * @return selected task status
     */
    public List<TaskStatus> getSelectedTaskStatus() {
        Stopwatch stopwatch = new Stopwatch(this, "getSelectedTaskStatus");
        return stopwatch.stop(this.selectedTaskStatus);
    }

    /**
     * Set selected task status.
     *
     * @param selectedStatus selected task status
     */
    public void setSelectedTaskStatus(List<TaskStatus> selectedStatus) {
        Stopwatch stopwatch = new Stopwatch(this, "setSelectedTaskStatus", "selectedStatus", Objects.toString(
            selectedStatus));
        this.selectedTaskStatus = selectedStatus;
        stopwatch.stop();
    }

    /**
     * Event listener for task status changed event.
     */
    public void taskStatusChanged() {
        Stopwatch stopwatch = new Stopwatch(this, "taskStatusChanged");
        this.setTaskStatusRestriction(this.selectedTaskStatus);
        stopwatch.stop();
    }

    /**
     * Set show automatic tasks.
     *
     * @param showAutomaticTasks
     *            as boolean
     */
    public void setShowAutomaticTasks(boolean showAutomaticTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setShowAutomaticTasks");
        ((LazyTaskModel)this.lazyBeanModel).setShowAutomaticTasks(showAutomaticTasks);
        stopwatch.stop();
    }

    /**
     * Check if it should hide correction tasks.
     *
     * @return boolean
     */
    public boolean isHideCorrectionTasks() {
        Stopwatch stopwatch = new Stopwatch(this, "isHideCorrectionTasks");
        return stopwatch.stop(((LazyTaskModel) this.lazyBeanModel).isHideCorrectionTasks());
    }

    /**
     * Set hide correction tasks.
     *
     * @param hideCorrectionTasks
     *            as boolean
     */
    public void setHideCorrectionTasks(boolean hideCorrectionTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setHideCorrectionTasks", "hideCorrectionTasks", Boolean.toString(
            hideCorrectionTasks));
        ((LazyTaskModel)this.lazyBeanModel).setHideCorrectionTasks(hideCorrectionTasks);
        stopwatch.stop();
    }

    /**
     * Get taskListPath.
     *
     * @return value of taskListPath
     */
    public String getTaskListPath() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskListPath");
        return stopwatch.stop(tasksPage);
    }

    /**
     * Return array of task custom column names.
     * @return array of task custom column names
     */
    public String[] getTaskCustomColumnNames() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskCustomColumnNames");
        return stopwatch.stop(initializer.getTaskProcessProperties());
    }

    /**
     * Retrieve and return process property value of property with given name 'propertyName' from process of given
     * Task 'task'.
     * @param task the Task object from which the property value is retrieved
     * @param propertyName name of the property for the property value is retrieved
     * @return property value if process has property with name 'propertyName', empty String otherwise
     */
    public static String getTaskProcessPropertyValue(Task task, String propertyName) {
        Stopwatch stopwatch = new Stopwatch(TaskListView.class, task, "getTaskProcessPropertyValue", "propertyName",
                propertyName);
        return stopwatch.stop(ProcessService.getPropertyValue(task.getProcess(), propertyName));
    }

    /**
     * Calculate and return age of given tasks process as a String.
     *
     * @param task Task object whose process is used
     * @return process age of given tasks process
     */
    public String getProcessDuration(Task task) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), task, "getProcessDuration");
        return stopwatch.stop(ProcessService.getProcessDuration(task.getProcess()));
    }

    /**
     * Changes the filter of the TaskListView and reloads it.
     *
     * @param filter
     *            the filter to apply
     * @return path of the page
     */
    public String changeFilter(String filter) {
        Stopwatch stopwatch = new Stopwatch(this, "changeFilter", "filter", filter);
        filterMenu.parseFilters(filter);
        setFilter(filter);
        return stopwatch.stop(reload());
    }

    /**
     * Set the filter that is used in the task list view.
     * 
     * @param filter the new filter
     */
    @Override
    public void setFilter(String filter) {
        final Stopwatch stopwatch = new Stopwatch(this, "setFilter", "filter", filter);
        super.filter = filter;
        this.lazyBeanModel.setFilterString(filter);
        this.sessionState.setLastFilter(filter);
        String script = "kitodo.updateQueryParameter('filter', '" + filter.replace("&", "%26") +  "');";
        PrimeFaces.current().executeScript(script);
        stopwatch.stop();
    }

    /**
     * Sets the current filter based on URL query parameters.
     * 
     * @param encodedFilter the filter input value
     * @param encodedTaskFilter additional task-specific filter options
     * @param encodedTaskStatus filter options based on the task status
     */
    public void setFilterFromTemplate(String encodedFilter, String encodedTaskFilter, String encodedTaskStatus) {
        // JSF by default assigns an empty string to the view parameter even if it is not present in the URL
        // instead, check whether the URL filter parameter is present in the HTTP request
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance()
            .getExternalContext().getRequestParameterMap();
        final boolean isFilter = requestParameterMap.containsKey("filter");
        if (isFilter && Objects.nonNull(encodedFilter)) {
            String decodedFilter = encodedFilter.replace("%26", "&");
            this.filterMenu.parseFilters(decodedFilter);
            this.setFilter(decodedFilter);
        } else {
            // use last filter from session state if filter parameter is not set at all
            String lastFilter = sessionState.getLastFilter();
            if (Objects.nonNull(lastFilter) && !lastFilter.isEmpty()) {
                this.filterMenu.parseFilters(lastFilter);
                this.setFilter(lastFilter);
            }
        }

        final boolean isTaskFilter = requestParameterMap.containsKey("taskFilter");
        if (isTaskFilter && Objects.nonNull(encodedTaskFilter)) {
            this.selectedTaskFilters = parseTaskFilterFromQueryParameter(encodedTaskFilter);
            this.taskFiltersChanged();
        }

        final boolean isTaskStatus = requestParameterMap.containsKey("taskStatus");
        if (isTaskStatus && Objects.nonNull(encodedTaskStatus)) {
            this.selectedTaskStatus = parseTaskStatusFromQueryParameter(encodedTaskStatus);
            this.taskStatusChanged();
        }
    }

    /**
     * Encodes the filter describing which tasks of a certain task status is supposed to be shown in the task list.
     * 
     * @param taskStatus the list of task status
     * @return encoded query parameter, e.g. `OPEN+INWORK`
     */
    private String encodeTaskStatusAsQueryParameter(List<TaskStatus> taskStatus) {
        return taskStatus.stream().map(Enum::toString).collect(Collectors.joining("+"));
    }

    /**
     * Parses the query parameter "taskStatus" to a list of TaskStatus instances. Ignores any unrelated values.
     * 
     * @param encodedTaskStatus the taskStatus query parameter, e.g. `OPEN+INWORK`
     * @return the list of TaskStatus instances
     */
    private List<TaskStatus> parseTaskStatusFromQueryParameter(String encodedTaskStatus) {
        Set<String> allowed = Arrays.stream(TaskStatus.values()).map(Enum::name).collect(Collectors.toSet());
        return Stream.of(encodedTaskStatus.split("\\+"))
            .map(String::trim)
            .map((s) -> allowed.contains(s) ? TaskStatus.valueOf(s) : null)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Encodes additional task-specific filter options as query parameter.
     * 
     * @param taskFilter the list of task-specific filter options
     * @return encoded query parameter, e.g. `automaticTasks+correctionTasks`
     */
    private String encodeTaskFilterAsQueryParameter(List<String> taskFilter) {
        return String.join("+", taskFilter);
    }

    /**
     * Parses the query parameter "taskFilter" to a list of filter options. Ignores any unrelated values.
     * 
     * @param encodedTaskFilter the taskFilter query parameter, e.g. `automaticTasks+correctionTasks`
     * @return the parsed list of filter options
     */
    private List<String> parseTaskFilterFromQueryParameter(String encodedTaskFilter) {
        Set<String> allowed = Set.of(AUTOMATIC_TASKS_FILTER, CORRECTION_TASKS_FILTER, OTHER_USERS_TASKS_FILTER);
        return Stream.of(encodedTaskFilter.split("\\+"))
            .map(String::trim)
            .filter(allowed::contains)
            .toList();
    }

    /**
     * Download to home for single process. First check if this volume is currently
     * being edited by another user and placed in his home directory, otherwise
     * download.
     */
    public void downloadToHome(int processId) {
        Stopwatch stopwatch = new Stopwatch(this, "downloadToHome", "processId", Integer.toString(processId));
        try {
            ProcessService.downloadToHome(new WebDav(), processId);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error downloading process " + processId + " to home directory!");
        }
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
     * Determine whether the last comment should be displayed in the comments column.
     *
     * @return boolean
     */
    public boolean showLastComment() {
        Stopwatch stopwatch = new Stopwatch(this, "showLastComment");
        return stopwatch.stop(ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.SHOW_LAST_COMMENT));
    }

    /**
     * Return combined list options (URL query parameters) that can be forwarded to edit view or used to reload page.
     * 
     * @return the combined list view options (URL query parameters)
     */
    @Override
    public String getCombinedListOptions() {
        return super.getCombinedListOptions() + "&" + Map.ofEntries(
            entry("taskFilter", encodeTaskFilterAsQueryParameter(getSelectedTaskFilters())),
            entry("taskStatus", encodeTaskStatusAsQueryParameter(getSelectedTaskStatus()))
        ).entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }

    /**
     * Declare the allowed sort fields for sanitizing the query parameter "sortField".
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        return Set.of(
            "title", "process.id", "process.title", "processingStatus", "processingUser.surname", 
            "processingBegin", "processingEnd", "correctionCommentStatus", "process.project.title", "process.creationDate"
        );
    }

    /**
     * Return view path to reload page keeping current list sort and filter options.
     * 
     * @return the view path to reload this view
     */
    private String reload() {
        return VIEW_PATH + "&" + getCombinedListOptions();
    }

}
