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

package org.kitodo.production.search.api;

import java.util.ArrayList;

/**
 * This interface defines a search engine for kitodo.
 * 
 * @author Robert Sehr
 */
@Deprecated
public interface ISearch {

    /**
     *
     * @param query
     *            the search query
     * @return an ArrayList with identifier for the resulting objects
     */

    public ArrayList<Integer> getSearchResults(String query);

    /**
     *
     * @param query
     *            the search query
     * @return count of the resulting objects
     */
    public int getSearchCount(String query);

}
