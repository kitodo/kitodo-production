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

package org.kitodo.data.index.elasticsearch.type;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
public class BatchType /*extends BaseType*/ {

    @SuppressWarnings("unchecked")
    public HttpEntity createDocument(Batch batch) {

        LinkedHashMap<String, String> orderedBatchMap = new LinkedHashMap<>();
        orderedBatchMap.put("title", batch.getTitle());
        String type = batch.getType() != null ? batch.getType().toString() : "null";
        orderedBatchMap.put("type", type);

        JSONArray processes = new JSONArray();
        List<Process> batchProcesses = batch.getProcesses();
        for (Process process : batchProcesses) {
            JSONObject processObject = new JSONObject();
            processObject.put("id", process.getId().toString());
            processes.add(processObject);
        }

        JSONObject batchObject = new JSONObject(orderedBatchMap);
        batchObject.put("processes", processes);

        return new NStringEntity(batchObject.toJSONString(), ContentType.APPLICATION_JSON);
    }

    public HashMap<Integer, HttpEntity> createDocuments(List<Batch> batches) {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();
        for (Batch batch : batches) {
            documents.put(batch.getId(), createDocument(batch));
        }
        return documents;
    }
}
