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

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.search.SearchResult;

/**
 * Tests for DocketService class.
 */
public class DocketServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException, ResponseException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindDocket() throws Exception {
        DocketService docketService = new DocketService();

        Docket docket = docketService.find(1);
        boolean condition = docket.getTitle().equals("default") && docket.getFile().equals("docket.xsl");
        assertTrue("Docket was not found in database!", condition);
    }

    @Test
    public void shouldFindAllDockets() throws Exception {
        DocketService docketService = new DocketService();

        List<Docket> dockets = docketService.findAll();
        assertEquals("Not all dockets were found in database!", 2, dockets.size());
    }

    @Test
    public void shouldFindById() throws Exception {
        DocketService docketService = new DocketService();

        SearchResult docket = docketService.findById(1);
        String actual = docket.getProperties().get("title");
        String expected = "default";
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        DocketService docketService = new DocketService();

        SearchResult docket = docketService.findByTitle("default", true);
        String actual = docket.getProperties().get("title");
        String expected = "default";
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByFile() throws Exception {
        DocketService docketService = new DocketService();

        SearchResult docket = docketService.findByFile("docket.xsl");
        String actual = docket.getProperties().get("file");
        String expected = "docket.xsl";
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndFile() throws Exception {
        DocketService docketService = new DocketService();

        SearchResult docket = docketService.findByTitleAndFile("default","docket.xsl");
        Integer actual = docket.getId();
        Integer expected = 1;
        assertEquals("Docket was not found in index!", expected, actual);

        docket = docketService.findByTitleAndFile("default","none");
        actual = docket.getId();
        expected = null;
        assertEquals("Docket was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleOrFile() throws Exception {
        DocketService docketService = new DocketService();

        List<SearchResult> docket = docketService.findByTitleOrFile("default","docket.xsl");
        Integer actual = docket.size();
        Integer expected = 2;
        assertEquals("Dockets were not found in index!", expected, actual);

        docket = docketService.findByTitleOrFile("default","none");
        actual = docket.size();
        expected = 1;
        assertEquals("Docket was not found in index!", expected, actual);

        docket = docketService.findByTitleOrFile("none","none");
        actual = docket.size();
        expected = 0;
        assertEquals("Some dockets were found in index!", expected, actual);
    }

    @Test
    public void shouldFindAllDocketsDocuments() throws Exception {
        DocketService docketService = new DocketService();

        List<SearchResult> dockets = docketService.findAllDocuments();
        assertEquals("Not all dockets were found in index!", 2, dockets.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertSearchResultsToObjectList() throws Exception {
        DocketService docketService = new DocketService();

        List<SearchResult> searchResults = docketService.findAllDocuments();
        List<Docket> dockets = (List<Docket>) docketService.convertSearchResultsToObjectList(searchResults, "Docket");
        assertEquals("Not all dockets were converted!", 2, dockets.size());
    }
}
