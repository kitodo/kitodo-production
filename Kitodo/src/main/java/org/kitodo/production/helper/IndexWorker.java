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

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.services.data.base.SearchService;

public class IndexWorker implements Runnable {

    private static final Logger logger = LogManager.getLogger(IndexWorker.class);
    
    private final boolean indexAllObjects;
    private final ObjectType objectType;
    private final SearchService searchService;
    private final IndexWorkerStatus indexWorkerStatus;

    /**
     * Constructor initializing an IndexWorker object with the given SearchService
     * and list of objects that will be indexed.
     *
     * @param searchService
     *            SearchService instance used for indexing
     */
    public IndexWorker(SearchService searchService, ObjectType objectType, IndexWorkerStatus indexWorkerStatus, boolean indexAllObjects) {
        this.searchService = searchService;
        this.indexWorkerStatus = indexWorkerStatus;
        this.indexAllObjects = indexAllObjects;
        this.objectType = objectType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        int maxAttempts = ConfigCore.getIntParameterOrDefaultValue(ParameterCore.ELASTICSEARCH_ATTEMPTS);
        int batchSize = ConfigCore.getIntParameterOrDefaultValue(ParameterCore.ELASTICSEARCH_BATCH);
        int timeBetweenAttempts = ConfigCore.getIntParameterOrDefaultValue(ParameterCore.ELASTICSEARCH_TIME_BETWEEN_ATTEMPTS);
        int maxBatch = indexWorkerStatus.getMaxBatch();

        int nextBatch = indexWorkerStatus.getAndIncrementNextBatch();
        while (!indexWorkerStatus.hasFailed() && nextBatch < maxBatch) {
            // nextBatch is a valid batch that needs to be processed

            int attempt = 1;
            while (attempt < maxAttempts) {
                try {
                    int offset = nextBatch * batchSize;
                    logger.info("index " + objectType.toString() + " with offset " + offset + " and attempt " 
                        + attempt + "/" + maxAttempts);

                    if (indexAllObjects) {
                        indexObjects(searchService.getAll(offset, batchSize));
                    } else {
                        indexObjects(searchService.getAllNotIndexed(offset, batchSize));
                    }

                    break;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    attempt += 1;
                    try {
                        Thread.sleep(timeBetweenAttempts);
                    } catch (InterruptedException e2) {
                        logger.trace("Index worker sleep is interrupted while waiting for next indexing attempt");
                    }
                }
            }

            if (attempt >= maxAttempts) {
                logger.error("stop indexing after maximum amount of attempts");
                this.indexWorkerStatus.markAsFailed();
            } else {
                // find next batch that can be indexed
                nextBatch = indexWorkerStatus.getAndIncrementNextBatch();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void indexObjects(List<Object> objectsToIndex) throws CustomResponseException, DAOException, IOException {
        this.searchService.addAllObjectsToIndex(objectsToIndex);
    }
}
