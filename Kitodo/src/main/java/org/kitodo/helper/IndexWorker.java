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

package org.kitodo.helper;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.HibernateUtil;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.data.base.SearchService;

public class IndexWorker implements Runnable {

    private int indexedObjects = 0;
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
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        this.indexedObjects = 0;
        int batchSize = ConfigCore.getIntParameterOrDefaultValue(ParameterCore.ELASTICSEARCH_BATCH);
        try {
            int amountToIndex;
            if (indexAllObjects) {
                amountToIndex = searchService.countDatabaseRows().intValue();
            } else {
                amountToIndex = searchService.countNotIndexedDatabaseRows().intValue();
            }
            if (amountToIndex < batchSize) {
                if (indexAllObjects) {
                    indexObjects(searchService.getAll());
                } else {
                    indexObjects(searchService.getAllNotIndexed());
                }
            } else {
                while (this.indexedObjects < amountToIndex) {
                    indexChunks(batchSize);
                }
            }
        } catch (CustomResponseException | DAOException | IOException e) {
            logger.error(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void indexChunks(int batchSize) throws CustomResponseException, DAOException, IOException {
        Session session = HibernateUtil.getSession();
        List<Object> objectsToIndex;
        if (indexAllObjects) {
            objectsToIndex = searchService.getAll(this.indexedObjects, batchSize);
        } else {
            objectsToIndex = searchService.getAllNotIndexed(this.indexedObjects, batchSize);
        }
        indexObjects(objectsToIndex);
        session.clear();
    }

    @SuppressWarnings("unchecked")
    private void indexObjects(List<Object> objectsToIndex) throws CustomResponseException, DAOException, IOException {
        for (Object object : objectsToIndex) {
            this.searchService.saveToIndexAndUpdateIndexFlag((BaseIndexedBean) object, false);
            this.indexedObjects++;
        }
    }

    /**
     * Return the number of objects that have already been indexed during the
     * current indexing process.
     *
     * @return int the number of objects indexed during the current indexing run
     */
    public int getIndexedObjects() {
        return indexedObjects;
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
