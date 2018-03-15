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

import javax.json.JsonObject;

import org.elasticsearch.index.query.Operator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

/**
 * Tests for DocketService class.
 */
public class DocketServiceIT {

    private static final DocketService docketService = new ServiceManager().getDocketService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertDockets();
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
    public void shouldCountAllDockets() throws Exception {
        Long amount = docketService.count();
        assertEquals("Dockets were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldCountAllDocketsAccordingToQuery() throws Exception {
        String query = matchQuery("title", "default").operator(Operator.AND).toString();
        Long amount = docketService.count(query);
        assertEquals("Dockets were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForDockets() throws Exception {
        Long amount = docketService.countDatabaseRows();
        assertEquals("Dockets were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldFindDocket() throws Exception {
        Docket docket = docketService.getById(1);
        boolean condition = docket.getTitle().equals("default") && docket.getFile().equals("docket.xsl");
        assertTrue("Docket was not found in database!", condition);
    }

    @Test
    public void shouldFindAllDockets() {
        List<Docket> dockets = docketService.getAll();
        assertEquals("Not all dockets were found in database!", 2, dockets.size());
    }

    @Test
    public void shouldGetAllDocketsInGivenRange() throws Exception {
        List<Docket> dockets = docketService.getAll(1,10);
        assertEquals("Not all dockets were found in database!", 1, dockets.size());
    }

    @Test
    public void shouldFindById() throws Exception {
        String actual = docketService.findById(1).getTitle();
        String expected = "default";
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        List<JsonObject> dockets = docketService.findByTitle("default", true);
        Integer actual = dockets.size();
        Integer expected = 1;
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByFile() throws Exception {
        JsonObject docket = docketService.findByFile("docket.xsl");
        JsonObject jsonObject = docket.getJsonObject("_source");
        String actual = jsonObject.getString("file");
        String expected = "docket.xsl";
        assertEquals("Docket was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndFile() throws Exception {
        JsonObject docket = docketService.findByTitleAndFile("default", "docket.xsl");
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
        List<JsonObject> docket = docketService.findByTitleOrFile("default", "docket.xsl");
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
        List<JsonObject> dockets = docketService.findAllDocuments();
        assertEquals("Not all dockets were found in index!", 2, dockets.size());
    }

    @Test
    public void shouldRemoveDocket() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("To Remove");
        docketService.save(docket);
        Docket foundDocket = docketService.getById(3);
        assertEquals("Additional docket was not inserted in database!", "To Remove", foundDocket.getTitle());

        docketService.remove(foundDocket);
        exception.expect(DAOException.class);
        docketService.getById(3);

        docket = new Docket();
        docket.setTitle("To remove");
        docketService.save(docket);
        foundDocket = docketService.getById(4);
        assertEquals("Additional docket was not inserted in database!", "To remove", foundDocket.getTitle());

        docketService.remove(4);
        exception.expect(DAOException.class);
        docketService.getById(4);
    }
}
