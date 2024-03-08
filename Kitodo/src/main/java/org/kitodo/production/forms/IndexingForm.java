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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonArray;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.enums.IndexStates;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.omnifaces.util.Ajax;

@Named
@ApplicationScoped
public class IndexingForm {

    private static final Logger logger = LogManager.getLogger(IndexingForm.class);
    private static final String POLLING_CHANNEL_NAME = "togglePollingChannel";
    private static final List<ObjectType> OBJECT_TYPES = ObjectType.getIndexableObjectTypes();

    @Inject
    @Push(channel = POLLING_CHANNEL_NAME)
    private PushContext pollingChannel;

    private String indexingStartedUser = "";
    private LocalDateTime indexingStartedTime = null;

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
        Exception e = new UnsupportedOperationException("currently not implemented");
        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
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
        return Collections.emptyMap();
    }

    /**
     * Return the total number of all objects that can be indexed.
     *
     * @return long number of all items that can be written to the index
     */
    public long getTotalCount() {
        return 0;
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
        return 0;
    }

    /**
     * Return the number of all objects processed during the current indexing
     * progress.
     *
     * @return long number of all currently indexed objects
     */
    public long getAllIndexed() {
        return 0;
    }

    /**
     * Index all objects of given type 'objectType'.
     *
     * @param type
     *            type objects that get indexed
     */
    public void callIndexing(ObjectType type) {
        Exception e = new UnsupportedOperationException("currently not implemented");
        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
    }

    /**
     * Index all objects of given type 'objectType'.
     *
     * @param type
     *            type objects that get indexed
     */
    public void callIndexingRemaining(ObjectType type) {
        Exception e = new UnsupportedOperationException("currently not implemented");
        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexing() {
        Exception e = new UnsupportedOperationException("currently not implemented");
        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexingRemaining() {
        Exception e = new UnsupportedOperationException("currently not implemented");
        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
    }

    /**
     * Return the overall progress in percent of the currently running indexing
     * process, incorporating the total number of indexed and all objects.
     *
     * @return the overall progress of the indexing process
     */
    public int getAllIndexingProgress() {
        return 0;
    }

    /**
     * Return whether any indexing process is currently in progress or not.
     *
     * @return boolean Value indicating whether any indexing process is currently in
     *         progress or not
     */
    public boolean indexingInProgress() {
        return false;
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
        Exception e = new UnsupportedOperationException("currently not implemented");
        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
    }

    /**
     * Delete whole ElasticSearch index.
     */
    public void deleteIndex() {
        Exception e = new UnsupportedOperationException("currently not implemented");
        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
    }

    /**
     * Return server information provided by the searchService and gathered by the
     * rest client.
     *
     * @return String information about the server
     */
    public String getServerInformation() {
        return "";
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
        return 0;
    }

    /**
     * Check if current mapping is empty.
     *
     * @return true if mapping is empty, otherwise false
     */
    public boolean isMappingEmpty() {
        return true;
    }

    /**
     * Tests and returns whether the Elastic Search index has been created or not.
     *
     * @return whether the Elastic Search index exists or not
     */
    public boolean indexExists() {
        return false;
    }

    /**
     * Return the state of the ES index. -2 = failed deleting the index -1 = failed
     * creating ES mapping 1 = successfully created ES mapping 2 = successfully
     * deleted index
     *
     * @return state of ES index
     */
    public IndexStates getIndexState() {
        return IndexStates.NO_STATE;
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
        return IndexStates.NO_STATE;
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
        return IndexStates.NO_STATE;
    }

    /**
     * Return the array of object type values defined in the ObjectType enum.
     *
     * @return array of object type values
     */
    public ObjectType[] getObjectTypes() {
        return OBJECT_TYPES.toArray(new ObjectType[0]);
    }

    /**
     * Return object types as JSONArray.
     *
     * @return JSONArray containing objects type constants.
     */
    public JsonArray getObjectTypesAsJson() {
        return Json.createArrayBuilder().build();
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
}
