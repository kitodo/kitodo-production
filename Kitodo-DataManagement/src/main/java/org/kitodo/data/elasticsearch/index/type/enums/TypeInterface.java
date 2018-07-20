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

import javax.json.JsonArray;
import javax.json.JsonObject;

interface TypeInterface {

    /**
     * Get boolean value from given json object.
     * 
     * @param jsonObject
     *            returned from ElasticSearch index
     * @return boolean value for given json
     */
    default boolean getBooleanValue(JsonObject jsonObject) {
        return jsonObject.getBoolean(this.toString());
    }

    /**
     * Get int value from given json object.
     * 
     * @param jsonObject
     *            returned from ElasticSearch index
     * @return int value for given json
     */
    default int getIntValue(JsonObject jsonObject) {
        return jsonObject.getInt(this.toString());
    }

    /**
     * Get String value from given json object.
     * 
     * @param jsonObject
     *            returned from ElasticSearch index
     * @return String value for given json
     */
    default String getStringValue(JsonObject jsonObject) {
        return jsonObject.getString(this.toString());
    }

    /**
     * Get size of related objects returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject
     * @return size of array with related objects
     */
    default int getSizeOfProperty(JsonObject object) {
        if (object != null) {
            JsonArray jsonArray = (JsonArray) object.get(this.toString());
            return jsonArray.size();
        }
        return 0;
    }
}
