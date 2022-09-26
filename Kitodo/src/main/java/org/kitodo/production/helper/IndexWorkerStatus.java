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
     * Initialize index worker status.
     * 
     * @param maxBatch the maximum number of batches to be processed
     */
    public IndexWorkerStatus(Integer maxBatch) {
        this.maxBatch = maxBatch;
    }

    public int getMaxBatch() {
        return this.maxBatch;
    }

    public int getAndIncrementNextBatch() {
        return this.nextBatch.getAndIncrement();
    }

}
