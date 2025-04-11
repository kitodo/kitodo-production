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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        given().ignoreExceptions().await().until(() -> Objects.nonNull(batchService.getById(1)));
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllBatches() throws DAOException {
        assertEquals(Long.valueOf(4), batchService.count(), "Batches were not counted correctly!");
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldCountAllBatchesAccordingToQuery() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldCountAllDatabaseRowsForBatches() throws Exception {
        Long amount = batchService.count();
        assertEquals(Long.valueOf(4), amount, "Batches were not counted correctly!");
    }

    @Test
    public void shouldGetBatch() throws Exception {
        Batch batch = batchService.getById(1);
        boolean condition = batch.getTitle().equals("First batch");
        assertTrue(condition, "Batch was not found in database!");

        assertEquals(1, batch.getProcesses().size(), "Batch was found but processes were not inserted!");
    }

    @Test
    public void shouldFindAllBatches() throws Exception {
        List<Batch> batches = batchService.getAll();
        assertEquals(4, batches.size(), "Not all batches were found in database!");
    }

    @Test
    public void shouldGetAllBatchesInGivenRange() throws Exception {
        List<Batch> batches = batchService.getAll(2, 10);
        assertEquals(2, batches.size(), "Not all batches were found in database!");
    }

    @Test
    public void shouldRemoveBatch() throws Exception {
        Batch batch = new Batch();
        batch.setTitle("To Remove");
        batchService.save(batch);
        Batch foundBatch = batchService.getById(5);
        assertEquals("To Remove", foundBatch.getTitle(), "Additional batch was not inserted in database!");

        batchService.remove(foundBatch);
        assertThrows(DAOException.class, () -> batchService.getById(5));

        batch = new Batch();
        batch.setTitle("To remove");
        batchService.save(batch);
        foundBatch = batchService.getById(6);
        assertEquals("To remove", foundBatch.getTitle(), "Additional batch was not inserted in database!");

        batchService.remove(foundBatch);
        assertThrows(DAOException.class, () -> batchService.getById(6));
    }

    @Test
    public void shouldFindById() throws DAOException {
        String expected = "First batch";
        assertEquals(expected, batchService.getById(1).getTitle(), BATCH_NOT_FOUND);
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindManyByTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindOneByTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByType() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindManyByProcessId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindOneByProcessId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByProcessId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindManyByProcessTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindOneByProcessTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByProcessTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldContainCharSequence() throws Exception {
        Batch batch = batchService.getById(1);
        boolean condition = batch.getTitle().contains("bat") == batchService.contains(batch, "bat");
        assertTrue(condition, "It doesn't contain given char sequence!");
    }

    @Test
    public void shouldGetIdString() throws Exception {
        Batch batch = batchService.getById(1);
        boolean condition = batchService.getIdString(batch).equals("1");
        assertTrue(condition, "Id's String doesn't match the given plain text!");
    }

    @Test
    public void shouldGetLabel() throws Exception {
        Batch firstBatch = batchService.getById(1);
        boolean firstCondition = batchService.getLabel(firstBatch).equals("First batch");
        assertTrue(firstCondition, "It doesn't get given label!");

        Batch secondBatch = batchService.getById(4);
        boolean secondCondition = batchService.getLabel(secondBatch).equals("Batch 4");
        assertTrue(secondCondition, "It doesn't get given label!");
    }

    @Test
    public void shouldGetSizeOfProcesses() throws Exception {
        Batch batch = batchService.getById(1);
        int size = batchService.size(batch);
        assertEquals(1, size, "Size of processes is not equal 1!");
    }

    @Test
    public void shouldCreateLabel() throws Exception {
        Batch batch = batchService.getById(1);
        String label = batchService.createLabel(batch);
        assertEquals("First batch (1 processes)", label, "Created label is incorrect!");
    }
}
