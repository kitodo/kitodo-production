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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexWorkerStatus {

    /**
     * Stores the maximum batch number that needs to be indexed.
     * 
     * <p>If a worker thread receives a batch number higher than this number, there are 
     * no additional batches that need to be processed, and the worker thread stops.</p>
     */
    private final Integer maxBatch;

    /**
     * Stores the number of the next batch that needs to be indexed.
     */
    private final AtomicInteger nextBatch = new AtomicInteger(0);

    /**
     * Stores whether there was a failure during indexing.
     */
    private final AtomicBoolean failed = new AtomicBoolean(false);

    /**
     * Initialize index worker status.
     * 
     * @param maxBatch the maximum number of batches to be processed
     */
    public IndexWorkerStatus(Integer maxBatch) {
        this.maxBatch = maxBatch;
    }

    /**
     * Returns the maximum batch number that can be indexed.
     * 
     * @return the maximum batch number that can be indexed
     */
    public int getMaxBatch() {
        return this.maxBatch;
    }

    /**
     * Access and increase next batch that needs to be indexed in a thread-safe way.
     * 
     * @return the next batch that needs to be indexed
     */
    public int getAndIncrementNextBatch() {
        return this.nextBatch.getAndIncrement();
    }

    /**
     * Returns whether the indexing process has failed.
     * 
     * @return true when the indexing process has failed
     */
    public boolean hasFailed() {
        return failed.get();
    }

    /**
     * Marks the index status as failed with some reason.
     */
    public void markAsFailed() {
        this.failed.set(true);
    }
}
