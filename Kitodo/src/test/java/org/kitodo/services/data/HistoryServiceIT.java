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

import org.joda.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.exceptions.DataException;

/**
 * Tests for TaskService class.
 */
public class HistoryServiceIT {

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
    public void shouldFindHistory() throws Exception {
        HistoryService historyService = new HistoryService();

        History history = historyService.find(1);
        boolean condition = history.getNumericValue().equals(2.0) && history.getStringValue().equals("History");
        assertTrue("History was not found in database!", condition);
    }

    @Test
    public void shouldFindAllHistories() {
        HistoryService historyService = new HistoryService();

        List<History> histories = historyService.findAll();
        assertEquals("Not all histories were found in database!", 1, histories.size());
    }

    @Test
    public void shouldRemoveHistory() throws Exception {
        HistoryService historyService = new HistoryService();

        History history = new History();
        history.setStringValue("To Remove");
        historyService.save(history);
        History foundHistory = historyService.convertSearchResultToObject(historyService.findById(2));
        assertEquals("Additional history was not inserted in database!", "To Remove", foundHistory.getStringValue());

        historyService.remove(foundHistory);
        foundHistory = historyService.convertSearchResultToObject(historyService.findById(2));
        assertEquals("Additional history was not removed from database!", null, foundHistory);

        history = new History();
        history.setStringValue("To remove");
        historyService.save(history);
        foundHistory = historyService.convertSearchResultToObject(historyService.findById(3));
        assertEquals("Additional history was not inserted in database!", "To remove", foundHistory.getStringValue());

        historyService.remove(3);
        foundHistory = historyService.convertSearchResultToObject(historyService.findById(3));
        assertEquals("Additional history was not removed from database!", null, foundHistory);
    }

    @Test
    public void shouldFindById() throws Exception {
        HistoryService historyService = new HistoryService();

        SearchResult history = historyService.findById(1);
        String actual = (String) history.getProperties().get("stringValue");
        String expected = "History";
        assertEquals("History was not found in index!", expected, actual);

        history = historyService.findById(2);
        Integer actualInt = history.getId();
        Integer expectedInt = null;
        assertEquals("History was not found in index!", expectedInt, actualInt);
    }

    @Test
    public void shouldFindByNumericValue() throws Exception {
        HistoryService historyService = new HistoryService();

        List<SearchResult> history = historyService.findByNumericValue(2.0);
        Integer actual = history.size();
        Integer expected = 1;
        assertEquals("History was not found in index!", expected, actual);

        history = historyService.findByNumericValue(3.0);
        actual = history.size();
        expected = 0;
        assertEquals("History was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByStringValue() throws Exception {
        HistoryService historyService = new HistoryService();

        List<SearchResult> history = historyService.findByStringValue("History");
        String actual = (String) history.get(0).getProperties().get("stringValue");
        String expected = "History";
        assertEquals("History was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByType() throws Exception {
        HistoryService historyService = new HistoryService();

        List<SearchResult> history = historyService.findByType(HistoryTypeEnum.color);
        Integer actual = history.size();
        Integer expected = 1;
        assertEquals("History was not found in index!", expected, actual);

        history = historyService.findByType(HistoryTypeEnum.bitonal);
        actual = history.size();
        expected = 0;
        assertEquals("History was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByDate() throws Exception {
        HistoryService historyService = new HistoryService();

        LocalDate localDate = new LocalDate(2017, 1, 14);
        List<SearchResult> history = historyService.findByDate(localDate.toDate());
        Integer actual = history.size();
        Integer expected = 1;
        assertEquals("History was not found in index!", expected, actual);

        localDate = new LocalDate(2017, 2, 14);
        history = historyService.findByDate(localDate.toDate());
        actual = history.size();
        expected = 0;
        assertEquals("Some histories were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessId() throws Exception {
        HistoryService historyService = new HistoryService();

        SearchResult history = historyService.findByProcessId(1);
        Integer actual = history.getId();
        Integer expected = 1;
        assertEquals("History was not found in index!", expected, actual);

        history = historyService.findByProcessId(2);
        actual = history.getId();
        expected = null;
        assertEquals("History was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessTitle() throws Exception {
        HistoryService historyService = new HistoryService();

        List<SearchResult> history = historyService.findByProcessTitle("First process");
        Integer actual = history.size();
        Integer expected = 1;
        assertEquals("History was not found in index!", expected, actual);

        history = historyService.findByProcessTitle("Some process");
        actual = history.size();
        expected = 0;
        assertEquals("History was found in index!", expected, actual);
    }

    @Test
    public void shouldFindAllHistoryDocuments() throws Exception {
        HistoryService historyService = new HistoryService();

        List<SearchResult> histories = historyService.findAllDocuments();
        assertEquals("Not all histories were found in index!", 1, histories.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertSearchResultsToObjectList() throws Exception {
        HistoryService historyService = new HistoryService();

        List<SearchResult> searchResults = historyService.findAllDocuments();
        List<History> historys = (List<History>) historyService.convertSearchResultsToObjectList(searchResults,
                "History");
        assertEquals("Not all histories were converted!", 1, historys.size());
    }
}
