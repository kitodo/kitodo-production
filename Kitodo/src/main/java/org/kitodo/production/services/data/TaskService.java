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

package org.kitodo.production.services.data;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.command.CommandResult;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.data.interfaces.TaskInterface;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.MediaNotFoundException;
import org.kitodo.export.ExportDms;
import org.kitodo.production.dto.DTOFactory;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.CommandService;
import org.kitodo.production.services.command.KitodoScriptService;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.data.interfaces.DatabaseTaskServiceInterface;
import org.kitodo.production.services.file.SubfolderFactoryService;
import org.kitodo.production.services.image.ImageGenerator;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.primefaces.model.SortOrder;

/**
 * The class provides a service for tasks. The service can be used to perform
 * functions on the task because the task itself is a database bean and
 * therefore may not include functionality.
 */
public class TaskService extends SearchDatabaseService<Task, TaskDAO> implements DatabaseTaskServiceInterface {

    private static final Map<String, String> SORT_FIELD_MAPPING;
    static {
        SORT_FIELD_MAPPING = new HashMap<>();
        SORT_FIELD_MAPPING.put("title", "title");
        SORT_FIELD_MAPPING.put("title.keyword", "title");
        SORT_FIELD_MAPPING.put("processForTask.id", "process_id");
        SORT_FIELD_MAPPING.put("processForTask.title.keyword", "process.title");
        SORT_FIELD_MAPPING.put("processingStatus", "processingStatus");
        SORT_FIELD_MAPPING.put("processingUser.name.keyword", "task.processingUser.surname");
        SORT_FIELD_MAPPING.put("processingBegin", "processingBegin");
        SORT_FIELD_MAPPING.put("processingEnd", "processingEnd");
        SORT_FIELD_MAPPING.put("correctionCommentStatus", "correction");
        SORT_FIELD_MAPPING.put("projectForTask.title.keyword", "process.project.title");
        SORT_FIELD_MAPPING.put("processForTask.creationDate", "process.creationDate");
    }

    private static final Logger logger = LogManager.getLogger(TaskService.class);
    private static volatile TaskService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private TaskService() {
        super(new TaskDAO());
    }

