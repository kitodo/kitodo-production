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

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.exceptions.DAOException;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for TaskService class.
 */
public class HistoryServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        //MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindHistory() throws Exception {
        HistoryService historyService = new HistoryService();

        History history = historyService.find(1);
        boolean condition = history.getNumericValue().equals(2.0) && history.getStringValue().equals("History");
        assertTrue("History was not found in database!", condition);
    }

    @Test
    public void shouldFindAllHistories() throws Exception {
        HistoryService historyService = new HistoryService();

        List<History> histories = historyService.findAll();
        assertEquals("Not all histories were found in database!", 1, histories.size());
    }
}
