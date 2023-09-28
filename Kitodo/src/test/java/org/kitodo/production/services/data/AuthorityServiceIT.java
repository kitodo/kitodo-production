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
import org.kitodo.production.services.ServiceManager;

public class AuthorityServiceIT {

    private static final AuthorityService authorityService = ServiceManager.getAuthorityService();
    private static final int EXPECTED_AUTHORITIES_COUNT = 99;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertRolesFull();
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
    public void shouldCountAllDatabaseRowsForAuthorities() throws Exception {
        Long amount = authorityService.countDatabaseRows();
        assertEquals("Authorizations were not counted correctly!", Long.valueOf(EXPECTED_AUTHORITIES_COUNT), amount);
    }

    @Test
    public void shouldGetAllAuthorities() throws Exception {
        List<Authority> authorities = authorityService.getAll();
        assertEquals("Authorizations were not found database!", EXPECTED_AUTHORITIES_COUNT, authorities.size());
    }

    @Test
    public void shouldGetByTitle() throws Exception {
        Authority authority = authorityService.getByTitle("viewAllRoles_globalAssignable");
        assertEquals("Authorizations were not found database!", 13, authority.getId().intValue());
    }

    @Test
    public void shouldNotGetByTitle() throws Exception {
        exception.expect(DAOException.class);
        exception.expectMessage("Object cannot be found in database");
        authorityService.getByTitle("viewAllStuff_globalAssignable");
    }

    @Test
    public void shouldNotSaveAlreadyExistingAuthorities() throws Exception {
        Authority adminAuthority = new Authority();
        adminAuthority.setTitle("viewAllClients_globalAssignable");
        exception.expect(DAOException.class);
        authorityService.saveToDatabase(adminAuthority);
    }

    @Test
    public void shouldGetAllClientAssignableAuthorities() throws DAOException {
        List<Authority> authorities = authorityService.getAllAssignableToClients();
        assertEquals("Client assignable authorities were not found database!", 72, authorities.size());
    }
}
