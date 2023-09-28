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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for RoleService class.
 */
public class RoleServiceIT {

    private static final RoleService roleService = ServiceManager.getRoleService();

    private static final int EXPECTED_ROLES_COUNT = 8;

    private static final String WRONG_NUMBER_OF_ROLES = "Amount of roles assigned to client is incorrect!";

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
    public void shouldCountAllDatabaseRowsForRoles() throws Exception {
        Long amount = roleService.countDatabaseRows();
        assertEquals("Roles were not counted correctly!", Long.valueOf(EXPECTED_ROLES_COUNT), amount);
    }

    @Test
    public void shouldGetRole() throws Exception {
        Role role = roleService.getById(1);
        assertEquals("Role title is not matching", "Admin", role.getTitle());
        assertEquals("Role first authorities title is not matching", "viewClient_globalAssignable",
            role.getAuthorities().get(1).getTitle());
    }

    @Test
    public void shouldGetAllRoles() throws Exception {
        List<Role> roles = roleService.getAll();
        assertEquals("Not all user's roles were found in database!", EXPECTED_ROLES_COUNT, roles.size());
    }

    @Test
    public void shouldGetAllRolesInGivenRange() throws Exception {
        List<Role> roles = roleService.getAll(1, 10);
        assertEquals("Not all user's roles were found in database!", 7, roles.size());
    }

    @Test
    public void shouldRemoveRole() throws Exception {
        Role role = new Role();
        role.setTitle("To Remove");
        roleService.saveToDatabase(role);
        Role foundRole = roleService.getByQuery("FROM Role WHERE title = 'To Remove'").get(0);
        assertEquals("Additional user group was not inserted in database!", "To Remove", foundRole.getTitle());

        roleService.removeFromDatabase(foundRole);
        exception.expect(DAOException.class);
        exception.expectMessage("");
        roleService.getById(foundRole.getId());

        role = new Role();
        role.setTitle("To remove");
        roleService.saveToDatabase(role);
        foundRole = roleService.getByQuery("FROM Role WHERE title = 'To remove'").get(0);
        assertEquals("Additional user group was not inserted in database!", "To remove", foundRole.getTitle());

        roleService.removeFromDatabase(foundRole.getId());
        exception.expect(DAOException.class);
        exception.expectMessage("");
    }

    @Test
    public void shouldRemoveRoleButNotUser() throws Exception {
        UserService userService = ServiceManager.getUserService();

        User user = new User();
        user.setLogin("Cascados");
        userService.saveToDatabase(user);

        Role role = new Role();
        role.setTitle("Cascados Group");
        role.getUsers().add(userService.getByQuery("FROM User WHERE login = 'Cascados' ORDER BY id DESC").get(0));
        roleService.saveToDatabase(role);

        Role foundRole = roleService.getByQuery("FROM Role WHERE title = 'Cascados Group'").get(0);
        assertEquals("Additional user was not inserted in database!", "Cascados Group", foundRole.getTitle());

        roleService.removeFromDatabase(foundRole);
        int size = roleService.getByQuery("FROM Role WHERE title = 'Cascados Group'").size();
        assertEquals("Additional user was not removed from database!", 0, size);

        size = userService.getByQuery("FROM User WHERE login = 'Cascados'").size();
        assertEquals("User was removed from database!", 1, size);

        userService.removeFromDatabase(userService.getByQuery("FROM User WHERE login = 'Cascados'").get(0));
    }

    @Test
    public void shouldGetAuthorizationsAsString() throws Exception {
        Role role = roleService.getById(1);
        int actual = roleService.getAuthorizationsAsString(role).size();
        int expected = 34;
        assertEquals("Number of authority strings doesn't match!", expected, actual);
    }

    @Test
    public void shouldGetAuthorities() throws Exception {
        Role role = roleService.getById(1);
        List<Authority> actual = role.getAuthorities();
        assertEquals("Permission strings doesn't match to given plain text!", "viewClient_globalAssignable",
            actual.get(1).getTitle());
    }

    @Test
    public void shouldSaveAndRemoveAuthorizationForRole() throws Exception {
        Role role = roleService.getById(1);
        List<Authority> authorities = role.getAuthorities();

        Authority authority = new Authority();
        authority.setTitle("newAuthorization");
        ServiceManager.getAuthorityService().saveToDatabase(authority);

        authorities.add(authority);

        role.setAuthorities(authorities);
        roleService.saveToDatabase(role);

        role = roleService.getById(1);

        List<String> actual = roleService.getAuthorizationsAsString(role);
        assertTrue("Title of Authority was not found in user group authorities!",
            actual.contains(authority.getTitle()));
    }

    @Test
    public void shouldGetAllRolesByClientIds() {
        List<Role> roles = roleService.getAllRolesByClientId(1);
        assertEquals(WRONG_NUMBER_OF_ROLES, 7, roles.size());

        roles = roleService.getAllRolesByClientId(2);
        assertEquals(WRONG_NUMBER_OF_ROLES, 1, roles.size());
    }

    @Test
    public void shouldGetAllAvailableForAssignToUser() throws Exception {
        User user = ServiceManager.getUserService().getById(1);
        List<Role> roles = roleService.getAllAvailableForAssignToUser(user);
        assertEquals(WRONG_NUMBER_OF_ROLES, 3, roles.size());

        user = ServiceManager.getUserService().getById(2);
        roles = roleService.getAllAvailableForAssignToUser(user);
        assertEquals(WRONG_NUMBER_OF_ROLES, 6, roles.size());
    }
}
