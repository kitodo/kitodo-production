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

package org.kitodo.data.elasticsearch.search;

import java.util.HashMap;

/**
 * Object contains results of search in Elastic Search index.
 */
public class SearchResult {
    private Integer id;
    private HashMap<String, Object> properties;

    /**
     * Get id.
     *
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set id.
     *
     * @param id
     *            of search result, equal to id of object in database
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get properties of search result.
     *
     * @return HashMap of properties - key is name of field and value is value
     *         of field (possible Integer, String, JSONArray)
     */
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    /**
     * Set properties of search result.
     *
     * @param properties
     *            HashMap - key is name of field and value is value of field (possible Integer, String, JSONArray)
     */
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }
}
