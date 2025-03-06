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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class AuthorityServiceIT {

    private static final AuthorityService authorityService = ServiceManager.getAuthorityService();
    private static final int EXPECTED_AUTHORITIES_COUNT = 100;

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertRolesFull();
        MockDatabase.setUpAwaitility();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllDatabaseRowsForAuthorities() throws Exception {
        Long amount = authorityService.count();
        assertEquals(Long.valueOf(EXPECTED_AUTHORITIES_COUNT), amount, "Authorizations were not counted correctly!");
    }

    @Test
    public void shouldGetAllAuthorities() throws Exception {
        List<Authority> authorities = authorityService.getAll();
        assertEquals(EXPECTED_AUTHORITIES_COUNT, authorities.size(), "Authorizations were not found database!");
    }

    @Test
    public void shouldGetByTitle() throws Exception {
        Authority authority = authorityService.getByTitle("viewAllRoles_globalAssignable");
        assertEquals(13, authority.getId().intValue(), "Authorizations were not found database!");
    }

    @Test
    public void shouldNotGetByTitle() {
        Exception exception = assertThrows(DAOException.class,
            () -> authorityService.getByTitle("viewAllStuff_globalAssignable"));

        assertEquals("Authority 'viewAllStuff_globalAssignable' cannot be found in database", exception.getMessage());
    }

    @Test
    public void shouldNotSaveAlreadyExistingAuthorities() {
        Authority adminAuthority = new Authority();
        adminAuthority.setTitle("viewAllClients_globalAssignable");
        assertThrows(DAOException.class,
            () -> authorityService.save(adminAuthority));
    }

    @Test
    public void shouldGetAllClientAssignableAuthorities() throws DAOException {
        List<Authority> authorities = authorityService.getAllAssignableToClients();
        assertEquals(72, authorities.size(), "Client assignable authorities were not found database!");
    }
}
