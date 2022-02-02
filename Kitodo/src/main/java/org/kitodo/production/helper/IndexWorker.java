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
import org.hibernate.HibernateException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.data.base.SearchService;
import org.kitodo.utils.Stopwatch;

public class IndexWorker implements Runnable {

    private int indexedObjects = 0;
    private int startIndexing;
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void run() {
        this.indexedObjects = 0;
        int batchSize = ConfigCore.getIntParameterOrDefaultValue(ParameterCore.ELASTICSEARCH_BATCH);
        int indexLimit = ConfigCore.getIntParameterOrDefaultValue(ParameterCore.ELASTICSEARCH_INDEXLIMIT);
        try {
            int amountToIndex = getAmountToIndex();

            if (amountToIndex < batchSize) {
                if (indexAllObjects) {
                    Stopwatch getAllStopwatch = new Stopwatch("Reading from database, mode \"all\",");
                    List all = searchService.getAll(this.startIndexing, amountToIndex);
                    getAllStopwatch.log(all, BaseBean::getId, logger);
                    indexObjects(all);
                } else {
                    Stopwatch getNotIndexedStopwatch = new Stopwatch("Reading from database, mode \"not indexed\",");
                    List allNotIndexed = searchService.getAllNotIndexed(this.startIndexing, amountToIndex);
                    getNotIndexedStopwatch.log(allNotIndexed, BaseBean::getId, logger);
                    indexObjects(allNotIndexed);
                }
            } else {
                if (amountToIndex > indexLimit) {
                    amountToIndex = indexLimit;
                }
                while (this.indexedObjects < amountToIndex) {
                    indexChunks(batchSize);
                }
            }
        } catch (CustomResponseException | DAOException | DataException | HibernateException e) {
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void indexChunks(int batchSize) throws CustomResponseException, DAOException, DataException {
        List objectsToIndex;
        int indexLimit = ConfigCore.getIntParameterOrDefaultValue(ParameterCore.ELASTICSEARCH_INDEXLIMIT);
        while (this.indexedObjects < indexLimit) {
            int offset = this.indexedObjects + this.startIndexing;

            if (indexAllObjects) {
                Stopwatch getAllStopwatch = new Stopwatch("Reading chunk from database, mode \"all\",");
                objectsToIndex = searchService.getAll(offset, batchSize);
                getAllStopwatch.log(objectsToIndex, BaseBean::getId, logger);
            } else {
            	Stopwatch getNotIndexedStopwatch = new Stopwatch("Reading chunk from database, mode \"not indexed\",");
                objectsToIndex = searchService.getAllNotIndexed(offset, batchSize);
                getNotIndexedStopwatch.log(objectsToIndex, BaseBean::getId, logger);
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
