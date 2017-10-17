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

package de.sub.goobi.forms;

import static java.lang.Math.toIntExact;

import de.sub.goobi.helper.IndexWorker;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.IndexRestClient;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;

@Named
@ApplicationScoped
public class IndexingForm {

    private static IndexRestClient indexRestClient = IndexRestClient.getInstance();
    private static final String MAPPING_STARTED_MESSAGE = "mapping_started";
    private static final String MAPPING_FINISHED_MESSAGE = "mapping_finished";
    private static final String MAPPING_FAILED_MESSAGE = "mapping_failed";
    private static final String DELETION_STARTED_MESSAGE = "deletion_started";
    private static final String DELETION_FINISHED_MESSAGE = "deletion_finished";
    private static final String DELETION_FAILED_MESSAGE = "deletion_failed";
    private static final String INDEXING_STARTED_MESSAGE = "indexing_started";
    private static final String INDEXING_FINISHED_MESSAGE = "indexing_finished";
    private static final String POLLING_CHANNEL_NAME = "togglePollingChannel";

    public LocalDateTime getIndexingStartedTime() {
        return indexingStartedTime;
    }

    private int pause = 1000;

    class IndexAllThread extends Thread {

        @Override
        public void run() {
            resetGlobalProgress();
            indexingAll = true;

            startUserIndexing();
            startUserGroupIndexing();
            startProjectIndexing();
            startRulesetIndexing();
            startDocketIndexing();
            startTaskIndexing();
            startTemplateIndexing();
            startProcessIndexing();
            startBatchIndexing();
            startPropertyIndexing();
            startWorkpieceIndexing();
            startFilterIndexing();

            try {
                sleep(pause);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted: " + e.getMessage());
            }

            currentIndexState = ObjectType.NONE;
            indexingAll = false;

            pollingChannel.send(INDEXING_FINISHED_MESSAGE);
        }
    }

    @Inject
    @Push(channel = POLLING_CHANNEL_NAME)
    private PushContext pollingChannel;

    private static final Logger logger = LogManager.getLogger(IndexingForm.class);

    private transient ServiceManager serviceManager = new ServiceManager();

    private ObjectType currentIndexState = ObjectType.NONE;

    private boolean indexingAll = false;

    private enum indexStates {
        NO_STATE,
        DELETE_ERROR,
        DELETE_SUCCESS,
        MAPPING_ERROR,
        MAPPING_SUCCESS,
    }

    private enum indexingStates {
        NO_STATE,
        INDEXING_STARTED,
        INDEXING_SUCCESSFUL,
        INDEXING_FAILED,
    }

    private indexStates currentState = indexStates.NO_STATE;

    private Map<ObjectType, LocalDateTime> lastIndexed = new EnumMap<>(ObjectType.class);

    private Map<ObjectType, indexingStates> objectIndexingStates = new EnumMap<>(ObjectType.class);

    private LocalDateTime indexingStartedTime;

    private int indexedBatches = 0;
    private int indexedDockets = 0;
    private int indexedProcesses = 0;
    private int indexedProjects = 0;
    private int indexedProperties = 0;
    private int indexedRulesetes = 0;
    private int indexedTasks = 0;
    private int indexedTemplates = 0;
    private int indexedUsers = 0;
    private int indexedUsergroups = 0;
    private int indexedWorkpieces = 0;
    private int indexedFilter = 0;

    private IndexWorker batchWorker;
    private IndexWorker docketWorker;
    private IndexWorker processWorker;
    private IndexWorker projectWorker;
    private IndexWorker propertyWorker;
    private IndexWorker rulesetWorker;
    private IndexWorker taskWorker;
    private IndexWorker templateWorker;
    private IndexWorker userWorker;
    private IndexWorker usergroupWorker;
    private IndexWorker workpieceWorker;
    private IndexWorker filterWorker;

    private Thread indexerThread = null;

