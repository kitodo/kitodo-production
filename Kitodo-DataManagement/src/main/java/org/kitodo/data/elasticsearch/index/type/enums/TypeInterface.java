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

package org.kitodo.data.elasticsearch.index.type.enums;

import java.util.Objects;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.kitodo.data.exceptions.DataException;

interface TypeInterface {

    /**
     * Get boolean value from given json object.
     * 
     * @param jsonObject
     *            returned from ElasticSearch index
     * @return boolean value for given json
     */
    default boolean getBooleanValue(JsonObject jsonObject) throws DataException {
        try {
            return jsonObject.getBoolean(this.toString());
        } catch (ClassCastException | NullPointerException e) {
            throw new DataException("Not possible to retrieve boolean value for key " + this.toString()
                    + ". Exception: " + e.getMessage());
        }
    }

    /**
     * Get int value from given json object.
     * 
     * @param jsonObject
     *            returned from ElasticSearch index
     * @return int value for given json
     */
    default int getIntValue(JsonObject jsonObject) throws DataException {
        try {
            return jsonObject.getInt(this.toString());
        } catch (ClassCastException | NullPointerException e) {
            throw new DataException("Not possible to retrieve int value for key " + this.toString()
                    + ". Exception: " + e.getMessage());
        }
    }

    /**
     * Get String value from given json object.
     * 
     * @param jsonObject
     *            returned from ElasticSearch index
     * @return String value for given json
     */
    default String getStringValue(JsonObject jsonObject) throws DataException {
        try {
            return jsonObject.getString(this.toString());
        } catch (ClassCastException | NullPointerException e) {
            throw new DataException("Not possible to retrieve String value for key " + this.toString()
                    + ". Exception: " + e.getMessage());
        }
    }

    /**
     * Get json array value from given json object.
     *
     * @param jsonObject
     *            returned from ElasticSearch index
     * @return json array for given json
     */
    default JsonArray getJsonArray(JsonObject jsonObject) throws DataException {
        try {
            return jsonObject.getJsonArray(this.toString());
        } catch (ClassCastException | NullPointerException e) {
            throw new DataException("Not possible to retrieve JsonArray value for key " + this.toString()
                    + ". Exception: " + e.getMessage());
        }
    }

    /**
     * Get size of related objects returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject
     * @return size of array with related objects
     */
    default int getSizeOfProperty(JsonObject object) throws DataException {
        if (Objects.nonNull(object)) {
            try {
                return object.getJsonArray(this.toString()).size();
            } catch (ClassCastException | NullPointerException e) {
                throw new DataException("Not possible to retrieve size of array for key " + this.toString()
                        + ". Exception: " + e.getMessage());
            }
        }
        return 0;
    }
}
