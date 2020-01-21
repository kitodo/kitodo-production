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

package org.kitodo.production.importer;

import java.util.ArrayList;
import java.util.List;

public class Record {

    private List<String> collections = new ArrayList<>();
    private String data = "";
    private String id = "";

    /**
     * Set data.
     *
     * @param data
     *            the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Get data.
     *
     * @return the data
     */
    public String getData() {
        return this.data;
    }

    /**
     * Set id.
     *
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    public void setCollections(List<String> collections) {
        this.collections = collections;
    }

    public List<String> getCollections() {
        return this.collections;
    }
}