    /**
     * Standard constructor.
     */
    public IndexingForm() {
        this.batchWorker = new IndexWorker(serviceManager.getBatchService());
        this.docketWorker = new IndexWorker(serviceManager.getDocketService());
        this.processWorker = new IndexWorker(serviceManager.getProcessService());
        this.projectWorker = new IndexWorker(serviceManager.getProjectService());
        this.propertyWorker = new IndexWorker(serviceManager.getPropertyService());
        this.rulesetWorker = new IndexWorker(serviceManager.getRulesetService());
        this.taskWorker = new IndexWorker(serviceManager.getTaskService());
        this.templateWorker = new IndexWorker(serviceManager.getTemplateService());
        this.userWorker = new IndexWorker(serviceManager.getUserService());
        this.usergroupWorker = new IndexWorker(serviceManager.getUserGroupService());
        this.workpieceWorker = new IndexWorker(serviceManager.getWorkpieceService());
        this.filterWorker = new IndexWorker(serviceManager.getFilterService());
        try {
            indexedBatches = toIntExact(serviceManager.getBatchService().count());
            indexedDockets = toIntExact(serviceManager.getDocketService().count());
            indexedProcesses = toIntExact(serviceManager.getProcessService().count());
            indexedProjects = toIntExact(serviceManager.getProjectService().count());
            indexedProperties = toIntExact(serviceManager.getPropertyService().count());
            indexedRulesetes = toIntExact(serviceManager.getRulesetService().count());
            indexedTasks = toIntExact(serviceManager.getTaskService().count());
            indexedTemplates = toIntExact(serviceManager.getTemplateService().count());
            indexedUsers = toIntExact(serviceManager.getUserService().count());
            indexedUsergroups = toIntExact(serviceManager.getUserGroupService().count());
            indexedWorkpieces = toIntExact(serviceManager.getWorkpieceService().count());
            indexedFilter = toIntExact(serviceManager.getFilterService().count());
        } catch (DataException e) {
            logger.error(e.getMessage());
        }

        for (ObjectType objectType : ObjectType.values()) {
            objectIndexingStates.put(objectType, indexingStates.NO_STATE);
        }

        indexRestClient.setIndex(ConfigMain.getParameter("elasticsearch.index", "kitodo"));
    }

    private long getCount(SearchService searchService) {
        try {
            return searchService.countDatabaseRows();
        } catch (DAOException e) {
            logger.error(e.getMessage());
            return 0;
        }
    }

    /**
     * Return the number of batches.
     *
     * @return long number of batches
     */
    public long getBatchCount() {
        return getCount(serviceManager.getBatchService());
    }

    /**
     * Return the number of dockets.
     *
     * @return long number of dockets
     */
    public long getDocketCount() {
        return getCount(serviceManager.getDocketService());
    }

    /**
     * Return the number of processes.
     *
     * @return long number of processes
     */
    public long getProcessCount() {
        return getCount(serviceManager.getProcessService());
    }

    /**
     * Return the number of projects.
     *
     * @return long number of projects
     */
    public long getProjectCount() {
        return getCount(serviceManager.getProjectService());
    }

    /**
     * Return the number of properties.
     *
     * @return long number of properties
     */
    public long getPropertyCount() {
        return getCount(serviceManager.getPropertyService());
    }

    /**
     * Return the number of rulesets.
     *
     * @return long number of rulesets
     */
    public long getRulesetCount() {
        return getCount(serviceManager.getRulesetService());
    }

    /**
     * Return the number of tasks.
     *
     * @return long number of tasks
     */
    public long getTaskCount() {
        return getCount(serviceManager.getTaskService());
    }

    /**
     * Return the number of templates.
     *
     * @return long number of templates
     */
    public long getTemplateCount() {
        return getCount(serviceManager.getTemplateService());
    }

    /**
     * Return the number of users.
     *
     * @return long number of users
     */
    public long getUserCount() {
        return getCount(serviceManager.getUserService());
    }

    /**
     * Return the number of user groups.
     *
     * @return long number of user groups
     */
    public long getUserGroupCount() {
        return getCount(serviceManager.getUserGroupService());
    }

    /**
     * Return the number of workpieces.
     *
     * @return long number of workpieces
     */
    public long getWorkpieceCount() {
        return getCount(serviceManager.getWorkpieceService());
    }

    /**
     * Return the number of filters.
     *
     * @return long number of filters
     */
    public long getFilterCount() {
        return getCount(serviceManager.getFilterService());
    }

