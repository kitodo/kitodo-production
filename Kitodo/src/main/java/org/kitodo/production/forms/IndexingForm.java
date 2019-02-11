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

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.IndexRestClient;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.IndexWorker;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchService;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;
import org.omnifaces.util.Ajax;

@Named
@ApplicationScoped
public class IndexingForm {

    private static IndexRestClient indexRestClient = IndexRestClient.getInstance();
    private static List<ObjectType> objectTypes = ObjectType.getIndexableObjectTypes();

    private static final String MAPPING_STARTED_MESSAGE = "mapping_started";
    private static final String MAPPING_FINISHED_MESSAGE = "mapping_finished";
    private static final String MAPPING_FAILED_MESSAGE = "mapping_failed";

    private static final String DELETION_STARTED_MESSAGE = "deletion_started";
    private static final String DELETION_FINISHED_MESSAGE = "deletion_finished";
    private static final String DELETION_FAILED_MESSAGE = "deletion_failed";

    private static final String INDEXING_STARTED_MESSAGE = "indexing_started";
    private static final String INDEXING_FINISHED_MESSAGE = "indexing_finished";

    private static final String POLLING_CHANNEL_NAME = "togglePollingChannel";

    private final Map<ObjectType, Integer> countDatabaseObjects = new EnumMap<>(ObjectType.class);
    private int indexLimit = 20000;
    private int pause = 1000;

    @Inject
    @Push(channel = POLLING_CHANNEL_NAME)
    private PushContext pollingChannel;

    private static final Logger logger = LogManager.getLogger(IndexingForm.class);

    private ObjectType currentIndexState = ObjectType.NONE;

    private boolean indexingAll = false;

    private enum IndexStates {
        NO_STATE,
        DELETE_ERROR,
        DELETE_SUCCESS,
        MAPPING_ERROR,
        MAPPING_SUCCESS,
    }

    private enum IndexingStates {
        NO_STATE,
        INDEXING_STARTED,
        INDEXING_SUCCESSFUL,
        INDEXING_FAILED,
    }

    private IndexStates currentState = IndexStates.NO_STATE;

    private Map<ObjectType, LocalDateTime> lastIndexed = new EnumMap<>(ObjectType.class);

    private Map<ObjectType, Integer> indexedObjects = new EnumMap<>(ObjectType.class);

    private Map<ObjectType, IndexingStates> objectIndexingStates = new EnumMap<>(ObjectType.class);

    private Map<ObjectType, SearchService> searchServices = new EnumMap<>(ObjectType.class);

    private Map<ObjectType, List<IndexWorker>> indexWorkers = new EnumMap<>(ObjectType.class);

    private IndexWorker currentIndexWorker;

    private LocalDateTime indexingStartedTime = null;

    private Thread indexerThread = null;

    /**
     * Standard constructor.
     */
    IndexingForm() {
        for (ObjectType objectType : objectTypes) {
            searchServices.put(objectType, getService(objectType));
            objectIndexingStates.put(objectType, IndexingStates.NO_STATE);
        }

        indexRestClient.setIndex(ConfigMain.getParameter("elasticsearch.index", "kitodo"));

        prepareIndexWorker();

        countDatabaseObjects();

        if (indexExists()) {
            for (ObjectType objectType : objectTypes) {
                indexedObjects.put(objectType, countDatabaseObjects.get(objectType));
            }
        }
    }

    /**
     * Count database objects. Execute it on application start and next on button
     * click.
     */
    public void countDatabaseObjects() {
        for (ObjectType objectType : objectTypes) {
            countDatabaseObjects.put(objectType, getNumberOfDatabaseObjects(objectType));
        }
    }

    /**
     * Get time when indexing has started.
     * 
     * @return time when indexing has started as LocalDateTime
     */
    public LocalDateTime getIndexingStartedTime() {
        return indexingStartedTime;
    }

    /**
     * Get count of database objects.
     *
     * @return value of countDatabaseObjects
     */
    public Map<ObjectType, Integer> getCountDatabaseObjects() {
        return countDatabaseObjects;
    }

    /**
     * Return the total number of all objects that can be indexed.
     *
     * @return long number of all items that can be written to the index
     */
    public long getTotalCount() {
        int totalCount = 0;
        for (ObjectType objectType : objectTypes) {
            totalCount += countDatabaseObjects.get(objectType);
        }
        return totalCount;
    }

