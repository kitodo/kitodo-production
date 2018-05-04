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
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;

/**
 * Implementation of Batch Type.
 */
public class BatchType extends BaseType<Batch> {

    @Override
    JsonObject getJsonObject(Batch batch) {
        String type = batch.getType() != null ? batch.getType().toString() : "";

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(BatchTypeField.TITLE.getName(), preventNull(batch.getTitle()));
        jsonObjectBuilder.add(BatchTypeField.TYPE.getName(), type);
        jsonObjectBuilder.add(BatchTypeField.PROCESSES.getName(), addObjectRelation(batch.getProcesses(), true));
        return jsonObjectBuilder.build();
    }
}