    /**
     * Return the total number of all objects that can be indexed.
     *
     * @return long number of all items that can be written to the index
     */
    public long getTotalCount() {
        return (getBatchCount() + getDocketCount() + getProcessCount() + getProjectCount() + getPropertyCount()
                + getRulesetCount() + getTaskCount() + getTemplateCount() + getUserCount() + getUserGroupCount()
                + getWorkpieceCount() + getFilterCount());
    }

    /**
     * Return the number of currently indexed batches.
     *
     * @return int number of currently indexed batches
     */
    public int getIndexedBatches() {
        if (currentIndexState == ObjectType.BATCH) {
            indexedBatches = batchWorker.getIndexedObjects();
        }
        return indexedBatches;
    }

    /**
     * Return the number of currently indexed dockets.
     *
     * @return int number of currently indexed dockets
     */
    public int getIndexedDockets() {
        if (currentIndexState == ObjectType.DOCKET) {
            indexedDockets = docketWorker.getIndexedObjects();
        }
        return indexedDockets;
    }

    /**
     * Return the number of currently indexed processes.
     *
     * @return int number of currently indexed processes
     */
    public int getIndexedProcesses() {
        if (currentIndexState == ObjectType.PROCESS) {
            indexedProcesses = processWorker.getIndexedObjects();
        }
        return indexedProcesses;
    }

    /**
     * Return the number of currently indexed projects.
     *
     * @return int number of currently indexed projects
     */
    public int getIndexedProjects() {
        if (currentIndexState == ObjectType.PROJECT) {
            indexedProjects = projectWorker.getIndexedObjects();
        }
        return indexedProjects;
    }

    /**
     * Return the number of currently indexed properties.
     *
     * @return int number of currently indexed properties
     */
    public int getIndexedProperties() {
        if (currentIndexState == ObjectType.PROPERTY) {
            indexedProperties = propertyWorker.getIndexedObjects();
        }
        return indexedProperties;
    }

    /**
     * Return the number of currently indexed rulesets.
     *
     * @return int number of currently indexed rulesets
     */
    public int getIndexedRulesets() {
        if (currentIndexState == ObjectType.RULESET) {
            indexedRulesetes = rulesetWorker.getIndexedObjects();
        }
        return indexedRulesetes;
    }

    /**
     * Return the number of currently indexed templates.
     *
     * @return int number of currently indexed templates
     */
    public int getIndexedTemplates() {
        if (currentIndexState == ObjectType.TEMPLATE) {
            indexedTemplates = templateWorker.getIndexedObjects();
        }
        return indexedTemplates;
    }

    /**
     * Return the number of currently indexed tasks.
     *
     * @return int number of currently indexed tasks
     */
    public int getIndexedTasks() {
        if (currentIndexState == ObjectType.TASK) {
            indexedTasks = taskWorker.getIndexedObjects();
        }
        return indexedTasks;
    }

    /**
     * Return the number of currently indexed users.
     *
     * @return int number of currently indexed users
     */
    public int getIndexedUsers() {
        if (currentIndexState == ObjectType.USER) {
            indexedUsers = userWorker.getIndexedObjects();
        }
        return indexedUsers;
    }

    /**
     * Return the number of currently indexed user groups.
     *
     * @return int number of currently indexed user groups
     */
    public int getIndexedUserGroups() {
        if (currentIndexState == ObjectType.USERGROUP) {
            indexedUsergroups = usergroupWorker.getIndexedObjects();
        }
        return indexedUsergroups;
    }

    /**
     * Return the number of currently indexed workpieces.
     *
     * @return int number of currently indexed workpieces
     */
    public int getIndexedWorkpieces() {
        if (currentIndexState == ObjectType.WORKPIECE) {
            indexedWorkpieces = workpieceWorker.getIndexedObjects();
        }
        return indexedWorkpieces;
    }

    /**
     * Return the number of currently indexed filters.
     *
     * @return int number of currently indexed filters
     */
    public int getIndexedFilter() {
        if (currentIndexState == ObjectType.FILTER) {
            indexedFilter = filterWorker.getIndexedObjects();
        }
        return indexedFilter;
    }

