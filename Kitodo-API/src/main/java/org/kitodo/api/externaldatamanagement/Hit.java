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
 * by a search query. Each 'Hit' contains a title that will be
 * displayed as a link in the hitlist and an ID to identify the
 * specific hit to be imported when the user clicks on one hit
 * in the hitlist.
 */
public class Hit {

    private String title;

    private String id;

    /**
     * Standard constructor setting title and id of hit.
     * @param title title of hit
     * @param id id of hit
     */
    public Hit(String title, String id) {
        this.title = title;
        this.id = id;
    }

    /**
     * Return title of hit.
     *
     * @return
     *      title of this hit.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title of the hit.
     *
     * @param title value of title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return ID of this hit.
     *
     * @return
     *      ID of this hit.
     */
    public String getId() {
        return id;
    }

    /**
     * Set ID of hit.
     *
     * @param id
     *            value of ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

}