    /**
     * Return singleton variable of type TaskService.
     *
     * @return unique instance of TaskService
     */
    public static TaskService getInstance() {
        TaskService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (TaskService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new TaskService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Task WHERE " + BaseDAO.getDateFilter("processingBegin"));
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countResults(new HashMap<String, String>(filters), false, false, false, null);
    }

    @Override
    public Long countResults(Map<?, String> filters, boolean onlyOwnTasks, boolean hideCorrectionTasks,
                             boolean showAutomaticTasks, List<TaskStatus> taskStatus)
            throws DataException {
        try {
            BeanQuery query = new BeanQuery(Task.class);
            query.restrictToClient(ServiceManager.getUserService().getSessionClientId());
            Collection<Integer> projectIDs = ServiceManager.getUserService().getCurrentUser().getProjects().stream()
                    .filter(Project::isActive).map(Project::getId).collect(Collectors.toList());
            query.restrictToProjects(projectIDs);
            Iterator<? extends Entry<?, String>> filtersIterator = filters.entrySet().iterator();
            if (filtersIterator.hasNext()) {
                String filterString = filtersIterator.next().getValue();
                if (StringUtils.isNotBlank(filterString)) {
                    query.restrictWithUserFilterString(filterString);
                }
            }
            if (onlyOwnTasks) {
                query.addIntegerRestriction("user_id", ServiceManager.getUserService().getCurrentUser().getId());
            }
            if (hideCorrectionTasks) {
                query.addIntegerRestriction("correction", 0);
            }
            if (!showAutomaticTasks) {
                query.addBooleanRestriction("typeAutomatic", Boolean.FALSE);
            }
            if (!taskStatus.isEmpty()) {
                List<Integer> selectedStatuses = taskStatus.stream().map(status -> status.getValue())
                        .collect(Collectors.toList());
                query.addInCollectionRestriction("processingStatus", selectedStatuses);
            }
            return countDatabaseRows(query.formCountQuery(), query.getQueryParameters());
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    @Override
    public List<TaskInterface> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return loadData(first, pageSize, sortField, sortOrder, filters, false, false, false,
                Arrays.asList(TaskStatus.OPEN, TaskStatus.INWORK));
    }

    @Override
    public List<Task> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map<?, String> filters,
                                  boolean onlyOwnTasks, boolean hideCorrectionTasks, boolean showAutomaticTasks,
                                  List<TaskStatus> taskStatus)
            throws DataException {

        BeanQuery query = new BeanQuery(Task.class);
        query.restrictToClient(ServiceManager.getUserService().getSessionClientId());
        Collection<Integer> projectIDs = ServiceManager.getUserService().getCurrentUser().getProjects().stream()
                .filter(Project::isActive).map(Project::getId).collect(Collectors.toList());
        query.restrictToProjects(projectIDs);
        Iterator<? extends Entry<?, String>> filtersIterator = filters.entrySet().iterator();
        if (filtersIterator.hasNext()) {
            String filterString = filtersIterator.next().getValue();
            if (StringUtils.isNotBlank(filterString)) {
                query.restrictWithUserFilterString(filterString);
            }
        }
        if (onlyOwnTasks) {
            query.addIntegerRestriction("user_id", ServiceManager.getUserService().getCurrentUser().getId());
        }
        if (hideCorrectionTasks) {
            query.addIntegerRestriction("correction", 0);
        }
        if (!showAutomaticTasks) {
            query.addBooleanRestriction("typeAutomatic", Boolean.FALSE);
        }
        if (!taskStatus.isEmpty()) {
            List<Integer> selectedStatuses = taskStatus.stream().map(status -> status.getValue())
                    .collect(Collectors.toList());
            query.addInCollectionRestriction("processingStatus", taskStatus);
        }
        query.defineSorting(SORT_FIELD_MAPPING.get(sortField), sortOrder);
        return getByQuery(query.formQueryForAll(), query.getQueryParameters(), first, pageSize);
    }

    private void manageProcessDependenciesForIndex(Task task)
            throws CustomResponseException, DataException, IOException {
        Process process = task.getProcess();
        if (task.getIndexAction() == IndexAction.DELETE) {
            process.getTasks().remove(task);
            ServiceManager.getProcessService().saveToIndex(process, false);
        } else {
            ServiceManager.getProcessService().saveToIndex(process, false);
        }
    }

    private void manageTemplateDependenciesForIndex(Task task)
            throws CustomResponseException, DataException, IOException {
        Template template = task.getTemplate();
        if (task.getIndexAction().equals(IndexAction.DELETE)) {
            template.getTasks().remove(task);
            ServiceManager.getTemplateService().saveToIndex(template, false);
        } else {
            ServiceManager.getTemplateService().saveToIndex(template, false);
        }
    }

    /**
     * Replace processing user for given task. Handles add/remove from list of
     * processing tasks.
     *
     * @param task
     *            for which user will be assigned as processing user
     * @param user
     *            which will process given task
     */
    public void replaceProcessingUser(Task task, User user) {
        User currentProcessingUser = task.getProcessingUser();

        if (Objects.isNull(user) && Objects.isNull(currentProcessingUser)) {
            logger.info("do nothing - there is neither a new nor an old user");
        } else if (Objects.isNull(user)) {
            currentProcessingUser.getProcessingTasks().remove(task);
            task.setProcessingUser(null);
        } else if (Objects.isNull(currentProcessingUser)) {
            user.getProcessingTasks().add(task);
            task.setProcessingUser(user);
        } else if (Objects.equals(currentProcessingUser.getId(), user.getId())) {
            logger.info("do nothing - both are the same");
        } else {
            currentProcessingUser.getProcessingTasks().remove(task);
            user.getProcessingTasks().add(task);
            task.setProcessingUser(user);
        }
    }

    @Override
    public List<String> findTaskTitlesDistinct() throws DataException, DAOException {
        throw new UnsupportedOperationException("not yet implemented");
        // return findDistinctValues(QueryBuilders.matchAllQuery(), "title.keyword", true, countDatabaseRows());
    }

    /**
     * Parses and adds properties related to the project of a task to the task.
     * 
     * @param jsonObject the jsonObject retrieved from the ElasticSearch index for a task
     * @param task the task
     */
    private void convertTaskProjectFromJsonObjectTo(Map<String, Object> jsonObject,
            TaskInterface task) throws DataException {

        ProjectInterface project = DTOFactory.instance().newProject();
        project.setId(TaskTypeField.PROJECT_ID.getIntValue(jsonObject));
        project.setTitle(TaskTypeField.PROJECT_TITLE.getStringValue(jsonObject));
        task.setProject(project);
    }

    private List<Integer> convertJSONValuesToList(List<Map<String, Object>> jsonObject) {
        return jsonObject.stream()
                .flatMap(map -> map.values().stream())
                .filter(o -> StringUtils.isNumeric(o.toString()))
                .map(o -> (Integer) o)
                .collect(Collectors.toList());
    }

    /**
     * Convert date of processing begin to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingBeginAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingBegin());
    }

    /**
     * Convert date of processing end to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingEndAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingEnd());
    }

    /**
     * Convert date of processing day to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingTimeAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingTime());
    }

    /**
     * Get localized (translated) title of task.
     *
     * @param title
     *            as String
     * @return localized title
     */
    public String getLocalizedTitle(String title) {
        return Helper.getTranslation(title);
    }

    /**
     * Get project(s). If the task belongs to a template, the projects are in
     * the template. If the task belongs to a process, the project is in the
     * process.
     *
     * @return value of project(s)
     */
    public static List<Project> getProjects(Task task) {
        Process process = task.getProcess();
        Template template = task.getTemplate();
        if (Objects.nonNull(process)) {
            return Collections.singletonList(process.getProject());
        } else if (Objects.nonNull(template)) {
            return template.getProjects();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get roles list size.
     *
     * @param task
     *            object
     * @return size of roles assigned to task
     */
    public int getRolesSize(Task task) {
        return task.getRoles().size();
    }

    /**
     * Get title with user.
     *
     * @return des Schritttitels sowie (sofern vorhanden) den Benutzer mit
     *         vollständigem Namen
     */
    public String getTitleWithUserName(Task task) {
        String titleWithUserName = task.getTitle();
        User user = task.getProcessingUser();
        if (Objects.nonNull(user) && Objects.nonNull(user.getId())) {
            titleWithUserName += " (" + ServiceManager.getUserService().getFullName(user) + ")";
        }
        return titleWithUserName;
    }

    /**
     * Get script path.
     *
     * @param task
     *            object
     * @return script path as String
     */
    public String getScriptPath(Task task) {
        if (Objects.nonNull(task.getScriptPath()) && !task.getScriptPath().isEmpty()) {
            return task.getScriptPath();
        }
        return "";
    }

    @Override
    public boolean executeScript(Task task, String script, boolean automatic) throws DataException {
        if (Objects.isNull(script) || script.isEmpty()) {
            return false;
        }
        script = script.replace("{", "(").replace("}", ")");
        LegacyMetsModsDigitalDocumentHelper dd = null;
        Process po = task.getProcess();

        LegacyPrefsHelper prefs = ServiceManager.getRulesetService().getPreferences(po.getRuleset());

        try {
            dd = ServiceManager.getProcessService()
                    .readMetadataFile(ServiceManager.getFileService().getMetadataFilePath(po), prefs)
                    .getDigitalDocument();
        } catch (IOException e2) {
            logger.error(e2);
        }
        VariableReplacer replacer = new VariableReplacer(dd.getWorkpiece(), po, task);

        script = replacer.replace(script);
        boolean executedSuccessful = false;
        try {
            if (script.startsWith("action:")) {
                logger.info("Calling KitodoScript interpreter: {}", script);

                KitodoScriptService kitodoScriptService = ServiceManager.getKitodoScriptService();
                kitodoScriptService.execute(Arrays.asList(task.getProcess()), script);
                executedSuccessful = true;
            } else {
                logger.info("Calling the shell: {}", script);

                CommandService commandService = ServiceManager.getCommandService();
                CommandResult commandResult = commandService.runCommand(script);
                executedSuccessful = commandResult.isSuccessful();
            }
            finishOrReturnAutomaticTask(task, automatic, executedSuccessful);
        } catch (IOException | DAOException | InvalidImagesException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        } catch (MediaNotFoundException e) {
            Helper.setWarnMessage(e.getMessage());
        }
        return executedSuccessful;
    }

    @Override
    public void executeScript(Task task, boolean automatic) throws DataException {
        String script = task.getScriptPath();
        boolean scriptFinishedSuccessful = true;
        logger.debug("starting script {}", script);
        if (Objects.nonNull(script) && !script.trim().isEmpty()) {
            scriptFinishedSuccessful = executeScript(task, script, automatic);
        }
        if (!scriptFinishedSuccessful) {
            abortTask(task);
        }
    }

    /**
     * Make the necessary changes when performing an automatic task.
     *
     * @param task
     *            ongoing task
     * @param automatic
     *            if it is an automatic task
     * @param successful
     *            if the processing was successful
     * @throws DataException
     *             if the task cannot be saved
     * @throws IOException
     *             if the task cannot be closed
     */
    private void finishOrReturnAutomaticTask(Task task, boolean automatic, boolean successful)
            throws DataException, IOException, DAOException {
        if (automatic) {
            task.setEditType(TaskEditType.AUTOMATIC);
            if (successful) {
                task.setProcessingStatus(TaskStatus.DONE);
                new WorkflowControllerService().close(task);
            } else {
                task.setProcessingStatus(TaskStatus.OPEN);
                save(task);
            }
        }
    }

    private void abortTask(Task task) throws DataException {
        task.setProcessingStatus(TaskStatus.OPEN);
        task.setEditType(TaskEditType.AUTOMATIC);
        save(task);
    }

    @Override
    public void generateImages(EmptyTask executingThread, Task task, boolean automatic) throws DataException {
        try {
            Process process = task.getProcess();
            Subfolder sourceFolder = new Subfolder(process, process.getProject().getGeneratorSource());
            List<Subfolder> foldersToGenerate = SubfolderFactoryService.createAll(process, task.getContentFolders());
            ImageGenerator generator = new ImageGenerator(sourceFolder, GenerationMode.ALL, foldersToGenerate);
            generator.setSupervisor(executingThread);
            generator.run();
            finishOrReturnAutomaticTask(task, automatic, Objects.isNull(executingThread.getException()));
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Execute DMS export.
     *
     * @param task
     *            as Task object
     */
    public void executeDmsExport(Task task) throws DataException, IOException, DAOException {
        new ExportDms(task).startExport(task);
    }

    @Override
    public List<Task> getCurrentTasksOfBatch(String title, Integer batchId) {
        return dao.getCurrentTasksOfBatch(title, batchId);
    }

    @Override
    public List<Task> getAllTasksInBetween(Integer orderingMax, Integer orderingMin, Integer processId) {
        return dao.getAllTasksInBetween(orderingMax, orderingMin, processId);
    }

    /**
     * Get next tasks for problem solution for given process id.
     *
     * @param ordering
     *            of Task for which it searches next ones as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
    public List<Task> getNextTasksForProblemSolution(Integer ordering, Integer processId) {
        return dao.getNextTasksForProblemSolution(ordering, processId);
    }

    @Override
    public List<Task> getPreviousTasksForProblemReporting(Integer ordering, Integer processId) {
        return dao.getPreviousTasksForProblemReporting(ordering, processId);
    }

    @Override
    public List<Map<String, Object>> findByTemplateId(Integer id) throws DataException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * The function determines, from projects, the folders whose contents can be
     * generated automatically.
     *
     * <p>
     * This feature is needed once by the task in the template to determine
     * which folders show buttons in the interface to turn content creation on
     * or off. In addition, the function of the task in the process is required
     * to determine if there is at least one folder to be created in the task,
     * because then action links for generating are displayed, and not
     * otherwise.
     *
     * <p>
     * To create content automatically, a folder must be defined as the template
     * folder in the project. The templates serve to create the contents in the
     * other folders to be created. Under no circumstances should the contents
     * of the template folder be automatically generated, even if, for example,
     * after a reconfiguration, this is still set as otherwise they would
     * overwrite themselves. Also, contents cannot be created in folders where
     * nothing is configured. The folders that are left over can be created.
     *
     * @param projects
     *            an object stream of projects that may have folders defined
     *            whose contents can be auto-generated
     * @return an object stream of generable folders
     */
    public static Stream<Folder> generatableFoldersFromProjects(Stream<Project> projects) {
        Stream<Project> projectsWithSourceFolder = skipProjectsWithoutSourceFolder(projects);
        Stream<Folder> allowedFolders = dropOwnSourceFolders(projectsWithSourceFolder);
        return removeFoldersThatCannotBeGenerated(allowedFolders);
    }

    /**
     * Only lets projects pass where a source folder is selected.
     *
     * @param projects
     *            the unpurified stream of projects
     * @return a stream only of projects that define a source to generate images
     */
    private static Stream<Project> skipProjectsWithoutSourceFolder(Stream<Project> projects) {
        return projects.filter(project -> Objects.nonNull(project.getGeneratorSource()));
    }

    /**
     * Drops all folders to generate if they are their own source folder.
     *
     * @param projects
     *            projects whose folders allowed to be generated are to be
     *            determined
     * @return a stream of folders that are allowed to be generated
     */
    private static Stream<Folder> dropOwnSourceFolders(Stream<Project> projects) {
        Stream<Pair<Folder, Folder>> withSources = projects.flatMap(
            project -> project.getFolders().stream().map(folder -> Pair.of(folder, project.getGeneratorSource())));
        Stream<Pair<Folder, Folder>> filteredWithSources = withSources.filter(
            destinationAndSource -> !destinationAndSource.getLeft().equals(destinationAndSource.getRight()));
        return filteredWithSources.map(Pair::getLeft);
    }

    /**
     * Removes all folders to generate which do not have anything to generate
     * configured.
     *
     * @param folders
     *            a stream of folders
     * @return a stream only of those folders where an image generation module
     *         has been selected
     */
    private static Stream<Folder> removeFoldersThatCannotBeGenerated(Stream<Folder> folders) {
        return folders.filter(folder -> folder.getDerivative().isPresent() || folder.getDpi().isPresent()
                || folder.getImageSize().isPresent());
    }

    /**
     * Get the duration of a task in days.
     * @param task the task to get the duration for
     * @return the duration in days
     */
    public long getDurationInDays(Task task) {

        Date end = task.getProcessingEnd();
        if (Objects.isNull(end)) {
            end = new Date();
        }
        Date begin = task.getProcessingBegin();
        if (Objects.isNull(begin)) {
            begin = task.getProcessingTime();
            if (Objects.isNull(begin)) {
                begin = new Date();
            }
        }
        long differenceTime = end.getTime() - begin.getTime();
        return differenceTime / (1000 * 60 * 60 * 24);
    }

    /**
     * Compute and return list of tasks that are open or in work in the given Process 'process' and are concurrent but
     * not equal not the to given Task 'task', if 'task' is not null.
     * @param process Process whose tasks are returned
     * @param task task to filter from returned list, if not null
     * @return List of concurrent open or inwork tasks
     */
    public static List<Task> getConcurrentTasksOpenOrInWork(Process process, Task task) {
        List<Task> tasks = process.getTasks().stream()
                .filter(t -> TaskStatus.INWORK.equals(t.getProcessingStatus())
                        || TaskStatus.OPEN.equals(t.getProcessingStatus()))
                .collect(Collectors.toList());
        if (Objects.nonNull(task) && task.isConcurrent()) {
            return tasks.stream()
                    .filter(t -> !t.getId().equals(task.getId()))
                    .collect(Collectors.toList());
        } else {
            return tasks;
        }
    }

    /**
     * Filter and return current list of tasks for those in work by other users.
     * @param tasks list of tasks
     * @return list of tasks in work by other users
     */
    public static List<Task> getTasksInWorkByOtherUsers(List<Task> tasks) {
        int authenticatedUserId = ServiceManager.getUserService().getAuthenticatedUser().getId();
        return tasks.stream()
                .filter(t -> Objects.nonNull(t.getProcessingUser())
                        && authenticatedUserId != t.getProcessingUser().getId())
                .collect(Collectors.toList());
    }

    /**
     * Compute and return list of tasks that are eligible as 'currentTask' for a new correction comment.
     *
     * @return list of current task options for new correction comment
     */
    public static List<Task> getCurrentTaskOptions(Process process) {
        int authenticatedUserId = ServiceManager.getUserService().getAuthenticatedUser().getId();
        // NOTE: checking for 'INWORK' tasks that do not have a 'processingUser' shouldn't be necessary, but the current
        // version of Kitodo.Production allows setting tasks to 'INWORK' without explicitly assigning a user to it via a
        // process' task list (e.g. administrative action)
        return process.getTasks().stream()
                .filter(t -> (TaskStatus.INWORK.equals(t.getProcessingStatus())
                        && ((Objects.nonNull(t.getProcessingUser()) && authenticatedUserId == t.getProcessingUser().getId())
                        || Objects.isNull(t.getProcessingUser())))
                        || TaskStatus.OPEN.equals(t.getProcessingStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Create and return a tooltip for the correction message switch using the give Process and Task.
     * @param process Process for which tooltip is created
     * @param task Task for which tooltip is created
     * @return tooltip for correction message switch
     */
    public static String getCorrectionMessageSwitchTooltip(Process process, Task task) {
        if (isCorrectionWorkflow(process)) {
            return Helper.getTranslation("dataEditor.comment.correctionWorkflowAlreadyActive");
        } else if (Objects.nonNull(task) && task.getOrdering() == 1) {
            return Helper.getTranslation("dataEditor.comment.firstTaskInWorkflow");
        } else {
            List<Task> concurrentTasks = TaskService.getConcurrentTasksOpenOrInWork(process, task);
            if (concurrentTasks.isEmpty()) {
                Helper.setErrorMessage("Invalid process state: no 'inwork' or 'open' task found!");
                return "";
            } else if (concurrentTasks.get(0).getOrdering() == 1) {
                return Helper.getTranslation("dataEditor.comment.firstTaskInWorkflow");
            } else {
                List<Task> tasksInWorkByOtherUsers = TaskService.getTasksInWorkByOtherUsers(concurrentTasks);
                if (tasksInWorkByOtherUsers.isEmpty()) {
                    return "";
                } else {
                    return MessageFormat.format(Helper.getTranslation("dataEditor.comment.parallelTaskInWorkText"),
                            tasksInWorkByOtherUsers.get(0).getTitle(),
                            tasksInWorkByOtherUsers.get(0).getProcessingUser().getFullName());
                }
            }
        }
    }

    /**
     * Check and return whether 'correction' flag is set to true for any task of the current process,
     * e.g. if process is currently in a correction workflow.
     *
     * @return whether process is in correction workflow or not
     */
    public static boolean isCorrectionWorkflow(Process process) {
        return process.getTasks().stream().anyMatch(Task::isCorrection);
    }

    /**
     * Get the id of the template task corresponding to the given task.
     * The corresponding template task was the blueprint when creating the given task.
     * @param task task to find the corresponding template task for
     * @return id of the template task or -1 if no matching task could be found
     */
    public static int getCorrespondingTemplateTaskId(Task task) {
        List<Task> templateTasks = task.getProcess().getTemplate().getTasks().stream()
                .filter(t -> t.getOrdering().equals(task.getOrdering()))
                .filter(t -> t.getTitle().equals(task.getTitle()))
                .collect(Collectors.toList());
        if (templateTasks.size() == 1) {
            return templateTasks.get(0).getId();
        }
        return -1;
    }
}