    /**
     * Return the number of all objects processed during the current indexing
     * progress.
     *
     * @return int number of all currently indexed objects
     */
    public int getAllIndexed() {
        return (getIndexedBatches() + getIndexedDockets() + getIndexedProcesses() + getIndexedProjects()
                + getIndexedProperties() + getIndexedRulesets() + getIndexedTasks() + getIndexedTemplates()
                + getIndexedUsers() + getIndexedUserGroups() + getIndexedWorkpieces() + getIndexedFilter());
    }

    /**
     * Return the date and time the last batch indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last batch indexing process
     */
    public LocalDateTime getBatchesLastIndexedDate() {
        return lastIndexed.get(ObjectType.BATCH);
    }

    /**
     * Return the date and time the last docket indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last docket indexing process
     */
    public LocalDateTime getDocketsLastIndexedDate() {
        return lastIndexed.get(ObjectType.DOCKET);
    }

    /**
     * Return the date and time the last process indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last process indexing process
     */
    public LocalDateTime getProcessesLastIndexedDate() {
        return lastIndexed.get(ObjectType.PROCESS);
    }

    /**
     * Return the date and time the last project indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last project indexing process
     */
    public LocalDateTime getProjectsLastIndexedDate() {
        return lastIndexed.get(ObjectType.PROJECT);
    }

    /**
     * Return the date and time the last properties indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last properties indexing process
     */
    public LocalDateTime getPropertiesLastIndexedDate() {
        return lastIndexed.get(ObjectType.PROPERTY);
    }

    /**
     * Return the date and time the last ruleset indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last ruleset indexing process
     */
    public LocalDateTime getRulsetsLastIndexedDate() {
        return lastIndexed.get(ObjectType.RULESET);
    }

    /**
     * Return the date and time the last template indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last template indexing process
     */
    public LocalDateTime getTemplatesLastIndexedDate() {
        return lastIndexed.get(ObjectType.TEMPLATE);
    }

    /**
     * Return the date and time the last task indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last task indexing process
     */
    public LocalDateTime getTasksLastIndexedDate() {
        return lastIndexed.get(ObjectType.TASK);
    }

    /**
     * Return the date and time the last user indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last user indexing process
     */
    public LocalDateTime getUsersLastIndexedDate() {
        return lastIndexed.get(ObjectType.USER);
    }

    /**
     * Return the date and time the last user group indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last user group indexing process
     */
    public LocalDateTime getUserGroupsLastIndexedDate() {
        return lastIndexed.get(ObjectType.USERGROUP);
    }

    /**
     * Return the date and time the last workpiece indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last workpiece indexing process
     */
    public LocalDateTime getWorkpiecesLastIndexedDate() {
        return lastIndexed.get(ObjectType.WORKPIECE);
    }

    /**
     * Returns the progress of the current batch indexing process in percent.
     *
     * @return the batch indexing progress
     */
    public int getBatchIndexingProgress() {
        return getProgress(getBatchCount(), ObjectType.BATCH, getIndexedBatches());
    }

    /**
     * Returns the progress of the current docket indexing process in percent.
     *
     * @return the docket indexing progress
     */
    public int getDocketsIndexingProgress() {
        return getProgress(getDocketCount(), ObjectType.DOCKET, getIndexedDockets());
    }

    /**
     * Returns the progress of the current process indexing process in percent.
     *
     * @return the process indexing progress
     */
    public int getProcessIndexingProgress() {
        return getProgress(getProcessCount(), ObjectType.PROCESS, getIndexedProcesses());
    }

    /**
     * Returns the progress of the current project indexing process in percent.
     *
     * @return the project indexing progress
     */
    public int getProjectsIndexingProgress() {
        return getProgress(getProjectCount(), ObjectType.PROJECT, getIndexedProjects());
    }

    /**
     * Returns the progress of the current properties indexing process in percent.
     *
     * @return the properties indexing progress
     */
    public int getPropertiesIndexingProgress() {
        return getProgress(getPropertyCount(), ObjectType.PROPERTY, getIndexedProperties());
    }

    /**
     * Returns the progress of the current ruleset indexing process in percent.
     *
     * @return the ruleset indexing progress
     */
    public int getRulesetsIndexingProgress() {
        return getProgress(getRulesetCount(), ObjectType.RULESET, getIndexedRulesets());
    }

