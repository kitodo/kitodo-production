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

package org.kitodo.data.elasticsearch.index.type;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Docket;

/**
 * Implementation of Docket Type.
 */
public class DocketType extends BaseType<Docket> {

    @Override
    JsonObject getJsonObject(Docket docket) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("title", preventNull(docket.getTitle()));
        jsonObjectBuilder.add("file", preventNull(docket.getFile()));
        return jsonObjectBuilder.build();
    }
}
