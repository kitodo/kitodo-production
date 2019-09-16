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

    private List<SingleHit> hits;

    private int numberOfHits;

    /**
     * Get number of hits.
     *
     * @return number of hits
     */
    public int getNumberOfHits() {
        return numberOfHits;
    }

    /**
     * Set number of hits.
     *
     * @param numberOfHits
     *            number of hits
     */
    public void setNumberOfHits(int numberOfHits) {
        this.numberOfHits = numberOfHits;
    }

    /**
     * Get list of hists.
     *
     * @return list of hits.
     */
    public List<SingleHit> getHits() {
        return hits;
    }

    /**
     * Set list of hits.
     *
     * @param hits
     *            list of hits.
     */
    public void setHits(List<SingleHit> hits) {
        this.hits = hits;
    }
}
