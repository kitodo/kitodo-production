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
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
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
        given().ignoreExceptions().await().until(() -> Objects.nonNull(docketService.getByTitle(defaultDocket)));
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllDockets() throws DAOException {
        assertEquals(Long.valueOf(4), docketService.count(), "Dockets were not counted correctly!");
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldCountAllDocketsAccordingToQuery() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldCountAllDatabaseRowsForDockets() throws Exception {
        Long amount = docketService.count();
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
    public void shouldFindById() throws DAOException {
        String expected = defaultDocket;
        assertEquals(expected, docketService.getById(1).getTitle(), docketNotFound);
    }

    @Test
    public void shouldFindByTitle() throws DAOException {
        assertEquals(1, docketService.getByTitle(defaultDocket).size(), docketNotFound);
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindByFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindManyByClientId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByClientId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindByTitleAndFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByTitleAndFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindManyByTitleOrFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindOneByTitleOrFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByTitleOrFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindAllDocketsDocuments() throws Exception {
        // TODO delete test stub
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

        docketService.remove(foundDocket);
        assertThrows(DAOException.class, () -> docketService.getById(6));
    }
}
