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

public class SearchCondition<S,V> {

    /** The field to search in. */
    private String fieldName;

    /** If the field should contain the value or not. */
    private boolean include;

    /** The value to search for. */
    private V value;

}
