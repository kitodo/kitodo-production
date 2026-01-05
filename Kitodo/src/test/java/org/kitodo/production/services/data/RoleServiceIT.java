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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for RoleService class.
 */
public class RoleServiceIT {

    private static final RoleService roleService = ServiceManager.getRoleService();

    private static final int EXPECTED_ROLES_COUNT = 10;

    private static final String WRONG_NUMBER_OF_ROLES = "Amount of roles assigned to client is incorrect!";

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
    public void shouldCountAllDatabaseRowsForRoles() throws Exception {
        Long amount = roleService.count();
        assertEquals(Long.valueOf(EXPECTED_ROLES_COUNT), amount, "Roles were not counted correctly!");
    }

    @Test
    public void shouldGetRole() throws Exception {
        Role role = roleService.getById(1);
        assertEquals("Admin", role.getTitle(), "Role title is not matching");
        assertEquals("viewClient_globalAssignable", role.getAuthorities().get(1).getTitle(), "Role first authorities title is not matching");
    }

    @Test
    public void shouldGetAllRoles() throws Exception {
        List<Role> roles = roleService.getAll();
        assertEquals(EXPECTED_ROLES_COUNT, roles.size(), "Not all user's roles were found in database!");
    }

    @Test
    public void shouldGetAllRolesInGivenRange() throws Exception {
        List<Role> roles = roleService.getAll(1, 10);
        assertEquals(9, roles.size(), "Not all user's roles were found in database!");
    }

    @Test
    public void shouldRemoveRole() throws Exception {
        Role role = new Role();
        role.setTitle("To Remove");
        roleService.save(role);
        Role foundRole = roleService.getByQuery("FROM Role WHERE title = 'To Remove'").getFirst();
        assertEquals("To Remove", foundRole.getTitle(), "Additional user group was not inserted in database!");

        roleService.remove(foundRole);
        Role finalFoundRole = foundRole;
        assertThrows(DAOException.class, () -> roleService.getById(finalFoundRole.getId()));

        role = new Role();
        role.setTitle("To remove");
        roleService.save(role);
        foundRole = roleService.getByQuery("FROM Role WHERE title = 'To remove'").getFirst();
        assertEquals("To remove", foundRole.getTitle(), "Additional user group was not inserted in database!");

        roleService.remove(foundRole.getId());
    }

    @Test
    public void shouldRemoveRoleButNotUser() throws Exception {
        UserService userService = ServiceManager.getUserService();

        User user = new User();
        user.setLogin("Cascados");
        userService.save(user);

        Role role = new Role();
        role.setTitle("Cascados Group");
        role.getUsers().add(userService.getByQuery("FROM User WHERE login = 'Cascados' ORDER BY id DESC").getFirst());
        roleService.save(role);

        Role foundRole = roleService.getByQuery("FROM Role WHERE title = 'Cascados Group'").getFirst();
        assertEquals("Cascados Group", foundRole.getTitle(), "Additional user was not inserted in database!");

        roleService.remove(foundRole);
        int size = roleService.getByQuery("FROM Role WHERE title = 'Cascados Group'").size();
        assertEquals(0, size, "Additional user was not removed from database!");

        size = userService.getByQuery("FROM User WHERE login = 'Cascados'").size();
        assertEquals(1, size, "User was removed from database!");

        userService.remove(userService.getByQuery("FROM User WHERE login = 'Cascados'").getFirst());
    }

    @Test
    public void shouldGetAuthorizationsAsString() throws Exception {
        Role role = roleService.getById(1);
        int actual = roleService.getAuthorizationsAsString(role).size();
        int expected = 35;
        assertEquals(expected, actual, "Number of authority strings doesn't match!");
    }

    @Test
    public void shouldGetAuthorities() throws Exception {
        Role role = roleService.getById(1);
        List<Authority> actual = role.getAuthorities();
        assertEquals("viewClient_globalAssignable", actual.get(1).getTitle(), "Permission strings doesn't match to given plain text!");
    }

    @Test
    public void shouldSaveAndRemoveAuthorizationForRole() throws Exception {
        Role role = roleService.getById(1);
        List<Authority> authorities = role.getAuthorities();

        Authority authority = new Authority();
        authority.setTitle("newAuthorization");
        ServiceManager.getAuthorityService().save(authority);

        authorities.add(authority);

        role.setAuthorities(authorities);
        roleService.save(role);

        role = roleService.getById(1);

        List<String> actual = roleService.getAuthorizationsAsString(role);
        assertTrue(actual.contains(authority.getTitle()), "Title of Authority was not found in user group authorities!");
    }

    @Test
    public void shouldGetAllRolesByClientIds() {
        List<Role> roles = roleService.getAllRolesByClientId(1);
        assertEquals(8, roles.size(), WRONG_NUMBER_OF_ROLES);

        roles = roleService.getAllRolesByClientId(2);
        assertEquals(2, roles.size(), WRONG_NUMBER_OF_ROLES);
    }

    @Test
    public void shouldGetAllAvailableForAssignToUser() throws Exception {
        User user = ServiceManager.getUserService().getById(1);
        List<Role> roles = roleService.getAllAvailableForAssignToUser(user);
        assertEquals(4, roles.size(), WRONG_NUMBER_OF_ROLES);

        user = ServiceManager.getUserService().getById(2);
        roles = roleService.getAllAvailableForAssignToUser(user);
        assertEquals(7, roles.size(), WRONG_NUMBER_OF_ROLES);
    }
}
