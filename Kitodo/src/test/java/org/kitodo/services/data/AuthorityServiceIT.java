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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

public class AuthorityServiceIT {

    private static final AuthorityService authorityService = new ServiceManager().getAuthorityService();
    private final int EXPECTED_AUTHORITIES_COUNT = 109;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertUserGroupsFull();
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
    public void shouldCountAllAuthorities() {
        await().untilAsserted(() -> assertEquals("Authorizations were not counted correctly!",
            Long.valueOf(EXPECTED_AUTHORITIES_COUNT), authorityService.count()));
    }

    @Test
    public void shouldCountAllDatabaseRowsForAuthorities() throws Exception {
        Long amount = authorityService.countDatabaseRows();
        assertEquals("Authorizations were not counted correctly!", Long.valueOf(EXPECTED_AUTHORITIES_COUNT), amount);
    }

    @Test
    public void shouldFindAllAuthorities() {
        await().untilAsserted(() -> assertEquals("Not all authorizations were found in database!",
            EXPECTED_AUTHORITIES_COUNT, authorityService.findAll().size()));
    }

    @Test
    public void shouldFindById() {
        String expected = "viewAllClients_globalAssignable";
        await().untilAsserted(
            () -> assertEquals("Authority was not found in index!", expected, authorityService.findById(2).getTitle()));
    }

    @Test
    public void shouldFindByTitle() {
        int expected = 1;
        await().untilAsserted(() -> assertEquals("Authority was not found in index!", expected,
            authorityService.findByTitle("viewAllUserGroups_globalAssignable", true).size()));
    }

    @Test
    public void shouldNotFindByTitle() {
        int expected = 0;
        await().untilAsserted(() -> assertEquals("Authority was found in index!", expected,
            authorityService.findByTitle("none", true).size()));
    }

    @Test
    public void shouldGetAllAuthorities() throws Exception {
        List<Authority> authorities = authorityService.getAll();
        assertEquals("Authorizations were not found database!", EXPECTED_AUTHORITIES_COUNT, authorities.size());
    }

    @Test
    public void shouldNotSaveAlreadyExistingAuthorities() throws DataException {
        Authority adminAuthority = new Authority();
        adminAuthority.setTitle("viewAllClients_globalAssignable");
        exception.expect(DataException.class);
        authorityService.save(adminAuthority);
    }

    @Test
    public void shouldGetAllClientAssignableAuthorities() throws DAOException {
        List<Authority> authorities = authorityService.getAllAssignableToClients();
        assertEquals("Client assignable authorities were not found database!", 45, authorities.size());
    }

    @Test
    public void shouldGetAllProjectAssignableAuthorities() throws DAOException {
        List<Authority> authorities = authorityService.getAllAssignableToProjects();
        assertEquals("Project assignable authorities were not found database!", 20, authorities.size());
    }
}
