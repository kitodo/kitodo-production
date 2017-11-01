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

import org.elasticsearch.index.query.Operator;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

/**
 * Tests for TaskService class.
 */
public class HistoryServiceIT {

    private static final HistoryService historyService = new ServiceManager().getHistoryService();

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
    public void shouldCountAllHistories() throws Exception {
        Long amount = historyService.count();
        assertEquals("Histories were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllPropertiesAccordingToQuery() throws Exception {
        String query = matchQuery("stringValue", "History").operator(Operator.AND).toString();
        Long amount = historyService.count(query);
        assertEquals("Histories were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForProperties() throws Exception {
        Long amount = historyService.countDatabaseRows();
        assertEquals("Histories were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldFindHistory() throws Exception {
        History history = historyService.getById(1);
        boolean condition = history.getNumericValue().equals(2.0) && history.getStringValue().equals("History");
        assertTrue("History was not found in database!", condition);
    }

    @Test
    public void shouldFindAllHistories() {
        List<History> histories = historyService.getAll();
        assertEquals("Not all histories were found in database!", 1, histories.size());
    }

    @Test
    public void shouldGetAllHistoriesInGivenRange() throws Exception {
        List<History> histories = historyService.getAll(1,10);
        assertEquals("Not all histories were found in database!", 0, histories.size());
    }

    @Test
    public void shouldRemoveHistory() throws Exception {
        History history = new History();
        history.setStringValue("To Remove");
        historyService.save(history);
        History foundHistory = historyService.getById(2);
        assertEquals("Additional history was not inserted in database!", "To Remove", foundHistory.getStringValue());

        historyService.remove(foundHistory);
        exception.expect(DAOException.class);
        historyService.getById(2);

        history = new History();
        history.setStringValue("To remove");
        historyService.save(history);
        foundHistory = historyService.getById(3);
        assertEquals("Additional history was not inserted in database!", "To remove", foundHistory.getStringValue());

        historyService.remove(3);
        exception.expect(DAOException.class);
        historyService.getById(3);
    }

    @Test
    public void shouldFindById() throws Exception {
        String actual = historyService.findById(1).getStringValue();
        String expected = "History";
        assertEquals("History was not found in index!", expected, actual);

        Integer actualInt = historyService.findById(2).getId();
        Integer expectedInt = 0;
        assertEquals("History was not found in index!", expectedInt, actualInt);
    }

    @Test
    public void shouldFindAllHistoryDocuments() throws Exception {
        List<JSONObject> histories = historyService.findAllDocuments();
        assertEquals("Not all histories were found in index!", 1, histories.size());
    }
}