    /**
     * Return the number of indexed objects for the given ObjectType.
     *
     * @param objectType
     *            ObjectType for which the number of indexed objects is returned
     *
     * @return number of indexed objects
     */
    public int getNumberOfIndexedObjects(ObjectType objectType) {
        if (currentIndexState == objectType) {
            List<IndexWorker> indexWorkerList = indexWorkers.get(objectType);
            if (indexWorkerList.size() > 1) {
                if (indexWorkerList.contains(currentIndexWorker)) {
                    indexedObjects.put(objectType, currentIndexWorker.getIndexedObjects());
                } else {
                    indexedObjects.put(objectType, indexWorkers.get(objectType).get(0).getIndexedObjects());
                }
            } else {
                indexedObjects.put(objectType, indexWorkers.get(objectType).get(0).getIndexedObjects());
            }
        } else if (!indexedObjects.containsKey(objectType)) {
            updateCount(objectType);
        }
        return indexedObjects.get(objectType);
    }

    /**
     * Return the number of all objects processed during the current indexing
     * progress.
     *
     * @return int number of all currently indexed objects
     */
    public int getAllIndexed() {
        int allIndexed = 0;
        for (ObjectType objectType : objectTypes) {
            allIndexed += getNumberOfIndexedObjects(objectType);
        }
        return allIndexed;
    }

    /**
     * Index all objects of given type 'objectType'.
     *
     * @param type
     *            type objects that get indexed
     */
    public void startIndexing(ObjectType type) {
        if (countDatabaseObjects.get(type) > 0) {
            List<IndexWorker> indexWorkerList = indexWorkers.get(type);
            for (IndexWorker worker : indexWorkerList) {
                currentIndexWorker = worker;
                runIndexing(currentIndexWorker, type);
            }
        }
    }

    /**
     * Index all objects of given type 'objectType'.
     *
     * @param type
     *            type objects that get indexed
     */
    public void startIndexingRemaining(ObjectType type) {
        if (countDatabaseObjects.get(type) > 0) {
            List<IndexWorker> indexWorkerList = indexWorkers.get(type);
            for (IndexWorker worker : indexWorkerList) {
                worker.setIndexAllObjects(false);
                currentIndexWorker = worker;
                runIndexing(currentIndexWorker, type);
            }
        }
    }

