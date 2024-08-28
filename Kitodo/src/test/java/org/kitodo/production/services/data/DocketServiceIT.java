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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
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


    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        MockDatabase.insertDockets();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(new User(), 1);
        given().ignoreExceptions().await().until(() -> Objects.nonNull(docketService.getByTitle(defaultDocket)));
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllDockets() throws DataException {
        assertEquals("Dockets were not counted correctly!", Long.valueOf(4), docketService.count());
    }

    @Test
    public void shouldCountAllDatabaseRowsForDockets() throws Exception {
        Long amount = docketService.countDatabaseRows();
        assertEquals("Dockets were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldFindDocket() throws Exception {
        Docket docket = docketService.getById(1);
        boolean condition = docket.getTitle().equals(defaultDocket) && docket.getFile().equals(fileName);
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
    public void shouldFindById() throws DataException {
        String expected = defaultDocket;
        assertEquals(docketNotFound, expected, docketService.findById(1).getTitle());
    }

    @Test
    public void shouldFindByTitle() throws DataException {
        assertEquals(docketNotFound, 1, docketService.getByTitle(defaultDocket).size());
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

        docketService.remove(foundDocket);
        exception.expect(DAOException.class);
        docketService.getById(6);
    }
}
