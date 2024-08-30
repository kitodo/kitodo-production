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

package org.kitodo.data.interfaces;

/**
 * Interface for persisting search queries from users.
 */
public interface FilterInterface extends BaseBeanInterface {

    /**
     * Returns the search query string.
     * 
     * @return the search query
     */
    String getValue();

    /**
     * Sets the search query string.
     * 
     * @param value
     *            query string to specify
     */
    void setValue(String value);
}
