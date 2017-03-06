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

package org.kitodo.api.search;

import java.util.ArrayList;

public interface SearchInterface<T>  {

    /**
     * Searches with a given query.
     *
     * @param query The query to execute.
     * @return A list of ids of the found objects.
     */
    ArrayList<Integer> search(String query);

    /**
     * Searches with given search conditions.
     *
     * @param searchConditions The search conditions for the search.
     * @return A list of ids of the found objects.
     */
    ArrayList<Integer> search(ArrayList<SearchCondition<?, ?>> searchConditions);
}