    private void runIndexing(IndexWorker worker, ObjectType type) {
        currentState = IndexStates.NO_STATE;
        int attempts = 0;
        while (attempts < 10) {
            try {
                if (Objects.equals(currentIndexState, ObjectType.NONE) || Objects.equals(currentIndexState, type)) {
                    if (Objects.equals(currentIndexState, ObjectType.NONE)) {
                        indexingStartedTime = LocalDateTime.now();
                        currentIndexState = type;
                        objectIndexingStates.put(type, IndexingStates.INDEXING_STARTED);
                        pollingChannel.send(INDEXING_STARTED_MESSAGE + currentIndexState);
                    }
                    indexerThread = new Thread(worker);
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
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexing() {
        IndexAllThread indexAllThread = new IndexAllThread();
        indexAllThread.start();
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexingRemaining() {
        for (Map.Entry<ObjectType, List<IndexWorker>> workerEntry : indexWorkers.entrySet()) {
            List<IndexWorker> indexWorkerList = workerEntry.getValue();
            for (IndexWorker worker : indexWorkerList) {
                worker.setIndexAllObjects(false);
            }
        }
        startAllIndexing();
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
            String mapping = readMapping();
            String mappingStateMessage;
            if (mapping.equals("")) {
                if (indexRestClient.createIndex()) {
                    currentState = IndexStates.MAPPING_SUCCESS;
                    mappingStateMessage = MAPPING_FINISHED_MESSAGE;
                } else {
                    currentState = IndexStates.MAPPING_ERROR;
                    mappingStateMessage = MAPPING_FAILED_MESSAGE;
                }
            } else {
                if (indexRestClient.createIndex(mapping)) {
                    if (isMappingValid(mapping)) {
                        currentState = IndexStates.MAPPING_SUCCESS;
                        mappingStateMessage = MAPPING_FINISHED_MESSAGE;
                    } else {
                        currentState = IndexStates.MAPPING_ERROR;
                        mappingStateMessage = MAPPING_FAILED_MESSAGE;
                    }
                } else {
                    currentState = IndexStates.MAPPING_ERROR;
                    mappingStateMessage = MAPPING_FAILED_MESSAGE;
                }
            }
            if (updatePollingChannel) {
                pollingChannel.send(mappingStateMessage);
            }
        } catch (CustomResponseException | IOException e) {
            currentState = IndexStates.MAPPING_ERROR;
            if (updatePollingChannel) {
                pollingChannel.send(MAPPING_FAILED_MESSAGE);
            }
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
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
            currentState = IndexStates.DELETE_SUCCESS;
            updateMessage = DELETION_FINISHED_MESSAGE;
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            currentState = IndexStates.DELETE_ERROR;
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
        try {
            return indexRestClient.getServerInformation();
        } catch (IOException e) {
            Helper.setErrorMessage("elasticSearchNotRunning", logger, e);
            return "";
        }
    }

    /**
     * Return the progress in percent of the currently running indexing process. If
     * the list of entries to be indexed is empty, this will return "0".
     *
     * @param currentType
     *            the ObjectType for which the progress will be determined
     * @return the progress of the current indexing process in percent
     */
    public int getProgress(ObjectType currentType) {
        long numberOfObjects = countDatabaseObjects.get(currentType);
        long nrOfIndexedObjects = getNumberOfIndexedObjects(currentType);
        int progress = numberOfObjects > 0 ? (int) ((nrOfIndexedObjects / (float) numberOfObjects) * 100) : 0;
        if (Objects.equals(currentIndexState, currentType) && (numberOfObjects == 0 || progress == 100)) {
            lastIndexed.put(currentIndexState, LocalDateTime.now());
            currentIndexState = ObjectType.NONE;
            if (numberOfObjects == 0) {
                objectIndexingStates.put(currentType, IndexingStates.NO_STATE);
            } else {
                objectIndexingStates.put(currentType, IndexingStates.INDEXING_SUCCESSFUL);
            }
            indexerThread.interrupt();
            pollingChannel.send(INDEXING_FINISHED_MESSAGE + currentType + "!");
        }
        return progress;
    }

    private void resetGlobalProgress() {
        for (ObjectType objectType : objectTypes) {
            indexedObjects.put(objectType, 0);
        }
    }

    private static String readMapping() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classloader.getResourceAsStream("mapping.json")) {
            String mapping = IOUtils.toString(inputStream, "UTF-8");
            try (JsonReader jsonReader = Json.createReader(new StringReader(mapping))) {
                JsonObject jsonObject = jsonReader.readObject();
                return jsonObject.toString();
            }
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return "";
        }
    }

    /**
     * Check if current mapping is empty.
     * 
     * @return true if mapping is empty, otherwise false
     */
    public boolean isMappingEmpty() {
        String emptyMapping = "{\n\"mappings\": {\n\n    }\n}";
        return isMappingEqualTo(emptyMapping);
    }

    private boolean isMappingValid(String mapping) {
        return isMappingEqualTo(mapping);
    }

    private boolean isMappingEqualTo(String mapping) {
        try (JsonReader mappingExpectedReader = Json.createReader(new StringReader(mapping));
                JsonReader mappingCurrentReader = Json.createReader(new StringReader(indexRestClient.getMapping()))) {
            JsonObject mappingExpected = mappingExpectedReader.readObject();
            JsonObject mappingCurrent = mappingCurrentReader.readObject().getJsonObject(indexRestClient.getIndex());
            return mappingExpected.equals(mappingCurrent);
        } catch (IOException e) {
            return false;
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
        } catch (IOException | CustomResponseException ignored) {
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
    public IndexStates getIndexState() {
        return currentState;
    }

    /**
     * Return the index state of the given objectType.
     *
     * @param objectType
     *            the objectType for which the IndexState should be returned
     *
     * @return indexing state of the given object type.
     */
    public IndexingStates getObjectIndexState(ObjectType objectType) {
        return objectIndexingStates.get(objectType);
    }

    /**
     * Return static variable representing the 'indexing failed' state.
     *
     * @return 'indexing failed' state variable
     */
    public IndexingStates getIndexingFailedState() {
        return IndexingStates.INDEXING_FAILED;
    }

    /**
     * Return static variable representing the 'indexing successful' state.
     *
     * @return 'indexing successful' state variable
     */
    public IndexingStates getIndexingSuccessfulState() {
        return IndexingStates.INDEXING_SUCCESSFUL;
    }

    /**
     * Return static variable representing the 'indexing started' state.
     *
     * @return 'indexing started' state variable
     */
    public IndexingStates getIndexingStartedState() {
        return IndexingStates.INDEXING_STARTED;
    }

    /**
     * Return static variable representing the global state. - return 'indexing
     * failed' state if any object type is in 'indexing failed' state - return 'no
     * state' if any object type is in 'no state' state - return 'indexing
     * successful' state if all object types are in 'indexing successful' state
     *
     * @return static variable for global indexing state
     */
    public IndexingStates getAllObjectsIndexingState() {
        for (ObjectType objectType : objectTypes) {
            if (Objects.equals(objectIndexingStates.get(objectType), IndexingStates.INDEXING_FAILED)) {
                return IndexingStates.INDEXING_FAILED;
            }
            if (Objects.equals(objectIndexingStates.get(objectType), IndexingStates.NO_STATE)) {
                return IndexingStates.NO_STATE;
            }
        }
        return IndexingStates.INDEXING_SUCCESSFUL;
    }

    /**
     * Return the array of object type values defined in the ObjectType enum.
     *
     * @return array of object type values
     */
    public ObjectType[] getObjectTypes() {
        return objectTypes.toArray(new ObjectType[0]);
    }

    /**
     * Return object types as JSONArray.
     *
     * @return JSONArray containing objects type constants.
     */
    @SuppressWarnings("unused")
    public JsonArray getObjectTypesAsJson() {
        JsonArrayBuilder objectsTypesJson = Json.createArrayBuilder();
        for (ObjectType objectType : objectTypes) {
            objectsTypesJson.add(objectType.toString());
        }
        return objectsTypesJson.build();
    }

    private SearchService getService(ObjectType objectType) {
        if (!searchServices.containsKey(objectType) || Objects.isNull(searchServices.get(objectType))) {
            switch (objectType) {
                case BATCH:
                    searchServices.put(objectType, ServiceManager.getBatchService());
                    break;
                case DOCKET:
                    searchServices.put(objectType, ServiceManager.getDocketService());
                    break;
                case PROCESS:
                    searchServices.put(objectType, ServiceManager.getProcessService());
                    break;
                case PROJECT:
                    searchServices.put(objectType, ServiceManager.getProjectService());
                    break;
                case PROPERTY:
                    searchServices.put(objectType, ServiceManager.getPropertyService());
                    break;
                case RULESET:
                    searchServices.put(objectType, ServiceManager.getRulesetService());
                    break;
                case TASK:
                    searchServices.put(objectType, ServiceManager.getTaskService());
                    break;
                case TEMPLATE:
                    searchServices.put(objectType, ServiceManager.getTemplateService());
                    break;
                case WORKFLOW:
                    searchServices.put(objectType, ServiceManager.getWorkflowService());
                    break;
                case FILTER:
                    searchServices.put(objectType, ServiceManager.getFilterService());
                    break;
                default:
                    return null;
            }
        }
        return searchServices.get(objectType);
    }

    /**
     * Return NONE object type.
     *
     * @return ObjectType NONE object type
     */
    public ObjectType getNoneType() {
        return ObjectType.NONE;
    }

    /**
     * Update the view.
     */
    public void updateView() {
        for (ObjectType objectType : objectTypes) {
            updateCount(objectType);
        }
        Ajax.update("@all");
    }

    private void updateCount(ObjectType objectType) {
        SearchService searchService = getService(objectType);
        if (Objects.nonNull(searchService)) {
            try {
                indexedObjects.put(objectType, toIntExact(searchService.count()));
            } catch (DataException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
    }

    private void prepareIndexWorker() {
        for (ObjectType objectType : ObjectType.values()) {
            List<IndexWorker> indexWorkerList = new ArrayList<>();

            int databaseObjectsSize = getNumberOfDatabaseObjects(objectType);
            if (databaseObjectsSize > indexLimit) {
                int start = 0;

                while (start < databaseObjectsSize) {
                    indexWorkerList.add(new IndexWorker(searchServices.get(objectType), start));
                    start += indexLimit;
                }
            } else {
                indexWorkerList.add(new IndexWorker(searchServices.get(objectType)));
            }

            indexWorkers.put(objectType, indexWorkerList);
        }
    }

    /**
     * Return the number of objects in the database for the given ObjectType.
     *
     * @param objectType
     *            name of ObjectType for which the number of database objects is
     *            returned
     * @return number of database objects
     */
    private int getNumberOfDatabaseObjects(ObjectType objectType) {
        try {
            SearchService searchService = searchServices.get(objectType);
            if (Objects.nonNull(searchService)) {
                return toIntExact(searchService.countDatabaseRows());
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return 0;
    }

    class IndexAllThread extends Thread {

        @Override
        public void run() {
            resetGlobalProgress();
            indexingAll = true;

            for (ObjectType objectType : objectTypes) {
                startIndexing(objectType);
            }

            try {
                sleep(pause);
            } catch (InterruptedException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                Thread.currentThread().interrupt();
            }

            currentIndexState = ObjectType.NONE;
            indexingAll = false;

            pollingChannel.send(INDEXING_FINISHED_MESSAGE);
        }
    }
}