    /**
     * Returns the progress of the current template indexing process in percent.
     *
     * @return the template indexing progress
     */
    public int getTemplatesIndexingProgress() {
        return getProgress(getTemplateCount(), ObjectType.TEMPLATE, getIndexedTemplates());
    }

    /**
     * Returns the progress of the current task indexing process in percent.
     *
     * @return the task indexing progress
     */
    public int getTasksIndexingProgress() {
        return getProgress(getTaskCount(), ObjectType.TASK, getIndexedTasks());
    }

    /**
     * Returns the progress of the current user indexing process in percent.
     *
     * @return the user indexing progress
     */
    public int getUserIndexingProgress() {
        return getProgress(getUserCount(), ObjectType.USER, getIndexedUsers());
    }

    /**
     * Returns the progress of the current usergroup indexing process in percent.
     *
     * @return the usergroup indexing progress
     */
    public int getUserGroupIndexingProgress() {
        return getProgress(getUserGroupCount(), ObjectType.USERGROUP, getIndexedUserGroups());
    }

    /**
     * Returns the progress of the current workpiece indexing process in percent.
     *
     * @return the workpiece indexing progress
     */
    public int getWorkpieceIndexingProgress() {
        return getProgress(getWorkpieceCount(), ObjectType.WORKPIECE, getIndexedWorkpieces());
    }

    /**
     * Returns the progress of the current filter indexing process in percent.
     *
     * @return the filter indexing progress
     */
    public int getFilterIndexingProgress() {
        return getProgress(getFilterCount(), ObjectType.FILTER, getIndexedFilter());
    }

