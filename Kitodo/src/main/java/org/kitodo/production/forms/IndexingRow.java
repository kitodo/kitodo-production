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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.production.enums.IndexStates;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.index.IndexingService;

/**
 * A row in the indexing table.
 */
public class IndexingRow implements MassIndexingMonitor {
    private static final Logger logger = LogManager.getLogger(IndexingRow.class);
    private final IndexingService indexingService = ServiceManager.getIndexingService();

    private final Class<? extends BaseBean> type;

    IndexStates objectIndexState = IndexStates.NO_STATE;
    private AtomicLong entitiesLoaded = new AtomicLong();
    private AtomicLong documentsBuilt = new AtomicLong();
    private AtomicLong documentsAdded = new AtomicLong();
    private AtomicLong totalCount = new AtomicLong();
    private long numberOfObjects;

    /**
     * <b>Constructor.</b> Creates a new table row for the given bean type.
     * 
     * @param type
     *            class of database beans to be indexed
     */
    IndexingRow(Class<? extends BaseBean> type) {
        this.type = type;
    }

    /**
     * Starts indexing.
     */
    void callIndexing() {
        long count = totalCount.get();
        if (count > 0) {
            numberOfObjects = count;
        }
        objectIndexState = IndexStates.INDEXING_STARTED;
        entitiesLoaded.set(0);
        documentsBuilt.set(0);
        documentsAdded.set(0);
        totalCount.set(0);
        indexingService.startIndexing(type, this).whenComplete((unused, throwable) -> {
            if (Objects.isNull(throwable)) {
                objectIndexState = IndexStates.INDEXING_SUCCESSFUL;
                logger.info("Indexing complete for {}", type.getSimpleName());
            } else {
                logger.error(throwable);
                objectIndexState = IndexStates.INDEXING_FAILED;
            }
        });
    }

    /**
     * Returns the total number of objects. Used for the total row of the table.
     * 
     * @return the total number of objects
     */
    long getCount() {
        long total = totalCount.get();
        return total == 0 ? numberOfObjects : total;
    }

    /**
     * Returns the number of indexed objects. Used for the total row of the
     * table.
     * 
     * @return the number of indexed objects
     */
    long getIndexed() {
        return documentsAdded.get();
    }

    /**
     * Returns the object count for the table row. If the indexer is not
     * running, this is just the total count. While the indexer is running, this
     * returns three values ​​before the slash: the number of objects that
     * Hibernate has already fetched from the database, the number of objects
     * that have been converted to an indexable JSON document, and the number of
     * indexed documents. After the slash is always the total number of objects.
     * 
     * @return the object count
     */
    /* Since the total count also has to be reset when indexing starts, a cached
     * value is used here so that a zero does not flash in the display in
     * between. */
    String getNumberOfObjects() {
        long objectCount = totalCount.get();
        if (objectCount == 0) {
            objectCount = numberOfObjects;
        }
        if (IndexStates.NO_STATE.equals(objectIndexState)) {
            return Long.toString(objectCount);
        } else {
            StringBuilder numberOfObjects = new StringBuilder(40);
            numberOfObjects.append(entitiesLoaded.get());
            numberOfObjects.append(" \u2012 ");
            numberOfObjects.append(documentsBuilt.get());
            numberOfObjects.append(" \u2012 ");
            numberOfObjects.append(documentsAdded.get());
            numberOfObjects.append(" / ");
            numberOfObjects.append(objectCount);
            return numberOfObjects.toString();
        }
    }

    /**
     * Returns the state of indexing.
     * 
     * @return the indexing state
     */
    IndexStates getObjectIndexState() {
        return objectIndexState;
    }

    /**
     * Returns the progress of indexing in percent. This is used to display a
     * progress bar.
     * 
     * @return the progress in [0 .. 100]
     */
    int getProgress() {
        long count = getCount();
        if (count == 0) {
            return 0;
        }
        return (int) (100 * getIndexed() / count);
    }

    /**
     * Returns whether indexing is currently running.
     * 
     * @return whether indexing is currently running
     */
    boolean isIndexingInProgress() {
        return IndexStates.INDEXING_STARTED.equals(objectIndexState);
    }

    /**
     * Sets the number of objects in the database. These are set initially so
     * that they are not zero everywhere before indexing starts.
     * 
     * @param numberOfDatabaseObjects
     *            number of objects in the database
     */
    void setNumberOfDatabaseObjects(long numberOfDatabaseObjects) {
        this.numberOfObjects = numberOfDatabaseObjects;
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
