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

package org.kitodo.production.helper;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.production.services.data.base.SearchService;

public class IndexWorker implements Runnable {

    private int indexedObjects = 0;
    private int startIndexing;
    private int indexLimit = 5000;
    private boolean indexAllObjects = true;
    private SearchService searchService;
    private static final Logger logger = LogManager.getLogger(IndexWorker.class);

    /**
     * Constructor initializing an IndexWorker object with the given SearchService
     * and list of objects that will be indexed.
     *
     * @param searchService
     *            SearchService instance used for indexing
     */
    public IndexWorker(SearchService searchService) {
        this.searchService = searchService;
        this.startIndexing = 0;
    }

    /**
     * Constructor initializing an IndexWorker object with the given SearchService
     * and list of objects that will be indexed.
     *
     * @param searchService
     *            SearchService instance used for indexing
     */
    public IndexWorker(SearchService searchService, int startIndexing) {
        this.searchService = searchService;
        this.startIndexing = startIndexing;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        this.indexedObjects = 0;
        int batchSize = ConfigCore.getIntParameterOrDefaultValue(ParameterCore.ELASTICSEARCH_BATCH);
        try {
            int amountToIndex = getAmountToIndex();

            if (amountToIndex < batchSize) {
                if (indexAllObjects) {
                    indexObjects(searchService.getAll());
                } else {
                    indexObjects(searchService.getAllNotIndexed());
                }
            } else {
                if (amountToIndex > this.indexLimit) {
                    amountToIndex = this.indexLimit;
                }
                while (this.indexedObjects < amountToIndex) {
                    indexChunks(batchSize);
                }
            }
        } catch (CustomResponseException | DAOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private int getAmountToIndex() throws DAOException {
        if (indexAllObjects) {
            return searchService.countDatabaseRows().intValue() - this.startIndexing;
        } else {
            return searchService.countNotIndexedDatabaseRows().intValue() - this.startIndexing;
        }
    }

    @SuppressWarnings("unchecked")
    private void indexChunks(int batchSize) throws CustomResponseException, DAOException {
        List<Object> objectsToIndex;
        while (this.indexedObjects < this.indexLimit) {
            int offset = this.indexedObjects + this.startIndexing;

            if (indexAllObjects) {
                objectsToIndex = searchService.getAll(offset, batchSize);
            } else {
                objectsToIndex = searchService.getAllNotIndexed(offset, batchSize);
            }
            if (objectsToIndex.isEmpty()) {
                break;
            }

            indexObjects(objectsToIndex);
        }
    }

    @SuppressWarnings("unchecked")
    private void indexObjects(List<Object> objectsToIndex) throws CustomResponseException, DAOException {
        this.searchService.addAllObjectsToIndex(objectsToIndex);
        this.indexedObjects = this.indexedObjects + objectsToIndex.size();
    }

    /**
     * Return the number of objects that have already been indexed during the
     * current indexing process.
     *
     * @return int the number of objects indexed during the current indexing run
     */
    public int getIndexedObjects() {
        return indexedObjects + startIndexing;
    }

    /**
     * Set value for indexAllObjects. If true, it indexes all objects, if false it
     * indexes only objects with flag IndexAction.INDEX.
     *
     * @param indexAllObjects
     *            as boolean
     */
    public void setIndexAllObjects(boolean indexAllObjects) {
        this.indexAllObjects = indexAllObjects;
    }
}
