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

import javax.json.JsonObject;

interface TypeInterface {

    public default boolean getBooleanValue(JsonObject jsonObject) {
        return jsonObject.getBoolean(this.toString());
    }

    public default int getIntValue(JsonObject jsonObject) {
        return jsonObject.getInt(this.toString());
    }

    public default String getStringValue(JsonObject jsonObject) {
        return jsonObject.getString(this.toString());
    }
}
