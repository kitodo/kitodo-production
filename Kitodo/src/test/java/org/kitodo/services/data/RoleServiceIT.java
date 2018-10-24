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
import static org.junit.Assert.assertTrue;

import java.util.Collections;
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
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.AuthorityDTO;
import org.kitodo.dto.RoleDTO;
import org.kitodo.services.ServiceManager;

/**
 * Tests for RoleService class.
 */
public class RoleServiceIT {

    private static final RoleService roleService = new ServiceManager().getRoleService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
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
    public void shouldCountAllRoles() {
        await().untilAsserted(
            () -> assertEquals("Roles were not counted correctly!", Long.valueOf(4), roleService.count()));
    }

    @Test
    public void shouldCountAllDatabaseRowsForRoles() throws Exception {
        Long amount = roleService.countDatabaseRows();
        assertEquals("Roles were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldFindAllRoles() {
        await().untilAsserted(
            () -> assertEquals("Not all user's roles were found in database!", 4, roleService.findAll().size()));
    }

    @Test
    public void shouldGetRole() throws Exception {
        Role role = roleService.getById(1);
        assertEquals("Role title is not matching", "Admin", role.getTitle());
        assertEquals("Role first authorities title is not matching", "viewAllClients_globalAssignable",
            role.getAuthorities().get(1).getTitle());
    }

    @Test
    public void shouldGetAllRolesInGivenRange() throws Exception {
        List<Role> roles = roleService.getAll(1, 10);
        assertEquals("Not all user's roles were found in database!", 3, roles.size());
    }

    @Test
    public void shouldRemoveRole() throws Exception {
        Role role = new Role();
        role.setTitle("To Remove");
        roleService.save(role);
        Role foundRole = roleService.convertJSONObjectToBean(roleService.findByTitle("To Remove", true).get(0));
        assertEquals("Additional user group was not inserted in database!", "To Remove", foundRole.getTitle());

        roleService.remove(foundRole);
        exception.expect(DAOException.class);
        exception.expectMessage("");
        roleService.getById(foundRole.getId());

        role = new Role();
        role.setTitle("To remove");
        roleService.save(role);
        foundRole = roleService.convertJSONObjectToBean(roleService.findByTitle("To remove", true).get(0));
        assertEquals("Additional user group was not inserted in database!", "To remove", foundRole.getTitle());

        roleService.remove(foundRole.getId());
        exception.expect(DAOException.class);
        exception.expectMessage("");
        roleService.convertJSONObjectToBean(roleService.findByTitle("To remove", true).get(0));
    }

    @Test
    public void shouldRemoveRoleButNotUser() throws Exception {
        UserService userService = new ServiceManager().getUserService();

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
    public void shouldFindById() {
        String expected = "Admin";
        await().untilAsserted(
            () -> assertEquals("Role was not found in index!", expected, roleService.findById(1).getTitle()));

        Integer expectedInt = 2;
        await().untilAsserted(
            () -> assertEquals("Role was not found in index!", expectedInt, roleService.findById(1).getUsersSize()));
    }

    @Test
    public void shouldFindByTitle() {
        await().untilAsserted(
            () -> assertEquals("Role was not found in index!", 1, roleService.findByTitle("Admin", true).size()));
    }

    @Test
    public void shouldNotFindByTitle() {
        await().untilAsserted(
            () -> assertEquals("Role was found in index!", 0, roleService.findByTitle("none", true).size()));
    }

    @Test
    public void shouldFindManyByAuthorization() {
        await().untilAsserted(() -> assertEquals("Role was not found in index!", 2,
            roleService.findByAuthorizationTitle("viewAllClients_globalAssignable").size()));
    }

    @Test
    public void shouldFindOneByAuthorization() {
        await().untilAsserted(() -> assertEquals("Role was not found in index!", 1,
            roleService.findByAuthorizationTitle("viewAllUsers_globalAssignable").size()));
    }

    @Test
    public void shouldNotFindByAuthorization() {
        await().untilAsserted(() -> assertEquals("Role was found in index!", 0,
            roleService.findByAuthorizationTitle("notExisting").size()));
    }

    @Test
    public void shouldFindByUserId() {
        await().untilAsserted(
            () -> assertEquals("Role was not found in index!", 1, roleService.findByUserId(1).size()));
    }

    @Test
    public void shouldNotFindByUserId() {
        await().untilAsserted(() -> assertEquals("Roles were found in index!", 0, roleService.findByUserId(5).size()));
    }

    @Test
    public void shouldFindByUserLogin() {
        await().untilAsserted(
            () -> assertEquals("Role was not found in index!", 1, roleService.findByUserLogin("kowal").size()));
    }

    @Test
    public void shouldNotFindByUserLogin() {
        await().untilAsserted(
            () -> assertEquals("Roles were found in index!", 0, roleService.findByUserLogin("user").size()));
    }

    @Test
    public void shouldGetAuthorizationsAsString() throws Exception {
        Role role = roleService.getById(1);
        int actual = roleService.getAuthorizationsAsString(role).size();
        int expected = 109;
        assertEquals("Number of authority strings doesn't match!", expected, actual);
    }

    @Test
    public void shouldGetAuthorities() throws Exception {
        Role role = roleService.getById(1);
        List<Authority> actual = role.getAuthorities();
        assertEquals("Permission strings doesn't match to given plain text!", "viewAllClients_globalAssignable",
            actual.get(1).getTitle());
    }

    @Test
    public void shouldNotSaveRoleWithAlreadyExistingTitle() throws DataException {
        Role role = new Role();
        role.setTitle("Admin");
        exception.expect(DataException.class);
        roleService.save(role);
    }

    @Test
    public void shouldGetAuthorityForAdmin() throws Exception {
        await().untilAsserted(() -> assertEquals("Incorrect amount of found roles", 1,
            roleService.convertJSONObjectsToDTOs(roleService.findByTitle("Admin", true), true).size()));

        List<RoleDTO> roleDTOS = roleService.convertJSONObjectsToDTOs(roleService.findByTitle("Admin", true), true);
        AuthorityDTO authorityDTO = roleDTOS.get(0).getAuthorities().get(0);
        assertEquals("Incorrect authority!", "admin_globalAssignable", authorityDTO.getTitle());
    }

    @Test
    public void shouldSaveAndRemoveAuthorizationForRole() throws Exception {
        Role role = roleService.getById(1);
        List<Authority> authorities = role.getAuthorities();

        Authority authority = new Authority();
        authority.setTitle("newAuthorization");
        ServiceManager serviceManager = new ServiceManager();
        serviceManager.getAuthorityService().save(authority);

        authorities.add(authority);

        role.setAuthorities(authorities);
        roleService.save(role);

        role = roleService.getById(1);

        List<String> actual = roleService.getAuthorizationsAsString(role);
        assertTrue("Title of Authority was not found in user group authorities!",
            actual.contains(authority.getTitle()));
    }

    @Test
    public void shouldGetAllRolesByClientIds() {
        List<Integer> clientIds = Collections.singletonList(1);
        List<Role> roles = roleService.getAllRolesByClientIds(clientIds);
        assertEquals("Amount of roles assigned to client is incorrect!", 2, roles.size());

        clientIds = Collections.singletonList(2);
        roles = roleService.getAllRolesByClientIds(clientIds);
        assertEquals("Amount of roles assigned to client is incorrect!", 1, roles.size());
    }
}
