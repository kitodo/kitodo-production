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
 * Tests for UserGroupService class.
 */
public class UserGroupServiceIT {

    private static final RoleService userGroupService = new ServiceManager().getUserGroupService();

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

    @org.junit.Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllUserGroups() {
        await().untilAsserted(
            () -> assertEquals("User groups were not counted correctly!", Long.valueOf(4), userGroupService.count()));
    }

    @Test
    public void shouldCountAllDatabaseRowsForUserGroups() throws Exception {
        Long amount = userGroupService.countDatabaseRows();
        assertEquals("User groups were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldFindAllUserGroups() {
        await().untilAsserted(
            () -> assertEquals("Not all user's groups were found in database!", 4, userGroupService.findAll().size()));
    }

    @Test
    public void shouldGetUserGroup() throws Exception {
        Role userGroup = userGroupService.getById(1);
        assertEquals("User group title is not matching", "Admin", userGroup.getTitle());
        assertEquals("User group first authorities title is not matching", "viewAllClients_globalAssignable",
            userGroup.getAuthorities().get(1).getTitle());
    }

    @Test
    public void shouldGetAllUserGroupsInGivenRange() throws Exception {
        List<Role> userGroups = userGroupService.getAll(1, 10);
        assertEquals("Not all user's groups were found in database!", 3, userGroups.size());
    }

    @Test
    public void shouldRemoveUserGroup() throws Exception {
        Role userGroup = new Role();
        userGroup.setTitle("To Remove");
        userGroupService.save(userGroup);
        Role foundUserGroup = userGroupService
                .convertJSONObjectToBean(userGroupService.findByTitle("To Remove", true).get(0));
        assertEquals("Additional user group was not inserted in database!", "To Remove", foundUserGroup.getTitle());

        userGroupService.remove(foundUserGroup);
        exception.expect(DAOException.class);
        userGroupService.getById(foundUserGroup.getId());

        userGroup = new Role();
        userGroup.setTitle("To remove");
        userGroupService.save(userGroup);
        foundUserGroup = userGroupService
                .convertJSONObjectToBean(userGroupService.findByTitle("To remove", true).get(0));
        assertEquals("Additional user group was not inserted in database!", "To remove", foundUserGroup.getTitle());

        userGroupService.remove(foundUserGroup.getId());
        exception.expect(DAOException.class);
        userGroupService.convertJSONObjectToBean(userGroupService.findByTitle("To remove", true).get(0));
    }

    @Test
    public void shouldRemoveUserGroupButNotUser() throws Exception {
        UserService userService = new ServiceManager().getUserService();

        User user = new User();
        user.setLogin("Cascados");
        userService.saveToDatabase(user);

        Role userGroup = new Role();
        userGroup.setTitle("Cascados Group");
        userGroup.getUsers().add(userService.getByQuery("FROM User WHERE login = 'Cascados' ORDER BY id DESC").get(0));
        userGroupService.saveToDatabase(userGroup);

        Role foundUserGroup = userGroupService.getByQuery("FROM UserGroup WHERE title = 'Cascados Group'").get(0);
        assertEquals("Additional user was not inserted in database!", "Cascados Group", foundUserGroup.getTitle());

        userGroupService.removeFromDatabase(foundUserGroup);
        int size = userGroupService.getByQuery("FROM UserGroup WHERE title = 'Cascados Group'").size();
        assertEquals("Additional user was not removed from database!", 0, size);

        size = userService.getByQuery("FROM User WHERE login = 'Cascados'").size();
        assertEquals("User was removed from database!", 1, size);

        userService.removeFromDatabase(userService.getByQuery("FROM User WHERE login = 'Cascados'").get(0));
    }

    @Test
    public void shouldFindById() {
        String expected = "Admin";
        await().untilAsserted(() -> assertEquals("User group was not found in index!", expected,
            userGroupService.findById(1).getTitle()));

        Integer expectedInt = 2;
        await().untilAsserted(() -> assertEquals("User group was not found in index!", expectedInt,
            userGroupService.findById(1).getUsersSize()));
    }

    @Test
    public void shouldFindByTitle() {
        await().untilAsserted(() -> assertEquals("User group was not found in index!", 1,
            userGroupService.findByTitle("Admin", true).size()));
    }

    @Test
    public void shouldNotFindByTitle() {
        await().untilAsserted(
            () -> assertEquals("User group was found in index!", 0, userGroupService.findByTitle("none", true).size()));
    }

    @Test
    public void shouldFindManyByAuthorization() {
        await().untilAsserted(() -> assertEquals("User group was not found in index!", 2,
            userGroupService.findByAuthorizationTitle("viewAllClients_globalAssignable").size()));
    }

    @Test
    public void shouldFindOneByAuthorization() {
        await().untilAsserted(() -> assertEquals("User group was not found in index!", 1,
            userGroupService.findByAuthorizationTitle("viewAllUsers_globalAssignable").size()));
    }

    @Test
    public void shouldNotFindByAuthorization() {
        await().untilAsserted(() -> assertEquals("User group was found in index!", 0,
            userGroupService.findByAuthorizationTitle("notExisting").size()));
    }

    @Test
    public void shouldFindByUserId() {
        await().untilAsserted(
            () -> assertEquals("User group was not found in index!", 1, userGroupService.findByUserId(1).size()));
    }

    @Test
    public void shouldNotFindByUserId() {
        await().untilAsserted(
            () -> assertEquals("User groups were found in index!", 0, userGroupService.findByUserId(5).size()));
    }

    @Test
    public void shouldFindByUserLogin() {
        await().untilAsserted(() -> assertEquals("User group was not found in index!", 1,
            userGroupService.findByUserLogin("kowal").size()));
    }

    @Test
    public void shouldNotFindByUserLogin() {
        await().untilAsserted(
            () -> assertEquals("User groups were found in index!", 0, userGroupService.findByUserLogin("user").size()));
    }

    @Test
    public void shouldGetAuthorizationsAsString() throws Exception {
        Role userGroup = userGroupService.getById(1);
        int actual = userGroupService.getAuthorizationsAsString(userGroup).size();
        int expected = 109;
        assertEquals("Number of authority strings doesn't match!", expected, actual);
    }

    @Test
    public void shouldGetAuthorities() throws Exception {
        Role userGroup = userGroupService.getById(1);
        List<Authority> actual = userGroup.getAuthorities();
        assertEquals("Permission strings doesn't match to given plain text!", "viewAllClients_globalAssignable",
            actual.get(1).getTitle());
    }

    @Test
    public void shouldNotSaveUsergroupWithAlreadyExistingTitle() throws DataException {
        Role userGroup = new Role();
        userGroup.setTitle("Admin");
        exception.expect(DataException.class);
        userGroupService.save(userGroup);
    }

    @Test
    public void shouldGetAuthorityForAdmin() throws Exception {
        await().untilAsserted(
                () -> assertEquals("Incorrect amount of found user groups", 1, userGroupService
                        .convertJSONObjectsToDTOs(userGroupService.findByTitle("Admin", true), true).size()));

        List<RoleDTO> userGroupDTOS = userGroupService
                .convertJSONObjectsToDTOs(userGroupService.findByTitle("Admin", true), true);
        AuthorityDTO authorityDTO = userGroupDTOS.get(0).getAuthorities().get(0);
        assertEquals("Incorrect authority!", "admin_globalAssignable", authorityDTO.getTitle());
    }

    @Test
    public void shouldSaveAndRemoveAuthorizationForUserGroup() throws Exception {
        Role userGroup = userGroupService.getById(1);
        List<Authority> authorities = userGroup.getAuthorities();

        Authority authority = new Authority();
        authority.setTitle("newAuthorization");
        ServiceManager serviceManager = new ServiceManager();
        serviceManager.getAuthorityService().save(authority);

        authorities.add(authority);

        userGroup.setAuthorities(authorities);
        userGroupService.save(userGroup);

        userGroup = userGroupService.getById(1);

        List<String> actual = userGroupService.getAuthorizationsAsString(userGroup);
        assertTrue("Title of Authority was not found in user group authorities!",
            actual.contains(authority.getTitle()));
    }

    @Test
    public void shouldGetAllUserGroupsByClientIds() {
        List<Integer> clientIds = Collections.singletonList(1);
        List<Role> userGroups = userGroupService.getAllUserGroupsByClientIds(clientIds);
        assertEquals("Amount of user groups assigned to client is incorrect!", 2, userGroups.size());

        clientIds = Collections.singletonList(2);
        userGroups = userGroupService.getAllUserGroupsByClientIds(clientIds);
        assertEquals("Amount of user groups assigned to client is incorrect!", 1, userGroups.size());
    }
}
