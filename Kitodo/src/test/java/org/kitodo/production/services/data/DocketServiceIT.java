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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.index.type.enums.DocketTypeField;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for DocketService class.
 */
public class DocketServiceIT {

    private static final DocketService docketService = ServiceManager.getDocketService();

    private static final String defaultDocket = "default";
    private static final String docketNotFound = "Docket was not found in index!";
    private static final String fileName = "docket.xsl";
    private static final String none = "none";


    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        MockDatabase.insertDockets();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(new User(), 1);
        given().ignoreExceptions().await().until(() -> Objects.nonNull(docketService.findByTitle(defaultDocket, true)));
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllDockets() throws DataException {
        assertEquals(Long.valueOf(4), docketService.count(), "Dockets were not counted correctly!");
    }

    @Test
    public void shouldCountAllDocketsAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("title", defaultDocket).operator(Operator.AND);
        assertEquals(Long.valueOf(1), docketService.count(query), "Dockets were not counted correctly!");
    }

    @Test
    public void shouldCountAllDatabaseRowsForDockets() throws Exception {
        Long amount = docketService.countDatabaseRows();
        assertEquals(Long.valueOf(4), amount, "Dockets were not counted correctly!");
    }

    @Test
    public void shouldFindDocket() throws Exception {
        Docket docket = docketService.getById(1);
        boolean condition = docket.getTitle().equals(defaultDocket) && docket.getFile().equals(fileName);
        assertTrue(condition, "Docket was not found in database!");
    }

    @Test
    public void shouldFindAllDockets() throws Exception {
        List<Docket> dockets = docketService.getAll();
        assertEquals(4, dockets.size(), "Not all dockets were found in database!");
    }

    @Test
    public void shouldGetAllDocketsInGivenRange() throws Exception {
        List<Docket> dockets = docketService.getAll(1, 10);
        assertEquals(3, dockets.size(), "Not all dockets were found in database!");
    }

    @Test
    public void shouldFindById() throws DataException {
        String expected = defaultDocket;
        assertEquals(expected, docketService.findById(1).getTitle(), docketNotFound);
    }

    @Test
    public void shouldFindByTitle() throws DataException {
        assertEquals(1, docketService.findByTitle(defaultDocket, true).size(), docketNotFound);
    }

    @Test
    public void shouldFindByFile() throws DataException {
        String expected = fileName;
        assertEquals(expected, docketService.findByFile(fileName).get(DocketTypeField.FILE.getKey()), docketNotFound);
    }

    @Test
    public void shouldFindManyByClientId() throws DataException {
        assertEquals(3, docketService.findByClientId(1).size(), "Dockets were not found in index!");
    }

    @Test
    public void shouldNotFindByClientId() throws DataException {
        assertEquals(0, docketService.findByClientId(3).size(), "Docket was found in index!");
    }

    @Test
    public void shouldFindByTitleAndFile() throws DataException {
        Integer expected = 1;
        assertEquals(expected, docketService.getIdFromJSONObject(docketService.findByTitleAndFile(defaultDocket, fileName)), docketNotFound);
    }

    @Test
    public void shouldNotFindByTitleAndFile() throws DataException {
        Integer expected = 0;
        assertEquals(expected, docketService.getIdFromJSONObject(docketService.findByTitleAndFile(defaultDocket, none)), "Docket was found in index!");
    }

    @Test
    public void shouldFindManyByTitleOrFile() throws DataException {
        assertEquals(2, docketService.findByTitleOrFile(defaultDocket, fileName).size(), "Dockets were not found in index!");
    }

    @Test
    public void shouldFindOneByTitleOrFile() throws DataException {
        assertEquals(1, docketService.findByTitleOrFile(defaultDocket, none).size(), docketNotFound);
    }

    @Test
    public void shouldNotFindByTitleOrFile() throws DataException {
        assertEquals(0, docketService.findByTitleOrFile(none, none).size(), "Some dockets were found in index!");
    }

    @Test
    public void shouldFindAllDocketsDocuments() throws DataException {
        assertEquals(4, docketService.findAllDocuments().size(), "Not all dockets were found in index!");
    }

    @Test
    public void shouldRemoveDocket() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("To Remove");
        docketService.save(docket);
        Docket foundDocket = docketService.getById(5);
        assertEquals("To Remove", foundDocket.getTitle(), "Additional docket was not inserted in database!");

        docketService.remove(foundDocket);
        assertThrows(DAOException.class, () -> docketService.getById(5));

        docket = new Docket();
        docket.setTitle("To remove");
        docketService.save(docket);
        foundDocket = docketService.getById(6);
        assertEquals("To remove", foundDocket.getTitle(), "Additional docket was not inserted in database!");

        docketService.remove(6);
        assertThrows(DAOException.class, () -> docketService.getById(6));
    }
}
