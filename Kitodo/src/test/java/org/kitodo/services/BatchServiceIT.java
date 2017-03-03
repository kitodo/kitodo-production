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

package org.kitodo.services;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for BatchService class.
 */
public class BatchServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindBatch() throws Exception {
        BatchService batchService = new BatchService();

        Batch batch = batchService.find(1);
        boolean condition = batch.getTitle().equals("First batch") && batch.getType().equals(Batch.Type.LOGISTIC);
        assertTrue("Batch was not found in database!", condition);
    }

    @Test
    public void shouldFindAllBatches() throws Exception {
        BatchService batchService = new BatchService();

        List<Batch> batches = batchService.findAll();
        assertEquals("Not all batches were found in database!", 4, batches.size());
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
