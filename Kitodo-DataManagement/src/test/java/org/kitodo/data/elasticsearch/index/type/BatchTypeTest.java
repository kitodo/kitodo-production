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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;

/**
 * Test class for BatchType.
 */
public class BatchTypeTest {

    private static List<Batch> prepareData() {

        List<Batch> batches = new ArrayList<>();
        List<Process> processes = new ArrayList<>();

        Process firstProcess = new Process();
        firstProcess.setId(1);
        firstProcess.setTitle("First");
        processes.add(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setId(2);
        secondProcess.setTitle("Second");
        processes.add(secondProcess);

        Batch firstBatch = new Batch();
        firstBatch.setId(1);
        firstBatch.setTitle("Batch1");
        firstBatch.setType(Batch.Type.LOGISTIC);
        firstBatch.setProcesses(processes);
        batches.add(firstBatch);

        Batch secondBatch = new Batch();
        secondBatch.setId(2);
        secondBatch.setTitle("Batch2");
        batches.add(secondBatch);

        Batch thirdBatch = new Batch();
        thirdBatch.setId(3);
        thirdBatch.setTitle("Batch3");
        thirdBatch.setType(Batch.Type.LOGISTIC);
        batches.add(thirdBatch);

        return batches;
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        BatchType batchType = new BatchType();
        JSONParser parser = new JSONParser();

        Batch batch = prepareData().get(0);
        HttpEntity document = batchType.createDocument(batch);
        JSONObject actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        JSONObject expected = (JSONObject) parser
                .parse("{\"title\":\"Batch1\",\"type\":\"LOGISTIC\",\"processes\":[{\"id\":1,\"title\":\"First\","
                        + "\"template\":false},{\"id\":2,\"title\":\"Second\",\"template\":false}]}");
        assertEquals("Batch JSONObject doesn't match to given JSONObject!", expected, actual);

        batch = prepareData().get(1);
        document = batchType.createDocument(batch);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"title\":\"Batch2\",\"type\":null,\"processes\":[]}");
        assertEquals("Batch JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        BatchType batchType = new BatchType();

        List<Batch> batches = prepareData();
        HashMap<Integer, HttpEntity> documents = batchType.createDocuments(batches);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
