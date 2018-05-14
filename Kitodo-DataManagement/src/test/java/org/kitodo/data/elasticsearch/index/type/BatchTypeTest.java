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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;

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
    public void shouldCreateFirstDocument() throws Exception {
        BatchType batchType = new BatchType();

        Batch batch = prepareData().get(0);
        HttpEntity document = batchType.createDocument(batch);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Batch1",
            actual.getString(BatchTypeField.TITLE.getName()));
        assertEquals("Key type doesn't match to given value!", "LOGISTIC",
            actual.getString(BatchTypeField.TYPE.getName()));

        JsonArray processes = actual.getJsonArray(BatchTypeField.PROCESSES.getName());
        assertEquals("Size processes doesn't match to given value!", 2, processes.size());

        JsonObject process = processes.getJsonObject(0);
        assertEquals("Key processes.id doesn't match to given value!", 1,
            process.getInt(ProcessTypeField.ID.getName()));
        assertEquals("Key processes.title doesn't match to given value!", "First",
            process.getString(ProcessTypeField.TITLE.getName()));

        process = processes.getJsonObject(1);
        assertEquals("Key processes.id doesn't match to given value!", 2,
            process.getInt(ProcessTypeField.ID.getName()));
        assertEquals("Key processes.title doesn't match to given value!", "Second",
            process.getString(ProcessTypeField.TITLE.getName()));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        BatchType batchType = new BatchType();

        Batch batch = prepareData().get(1);
        HttpEntity document = batchType.createDocument(batch);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Batch2",
            actual.getString(BatchTypeField.TITLE.getName()));
        assertEquals("Key type doesn't match to given value!", "", actual.getString(BatchTypeField.TYPE.getName()));

        JsonArray processes = actual.getJsonArray(BatchTypeField.PROCESSES.getName());
        assertEquals("Size processes doesn't match to given value!", 0, processes.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        BatchType batchType = new BatchType();

        Batch batch = prepareData().get(0);
        HttpEntity document = batchType.createDocument(batch);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 3, actual.keySet().size());

        JsonArray processes = actual.getJsonArray(BatchTypeField.PROCESSES.getName());
        JsonObject process = processes.getJsonObject(0);
        assertEquals("Amount of keys in processes is incorrect!", 2, process.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        BatchType batchType = new BatchType();

        List<Batch> batches = prepareData();
        Map<Integer, HttpEntity> documents = batchType.createDocuments(batches);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
