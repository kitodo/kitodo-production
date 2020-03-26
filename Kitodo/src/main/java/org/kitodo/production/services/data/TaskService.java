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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.goobi.production.flow.helper.SearchResultGeneration;
import org.kitodo.api.command.CommandResult;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TaskType;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.export.ExportDms;
import org.kitodo.production.dto.TaskDTO;
import org.kitodo.production.dto.UserDTO;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.CommandService;
import org.kitodo.production.services.data.base.ProjectSearchService;
import org.kitodo.production.services.file.SubfolderFactoryService;
import org.kitodo.production.services.image.ImageGenerator;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.primefaces.model.SortOrder;

/**
 * The class provides a service for tasks. The service can be used to perform
 * functions on the task because the task itself is a database bean and
 * therefore may not include functionality.
 */
public class TaskService extends ProjectSearchService<Task, TaskDTO, TaskDAO> {

    private static final Logger logger = LogManager.getLogger(TaskService.class);
    private static volatile TaskService instance = null;
    private boolean onlyOpenTasks = false;
    private boolean onlyOwnTasks = false;
    private boolean showAutomaticTasks = false;
    private boolean hideCorrectionTasks = false;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private TaskService() {
        super(new TaskDAO(), new TaskType(), new Indexer<>(Task.class), new Searcher(Task.class),
                TaskTypeField.CLIENT_ID.getKey(), TaskTypeField.PROJECT_ID.getKey());
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

    /**
     * Creates and returns a query to retrieve tasks for which the currently
     * logged in user is eligible.
     *
     * @return query to retrieve tasks for which the user eligible.
     */
    private BoolQueryBuilder createUserTaskQuery(String filter) throws DataException {
        User user = ServiceManager.getUserService().getAuthenticatedUser();

        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(getQueryForTemplate(0));
        if (Objects.isNull(filter)) {
            filter = "";
        }
        SearchResultGeneration searchResultGeneration = new SearchResultGeneration(filter, true, true);
        query.must(searchResultGeneration.getQueryForFilter(ObjectType.TASK));

        if (onlyOpenTasks) {
            query.must(getQueryForProcessingStatus(TaskStatus.OPEN.getValue()));
        } else {
            Set<Integer> processingStatuses = new HashSet<>();
            processingStatuses.add(TaskStatus.OPEN.getValue());
            processingStatuses.add(TaskStatus.INWORK.getValue());
            query.must(getQueryForProcessingStatuses(processingStatuses));
        }

        if (onlyOwnTasks) {
            query.must(getQueryForProcessingUser(user.getId()));
        } else {
            BoolQueryBuilder subQuery = new BoolQueryBuilder();
            subQuery.should(getQueryForProcessingUser(user.getId()));
            for (Role role : user.getRoles()) {
                subQuery.should(createSimpleQuery(TaskTypeField.ROLES + ".id", role.getId(), true));
            }
            query.must(subQuery);
        }

        if (hideCorrectionTasks) {
            query.must(createSimpleQuery(TaskTypeField.CORRECTION.getKey(), false, true));
        }

        if (!showAutomaticTasks) {
            query.must(getQueryForTypeAutomatic(false));
        }

        return query;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Task");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Task WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(createUserTaskQuery(null));
    }

    @Override
    public List<Task> getAllNotIndexed() {
        return getByQuery("FROM Task WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Task> getAllForSelectedClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TaskDTO> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        String filter = ServiceManager.getFilterService().parsePrimeFacesFilter(filters);
        return findByQuery(createUserTaskQuery(filter), getSortBuilder(sortField, sortOrder), first, pageSize, false);
    }

    /**
     * Method saves or removes dependencies with process, users and user's
     * groups related to modified task.
     *
     * @param task
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Task task) throws CustomResponseException, DataException, IOException {
        if (Objects.nonNull(task.getProcess())) {
            manageProcessDependenciesForIndex(task);
        } else if (Objects.nonNull(task.getTemplate())) {
            manageTemplateDependenciesForIndex(task);
        }
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
            logger.info("do nothing - there is not new nor old user");
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

    /**
     * Find the distinct task titles.
     *
     * @return a list of titles
     */
    public List<String> findTaskTitlesDistinct() throws DataException, DAOException {
        return findDistinctValues(QueryBuilders.matchAllQuery(), "title.keyword", true, countDatabaseRows());
    }

    @Override
    public TaskDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(getIdFromJSONObject(jsonObject));
        taskDTO.setTitle(TaskTypeField.TITLE.getStringValue(jsonObject));
        taskDTO.setLocalizedTitle(getLocalizedTitle(taskDTO.getTitle()));
        taskDTO.setOrdering(TaskTypeField.ORDERING.getIntValue(jsonObject));
        int taskStatus = TaskTypeField.PROCESSING_STATUS.getIntValue(jsonObject);
        taskDTO.setProcessingStatus(TaskStatus.getStatusFromValue(taskStatus));
        taskDTO.setProcessingStatusTitle(Helper.getTranslation(taskDTO.getProcessingStatus().getTitle()));
        int editType = TaskTypeField.EDIT_TYPE.getIntValue(jsonObject);
        taskDTO.setEditType(TaskEditType.getTypeFromValue(editType));
        taskDTO.setEditTypeTitle(Helper.getTranslation(taskDTO.getEditType().getTitle()));
        taskDTO.setProcessingTime(TaskTypeField.PROCESSING_TIME.getStringValue(jsonObject));
        taskDTO.setProcessingBegin(TaskTypeField.PROCESSING_BEGIN.getStringValue(jsonObject));
        taskDTO.setProcessingEnd(TaskTypeField.PROCESSING_END.getStringValue(jsonObject));
        taskDTO.setCorrection(TaskTypeField.CORRECTION.getBooleanValue(jsonObject));
        taskDTO.setTypeAutomatic(TaskTypeField.TYPE_AUTOMATIC.getBooleanValue(jsonObject));
        taskDTO.setTypeMetadata(TaskTypeField.TYPE_METADATA.getBooleanValue(jsonObject));
        taskDTO.setTypeImagesWrite(TaskTypeField.TYPE_IMAGES_WRITE.getBooleanValue(jsonObject));
        taskDTO.setTypeImagesRead(TaskTypeField.TYPE_IMAGES_READ.getBooleanValue(jsonObject));
        taskDTO.setBatchStep(TaskTypeField.BATCH_STEP.getBooleanValue(jsonObject));
        taskDTO.setRolesSize(TaskTypeField.ROLES.getSizeOfProperty(jsonObject));

        /*
         * We read the list of the process but not the list of templates, because only process tasks
         * are displayed in the task list and reading the template list would result in
         * never-ending loops as the list of templates reads the list of tasks.
         */
        int process = TaskTypeField.PROCESS_ID.getIntValue(jsonObject);
        if (process > 0 && !related) {
            taskDTO.setProcess(ServiceManager.getProcessService().findById(process, true));
            taskDTO.setBatchAvailable(ServiceManager.getProcessService()
                    .isProcessAssignedToOnlyOneBatch(taskDTO.getProcess().getBatches()));
        }

        int processingUser = TaskTypeField.PROCESSING_USER_ID.getIntValue(jsonObject);
        if (processingUser > 0) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(processingUser);
            userDTO.setLogin(TaskTypeField.PROCESSING_USER_LOGIN.getStringValue(jsonObject));
            userDTO.setName(TaskTypeField.PROCESSING_USER_NAME.getStringValue(jsonObject));
            userDTO.setSurname(TaskTypeField.PROCESSING_USER_SURNAME.getStringValue(jsonObject));
            userDTO.setFullName(ServiceManager.getUserService().getFullName(userDTO));
            taskDTO.setProcessingUser(userDTO);
        }
        return taskDTO;
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

    /**
     * Execute script for task.
     *
     * @param task
     *            object
     * @param script
     *            String
     * @param automatic
     *            boolean
     * @return int
     */
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
        VariableReplacer replacer = new VariableReplacer(dd, prefs, po, task);

        script = replacer.replace(script);
        boolean executedSuccessful = false;
        try {
            logger.info("Calling the shell: {}", script);

            CommandService commandService = ServiceManager.getCommandService();
            CommandResult commandResult = commandService.runCommand(script);
            executedSuccessful = commandResult.isSuccessful();
            finishOrReturnAutomaticTask(task, automatic, commandResult.isSuccessful());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return executedSuccessful;
    }

    /**
     * Execute all scripts for step.
     *
     * @param task
     *            StepObject
     * @param automatic
     *            boolean
     */
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
            throws DataException, IOException {
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

    /**
     * Performs creating images when this happens automatically in a task.
     *
     * @param executingThread
     *            Executing thread (displayed in the taskmanager)
     * @param task
     *            Task that generates images
     * @param automatic
     *            Whether it is an automatic task
     * @throws DataException
     *             if the task cannot be saved
     */
    public void generateImages(EmptyTask executingThread, Task task, boolean automatic) throws DataException {
        try {
            Process process = task.getProcess();
            Subfolder sourceFolder = new Subfolder(process, process.getProject().getGeneratorSource());
            List<Subfolder> foldersToGenerate = SubfolderFactoryService.createAll(process, task.getContentFolders());
            ImageGenerator generator = new ImageGenerator(sourceFolder, GenerationMode.ALL, foldersToGenerate);
            generator.setSupervisor(executingThread);
            generator.run();
            finishOrReturnAutomaticTask(task, automatic, Objects.isNull(executingThread.getException()));
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Execute DMS export.
     *
     * @param task
     *            as Task object
     */
    public void executeDmsExport(Task task) throws DataException {
        ExportDms export = new ExportDms();
        try {
            export.startExport(task.getProcess());
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage("errorExport", new Object[] {task.getProcess().getTitle() }, logger, e);
            abortTask(task);
        }
    }

    /**
     * Set shown only open tasks.
     *
     * @param onlyOpenTasks
     *            as boolean
     */
    public void setOnlyOpenTasks(boolean onlyOpenTasks) {
        this.onlyOpenTasks = onlyOpenTasks;
    }

    /**
     * Set shown only tasks owned by currently logged user.
     *
     * @param onlyOwnTasks
     *            as boolean
     */
    public void setOnlyOwnTasks(boolean onlyOwnTasks) {
        this.onlyOwnTasks = onlyOwnTasks;
    }

    /**
     * Set hide correction tasks.
     *
     * @param hideCorrectionTasks
     *            as boolean
     */
    public void setHideCorrectionTasks(boolean hideCorrectionTasks) {
        this.hideCorrectionTasks = hideCorrectionTasks;
    }

    /**
     * Set show automatic tasks.
     *
     * @param showAutomaticTasks
     *            as boolean
     */
    public void setShowAutomaticTasks(boolean showAutomaticTasks) {
        this.showAutomaticTasks = showAutomaticTasks;
    }

    /**
     * Get current tasks with exact title for batch with exact id.
     *
     * @param title
     *            of task as String
     * @param batchId
     *            id of batch as Integer
     * @return list of Task objects
     */
    public List<Task> getCurrentTasksOfBatch(String title, Integer batchId) {
        return dao.getCurrentTasksOfBatch(title, batchId);
    }

    /**
     * Get all tasks between two given ordering of tasks for given process id.
     *
     * @param orderingMax
     *            as Integer
     * @param orderingMin
     *            as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
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

    /**
     * Get previous tasks for problem solution for given process id.
     *
     * @param ordering
     *            of Task for which it searches previous ones as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
    public List<Task> getPreviousTasksForProblemReporting(Integer ordering, Integer processId) {
        return dao.getPreviousTasksForProblemReporting(ordering, processId);
    }

    /**
     * Find tasks by id of process.
     *
     * @param id
     *            of process
     * @return list of JSON objects with tasks for specific process id
     */
    List<Map<String, Object>> findByProcessId(Integer id) throws DataException {
        return findDocuments(getQueryForProcess(id));
    }

    /**
     * Find tasks by id of template.
     *
     * @param id
     *            of template
     * @return list of JSON objects with tasks for specific template id
     */
    List<Map<String, Object>> findByTemplateId(Integer id) throws DataException {
        return findDocuments(getQueryForTemplate(id));
    }

    /**
     * Get query for automatic type of task.
     *
     * @param typeAutomatic
     *            automatic type of task as boolean
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryForTypeAutomatic(boolean typeAutomatic) {
        return createSimpleQuery(TaskTypeField.TYPE_AUTOMATIC.getKey(), typeAutomatic, true);
    }

    /**
     * Get query for process.
     *
     * @param processId
     *            process id as int
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryForProcess(int processId) {
        return createSimpleQuery(TaskTypeField.PROCESS_ID.getKey(), processId, true);
    }

    /**
     * Get query for processing user.
     *
     * @param processingUserId
     *            processing user id as int
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryForProcessingUser(int processingUserId) {
        return createSimpleQuery(TaskTypeField.PROCESSING_USER_ID.getKey(), processingUserId, true);
    }

    /**
     * Get query for processing status.
     *
     * @param processingStatus
     *            processing status as int
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryForProcessingStatus(int processingStatus) {
        return createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), processingStatus, true);
    }

    /**
     * Get query for processing statuses.
     *
     * @param processingStatus
     *            set of processing statuses as Integer
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryForProcessingStatuses(Set<Integer> processingStatus) {
        return createSetQuery(TaskTypeField.PROCESSING_STATUS.getKey(), processingStatus, true);
    }

    /**
     * Get query for template.
     *
     * @param templateId
     *            template id as int
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryForTemplate(int templateId) {
        return createSimpleQuery(TaskTypeField.TEMPLATE_ID.getKey(), templateId, true);
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
                || folder.getImageScale().isPresent() || folder.getImageSize().isPresent());
    }

    public long getDurationInDays(Task task) {

        Date end = task.getProcessingEnd();
        if(Objects.isNull(end)){
            end = new Date();
        }
        Date begin = task.getProcessingBegin();
        if(Objects.isNull(begin)){
            begin = task.getProcessingTime();
            if(Objects.isNull(begin)) {
                begin = new Date();
            }
        }
        long differenceTime = end.getTime() - begin.getTime();
        return differenceTime  / (1000 * 60 * 60 * 24);
    }
}
