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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;
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
        given().ignoreExceptions().await().until(() -> Objects.nonNull(batchService.getById(1)));
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllBatches() throws DAOException {
        assertEquals("Batches were not counted correctly!", Long.valueOf(4), batchService.count());
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldCountAllBatchesAccordingToQuery() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldCountAllDatabaseRowsForBatches() throws Exception {
        Long amount = batchService.count();
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

        batchService.remove(foundBatch);
        exception.expect(DAOException.class);
        batchService.getById(6);
    }

    @Test
    public void shouldFindById() throws DAOException {
        String expected = "First batch";
        assertEquals(BATCH_NOT_FOUND, expected, batchService.getById(1).getTitle());
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldFindManyByTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldFindOneByTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldNotFindByType() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldFindManyByProcessId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldFindOneByProcessId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldNotFindByProcessId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldFindManyByProcessTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldFindOneByProcessTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Ignore("functionality nowhere used, no longer implemented")
    public void shouldNotFindByProcessTitle() throws Exception {
        // TODO delete test stub
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
