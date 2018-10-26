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

package org.kitodo.services.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.api.command.CommandResult;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TaskType;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.TaskDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.enums.GenerationMode;
import org.kitodo.helper.Helper;
import org.kitodo.helper.VariableReplacer;
import org.kitodo.helper.tasks.EmptyTask;
import org.kitodo.model.Subfolder;
import org.kitodo.security.SecurityUserDetails;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.command.CommandService;
import org.kitodo.services.data.base.TitleSearchService;
import org.kitodo.services.file.SubfolderFactoryService;
import org.kitodo.services.image.ImageGenerator;

/**
 * The class provides a service for tasks. The service can be used to perform
 * functions on the task because the task itself is a database bean and
 * therefore may not include functionality.
 */
public class TaskService extends TitleSearchService<Task, TaskDTO, TaskDAO> {

    private static final Logger logger = LogManager.getLogger(TaskService.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private static TaskService instance = null;
    private boolean onlyOpenTasks = false;
    private boolean onlyOwnTasks = false;
    private boolean showAutomaticTasks = false;
    private boolean hideCorrectionTasks = false;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private TaskService() {
        super(new TaskDAO(), new TaskType(), new Indexer<>(Task.class), new Searcher(Task.class));
    }

    /**
     * Return singleton variable of type TaskService.
     *
     * @return unique instance of TaskService
     */
    public static TaskService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (TaskService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new TaskService();
                }
            }
        }
        return instance;
    }

    /**
     * Creates and returns a query to retrieve tasks for which the currently
     * logged in user is eligible.
     *
     * @param user
     *            currently logged in user
     * @return query to retrieve tasks for which the user eligible.
     */
    private BoolQueryBuilder createUserTaskQuery(User user) throws DataException {
        Set<Integer> processingStatuses = new HashSet<>();
        processingStatuses.add(TaskStatus.OPEN.getValue());
        processingStatuses.add(TaskStatus.INWORK.getValue());

        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(getQueryForProcessingStatus(processingStatuses));
        query.must(createSimpleQuery(TaskTypeField.TEMPLATE_ID.getKey(), 0, true));

        if (onlyOpenTasks) {
            query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), TaskStatus.OPEN.getValue(), true));
        }

        if (onlyOwnTasks) {
            query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER.getKey(), user.getId(), true));
        } else {
            BoolQueryBuilder subQuery = new BoolQueryBuilder();
            subQuery.should(createSimpleQuery(TaskTypeField.PROCESSING_USER.getKey(), user.getId(), true));
            for (Role role : user.getRoles()) {
                subQuery.should(createSimpleQuery(TaskTypeField.ROLES + ".id", role.getId(), true));
            }
            query.must(subQuery);
        }

        if (hideCorrectionTasks) {
            query.must(createSimpleQuery(TaskTypeField.PRIORITY.getKey(), 10, true));
        }

        if (!showAutomaticTasks) {
            query.must(createSimpleQuery(TaskTypeField.TYPE_AUTOMATIC.getKey(), "false", true));
        }

        SecurityUserDetails authenticatedUser = serviceManager.getUserService().getAuthenticatedUser();
        if (Objects.nonNull(authenticatedUser.getSessionClient())) {
            List<JsonObject> processes = serviceManager.getProcessService().findForCurrentSessionClient();
            query.must(createSetQuery(TaskTypeField.PROCESS_ID.getKey(), processes, true));
        }

        return query;
    }

    @Override
    public List<TaskDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        User user = serviceManager.getUserService().getAuthenticatedUser();
        BoolQueryBuilder query = createUserTaskQuery(user);
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString(), sort, offset, size), false);
    }

    @Override
    public String createCountQuery(Map filters) throws DataException {
        User user = serviceManager.getUserService().getAuthenticatedUser();
        BoolQueryBuilder query = createUserTaskQuery(user);
        return query.toString();
    }

    /**
     * Method saves or removes dependencies with process, users and user's
     * groups related to modified task.
     *
     * @param task
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Task task)
            throws CustomResponseException, DAOException, DataException, IOException {
        manageProcessDependenciesForIndex(task);
        manageTemplateDependenciesForIndex(task);
        manageProcessingUserDependenciesForIndex(task);
        manageRolesDependenciesForIndex(task);
    }

    private void manageProcessDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            Process process = task.getProcess();
            if (process != null) {
                process.getTasks().remove(task);
                serviceManager.getProcessService().saveToIndex(process, false);
            }
        } else {
            Process process = task.getProcess();
            serviceManager.getProcessService().saveToIndex(process, false);
        }
    }

    private void manageTemplateDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction().equals(IndexAction.DELETE)) {
            Template template = task.getTemplate();
            if (Objects.nonNull(template)) {
                template.getTasks().remove(task);
                serviceManager.getTemplateService().saveToIndex(template, false);
            }
        } else {
            Template template = task.getTemplate();
            serviceManager.getTemplateService().saveToIndex(template, false);
        }
    }

    private void manageProcessingUserDependenciesForIndex(Task task)
            throws CustomResponseException, DAOException, DataException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            User user = task.getProcessingUser();
            if (user != null) {
                user.getProcessingTasks().remove(task);
                serviceManager.getUserService().saveToIndex(user, false);
            }
        } else {
            User user = task.getProcessingUser();
            if (user != null) {
                serviceManager.getUserService().saveToIndex(user, false);
            }
            reIndexUserAfterRemoveFromProcessing(task);
        }
    }

    private void reIndexUserAfterRemoveFromProcessing(Task task)
            throws CustomResponseException, DAOException, DataException, IOException {
        List<UserDTO> userDTOS = serviceManager.getUserService().findByProcessingTask(task.getId(), true);
        for (UserDTO userDTO : userDTOS) {
            serviceManager.getUserService().saveToIndex(serviceManager.getUserService().getById(userDTO.getId()),
                false);
        }
    }

    private void manageRolesDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            for (Role role : task.getRoles()) {
                role.getTasks().remove(task);
                serviceManager.getRoleService().saveToIndex(role, false);
            }
        } else {
            for (Role role : task.getRoles()) {
                serviceManager.getRoleService().saveToIndex(role, false);
            }
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
    public List<String> findTaskTitlesDistinct() throws DataException {
        return findDistinctValues(null, "title.keyword", true);
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
    public List<Task> getAllNotIndexed() {
        return getByQuery("FROM Task WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    /**
     * Get query for processing statuses.
     *
     * @param processingStatus
     *            set of processing statuses as Integer
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryForProcessingStatus(Set<Integer> processingStatus) {
        return createSetQuery(TaskTypeField.PROCESSING_STATUS.getKey(), processingStatus, true);
    }

    /**
     * Find tasks by id of process.
     *
     * @param id
     *            of process
     * @return list of JSON objects with tasks for specific process id
     */
    List<JsonObject> findByProcessId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery(TaskTypeField.PROCESS_ID.getKey(), id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find tasks by four parameters.
     *
     * @param taskStatus
     *            as String
     * @param processingUser
     *            id of processing user
     * @return list of task as JSONObject objects
     */
    List<JsonObject> findByProcessingStatusAndUser(TaskStatus taskStatus, Integer processingUser, String sort)
            throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), true));
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER.getKey(), processingUser, true));
        return searcher.findDocuments(query.toString(), sort);
    }

    /**
     * Find tasks by four parameters.
     *
     * @param taskStatus
     *            as String
     * @param processingUser
     *            id of processing user
     * @param priority
     *            as Integer
     * @return list of task as JSONObject objects
     */
    private List<JsonObject> findByProcessingStatusUserAndPriority(TaskStatus taskStatus, Integer processingUser,
            Integer priority, String sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), true));
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER.getKey(), processingUser, true));
        query.must(createSimpleQuery(TaskTypeField.PRIORITY.getKey(), priority, true));
        return searcher.findDocuments(query.toString(), sort);
    }

    /**
     * Find tasks by three parameters.
     *
     * @param taskStatus
     *            as String
     * @param processingUser
     *            id of processing user
     * @param typeAutomatic
     *            as boolean
     * @return list of task as JSONObject objects
     */
    private List<JsonObject> findByProcessingStatusUserAndTypeAutomatic(TaskStatus taskStatus, Integer processingUser,
            boolean typeAutomatic, String sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), true));
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER.getKey(), processingUser, true));
        query.must(createSimpleQuery(TaskTypeField.TYPE_AUTOMATIC.getKey(), String.valueOf(typeAutomatic), true));
        return searcher.findDocuments(query.toString(), sort);
    }

    /**
     * Find tasks by four parameters.
     *
     * @param taskStatus
     *            as String
     * @param processingUser
     *            id of processing user
     * @param priority
     *            as Integer
     * @param typeAutomatic
     *            as boolean
     * @return list of task as JSONObject objects
     */
    private List<JsonObject> findByProcessingStatusUserPriorityAndTypeAutomatic(TaskStatus taskStatus,
            Integer processingUser, Integer priority, boolean typeAutomatic, String sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), true));
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER.getKey(), processingUser, true));
        query.must(createSimpleQuery(TaskTypeField.PRIORITY.getKey(), priority, true));
        query.must(createSimpleQuery(TaskTypeField.TYPE_AUTOMATIC.getKey(), String.valueOf(typeAutomatic), true));
        return searcher.findDocuments(query.toString(), sort);
    }

    @Override
    public TaskDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject taskJSONObject = jsonObject.getJsonObject("_source");
        taskDTO.setTitle(TaskTypeField.TITLE.getStringValue(taskJSONObject));
        taskDTO.setLocalizedTitle(getLocalizedTitle(taskDTO.getTitle()));
        taskDTO.setPriority(TaskTypeField.PRIORITY.getIntValue(taskJSONObject));
        taskDTO.setOrdering(TaskTypeField.ORDERING.getIntValue(taskJSONObject));
        Integer taskStatus = TaskTypeField.PROCESSING_STATUS.getIntValue(taskJSONObject);
        taskDTO.setProcessingStatus(TaskStatus.getStatusFromValue(taskStatus));
        taskDTO.setProcessingStatusTitle(Helper.getTranslation(taskDTO.getProcessingStatus().getTitle()));
        Integer editType = TaskTypeField.EDIT_TYPE.getIntValue(taskJSONObject);
        taskDTO.setEditType(TaskEditType.getTypeFromValue(editType));
        taskDTO.setEditTypeTitle(Helper.getTranslation(taskDTO.getEditType().getTitle()));
        JsonValue processingTime = taskJSONObject.get(TaskTypeField.PROCESSING_TIME.getKey());
        taskDTO.setProcessingTime(getDateFromJsonValue(processingTime));
        JsonValue processingBegin = taskJSONObject.get(TaskTypeField.PROCESSING_BEGIN.getKey());
        taskDTO.setProcessingBegin(getDateFromJsonValue(processingBegin));
        JsonValue processingEnd = taskJSONObject.get(TaskTypeField.PROCESSING_END.getKey());
        taskDTO.setProcessingEnd(getDateFromJsonValue(processingEnd));
        taskDTO.setTypeAutomatic(TaskTypeField.TYPE_AUTOMATIC.getBooleanValue(taskJSONObject));
        taskDTO.setTypeMetadata(TaskTypeField.TYPE_METADATA.getBooleanValue(taskJSONObject));
        taskDTO.setTypeImagesWrite(TaskTypeField.TYPE_IMAGES_WRITE.getBooleanValue(taskJSONObject));
        taskDTO.setTypeImagesRead(TaskTypeField.TYPE_IMAGES_READ.getBooleanValue(taskJSONObject));
        taskDTO.setBatchStep(TaskTypeField.BATCH_STEP.getBooleanValue(taskJSONObject));
        taskDTO.setRolesSize(TaskTypeField.ROLES.getSizeOfProperty(taskJSONObject));

        /*
         * We read the list of the process but not the list of templates, because only process tasks
         * are displayed in the task list and reading the template list would result in
         * never-ending loops as the list of templates reads the list of tasks.
         */
        Integer process = TaskTypeField.PROCESS_ID.getIntValue(taskJSONObject);
        if (process > 0) {
            taskDTO.setProcess(serviceManager.getProcessService().findById(process, true));
            taskDTO.setBatchAvailable(serviceManager.getProcessService()
                    .isProcessAssignedToOnlyOneLogisticBatch(taskDTO.getProcess().getBatches()));
        }

        if (!related) {
            convertRelatedJSONObjects(taskJSONObject, taskDTO);
        }
        return taskDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, TaskDTO taskDTO) throws DataException {
        int processingUser = TaskTypeField.PROCESSING_USER.getIntValue(jsonObject);
        if (processingUser != 0) {
            taskDTO.setProcessingUser(serviceManager.getUserService().findById(processingUser, true));
        }
        taskDTO.setRoles(
            convertRelatedJSONObjectToDTO(jsonObject, TaskTypeField.ROLES.getKey(), serviceManager.getRoleService()));
    }

    private String getDateFromJsonValue(JsonValue date) {
        return date != JsonValue.NULL ? date.toString().replace("\"", "") : "";
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
     * Get normalized title of task.
     *
     * @param title
     *            as String
     * @return normalized title
     */
    public String getNormalizedTitle(String title) {
        return title.replace(" ", "_");
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
            return Arrays.asList(process.getProject());
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
        if (task.getRoles() == null) {
            return 0;
        } else {
            return task.getRoles().size();
        }
    }

    /**
     * Get title with user.
     *
     * @return des Schritttitels sowie (sofern vorhanden) den Benutzer mit
     *         vollständigem Namen
     */
    public String getTitleWithUserName(Task task) {
        String result = task.getTitle();
        UserService userService = serviceManager.getUserService();
        if (task.getProcessingUser() != null && task.getProcessingUser().getId() != null
                && task.getProcessingUser().getId() != 0) {
            result += " (" + userService.getFullName(task.getProcessingUser()) + ")";
        }
        return result;
    }

    public String getProcessingStatusAsString(Task task) {
        return String.valueOf(task.getProcessingStatus().intValue());
    }

    public Integer setProcessingStatusAsString(String inputProcessingStatus) {
        return Integer.parseInt(inputProcessingStatus);
    }

    /**
     * Get script path.
     *
     * @param task
     *            object
     * @return script path as String
     */
    public String getScriptPath(Task task) {
        if (task.getScriptPath() != null && !task.getScriptPath().equals("")) {
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
        if (script == null || script.length() == 0) {
            return false;
        }
        script = script.replace("{", "(").replace("}", ")");
        DigitalDocumentInterface dd = null;
        Process po = task.getProcess();

        PrefsInterface prefs = serviceManager.getRulesetService().getPreferences(po.getRuleset());

        try {
            dd = serviceManager.getProcessService()
                    .readMetadataFile(serviceManager.getFileService().getMetadataFilePath(po), prefs)
                    .getDigitalDocument();
        } catch (PreferencesException | ReadException | IOException e2) {
            logger.error(e2);
        }
        VariableReplacer replacer = new VariableReplacer(dd, prefs, po, task);

        script = replacer.replace(script);
        boolean executedSuccessful = false;
        try {
            logger.info("Calling the shell: {}", script);

            CommandService commandService = serviceManager.getCommandService();
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
        if (script != null && !script.equals(" ") && script.length() != 0) {
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
            task.setEditType(TaskEditType.AUTOMATIC.getValue());
            if (successful) {
                task.setProcessingStatus(TaskStatus.DONE.getValue());
                serviceManager.getWorkflowControllerService().close(task);
            } else {
                task.setProcessingStatus(TaskStatus.OPEN.getValue());
                save(task);
            }
        }
    }

    private void abortTask(Task task) throws DataException {
        task.setProcessingStatus(TaskStatus.OPEN.getValue());
        task.setEditType(TaskEditType.AUTOMATIC.getValue());
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
            finishOrReturnAutomaticTask(task, automatic, executingThread.getException() == null);
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
        boolean automaticExportWithImages = ConfigCore
                .getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_WITH_IMAGES);
        boolean automaticExportWithOcr = ConfigCore
                .getBooleanParameterOrDefaultValue(ParameterCore.AUTOMATIC_EXPORT_WITH_OCR);
        Process process = task.getProcess();
        try {
            boolean validate = serviceManager.getProcessService().startDmsExport(process, automaticExportWithImages,
                automaticExportWithOcr);
            if (validate) {
                serviceManager.getWorkflowControllerService().close(task);
            } else {
                abortTask(task);
            }
        } catch (PreferencesException | WriteException | IOException | JAXBException e) {
            logger.error(e.getMessage(), e);
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
     * Find open tasks for current user sorted according to sort query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     *
     * @return the list of sorted tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenTasksForCurrentUser(String sort) throws DataException {
        User user = serviceManager.getUserService().getAuthenticatedUser();
        List<JsonObject> results = findByProcessingStatusAndUser(TaskStatus.INWORK, user.getId(), sort);
        return convertJSONObjectsToDTOs(results, false);
    }

    /**
     * Find open tasks without correction for current user sorted according to
     * sort query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of sorted tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenTasksWithoutCorrectionForCurrentUser(String sort) throws DataException {
        User user = serviceManager.getUserService().getAuthenticatedUser();
        List<JsonObject> results = findByProcessingStatusUserAndPriority(TaskStatus.INWORK, user.getId(), 10, sort);
        return convertJSONObjectsToDTOs(results, false);
    }

    /**
     * Find open not automatic tasks for current user sorted according to sort
     * query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of sorted tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenNotAutomaticTasksForCurrentUser(String sort) throws DataException {
        User user = serviceManager.getUserService().getAuthenticatedUser();
        List<JsonObject> results = findByProcessingStatusUserAndTypeAutomatic(TaskStatus.INWORK, user.getId(), false,
            sort);
        return convertJSONObjectsToDTOs(results, false);
    }

    /**
     * Find open not automatic tasks without correction for current user sorted
     * according to sort query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenNotAutomaticTasksWithoutCorrectionForCurrentUser(String sort) throws DataException {
        User user = serviceManager.getUserService().getAuthenticatedUser();
        List<JsonObject> results = findByProcessingStatusUserPriorityAndTypeAutomatic(TaskStatus.INWORK, user.getId(),
            10, false, sort);
        return convertJSONObjectsToDTOs(results, false);
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
     * Get tasks for non template processes for given project id and ordered by
     * ordering column in Task table.
     *
     * @param projectId
     *            as Integer
     * @return list of Long
     */
    public List<Task> getTasksForProjectHelper(Integer projectId) {
        return dao.getTasksForProcessesForProjectIdOrderByOrdering(projectId);
    }

    /**
     * Get size of tasks for non template processes for given project id and
     * ordered by ordering column in Task table.
     *
     * @param projectId
     *            as Integer
     * @return list of Long
     */
    public List<Long> getSizeOfTasksForProjectHelper(Integer projectId) {
        return dao.getSizeOfTasksForProcessesForProjectIdOrderByOrdering(projectId);
    }

    /**
     * Get average ordering of tasks for non template processes for given
     * project id and ordered by ordering column in Task table.
     *
     * @param projectId
     *            as Integer
     * @return list of Double
     */
    public List<Double> getAverageOrderingOfTasksForProjectHelper(Integer projectId) {
        return dao.getAverageOrderingOfTasksForProcessesForProjectIdOrderByOrdering(projectId);
    }

    /**
     * Get tasks for non template processes for given project id and ordered by
     * ordering column in Task table.
     *
     * @param processingStatus
     *            as Integer
     * @param projectId
     *            as Integer
     * @return list of Long
     */
    public List<Task> getTasksWithProcessingStatusForProjectHelper(Integer processingStatus, Integer projectId) {
        return dao.getTasksWithProcessingStatusForProcessesForProjectIdOrderByOrdering(processingStatus, projectId);
    }

    /**
     * Get size of tasks for non template processes for given project id and
     * ordered by ordering column in Task table.
     *
     * @param processingStatus
     *            as Integer
     * @param projectId
     *            as Integer
     * @return list of Long
     */
    public List<Long> getSizeOfTasksWithProcessingStatusForProjectHelper(Integer processingStatus, Integer projectId) {
        return dao.getSizeOfTasksWithProcessingStatusForProcessesForProjectIdOrderByOrdering(processingStatus,
            projectId);
    }

    /**
     * Get amount of images of tasks for non template processes for given
     * project id and ordered by ordering column in Task table.
     *
     * @param processingStatus
     *            as Integer
     * @param projectId
     *            as Integer
     * @return list of Long
     */
    public List<Long> getAmountOfImagesForTasksWithProcessingStatusForProjectHelper(Integer processingStatus,
            Integer projectId) {
        return dao.getAmountOfImagesForTasksWithProcessingStatusForProcessesForProjectIdOrderByOrdering(
            processingStatus, projectId);
    }

    /**
     * Set up matching error messages for unreachable tasks. Unreachable task is
     * this one which has no roles assigned to itself. Other possibility is that
     * given list is empty. It means that whole workflow is unreachable.
     *
     * @param tasks
     *            list of tasks for check
     */
    public void setUpErrorMessagesForUnreachableTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            Helper.setErrorMessage("noStepsInWorkflow");
        }
        for (Task task : tasks) {
            if (getRolesSize(task) == 0) {
                Helper.setErrorMessage("noUserInStep", new Object[] {task.getTitle() });
            }
        }
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
     * overwrite themselves. Also, contents can not be created in folders where
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
        Stream<Folder> generatableFolders = removeFoldersThatCannotBeGenerated(allowedFolders);
        return generatableFolders;
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
        Stream<Folder> filteredFolders = filteredWithSources
                .map(destinationAndSource -> destinationAndSource.getLeft());
        return filteredFolders;
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
}
