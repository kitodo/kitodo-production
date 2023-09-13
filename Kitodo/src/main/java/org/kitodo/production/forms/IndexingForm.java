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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.IndexStates;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.index.IndexingService;
import org.omnifaces.util.Ajax;

@Named
@ApplicationScoped
public class IndexingForm {

    private static final List<ObjectType> objectTypes = ObjectType.getIndexableObjectTypes();
    private static final Logger logger = LogManager.getLogger(IndexingForm.class);
    private static final String POLLING_CHANNEL_NAME = "togglePollingChannel";
    private String indexingStartedUser = "";
    private LocalDateTime indexingStartedTime = null;

    @Inject
    @Push(channel = POLLING_CHANNEL_NAME)
    private PushContext pollingChannel;

    /**
     * Get user which started indexing.
     *
     * @return user which started indexing
     */
    public String getIndexingStartedUser() {
        return indexingStartedUser;
    }

    /**
     * Count database objects. Execute it on application start and next on button
     * click.
     */
    public void countDatabaseObjects() {
        try {
            ServiceManager.getIndexingService().countDatabaseObjects();
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
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
        return ServiceManager.getIndexingService().getCountDatabaseObjects();
    }

    /**
     * Return the total number of all objects that can be indexed.
     *
     * @return long number of all items that can be written to the index
     */
    public long getTotalCount() {
        return ServiceManager.getIndexingService().getTotalCount();
    }

    /**
     * Return the number of indexed objects for the given ObjectType.
     *
     * @param objectType
     *            ObjectType for which the number of indexed objects is returned
     *
     * @return number of indexed objects
     */
    public long getNumberOfIndexedObjects(ObjectType objectType) {
        try {
            return ServiceManager.getIndexingService().getNumberOfIndexedObjects(objectType);
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return 0;
        }
    }

    /**
     * Return the number of all objects processed during the current indexing
     * progress.
     *
     * @return long number of all currently indexed objects
     */
    public long getAllIndexed() {
        try {
            return ServiceManager.getIndexingService().getAllIndexed();
        } catch (DataException | ArithmeticException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return 0;
        }
    }

    /**
     * Index all objects of given type 'objectType'.
     *
     * @param type
     *            type objects that get indexed
     */
    public void callIndexing(ObjectType type) {
        indexingStartedTime = LocalDateTime.now();
        indexingStartedUser = ServiceManager.getUserService().getAuthenticatedUser().getFullName();
        try {
            ServiceManager.getIndexingService().startIndexing(pollingChannel, type);
        } catch (IllegalStateException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Index all objects of given type 'objectType'.
     *
     * @param type
     *            type objects that get indexed
     */
    public void callIndexingRemaining(ObjectType type) {
        indexingStartedTime = LocalDateTime.now();
        indexingStartedUser = ServiceManager.getUserService().getAuthenticatedUser().getFullName();
        try {
            ServiceManager.getIndexingService().startIndexingRemaining(pollingChannel, type);
        } catch (IllegalStateException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexing() {
        indexingStartedTime = LocalDateTime.now();
        indexingStartedUser = ServiceManager.getUserService().getAuthenticatedUser().getFullName();
        ServiceManager.getIndexingService().startAllIndexing(pollingChannel);
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexingRemaining() {
        ServiceManager.getIndexingService().startAllIndexingRemaining(pollingChannel);
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
        return ServiceManager.getIndexingService().indexingInProgress();
    }

    /**
     * Create mapping which enables sorting and other aggregation functions.
     *
     * @param updatePollingChannel
     *            flag indicating whether the web socket channel to the frontend
     *            should be updated with the success status of the mapping creation
     *            or not.
     */
    public void createMapping(boolean updatePollingChannel) {
        try {
            if (updatePollingChannel) {
                pollingChannel.send(IndexingService.MAPPING_STARTED_MESSAGE);
            }
            String mappingStateMessage = ServiceManager.getIndexingService().createMapping();
            if (updatePollingChannel) {
                pollingChannel.send(mappingStateMessage);
            }
        } catch (IOException | CustomResponseException e) {
            ServiceManager.getIndexingService().setIndexState(IndexStates.CREATING_MAPPING_FAILED);
            if (updatePollingChannel) {
                pollingChannel.send(IndexingService.MAPPING_FAILED_MESSAGE);
            }
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Delete whole ElasticSearch index.
     */
    public void deleteIndex() {
        pollingChannel.send(IndexingService.DELETION_STARTED_MESSAGE);
        String updateMessage = ServiceManager.getIndexingService().deleteIndex();
        pollingChannel.send(updateMessage);
    }

    /**
     * Return server information provided by the searchService and gathered by the
     * rest client.
     *
     * @return String information about the server
     */
    public String getServerInformation() {
        try {
            return ServiceManager.getIndexingService().getServerInformation();
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
        try {
            return ServiceManager.getIndexingService().getProgress(currentType, pollingChannel);
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return 0;
        }
    }

    /**
     * Check if current mapping is empty.
     *
     * @return true if mapping is empty, otherwise false
     */
    public boolean isMappingEmpty() {
        return ServiceManager.getIndexingService().isMappingEmpty();
    }

    /**
     * Tests and returns whether the Elastic Search index has been created or not.
     *
     * @return whether the Elastic Search index exists or not
     */
    public boolean indexExists() {
        try {
            return ServiceManager.getIndexingService().indexExists();
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
        return ServiceManager.getIndexingService().getIndexState();
    }

    /**
     * Return the index state of the given objectType.
     *
     * @param objectType
     *            the objectType for which the IndexState should be returned
     *
     * @return indexing state of the given object type.
     */
    public IndexStates getObjectIndexState(ObjectType objectType) {
        return ServiceManager.getIndexingService().getObjectIndexState(objectType);
    }

    /**
     * Return static variable representing the global state. - return 'indexing
     * failed' state if any object type is in 'indexing failed' state - return 'no
     * state' if any object type is in 'no state' state - return 'indexing
     * successful' state if all object types are in 'indexing successful' state
     *
     * @return static variable for global indexing state
     */
    public IndexStates getAllObjectsIndexingState() {
        return ServiceManager.getIndexingService().getAllObjectsIndexingState();
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
        Ajax.update("@all");
    }

    /**
     * Cancel indexing upon user request.
     */
    public void cancelIndexing() {
        ServiceManager.getIndexingService().cancelIndexing();
    }
}
