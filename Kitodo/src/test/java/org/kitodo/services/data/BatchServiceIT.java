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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.exceptions.DataException;

/**
 * Tests for BatchService class.
 */
public class BatchServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, DataException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void shouldFindBatch() throws Exception {
        BatchService batchService = new BatchService();

        Batch batch = batchService.find(1);
        boolean condition = batch.getTitle().equals("First batch") && batch.getType().equals(Batch.Type.LOGISTIC);
        assertTrue("Batch was not found in database!", condition);
    }

    @Test
    public void shouldFindAllBatches() {
        BatchService batchService = new BatchService();

        List<Batch> batches = batchService.findAll();
        assertEquals("Not all batches were found in database!", 4, batches.size());
    }

    @Test
    public void shouldRemoveBatch() throws Exception {
        BatchService batchService = new BatchService();

        Batch batch = new Batch();
        batch.setTitle("To Remove");
        batch.setType(Batch.Type.SERIAL);
        batchService.save(batch);
        Batch foundBatch = batchService.convertSearchResultToObject(batchService.findById(5));
        assertEquals("Additional batch was not inserted in database!", "To Remove", foundBatch.getTitle());

        batchService.remove(foundBatch);
        foundBatch = batchService.convertSearchResultToObject(batchService.findById(5));
        assertEquals("Additional batch was not removed from database!", null, foundBatch);

        batch = new Batch();
        batch.setTitle("To remove");
        batch.setType(Batch.Type.SERIAL);
        batchService.save(batch);
        foundBatch = batchService.convertSearchResultToObject(batchService.findById(6));
        assertEquals("Additional batch was not inserted in database!", "To remove", foundBatch.getTitle());

        batchService.remove(6);
        foundBatch = batchService.convertSearchResultToObject(batchService.findById(6));
        assertEquals("Additional batch was not removed from database!", null, foundBatch);
    }

    @Test
    public void shouldFindById() throws Exception {
        BatchService batchService = new BatchService();

        SearchResult batch = batchService.findById(1);
        String actual = batch.getProperties().get("title");
        String expected = "First batch";
        assertEquals("Batch was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        BatchService batchService = new BatchService();

        List<SearchResult> batches = batchService.findByTitle("batch", true);
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
        BatchService batchService = new BatchService();

        List<SearchResult> batches = batchService.findByType(Batch.Type.LOGISTIC, true);
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
        BatchService batchService = new BatchService();

        List<SearchResult> batches = batchService.findByTitleAndType("First batch", Batch.Type.LOGISTIC);
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
        BatchService batchService = new BatchService();

        List<SearchResult> batches = batchService.findByTitleOrType("First batch", Batch.Type.SERIAL);
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
        BatchService batchService = new BatchService();

        List<SearchResult> batches = batchService.findByProcessId(1);
        Integer actual = batches.size();
        Integer expected = 2;
        assertEquals("Batches were not found in index!", expected, actual);

        batches = batchService.findByProcessId(2);
        actual = batches.size();
        expected = 0;
        assertEquals("Some batches ere found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessTitle() throws Exception {
        BatchService batchService = new BatchService();

        List<SearchResult> batches = batchService.findByProcessTitle("First process");
        Integer actual = batches.size();
        Integer expected = 2;
        assertEquals("Batches were not found in index!", expected, actual);

        batches = batchService.findByProcessTitle("Second process");
        actual = batches.size();
        expected = 0;
        assertEquals("Batches were not found in index!", expected, actual);
    }

    @Test
    public void shouldContainCharSequence() throws Exception {
        BatchService batchService = new BatchService();

        Batch batch = batchService.find(1);
        boolean condition = batch.getTitle().contains("bat") == batchService.contains(batch, "bat");
        assertTrue("It doesn't contain given char sequence!", condition);
    }

    @Test
    public void shouldGetIdString() throws Exception {
        BatchService batchService = new BatchService();

        Batch batch = batchService.find(1);
        boolean condition = batchService.getIdString(batch).equals("1");
        assertTrue("Id's String doesn't match the given plain text!", condition);
    }

    @Test
    public void shouldGetLabel() throws Exception {
        BatchService batchService = new BatchService();

        Batch firstBatch = batchService.find(1);
        boolean firstCondition = batchService.getLabel(firstBatch).equals("First batch");
        assertTrue("It doesn't get given label!", firstCondition);

        Batch secondBatch = batchService.find(4);
        boolean secondCondition = batchService.getLabel(secondBatch).equals("Batch 4");
        assertTrue("It doesn't get given label!", secondCondition);
    }

    @Test
    public void shouldGetSizeOfProcesses() throws Exception {
        BatchService batchService = new BatchService();

        Batch batch = batchService.find(1);
        int size = batchService.size(batch);
        assertEquals("Size of processes is not equal 1!", 1, size);
    }

    @Test
    public void shouldOverrideToString() throws Exception {
        BatchService batchService = new BatchService();

        Batch batch = batchService.find(1);
        String toString = batchService.toString(batch);
        assertTrue("Override toString method is incorrect!", toString.equals("First batch (1 processes) [logistics]"));
    }
}
