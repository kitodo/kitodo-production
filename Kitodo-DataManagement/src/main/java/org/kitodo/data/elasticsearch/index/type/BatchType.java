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

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;

/**
 * Implementation of Batch Type.
 */
public class BatchType extends BaseType<Batch> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Batch batch) {

        JSONObject batchObject = new JSONObject();
        batchObject.put("title", batch.getTitle());
        String type = batch.getType() != null ? batch.getType().toString() : null;
        batchObject.put("type", type);

        JSONArray processes = new JSONArray();
        List<Process> batchProcesses = batch.getProcesses();
        for (Process process : batchProcesses) {
            JSONObject processObject = new JSONObject();
            processObject.put("id", process.getId());
            processes.add(processObject);
        }
        batchObject.put("processes", processes);

        return new NStringEntity(batchObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
