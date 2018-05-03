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

package org.kitodo.services.data;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.json.JsonObject;

import org.elasticsearch.index.query.Operator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

/**
 * Tests for BatchService class.
 */
public class BatchServiceIT {

    private static final BatchService batchService = new ServiceManager().getBatchService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(500);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllBatches() throws Exception {
        Long amount = batchService.count();
        assertEquals("Batches were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldCountAllBatchesAccordingToQuery() throws Exception {
        String query = matchQuery("title", "First batch").operator(Operator.AND).toString();
        Long amount = batchService.count(query);
        assertEquals("Batches were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForBatches() throws Exception {
        Long amount = batchService.countDatabaseRows();
        assertEquals("Batches were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldFindBatch() throws Exception {
        Batch batch = batchService.getById(1);
        boolean condition = batch.getTitle().equals("First batch") && batch.getType().equals(Batch.Type.LOGISTIC);
        assertTrue("Batch was not found in database!", condition);
    }

    @Test
    public void shouldFindAllBatches() {
        List<Batch> batches = batchService.getAll();
        assertEquals("Not all batches were found in database!", 4, batches.size());
    }

    @Test
    public void shouldGetAllBatchesInGivenRange() throws Exception {
        List<Batch> batches = batchService.getAll(2,10);
        assertEquals("Not all batches were found in database!", 2, batches.size());
    }

    @Test
    public void shouldRemoveBatch() throws Exception {
        Batch batch = new Batch();
        batch.setTitle("To Remove");
        batch.setType(Batch.Type.SERIAL);
        batchService.save(batch);
        Batch foundBatch = batchService.getById(5);
        assertEquals("Additional batch was not inserted in database!", "To Remove", foundBatch.getTitle());

        batchService.remove(foundBatch);
        exception.expect(DAOException.class);
        batchService.getById(5);

        batch = new Batch();
        batch.setTitle("To remove");
        batch.setType(Batch.Type.SERIAL);
        batchService.save(batch);
        foundBatch = batchService.getById(6);
        assertEquals("Additional batch was not inserted in database!", "To remove", foundBatch.getTitle());

        batchService.remove(6);
        exception.expect(DAOException.class);
        batchService.getById(6);
    }

    @Test
    public void shouldFindById() throws Exception {
        String actual = batchService.findById(1).getTitle();
        String expected = "First batch";
        assertEquals("Batch was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        List<JsonObject> batches = batchService.findByTitle("batch", true);
        Integer actual = batches.size();
        Integer expected = 3;
        assertEquals("Batches were not found in index!", expected, actual);

        batches = batchService.findByTitle("First batch", true);
        actual = batches.size();
        expected = 1;
        assertEquals("Batch was not found in index!", expected, actual);

        batches = batchService.findByTitle("noBatch", true);
        actual = batches.size();
        expected = 0;
        assertEquals("Batch was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByType() throws Exception {
        List<JsonObject> batches = batchService.findByType(Batch.Type.LOGISTIC, true);
        Integer actual = batches.size();
        Integer expected = 2;
        assertEquals("Batches were not found in index!", expected, actual);

        batches = batchService.findByType(Batch.Type.SERIAL, true);
        actual = batches.size();
        expected = 1;
        assertEquals("Batch was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndType() throws Exception {
        List<JsonObject> batches = batchService.findByTitleAndType("First batch", Batch.Type.LOGISTIC);
        Integer actual = batches.size();
        Integer expected = 1;
        assertEquals("Batch was not found in index!", expected, actual);

        batches = batchService.findByTitleAndType("Second batch", Batch.Type.SERIAL);
        actual = batches.size();
        expected = 0;
        assertEquals("Batch was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleOrType() throws Exception {
        List<JsonObject> batches = batchService.findByTitleOrType("First batch", Batch.Type.SERIAL);
        Integer actual = batches.size();
        Integer expected = 2;
        assertEquals("Batches were not found in index!", expected, actual);

        batches = batchService.findByTitleOrType("None", Batch.Type.SERIAL);
        actual = batches.size();
        expected = 1;
        assertEquals("More batches were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessId() throws Exception {
        List<JsonObject> batches = batchService.findByProcessId(1);
        Integer actual = batches.size();
        Integer expected = 2;
        assertEquals("Batches were not found in index!", expected, actual);

        batches = batchService.findByProcessId(2);
        actual = batches.size();
        expected = 1;
        assertEquals("Batch was not found in index!", expected, actual);

        batches = batchService.findByProcessId(3);
        actual = batches.size();
        expected = 0;
        assertEquals("Some batches were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessTitle() throws Exception {
        List<JsonObject> batches = batchService.findByProcessTitle("First process");
        Integer actual = batches.size();
        Integer expected = 2;
        assertEquals("Batches were not found in index!", expected, actual);

        batches = batchService.findByProcessTitle("Second process");
        actual = batches.size();
        expected = 1;
        assertEquals("Batch was not found in index!", expected, actual);

        batches = batchService.findByProcessTitle("DBConnectionTest");
        actual = batches.size();
        expected = 0;
        assertEquals("Some batches were found in index!", expected, actual);
    }

    @Test
    public void shouldContainCharSequence() throws Exception {
        Batch batch = batchService.getById(1);
        boolean condition = batch.getTitle().contains("bat") == batchService.contains(batch, "bat");
        assertTrue("It doesn't contain given char sequence!", condition);
    }

    @Test
    public void shouldGetIdString() throws Exception {
        Batch batch = batchService.getById(1);
        boolean condition = batchService.getIdString(batch).equals("1");
        assertTrue("Id's String doesn't match the given plain text!", condition);
    }

    @Test
    public void shouldGetLabel() throws Exception {
        Batch firstBatch = batchService.getById(1);
        boolean firstCondition = batchService.getLabel(firstBatch).equals("First batch");
        assertTrue("It doesn't get given label!", firstCondition);

        Batch secondBatch = batchService.getById(4);
        boolean secondCondition = batchService.getLabel(secondBatch).equals("Batch 4");
        assertTrue("It doesn't get given label!", secondCondition);
    }

    @Test
    public void shouldGetSizeOfProcesses() throws Exception {
        Batch batch = batchService.getById(1);
        int size = batchService.size(batch);
        assertEquals("Size of processes is not equal 1!", 1, size);
    }

    @Test
    public void shouldOverrideToString() throws Exception {
        Batch batch = batchService.getById(1);
        String toString = batchService.toString(batch);
        assertEquals("Override toString method is incorrect!", "First batch (1 processes) [logistics]", toString);
    }
}
