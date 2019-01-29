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

import static org.awaitility.Awaitility.await;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.index.type.enums.DocketTypeField;
import org.kitodo.production.MockDatabase;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for DocketService class.
 */
public class DocketServiceIT {

    private static final DocketService docketService = ServiceManager.getDocketService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        MockDatabase.insertDockets();
        MockDatabase.setUpAwaitility();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllDockets() {
        await().untilAsserted(
            () -> assertEquals("Dockets were not counted correctly!", Long.valueOf(4), docketService.count()));
    }

    @Test
    public void shouldCountAllDocketsAccordingToQuery() {
        String query = matchQuery("title", "default").operator(Operator.AND).toString();
        await().untilAsserted(
            () -> assertEquals("Dockets were not counted correctly!", Long.valueOf(1), docketService.count(query)));
    }

    @Test
    public void shouldCountAllDatabaseRowsForDockets() throws Exception {
        Long amount = docketService.countDatabaseRows();
        assertEquals("Dockets were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldFindDocket() throws Exception {
        Docket docket = docketService.getById(1);
        boolean condition = docket.getTitle().equals("default") && docket.getFile().equals("docket.xsl");
        assertTrue("Docket was not found in database!", condition);
    }

    @Test
    public void shouldFindAllDockets() throws Exception {
        List<Docket> dockets = docketService.getAll();
        assertEquals("Not all dockets were found in database!", 4, dockets.size());
    }

    @Test
    public void shouldGetAllDocketsInGivenRange() throws Exception {
        List<Docket> dockets = docketService.getAll(1, 10);
        assertEquals("Not all dockets were found in database!", 3, dockets.size());
    }

    @Test
    public void shouldFindById() {
        String expected = "default";
        await().untilAsserted(
            () -> assertEquals("Docket was not found in index!", expected, docketService.findById(1).getTitle()));
    }

    @Test
    public void shouldFindByTitle() {
        await().untilAsserted(
            () -> assertEquals("Docket was not found in index!", 1, docketService.findByTitle("default", true).size()));
    }

    @Test
    public void shouldFindByFile() {
        String expected = "docket.xsl";
        await().untilAsserted(() -> assertEquals("Docket was not found in index!", expected,
            docketService.findByFile("docket.xsl").getJsonObject("_source").getString(DocketTypeField.FILE.getKey())));
    }

    @Test
    public void shouldFindManyByClientId() {
        await().untilAsserted(
            () -> assertEquals("Dockets were not found in index!", 3, docketService.findByClientId(1).size()));
    }

    @Test
    public void shouldFindOneByClientId() {
        await().untilAsserted(
            () -> assertEquals("Docket was not found in index!", 1, docketService.findByClientId(2).size()));
    }

    @Test
    public void shouldNotFindByClientId() {
        await().untilAsserted(
            () -> assertEquals("Docket was found in index!", 0, docketService.findByClientId(3).size()));
    }

    @Test
    public void shouldFindByTitleAndFile() {
        Integer expected = 1;
        await().untilAsserted(() -> assertEquals("Docket was not found in index!", expected,
            docketService.getIdFromJSONObject(docketService.findByTitleAndFile("default", "docket.xsl"))));
    }

    @Test
    public void shouldNotFindByTitleAndFile() {
        Integer expected = 0;
        await().untilAsserted(() -> assertEquals("Docket was found in index!", expected,
            docketService.getIdFromJSONObject(docketService.findByTitleAndFile("default", "none"))));
    }

    @Test
    public void shouldFindManyByTitleOrFile() {
        await().untilAsserted(() -> assertEquals("Dockets were not found in index!", 2,
            docketService.findByTitleOrFile("default", "docket.xsl").size()));
    }

    @Test
    public void shouldFindOneByTitleOrFile() {
        await().untilAsserted(() -> assertEquals("Docket was not found in index!", 1,
            docketService.findByTitleOrFile("default", "none").size()));
    }

    @Test
    public void shouldNotFindByTitleOrFile() {
        await().untilAsserted(() -> assertEquals("Some dockets were found in index!", 0,
            docketService.findByTitleOrFile("none", "none").size()));
    }

    @Test
    public void shouldFindAllDocketsDocuments() {
        await().untilAsserted(
            () -> assertEquals("Not all dockets were found in index!", 4, docketService.findAllDocuments().size()));
    }

    @Test
    public void shouldRemoveDocket() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("To Remove");
        docketService.save(docket);
        Docket foundDocket = docketService.getById(5);
        assertEquals("Additional docket was not inserted in database!", "To Remove", foundDocket.getTitle());

        docketService.remove(foundDocket);
        exception.expect(DAOException.class);
        docketService.getById(5);

        docket = new Docket();
        docket.setTitle("To remove");
        docketService.save(docket);
        foundDocket = docketService.getById(6);
        assertEquals("Additional docket was not inserted in database!", "To remove", foundDocket.getTitle());

        docketService.remove(6);
        exception.expect(DAOException.class);
        docketService.getById(6);
    }
}
