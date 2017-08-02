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

import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Docket;

/**
 * Tests for DocketService class.
 */
public class DocketServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
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
    public void shouldFindDocket() throws Exception {
        DocketService docketService = new DocketService();

        Docket docket = docketService.find(1);
        boolean condition = docket.getTitle().equals("default") && docket.getFile().equals("docket.xsl");
        assertTrue("Docket was not found in database!", condition);
    }

    @Test
    public void shouldFindAllDockets() {
        DocketService docketService = new DocketService();

        List<Docket> dockets = docketService.findAll();
        assertEquals("Not all dockets were found in database!", 2, dockets.size());
    }

    @Test
    public void shouldFindById() throws Exception {
        DocketService docketService = new DocketService();

        JSONObject docket = docketService.findById(1);
        JSONObject jsonObject = (JSONObject) docket.get("_source");
        String actual = (String) jsonObject.get("title");
        String expected = "default";
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        DocketService docketService = new DocketService();

        List<JSONObject> dockets = docketService.findByTitle("default", true);
        Integer actual = dockets.size();
        Integer expected = 1;
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByFile() throws Exception {
        DocketService docketService = new DocketService();

        JSONObject docket = docketService.findByFile("docket.xsl");
        JSONObject jsonObject = (JSONObject) docket.get("_source");
        String actual = (String) jsonObject.get("file");
        String expected = "docket.xsl";
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndFile() throws Exception {
        DocketService docketService = new DocketService();

        JSONObject docket = docketService.findByTitleAndFile("default", "docket.xsl");
        Integer actual = docketService.getIdFromJSONObject(docket);
        Integer expected = 1;
        assertEquals("Docket was not found in index!", expected, actual);

        docket = docketService.findByTitleAndFile("default", "none");
        actual = docketService.getIdFromJSONObject(docket);
        expected = 0;
        assertEquals("Docket was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleOrFile() throws Exception {
        DocketService docketService = new DocketService();

        List<JSONObject> docket = docketService.findByTitleOrFile("default", "docket.xsl");
        Integer actual = docket.size();
        Integer expected = 2;
        assertEquals("Dockets were not found in index!", expected, actual);

        docket = docketService.findByTitleOrFile("default", "none");
        actual = docket.size();
        expected = 1;
        assertEquals("Docket was not found in index!", expected, actual);

        docket = docketService.findByTitleOrFile("none", "none");
        actual = docket.size();
        expected = 0;
        assertEquals("Some dockets were found in index!", expected, actual);
    }

    @Test
    public void shouldFindAllDocketsDocuments() throws Exception {
        DocketService docketService = new DocketService();

        List<JSONObject> dockets = docketService.findAllDocuments();
        assertEquals("Not all dockets were found in index!", 2, dockets.size());
    }

    @Test
    public void shouldRemoveDocket() throws Exception {
        DocketService docketService = new DocketService();

        Docket docket = new Docket();
        docket.setTitle("To Remove");
        docketService.save(docket);
        Docket foundDocket = docketService.convertJSONObjectToObject(docketService.findById(3));
        assertEquals("Additional docket was not inserted in database!", "To Remove", foundDocket.getTitle());

        docketService.remove(foundDocket);
        foundDocket = docketService.convertJSONObjectToObject(docketService.findById(3));
        assertEquals("Additional docket was not removed from database!", null, foundDocket);

        docket = new Docket();
        docket.setTitle("To remove");
        docketService.save(docket);
        foundDocket = docketService.convertJSONObjectToObject(docketService.findById(4));
        assertEquals("Additional docket was not inserted in database!", "To remove", foundDocket.getTitle());

        docketService.remove(4);
        foundDocket = docketService.convertJSONObjectToObject(docketService.findById(4));
        assertEquals("Additional docket was not removed from database!", null, foundDocket);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertJSONObjectsToObjectList() throws Exception {
        DocketService docketService = new DocketService();

        List<JSONObject> searchResults = docketService.findAllDocuments();
        List<Docket> dockets = (List<Docket>) docketService.convertJSONObjectsToObjectList(searchResults, "Docket");
        assertEquals("Not all dockets were converted!", 2, dockets.size());
    }
}
