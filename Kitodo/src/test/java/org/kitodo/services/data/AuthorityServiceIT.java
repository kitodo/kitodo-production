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

import java.util.List;

import javax.json.JsonObject;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.AuthorityDTO;
import org.kitodo.services.ServiceManager;

public class AuthorityServiceIT {

    private static final AuthorityService authorityService = new ServiceManager().getAuthorityService();
    private final int EXPECTED_AUTHORITIES_COUNT = 36;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertUserGroupsFull();
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
    public void shouldCountAllAuthorities() throws Exception {
        Long amount = authorityService.count();
        assertEquals("Authorizations were not counted correctly!", Long.valueOf(EXPECTED_AUTHORITIES_COUNT), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForAuthorities() throws Exception {
        Long amount = authorityService.countDatabaseRows();
        assertEquals("Authorizations were not counted correctly!", Long.valueOf(EXPECTED_AUTHORITIES_COUNT), amount);
    }

    @Test
    public void shouldFindAllAuthorities() throws Exception {
        List<AuthorityDTO> authorizations = authorityService.findAll();
        assertEquals("Not all authorizations were found in database!", EXPECTED_AUTHORITIES_COUNT,
            authorizations.size());
    }

    @Test
    public void shouldFindById() throws Exception {
        AuthorityDTO authority = authorityService.findById(2);
        String actual = authority.getTitle();
        String expected = "viewClient";
        assertEquals("Authority was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        List<JsonObject> authorities = authorityService.findByTitle("viewAllUserGroups", true);
        Integer actual = authorities.size();
        Integer expected = 1;
        assertEquals("Authority was not found in index!", expected, actual);

        authorities = authorityService.findByTitle("none", true);
        actual = authorities.size();
        expected = 0;
        assertEquals("Authority was found in index!", expected, actual);
    }

    @Test
    public void shouldGetAllAuthorities() {
        List<Authority> authorities = authorityService.getAll();
        assertEquals("Authorizations were not found databse!", EXPECTED_AUTHORITIES_COUNT, authorities.size());
    }

    @Test
    public void shouldNotSaveAlreadyExistinAuthorities() throws DataException {
        Authority adminAuthority = new Authority();
        adminAuthority.setTitle("viewClient");
        exception.expect(DataException.class);
        authorityService.save(adminAuthority);
    }

    @Test
    public void shouldGetAllClientAssignableAuthorities() {
        List<Authority> authorities = authorityService.getAllAssignableToClients();
        assertEquals("Client assignable authorities were not found databse!", 32, authorities.size());
    }

    @Test
    public void shouldGetAllProjectAssignableAuthorities() {
        List<Authority> authorities = authorityService.getAllAssignableToProjects();
        assertEquals("Project assignable authorities were not found databse!", 17, authorities.size());
    }
}
