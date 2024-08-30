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
 * Meta interface over the data interfaces of this interface.
 */
public interface BaseBeanInterface {

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Returns the record number of the object in the database. Can be
     * {@code null} if the object has not yet been persisted.
     *
     * @return the record number
     */
    Integer getId();

    /**
     * Sets the data record number of the object. This should only happen when
     * data from a third-party source is integrated during operation, or in
     * tests. Normally the data record number is assigned by the database when
     * the object is saved.
     *
     * @param id
     *            data record number to use
     */
    void setId(Integer id);
}
