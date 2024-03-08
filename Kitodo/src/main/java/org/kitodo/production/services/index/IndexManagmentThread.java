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

package org.kitodo.production.services.index;

import java.util.Objects;

import javax.faces.push.PushContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.IndexWorkerStatus;

public class IndexManagmentThread extends Thread {

    private static final Logger logger = LogManager.getLogger(IndexManagmentThread.class);

    private final PushContext context;
    private final IndexingService indexingService;
    private final boolean indexAllObjects;
    private final ObjectType objectType;

    /**
     * Initialize indexing managment thread.
     * @param pushContext the UI channel used to trigger refresh
     * @param service the service class for indexing
     * @param objectType optional objectType (if null, all types are indexed, otherwise only that one)
     * @param indexAllObjects whether all objects are indexed or only remaining ones
     */
    IndexManagmentThread(PushContext pushContext, IndexingService service, ObjectType objectType, boolean indexAllObjects) {
        context = pushContext;
        indexingService = service;
        this.indexAllObjects = indexAllObjects;
        this.objectType = objectType;
    }

    @Override
    public void run() {
        try {
            indexingService.setIndexingAll(true);

            for (ObjectType currentType : ObjectType.getIndexableObjectTypes()) {
                if (Objects.isNull(this.objectType) || currentType.equals(objectType)) {
                    try {
                        IndexWorkerStatus status = indexingService.runIndexing(currentType, context, indexAllObjects);
                        if (Objects.nonNull(status) && (status.hasFailed())) {
                            // stop indexing due to failure
                            break;
                        }
                    } catch (DataException | CustomResponseException | DAOException | RuntimeException e) {
                        logger.error(e);
                        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                    }
                }
            }
            try {
                sleep(IndexingService.PAUSE);
            } catch (InterruptedException e) {
                logger.trace("Index management sleep interrupted while waiting to finish indexing");
            }
        } finally {
            indexingService.resetCurrentIndexState();
            indexingService.setIndexingAll(false);
            context.send(IndexingService.INDEXING_FINISHED_MESSAGE);
        }
    }
}
