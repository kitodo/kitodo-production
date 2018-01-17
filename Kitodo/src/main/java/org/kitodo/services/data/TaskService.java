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
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.api.command.CommandResult;
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

import ugh.dl.DigitalDocument;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

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

    @Override
    public List<TaskDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {

        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null) {
            return new ArrayList<>();
        }

        // TODO: find other way than retrieving the form bean to access "hideCorrectionTasks" and "showAutomaticTasks"
        // e.g. which tasks should be returned!
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("processingUser", login.getMyBenutzer().getId(), true));
        query.must(createSimpleQuery("processingStatus", TaskStatus.LOCKED.getValue(), false));
        query.must(createSimpleQuery("processingStatus", TaskStatus.DONE.getValue(), false));
        AktuelleSchritteForm form = (AktuelleSchritteForm) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
        if (form.getHideCorrectionTasks()) {
            query.must(createSimpleQuery("priority", 10, true));
        }
        if (!form.getShowAutomaticTasks()) {
            query.must(createSimpleQuery("typeAutomatic", "false", true));
        }

        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString(), sort, offset, size), false);
    }


    /**
     * Method saves or removes dependencies with process, users and user's groups
     * related to modified task.
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

        List<JSONObject> templateProcesses = serviceManager.getProcessService().findByTemplate(true, null);
        if (templateProcesses.size() > 0) {
            Set<Integer> templates = new HashSet<>();
            for (JSONObject jsonObject : templateProcesses) {
                templates.add(getIdFromJSONObject(jsonObject));
            }

            boolQuery.mustNot(createSetQuery("processForTask.id", templates, true));
        }

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
    public List<JSONObject> findByProcessId(Integer id) throws DataException {
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
    List<JSONObject> findByProcessingStatusAndUser(TaskStatus taskStatus, Integer processingUser, String sort)
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
    List<JSONObject> findByProcessingStatusUserAndPriority(TaskStatus taskStatus, Integer processingUser,
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
    List<JSONObject> findByProcessingStatusUserAndTypeAutomatic(TaskStatus taskStatus, Integer processingUser,
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
    List<JSONObject> findByProcessingStatusUserPriorityAndTypeAutomatic(TaskStatus taskStatus, Integer processingUser,
            Integer priority, boolean typeAutomatic, String sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("processingStatus", taskStatus.getValue(), true));
        query.must(createSimpleQuery("processingUser", processingUser, true));
        query.must(createSimpleQuery("priority", priority, true));
        query.must(createSimpleQuery("typeAutomatic", String.valueOf(typeAutomatic), true));
        return searcher.findDocuments(query.toString(), sort);
    }

    @Override
    public TaskDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject taskJSONObject = getSource(jsonObject);
        taskDTO.setTitle(getStringPropertyForDTO(taskJSONObject, "title"));
        taskDTO.setLocalizedTitle(getLocalizedTitle(taskDTO.getTitle()));
        taskDTO.setPriority(getIntegerPropertyForDTO(taskJSONObject, "priority"));
        taskDTO.setOrdering(getIntegerPropertyForDTO(taskJSONObject, "ordering"));
        Integer taskStatus = getIntegerPropertyForDTO(taskJSONObject, "processingStatus");
        taskDTO.setProcessingStatus(TaskStatus.getStatusFromValue(taskStatus));
        taskDTO.setProcessingStatusTitle(Helper.getTranslation(taskDTO.getProcessingStatus().getTitle()));
        Integer editType = getIntegerPropertyForDTO(taskJSONObject, "processingStatus");
        taskDTO.setEditType(TaskEditType.getTypeFromValue(editType));
        taskDTO.setEditTypeTitle(Helper.getTranslation(taskDTO.getEditType().getTitle()));
        taskDTO.setProcessingTime(getStringPropertyForDTO(taskJSONObject, "processingTime"));
        taskDTO.setProcessingBegin(getStringPropertyForDTO(taskJSONObject, "processingBegin"));
        taskDTO.setProcessingEnd(getStringPropertyForDTO(taskJSONObject, "processingEnd"));
        taskDTO.setTypeAutomatic(getBooleanPropertyForDTO(taskJSONObject, "typeAutomatic"));
        taskDTO.setTypeMetadata(getBooleanPropertyForDTO(taskJSONObject, "typeMetadata"));
        taskDTO.setTypeImportFileUpload(getBooleanPropertyForDTO(taskJSONObject, "typeImportFileUpload"));
        taskDTO.setTypeExportRussian(getBooleanPropertyForDTO(taskJSONObject, "typeExportRussian"));
        taskDTO.setTypeImagesWrite(getBooleanPropertyForDTO(taskJSONObject, "typeImagesWrite"));
        taskDTO.setTypeImagesRead(getBooleanPropertyForDTO(taskJSONObject, "typeImagesRead"));
        taskDTO.setUsersSize(getSizeOfRelatedPropertyForDTO(taskJSONObject, "users"));
        taskDTO.setUserGroupsSize(getSizeOfRelatedPropertyForDTO(taskJSONObject, "userGroups"));
        Integer process = getIntegerPropertyForDTO(taskJSONObject, "processForTask.id");
        taskDTO.setProcess(serviceManager.getProcessService().findById(process, true));
        if (!related) {
            taskDTO = convertRelatedJSONObjects(taskJSONObject, taskDTO);
        }
        return taskDTO;
    }

    private TaskDTO convertRelatedJSONObjects(JSONObject jsonObject, TaskDTO taskDTO) throws DataException {
        Integer processingUser = getIntegerPropertyForDTO(jsonObject, "processingUser");
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
     * If you change anything in the logic of priorities make sure that you catch
     * dependencies on this system which are not directly related to priorities.
     * TODO: check it!
     */
    public Boolean isCorrectionStep(Task task) {
        return (task.getPriority() == 10);
    }

    public Task setCorrectionStep(Task task) {
        task.setPriority(10);
        return task;
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
     * Set processing status up.
     *
     * @param task
     *            object
     * @return task object
     */
    public Task setProcessingStatusUp(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
            task.setProcessingStatus(task.getProcessingStatus() + 1);
        }
        return task;
    }

    /**
     * Set processing status down.
     *
     * @param task
     *            object
     * @return task object
     */
    public Task setProcessingStatusDown(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.LOCKED) {
            task.setProcessingStatus(task.getProcessingStatus() - 1);
        }
        return task;
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
     * Get script and its path.
     *
     * @param task
     *            object
     * @return hash map - key script name and value script path
     */
    public HashMap<String, String> getScript(Task task) {
        HashMap<String, String> answer = new HashMap<>();
        if (task.getScriptPath() != null && !task.getScriptPath().equals("")) {
            answer.put(task.getScriptName(), task.getScriptPath());
        }
        return answer;
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
        DigitalDocument dd = null;
        Process po = task.getProcess();

        Prefs prefs = serviceManager.getRulesetService().getPreferences(po.getRuleset());

        try {
            dd = serviceManager.getProcessService().readMetadataFile(serviceManager.getFileService().getMetadataFilePath(po), prefs)
                    .getDigitalDocument();
        } catch (PreferencesException | ReadException | IOException e2) {
            logger.error(e2);
        }
        VariableReplacer replacer = new VariableReplacer(dd, prefs, po, task);

        script = replacer.replace(script);
        boolean executedSuccessful = false;
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Calling the shell: " + script);
            }

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
        if (logger.isDebugEnabled()) {
            logger.debug("starting script " + script);
        }
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
     * batch as read-only property "batchSize".
     * //TODO: is it ever used?
     *
     * @return whether this step’s process is in a batch
     */
    public boolean isBatchSize(Task task) {
        return task.getProcess().getBatches().size() > 0;
    }

    /**
     * Execute DMS export.
     *
     * @param step
     *            StepObject
     * @param automatic
     *            boolean
     */
    public void executeDmsExport(Task step, boolean automatic) throws DataException, ConfigurationException {
        ConfigCore.getBooleanParameter("automaticExportWithImages", true);
        if (!ConfigCore.getBooleanParameter("automaticExportWithOcr", true)) {
            // TODO: check why this if is empty
        }
        Process po = step.getProcess();
        try {
            boolean validate = serviceManager.getProcessService().startDmsExport(po,
                    ConfigCore.getBooleanParameter("automaticExportWithImages", true), false);
            if (validate) {
                serviceManager.getWorkflowService().close(step);
            } else {
                abortTask(step);
            }
        } catch (PreferencesException | WriteException | IOException e) {
            logger.error(e);
            abortTask(step);
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
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null) {
            return new ArrayList<>();
        }
        List<JSONObject> results = findByProcessingStatusAndUser(TaskStatus.INWORK, login.getMyBenutzer().getId(),
                sort);
        return convertJSONObjectsToDTOs(results, false);
    }

    /**
     * Find open tasks without correction for current user sorted according to sort
     * query.
     * 
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of sorted tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenTasksWithoutCorrectionForCurrentUser(String sort) throws DataException {
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null) {
            return new ArrayList<>();
        }
        List<JSONObject> results = findByProcessingStatusUserAndPriority(TaskStatus.INWORK,
                login.getMyBenutzer().getId(), 10, sort);
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
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null) {
            return new ArrayList<>();
        }
        List<JSONObject> results = findByProcessingStatusUserAndTypeAutomatic(TaskStatus.INWORK,
                login.getMyBenutzer().getId(), false, sort);
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
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null) {
            return new ArrayList<>();
        }
        List<JSONObject> results = findByProcessingStatusUserPriorityAndTypeAutomatic(TaskStatus.INWORK,
                login.getMyBenutzer().getId(), 10, false, sort);
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
