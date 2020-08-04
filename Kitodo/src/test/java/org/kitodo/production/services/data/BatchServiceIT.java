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

package org.kitodo.production.services.data;

import static org.awaitility.Awaitility.given;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for BatchService class.
 */
public class BatchServiceIT {

    private static final BatchService batchService = ServiceManager.getBatchService();

    private static final String BATCH_NOT_FOUND = "Batch was not found in index!";
    private static final String BATCHES_NOT_FOUND = "Batches were not found in index!";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        given().ignoreExceptions().await().until(() -> Objects.nonNull(batchService.findById(1, true)));
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllBatches() throws DataException {
        assertEquals("Batches were not counted correctly!", Long.valueOf(4), batchService.count());
    }

    @Test
    public void shouldCountAllBatchesAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("title", "First batch").operator(Operator.AND);
        assertEquals("Batches were not counted correctly!", Long.valueOf(1), batchService.count(query));
    }

    @Test
    public void shouldCountAllDatabaseRowsForBatches() throws Exception {
        Long amount = batchService.countDatabaseRows();
        assertEquals("Batches were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldGetBatch() throws Exception {
        Batch batch = batchService.getById(1);
        boolean condition = batch.getTitle().equals("First batch");
        assertTrue("Batch was not found in database!", condition);

        assertEquals("Batch was found but processes were not inserted!", 1, batch.getProcesses().size());
    }

    @Test
    public void shouldFindAllBatches() throws Exception {
        List<Batch> batches = batchService.getAll();
        assertEquals("Not all batches were found in database!", 4, batches.size());
    }

    @Test
    public void shouldGetAllBatchesInGivenRange() throws Exception {
        List<Batch> batches = batchService.getAll(2, 10);
        assertEquals("Not all batches were found in database!", 2, batches.size());
    }

    @Test
    public void shouldRemoveBatch() throws Exception {
        Batch batch = new Batch();
        batch.setTitle("To Remove");
        batchService.save(batch);
        Batch foundBatch = batchService.getById(5);
        assertEquals("Additional batch was not inserted in database!", "To Remove", foundBatch.getTitle());

        batchService.remove(foundBatch);
        exception.expect(DAOException.class);
        batchService.getById(5);

        batch = new Batch();
        batch.setTitle("To remove");
        batchService.save(batch);
        foundBatch = batchService.getById(6);
        assertEquals("Additional batch was not inserted in database!", "To remove", foundBatch.getTitle());

        batchService.remove(6);
        exception.expect(DAOException.class);
        batchService.getById(6);
    }

    @Test
    public void shouldFindById() throws DataException {
        String expected = "First batch";
        assertEquals(BATCH_NOT_FOUND, expected, batchService.findById(1).getTitle());
    }

    @Test
    public void shouldFindManyByTitle() throws DataException {
        assertEquals(BATCHES_NOT_FOUND, 3, batchService.findByTitle("batch", true).size());
    }

    @Test
    public void shouldFindOneByTitle() throws DataException {
        assertEquals(BATCH_NOT_FOUND, 1,
                batchService.findByTitle("First batch", true).size());
    }

    @Test
    public void shouldNotFindByType() throws DataException {
        assertEquals("Batch was found in index!", 0, batchService.findByTitle("noBatch", true).size());
    }

    @Test
    public void shouldFindManyByProcessId() throws DataException {
        assertEquals(BATCHES_NOT_FOUND, 2, batchService.findByProcessId(1).size());
    }

    @Test
    public void shouldFindOneByProcessId() throws DataException {
        assertEquals(BATCH_NOT_FOUND, 1, batchService.findByProcessId(2).size());
    }

    @Test
    public void shouldNotFindByProcessId() throws DataException {
        assertEquals("Some batches were found in index!", 0, batchService.findByProcessId(3).size());
    }

    @Test
    public void shouldFindManyByProcessTitle() throws DataException {
        assertEquals(BATCHES_NOT_FOUND, 2,
                batchService.findByProcessTitle("First process").size());
    }

    @Test
    public void shouldFindOneByProcessTitle() throws DataException {
        assertEquals(BATCH_NOT_FOUND, 1,
                batchService.findByProcessTitle("Second process").size());
    }

    @Test
    public void shouldNotFindByProcessTitle() throws DataException {
        assertEquals("Some batches were found in index!", 0,
                batchService.findByProcessTitle("DBConnectionTest").size());
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
    public void shouldCreateLabel() throws Exception {
        Batch batch = batchService.getById(1);
        String label = batchService.createLabel(batch);
        assertEquals("Created label is incorrect!", "First batch (1 processes)", label);
    }
}
