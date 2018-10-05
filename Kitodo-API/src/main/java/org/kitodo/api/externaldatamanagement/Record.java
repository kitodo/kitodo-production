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

/**
 * This class represents one element of a hitlist that is created
 * by a search query. Each Record contains a title that will be
 * displayed as a link in the hitlist and an ID to identify the
 * specific record to be imported when the user clicks on one record
 * in the hitlist.
 */
public class Record {

    private String title;

    private String id;

    /**
     * Return title of record.
     *
     * @return
     *      title of this record.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title of the record.
     *
     * @param title value of title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return ID of this record.
     *
     * @return
     *      ID of this record.
     */
    public String getId() {
        return id;
    }

    /**
     * Set ID of record.
     *
     * @param id
     *            value of ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

}