    private void startIndexing(ObjectType type, IndexWorker worker) {
        currentState = indexStates.NO_STATE;
        int attempts = 0;
        while (attempts < 10) {
            try {
                if (Objects.equals(currentIndexState, ObjectType.NONE)) {
                    indexingStartedTime = LocalDateTime.now();
                    currentIndexState = type;
                    objectIndexingStates.put(type, indexingStates.INDEXING_STARTED);
                    pollingChannel.send(INDEXING_STARTED_MESSAGE + currentIndexState);
                    indexerThread = new Thread((worker));
                    indexerThread.setDaemon(true);
                    indexerThread.start();
                    indexerThread.join();
                    break;
                } else {
                    logger.debug("Cannot start '" + type + "' indexing while a different indexing process running: '"
                            + currentIndexState + "'");
                    Thread.sleep(pause);
                    attempts++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the process of indexing batches to the ElasticSearch index.
     */
    public void startBatchIndexing() {
        startIndexing(ObjectType.BATCH, batchWorker);
    }

    /**
     * Starts the process of indexing dockets to the ElasticSearch index.
     */
    public void startDocketIndexing() {
        startIndexing(ObjectType.DOCKET, docketWorker);
    }

    /**
     * Starts the process of indexing processes to the ElasticSearch index.
     */
    public void startProcessIndexing() {
        startIndexing(ObjectType.PROCESS, processWorker);
    }

    /**
     * Starts the process of indexing projects to the ElasticSearch index.
     */
    public void startProjectIndexing() {
        startIndexing(ObjectType.PROJECT, projectWorker);
    }

    /**
     * Starts the process of indexing properties to the ElasticSearch index.
     */
    public void startPropertyIndexing() {
        startIndexing(ObjectType.PROPERTY, propertyWorker);
    }

    /**
     * Starts the process of indexing rulesets to the ElasticSearch index.
     */
    public void startRulesetIndexing() {
        startIndexing(ObjectType.RULESET, rulesetWorker);
    }

    /**
     * Starts the process of indexing tasks to the ElasticSearch index.
     */
    public void startTaskIndexing() {
        startIndexing(ObjectType.TASK, taskWorker);
    }

    /**
     * Starts the process of indexing templates to the ElasticSearch index.
     */
    public void startTemplateIndexing() {
        startIndexing(ObjectType.TEMPLATE, templateWorker);
    }

    /**
     * Starts the process of indexing users to the ElasticSearch index.
     */
    public void startUserIndexing() {
        startIndexing(ObjectType.USER, userWorker);
    }

    /**
     * Starts the process of indexing user groups to the ElasticSearch index.
     */
    public void startUserGroupIndexing() {
        startIndexing(ObjectType.USERGROUP, usergroupWorker);
    }

    /**
     * Starts the process of indexing workpieces to the ElasticSearch index.
     */
    public void startWorkpieceIndexing() {
        startIndexing(ObjectType.WORKPIECE, workpieceWorker);
    }

    /**
     * Starts the process of indexing filters to the ElasticSearch index.
     */
    public void startFilterIndexing() {
        startIndexing(ObjectType.FILTER, filterWorker);
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexing() {
        IndexAllThread indexAllThread = new IndexAllThread();
        indexAllThread.start();
    }

    /**
     * Return the overall progress in percent of the currently running indexing
     * process, incorporating the total number of indexed and all objects.
     *
     * @return the overall progress of the indexing process
     */
    public int getAllIndexingProgress() {
        return (int) ((getAllIndexed() / (float) getTotalCount()) * 100);
    }

    /**
     * Return whether any indexing process is currently in progress or not.
     *
     * @return boolean Value indicating whether any indexing process is currently in
     *         progress or not
     */
    public boolean indexingInProgress() {
        return (!Objects.equals(this.currentIndexState, ObjectType.NONE) || indexingAll);
    }

    /**
     * Create mapping which enables sorting and other aggregation functionalities.
     *
     * @param updatePollingChannel
     *            flag indicating whether the web socket channel to the frontend
     *            should be updated with the success status of the mapping creation
     *            or not.
     */
    public void createMapping(boolean updatePollingChannel) {
        if (updatePollingChannel) {
            pollingChannel.send(MAPPING_STARTED_MESSAGE);
        }
        try {
            String mappingStateMessage;
            if (readMapping().equals("")) {
                if (indexRestClient.createIndex()) {
                    currentState = indexStates.MAPPING_SUCCESS;
                    mappingStateMessage = MAPPING_FINISHED_MESSAGE;
                } else {
                    currentState = indexStates.MAPPING_ERROR;
                    mappingStateMessage = MAPPING_FAILED_MESSAGE;
                }
            } else {
                if (indexRestClient.createIndex(readMapping())) {
                    currentState = indexStates.MAPPING_SUCCESS;
                    mappingStateMessage = MAPPING_FINISHED_MESSAGE;
                } else {
                    currentState = indexStates.MAPPING_ERROR;
                    mappingStateMessage = MAPPING_FAILED_MESSAGE;
                }
            }
            if (updatePollingChannel) {
                pollingChannel.send(mappingStateMessage);
            }
        } catch (CustomResponseException | IOException | ParseException e) {
            currentState = indexStates.MAPPING_ERROR;
            if (updatePollingChannel) {
                pollingChannel.send(MAPPING_FAILED_MESSAGE);
            }
            logger.error(e);
        }
    }

    /**
     * Delete whole Elastic Search index.
     */
    public void deleteIndex(boolean updatePollingChannel) {
        if (updatePollingChannel) {
            pollingChannel.send(DELETION_STARTED_MESSAGE);
        }
        String updateMessage;
        try {
            indexRestClient.deleteIndex();
            resetGlobalProgress();
            currentState = indexStates.DELETE_SUCCESS;
            updateMessage = DELETION_FINISHED_MESSAGE;
        } catch (IOException e) {
            logger.error(e.getMessage());
            currentState = indexStates.DELETE_ERROR;
            updateMessage = DELETION_FAILED_MESSAGE;
        }
        if (updatePollingChannel) {
            pollingChannel.send(updateMessage);
        }
    }

    /**
     * Return server information provided by the searchService and gathered by the
     * rest client.
     *
     * @return String information about the server
     */
    public String getServerInformation() {
        return this.serviceManager.getBatchService().getServerInformation();
    }

    /**
     * Return the progress in percent of the currently running indexing process. If
     * the list of entries to be indexed is empty, this will return "0".
     *
     * @param numberOfObjects
     *            the number of existing objects of the given ObjectType in the
     *            database
     * @param currentType
     *            the ObjectType for which the progress will be determined
     * @param nrOfindexedObjects
     *            the number of objects of the given ObjectType that have already
     *            been indexed
     *
     * @return the progress of the current indexing process in percent
     */
    private int getProgress(long numberOfObjects, ObjectType currentType, long nrOfindexedObjects) {
        int progress = numberOfObjects > 0 ? (int) ((nrOfindexedObjects / (float) numberOfObjects) * 100) : 0;
        if (Objects.equals(currentIndexState, currentType)) {
            if (numberOfObjects == 0 || progress == 100) {
                lastIndexed.put(currentIndexState, LocalDateTime.now());
                currentIndexState = ObjectType.NONE;
                if (numberOfObjects == 0) {
                    objectIndexingStates.put(currentType, indexingStates.NO_STATE);
                } else {
                    objectIndexingStates.put(currentType, indexingStates.INDEXING_SUCCESSFUL);
                }
                indexerThread.interrupt();
                pollingChannel.send(INDEXING_FINISHED_MESSAGE + currentType + "!");
            }
        }
        return progress;
    }

    private void resetGlobalProgress() {
        indexedBatches = 0;
        indexedDockets = 0;
        indexedProcesses = 0;
        indexedProjects = 0;
        indexedProperties = 0;
        indexedRulesetes = 0;
        indexedTasks = 0;
        indexedTemplates = 0;
        indexedUsers = 0;
        indexedUsergroups = 0;
        indexedWorkpieces = 0;
        indexedFilter = 0;
    }

    private static String readMapping() throws ParseException {
        JSONParser parser = new JSONParser();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classloader.getResourceAsStream("mapping.json")) {
            String mapping = IOUtils.toString(inputStream, "UTF-8");
            Object object = parser.parse(mapping);
            JSONObject jsonObject = (JSONObject) object;
            return jsonObject.toJSONString();
        } catch (IOException e) {
            logger.error(e);
            return "";
        }
    }

    /**
     * Tests and returns whether the Elastic Search index has been created or not.
     *
     * @return whether the Elastic Search index exists or not
     */
    public boolean indexExists() {
        try {
            return indexRestClient.indexExists();
        } catch (IOException | CustomResponseException e) {
            return false;
        }
    }

    /**
     * Return the state of the ES index. -2 = failed deleting the index -1 = failed
     * creating ES mapping 1 = successfully created ES mapping 2 = successfully
     * deleted index
     *
     * @return state of ES index
     */
    public indexStates getIndexState() {
        return currentState;
    }

    /**
     *
     * @param objectType
     *
     * @return indexing state of the given object type
     */
    public indexingStates getObjectIndexState(ObjectType objectType) {
        return objectIndexingStates.get(objectType);
    }

    /**
     * Return static variable representing the 'indexing failed' state.
     *
     * @return 'indexing failed' state variable
     */
    public indexingStates getIndexingFailedState() {
        return indexingStates.INDEXING_FAILED;
    }

    /**
     * Return static variable representing the 'indexing successful' state.
     *
     * @return 'indexing successful' state variable
     */
    public indexingStates getIndexingSuccessfulState() {
        return indexingStates.INDEXING_SUCCESSFUL;
    }

    /**
     * Return static variable representing the 'indexing started' state.
     *
     * @return 'indexing started' state variable
     */
    public indexingStates getIndexingStartedState() {
        return indexingStates.INDEXING_STARTED;
    }

    /**
     * Return static variable representing the global state. - return 'indexing
     * failed' state if any object type is in 'indexing failed' state - return 'no
     * state' if any object type is in 'no state' state - return 'indexing
     * successful' state if all object types are in 'indexing successful' state
     *
     * @return static variable for global indexing state
     */
    public indexingStates getAllObjectsIndexingState() {
        for (ObjectType objectType : ObjectType.values()) {
            if (Objects.equals(objectIndexingStates.get(objectType), indexingStates.INDEXING_FAILED)) {
                return indexingStates.INDEXING_FAILED;
            }
            if (Objects.equals(objectIndexingStates.get(objectType), indexingStates.NO_STATE)) {
                return indexingStates.NO_STATE;
            }
        }
        return indexingStates.INDEXING_SUCCESSFUL;
    }

}
