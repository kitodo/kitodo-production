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

import org.kitodo.data.database.beans.Batch;

/**
 * Implementation of Batch Type.
 */
public class BatchType extends BaseType<Batch> {

    @Override
    JsonObject getJsonObject(Batch batch) {
        String type = batch.getType() != null ? batch.getType().toString() : "";

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("title", preventNull(batch.getTitle()));
        jsonObjectBuilder.add("type", type);
        jsonObjectBuilder.add("processes", addObjectRelation(batch.getProcesses(), true));
        return jsonObjectBuilder.build();
    }
}
