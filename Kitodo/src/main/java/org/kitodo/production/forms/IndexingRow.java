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

import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.production.enums.IndexStates;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.index.IndexingService;

public class IndexingRow implements MassIndexingMonitor {

    private final IndexingService indexingService = ServiceManager.getIndexingService();

    private final Class<? extends BaseBean> type;

    IndexStates objectIndexState = IndexStates.NO_STATE;
    private AtomicLong entitiesLoaded = new AtomicLong();
    private AtomicLong documentsBuilt = new AtomicLong();
    private AtomicLong documentsAdded = new AtomicLong();
    private AtomicLong totalCount = new AtomicLong();

    IndexingRow(Class<? extends BaseBean> type) {
        this.type = type;
    }

    void callIndexing() {
        objectIndexState = IndexStates.INDEXING_STARTED;
        entitiesLoaded.set(0);
        documentsBuilt.set(0);
        documentsAdded.set(0);
        totalCount.set(0);
        indexingService.startIndexing(type, this);
    }

    long getCount() {
        return Math.max(Math.max(documentsAdded.get(), documentsBuilt.get()),
            Math.max(entitiesLoaded.get(), totalCount.get()));
    }

    long getIndexed() {
        return Math.min(Math.min(documentsAdded.get(), documentsBuilt.get()),
            Math.min(entitiesLoaded.get(), totalCount.get()));
    }

    String getNumberOfObjects() {
        if (IndexStates.NO_STATE.equals(objectIndexState)) {
            return Long.toString(totalCount.get());
        } else {
            StringBuilder numberOfObjects = new StringBuilder(40);
            numberOfObjects.append(entitiesLoaded.get());
            numberOfObjects.append(" \u2012 ");
            numberOfObjects.append(documentsBuilt.get());
            numberOfObjects.append(" \u2012 ");
            numberOfObjects.append(documentsAdded.get());
            numberOfObjects.append(" / ");
            numberOfObjects.append(totalCount.get());
            return numberOfObjects.toString();
        }
    }

    IndexStates getObjectIndexState() {
        return objectIndexState;
    }

    int getProgress() {
        long count = getCount();
        if (count == 0) {
            return 0;
        }
        return (int) (100 * getIndexed() / count);
    }

    boolean isIndexingInProgress() {
        return IndexStates.INDEXING_STARTED.equals(objectIndexState);
    }

    void setNumberOfDatabaseObjects(long count) {
        if (IndexStates.NO_STATE.equals(objectIndexState)) {
            totalCount.set(count);
        }
    }

    public void setObjectIndexState(IndexStates objectIndexState) {
        this.objectIndexState = objectIndexState;
    }

    @Override
    public void documentsAdded(long increment) {
        documentsAdded.addAndGet(increment);
    }

    @Override
    public void documentsBuilt(long increment) {
        documentsBuilt.addAndGet(increment);
    }

    @Override
    public void entitiesLoaded(long increment) {
        entitiesLoaded.addAndGet(increment);
    }

    @Override
    public void addToTotalCount(long increment) {
        totalCount.addAndGet(increment);
    }

    @Override
    public void indexingCompleted() {
    }
}
