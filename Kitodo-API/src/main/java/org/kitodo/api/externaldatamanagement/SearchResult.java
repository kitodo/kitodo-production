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

package org.kitodo.api.externaldatamanagement;

import java.util.List;

/**
 * This class represents the result of a search query performed against a
 * remote catalog interface.
 */
public class SearchResult {

    private List<Record> hits;

    private int numberOfRecords;

    /**
     * Get number of records.
     *
     * @return number of records
     */
    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    /**
     * Set number of records.
     *
     * @param numberOfRecords
     *            number of records
     */
    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    /**
     * Get list of hists.
     *
     * @return list of hits.
     */
    public List<Record> getHits() {
        return hits;
    }

    /**
     * Set list of hits.
     *
     * @param hits
     *            list of hits.
     */
    public void setHits(List<Record> hits) {
        this.hits = hits;
    }
}
