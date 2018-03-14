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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.kitodo.data.database.beans.Batch;

/**
 * Implementation of Batch Type.
 */
public class BatchType extends BaseType<Batch> {

    @Override
    public HttpEntity createDocument(Batch batch) {
        String type = batch.getType() != null ? batch.getType().toString() : "";

        JsonObject batchObject = Json.createObjectBuilder()
                .add("title", preventNull(batch.getTitle()))
                .add("type", type)
                .add("processes", addObjectRelation(batch.getProcesses(), true)).build();

        return new NStringEntity(batchObject.toString(), ContentType.APPLICATION_JSON);
    }
}
