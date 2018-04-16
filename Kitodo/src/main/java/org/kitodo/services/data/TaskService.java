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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.forms.AktuelleSchritteForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonValue;

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
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TaskType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.TaskDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.command.CommandService;
import org.kitodo.services.data.base.TitleSearchService;

public class TaskService extends TitleSearchService<Task, TaskDTO, TaskDAO> {

    private static final Logger logger = LogManager.getLogger(TaskService.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private static TaskService instance = null;

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
    private BoolQueryBuilder createUserTaskQuery(User user) {

        BoolQueryBuilder subquery = new BoolQueryBuilder();
        subquery.should(createSimpleQuery("processingUser", user.getId(), true));
        subquery.should(createSimpleQuery("users.id", user.getId(), true));
        for (UserGroup userGroup : user.getUserGroups()) {
            subquery.should(createSimpleQuery("userGroups.id", userGroup.getId(), true));
        }

        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(subquery);
        query.must(createSimpleQuery("processingStatus", TaskStatus.LOCKED.getValue(), false));
        query.must(createSimpleQuery("processingStatus", TaskStatus.DONE.getValue(), false));
        query.must(createSimpleQuery("templateForTask.id", 0, true));

        // TODO: find other way than retrieving the form bean to access
        // "hideCorrectionTasks" and "showAutomaticTasks"
        // e.g. which tasks should be returned!
        AktuelleSchritteForm form = (AktuelleSchritteForm) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
        if (Objects.equals(form, null)) {
            form = new AktuelleSchritteForm();
        }
        if (form.getHideCorrectionTasks()) {
            query.must(createSimpleQuery("priority", 10, true));
        }
        if (!form.getShowAutomaticTasks()) {
            query.must(createSimpleQuery("typeAutomatic", "false", true));
        }

        return query;
    }

    @Override
    public List<TaskDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        User user = Helper.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }
        BoolQueryBuilder query = createUserTaskQuery(user);
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString(), sort, offset, size), false);
    }

    @Override
    public String createCountQuery(Map filters) {
        User user = Helper.getCurrentUser();
        if (user == null) {
            return "";
        }
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
        manageProcessingUserDependenciesForIndex(task);
        manageUsersDependenciesForIndex(task);
        manageUserGroupsDependenciesForIndex(task);
    }

    private void manageProcessDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            Process process = task.getProcess();
            if (process != null) {
                process.getTasks().remove(task);
                serviceManager.getProcessService().saveToIndex(process);
            }
        } else {
            Process process = task.getProcess();
            serviceManager.getProcessService().saveToIndex(process);
        }
    }

    private void manageProcessingUserDependenciesForIndex(Task task)
            throws CustomResponseException, DAOException, DataException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            User user = task.getProcessingUser();
            if (user != null) {
                user.getProcessingTasks().remove(task);
                serviceManager.getUserService().saveToIndex(user);
            }
        } else {
            User user = task.getProcessingUser();
            if (user != null) {
                serviceManager.getUserService().saveToIndex(user);
            }
            reIndexUserAfterRemoveFromProcessing(task);
        }
    }

    private void reIndexUserAfterRemoveFromProcessing(Task task)
            throws CustomResponseException, DAOException, DataException, IOException {
        List<UserDTO> userDTOS = serviceManager.getUserService().findByProcessingTask(task.getId(), true);
        for (UserDTO userDTO : userDTOS) {
            serviceManager.getUserService().saveToIndex(serviceManager.getUserService().getById(userDTO.getId()));
        }
    }

    private void manageUsersDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            for (User user : task.getUsers()) {
                user.getTasks().remove(task);
                serviceManager.getUserService().saveToIndex(user);
            }
        } else {
            for (User user : task.getUsers()) {
                serviceManager.getUserService().saveToIndex(user);
            }
        }
    }

    private void manageUserGroupsDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            for (UserGroup userGroup : task.getUserGroups()) {
                userGroup.getTasks().remove(task);
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        } else {
            for (UserGroup userGroup : task.getUserGroups()) {
                serviceManager.getUserGroupService().saveToIndex(userGroup);
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
            // do nothing - there is not new nor old user
        } else if (Objects.isNull(user)) {
            currentProcessingUser.getProcessingTasks().remove(task);
            task.setProcessingUser(null);
        } else if (Objects.isNull(currentProcessingUser)) {
            user.getProcessingTasks().add(task);
            task.setProcessingUser(user);
        } else if (Objects.equals(currentProcessingUser.getId(), user.getId())) {
            // do nothing - both are the same
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
        return countDatabaseRows("FROM Task");
    }

    /**
     * Get amount of current tasks for current user.
     *
     * @param open
     *            true or false
     * @param inProcessing
     *            true or false
     * @param user
     *            current user
     * @return amount of current tasks for current user
     */
    public Long getAmountOfCurrentTasks(boolean open, boolean inProcessing, User user) throws DataException {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        Set<Integer> processingStatus = new HashSet<>();
        processingStatus.add(1);
        processingStatus.add(2);

        if (!open && !inProcessing) {
            boolQuery.must(getQueryForProcessingStatus(processingStatus));
        } else if (open && !inProcessing) {
            boolQuery.must(createSimpleQuery("processingStatus", 1, true));
        } else if (!open && inProcessing) {
            boolQuery.must(createSimpleQuery("processingStatus", 2, true));
        } else {
            boolQuery.must(createSetQuery("processingStatus", processingStatus, true));
        }

        Set<Integer> userGroups = new HashSet<>();
        for (UserGroup userGroup : user.getUserGroups()) {
            userGroups.add(userGroup.getId());
        }
        BoolQueryBuilder nestedBoolQuery = new BoolQueryBuilder();
        nestedBoolQuery.should(createSetQuery("userGroups.id", userGroups, true));
        nestedBoolQuery.should(createSimpleQuery("users.id", user.getId(), true));
        boolQuery.must(nestedBoolQuery);
        boolQuery.must(createSimpleQuery("templateForTask.id", 0, true));

        return count(boolQuery.toString());
    }

    /**
     * Get query for processing statuses.
     *
     * @param processingStatus
     *            set of processing statuses as Integer
     * @return query as QueryBuilder
     */
    public QueryBuilder getQueryForProcessingStatus(Set<Integer> processingStatus) {
        return createSetQuery("processingStatus", processingStatus, true);
    }

    /**
     * Find tasks by id of process.
     *
     * @param id
     *            of process
     * @return list of JSON objects with tasks for specific process id
     */
    public List<JsonObject> findByProcessId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("process", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Get query for process ids.
     *
     * @param processIds
     *            set of process ids as Integer
     * @return query as QueryBuilder
     */
    public QueryBuilder getQueryProcessIds(Set<Integer> processIds) {
        return createSetQuery("process", processIds, true);
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
        query.must(createSimpleQuery("processingStatus", taskStatus.getValue(), true));
        query.must(createSimpleQuery("processingUser", processingUser, true));
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
    List<JsonObject> findByProcessingStatusUserAndPriority(TaskStatus taskStatus, Integer processingUser,
            Integer priority, String sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("processingStatus", taskStatus.getValue(), true));
        query.must(createSimpleQuery("processingUser", processingUser, true));
        query.must(createSimpleQuery("priority", priority, true));
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
    List<JsonObject> findByProcessingStatusUserAndTypeAutomatic(TaskStatus taskStatus, Integer processingUser,
            boolean typeAutomatic, String sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("processingStatus", taskStatus.getValue(), true));
        query.must(createSimpleQuery("processingUser", processingUser, true));
        query.must(createSimpleQuery("typeAutomatic", String.valueOf(typeAutomatic), true));
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
    List<JsonObject> findByProcessingStatusUserPriorityAndTypeAutomatic(TaskStatus taskStatus, Integer processingUser,
            Integer priority, boolean typeAutomatic, String sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("processingStatus", taskStatus.getValue(), true));
        query.must(createSimpleQuery("processingUser", processingUser, true));
        query.must(createSimpleQuery("priority", priority, true));
        query.must(createSimpleQuery("typeAutomatic", String.valueOf(typeAutomatic), true));
        return searcher.findDocuments(query.toString(), sort);
    }

    @Override
    public TaskDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject taskJSONObject = jsonObject.getJsonObject("_source");
        taskDTO.setTitle(taskJSONObject.getString("title"));
        taskDTO.setLocalizedTitle(getLocalizedTitle(taskDTO.getTitle()));
        taskDTO.setPriority(taskJSONObject.getInt("priority"));
        taskDTO.setOrdering(taskJSONObject.getInt("ordering"));
        Integer taskStatus = taskJSONObject.getInt("processingStatus");
        taskDTO.setProcessingStatus(TaskStatus.getStatusFromValue(taskStatus));
        taskDTO.setProcessingStatusTitle(Helper.getTranslation(taskDTO.getProcessingStatus().getTitle()));
        Integer editType = taskJSONObject.getInt("processingStatus");
        taskDTO.setEditType(TaskEditType.getTypeFromValue(editType));
        taskDTO.setEditTypeTitle(Helper.getTranslation(taskDTO.getEditType().getTitle()));
        JsonValue processingTime = taskJSONObject.get("processingTime");
        taskDTO.setProcessingTime(processingTime != JsonValue.NULL ? processingTime.toString() : null);
        JsonValue processingBegin = taskJSONObject.get("processingBegin");
        taskDTO.setProcessingBegin(processingBegin != JsonValue.NULL ? processingBegin.toString() : null);
        JsonValue processingEnd = taskJSONObject.get("processingEnd");
        taskDTO.setProcessingEnd(processingEnd != JsonValue.NULL ? processingEnd.toString() : null);
        taskDTO.setTypeAutomatic(taskJSONObject.getBoolean("typeAutomatic"));
        taskDTO.setTypeMetadata(taskJSONObject.getBoolean("typeMetadata"));
        taskDTO.setTypeImportFileUpload(taskJSONObject.getBoolean("typeImportFileUpload"));
        taskDTO.setTypeExportRussian(taskJSONObject.getBoolean("typeExportRussian"));
        taskDTO.setTypeImagesWrite(taskJSONObject.getBoolean("typeImagesWrite"));
        taskDTO.setTypeImagesRead(taskJSONObject.getBoolean("typeImagesRead"));
        taskDTO.setUsersSize(getSizeOfRelatedPropertyForDTO(taskJSONObject, "users"));
        taskDTO.setUserGroupsSize(getSizeOfRelatedPropertyForDTO(taskJSONObject, "userGroups"));
        Integer process = taskJSONObject.getInt("processForTask.id");
        if (process > 0) {
            taskDTO.setProcess(serviceManager.getProcessService().findById(process, true));
        }
        Integer template = taskJSONObject.getInt("templateForTask.id");
        if (template > 0) {
            taskDTO.setTemplate(serviceManager.getTemplateService().findById(template, true));
        }
        if (!related) {
            taskDTO = convertRelatedJSONObjects(taskJSONObject, taskDTO);
        }
        return taskDTO;
    }

    private TaskDTO convertRelatedJSONObjects(JsonObject jsonObject, TaskDTO taskDTO) throws DataException {
        Integer processingUser = jsonObject.getInt("processingUser");
        if (processingUser != 0) {
            taskDTO.setProcessingUser(serviceManager.getUserService().findById(processingUser, true));
        }
        taskDTO.setUsers(convertRelatedJSONObjectToDTO(jsonObject, "users", serviceManager.getUserService()));
        taskDTO.setUserGroups(
            convertRelatedJSONObjectToDTO(jsonObject, "userGroups", serviceManager.getUserGroupService()));
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

    // a parameter is given here (even if not used) because jsf expects setter
    // convention
    public void setProcessingTimeNow(Task task) {
        task.setProcessingTime(new Date());
    }

    public int getProcessingTimeNow() {
        return 1;
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
     * Get users' list size.
     *
     * @param task
     *            object
     * @return size
     */
    public int getUsersSize(Task task) {
        if (task.getUsers() == null) {
            return 0;
        } else {
            return task.getUsers().size();
        }
    }

    /**
     * Get user groups' list size.
     *
     * @param task
     *            object
     * @return size
     */
    public int getUserGroupsSize(Task task) {
        if (task.getUserGroups() == null) {
            return 0;
        } else {
            return task.getUserGroups().size();
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
            if (automatic) {
                if (commandResult.isSuccessful()) {
                    task.setEditType(TaskEditType.AUTOMATIC.getValue());
                    task.setProcessingStatus(TaskStatus.DONE.getValue());
                } else {
                    task.setEditType(TaskEditType.AUTOMATIC.getValue());
                    task.setProcessingStatus(TaskStatus.OPEN.getValue());
                    save(task);
                }
            }
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException: ", e.getMessage());
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

    private void abortTask(Task task) throws DataException {
        task.setProcessingStatus(TaskStatus.OPEN.getValue());
        task.setEditType(TaskEditType.AUTOMATIC.getValue());
        save(task);
    }

    /**
     * Returns whether this is a step of a process that is part of at least one
     * batch as read-only property "batchSize". //TODO: is it ever used?
     *
     * @return whether this step’s process is in a batch
     */
    public boolean isBatchSize(Task task) {
        return task.getProcess().getBatches().size() > 0;
    }

    /**
     * Execute DMS export.
     *
     * @param task
     *            as Task object
     */
    public void executeDmsExport(Task task) throws DataException {
        boolean automaticExportWithImages = ConfigCore.getBooleanParameter("automaticExportWithImages", true);
        boolean automaticExportWithOcr = ConfigCore.getBooleanParameter("automaticExportWithOcr", true);
        Process process = task.getProcess();
        try {
            boolean validate = serviceManager.getProcessService().startDmsExport(process, automaticExportWithImages,
                automaticExportWithOcr);
            if (validate) {
                serviceManager.getWorkflowService().close(task);
            } else {
                abortTask(task);
            }
        } catch (PreferencesException | WriteException | IOException e) {
            logger.error(e);
            abortTask(task);
        }
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
        User user = Helper.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }
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
        User user = Helper.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }
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
        User user = Helper.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }
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
        User user = Helper.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }
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
        return dao.getTasksForNotTemplateProcessesForProjectIdOrderByOrdering(projectId);
    }

    /**
     * Get size of tasks for non template processes for given project id and ordered
     * by ordering column in Task table.
     *
     * @param projectId
     *            as Integer
     * @return list of Long
     */
    public List<Long> getSizeOfTasksForProjectHelper(Integer projectId) {
        return dao.getSizeOfTasksForNotTemplateProcessesForProjectIdOrderByOrdering(projectId);
    }

    /**
     * Get average ordering of tasks for non template processes for given project id
     * and ordered by ordering column in Task table.
     *
     * @param projectId
     *            as Integer
     * @return list of Double
     */
    public List<Double> getAverageOrderingOfTasksForProjectHelper(Integer projectId) {
        return dao.getAverageOrderingOfTasksForNotTemplateProcessesForProjectIdOrderByOrdering(projectId);
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
        return dao.getTasksWithProcessingStatusForNotTemplateProcessesForProjectIdOrderByOrdering(processingStatus,
            projectId);
    }

    /**
     * Get size of tasks for non template processes for given project id and ordered
     * by ordering column in Task table.
     *
     * @param processingStatus
     *            as Integer
     * @param projectId
     *            as Integer
     * @return list of Long
     */
    public List<Long> getSizeOfTasksWithProcessingStatusForProjectHelper(Integer processingStatus, Integer projectId) {
        return dao.getSizeOfTasksWithProcessingStatusForNotTemplateProcessesForProjectIdOrderByOrdering(
            processingStatus, projectId);
    }

    /**
     * Get amount of images of tasks for non template processes for given project id
     * and ordered by ordering column in Task table.
     *
     * @param processingStatus
     *            as Integer
     * @param projectId
     *            as Integer
     * @return list of Long
     */
    public List<Long> getAmountOfImagesForTasksWithProcessingStatusForProjectHelper(Integer processingStatus,
            Integer projectId) {
        return dao.getAmountOfImagesForTasksWithProcessingStatusForNotTemplateProcessesForProjectIdOrderByOrdering(
            processingStatus, projectId);
    }
}
