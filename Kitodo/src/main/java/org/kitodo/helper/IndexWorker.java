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

import de.sub.goobi.config.ConfigCore;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.kitodo.config.DefaultValues;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.HibernateUtil;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.data.base.SearchService;

public class IndexWorker implements Runnable {

    private int indexedObjects = 0;
    private SearchService searchService;
    private static final Logger logger = LogManager.getLogger(IndexWorker.class);

    /**
     * Constructor initializing an IndexWorker object with the given
     * SearchService and list of objects that will be indexed.
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
        int batchSize = ConfigCore.getIntParameter(Parameters.ELASTICSEARCH_BATCH,
            DefaultValues.ELASTICSEARCH_BATCH);
        try {
            int amountToIndex = searchService.countDatabaseRows().intValue();
            if (amountToIndex < batchSize) {
                indexObjects(searchService.getAll());
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
        List<Object> objectsToIndex = searchService.getAll(this.indexedObjects, batchSize);
        indexObjects(objectsToIndex);
        session.clear();
    }

    @SuppressWarnings("unchecked")
    private void indexObjects(List<Object> objectsToIndex) throws CustomResponseException, IOException {
        for (Object object : objectsToIndex) {
            this.searchService.saveToIndex((BaseIndexedBean) object, false);
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
}
