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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * Tests for UserGroupService class.
 */
public class UserGroupServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabaseAndIndex() {
        // MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllUserGroups() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        Long amount = userGroupService.count();
        assertEquals("User groups were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForUserGroups() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        Long amount = userGroupService.countDatabaseRows();
        assertEquals("User groups were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldFindUserGroup() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        boolean condition = userGroup.getTitle().equals("Admin") && userGroup.getPermission().equals(1);
        assertTrue("User group was not found in database!", condition);
    }

    @Test
    public void shouldRemoveUserGroup() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("To Remove");
        userGroupService.save(userGroup);
        Thread.sleep(1000);
        UserGroup foundUserGroup = userGroupService
                .convertJSONObjectToBean(userGroupService.findByTitle("To Remove", true).get(0));
        assertEquals("Additional user group was not inserted in database!", "To Remove", foundUserGroup.getTitle());

        userGroupService.remove(foundUserGroup);
        foundUserGroup = userGroupService
                .convertJSONObjectToBean(userGroupService.findById(foundUserGroup.getId()));
        assertEquals("Additional user group was not removed from database!", null, foundUserGroup);

        userGroup = new UserGroup();
        userGroup.setTitle("To remove");
        userGroupService.save(userGroup);
        Thread.sleep(1000);
        foundUserGroup = userGroupService
                .convertJSONObjectToBean(userGroupService.findByTitle("To remove", true).get(0));
        assertEquals("Additional user group was not inserted in database!", "To remove", foundUserGroup.getTitle());

        userGroupService.remove(foundUserGroup.getId());
        exception.expect(DAOException.class);
        userGroupService.convertJSONObjectToBean(userGroupService.findByTitle("To remove", true).get(0));
    }

    @Test
    public void shouldRemoveUserGroupButNotUser() throws Exception {
        UserService userService = new UserService();
        UserGroupService userGroupService = new UserGroupService();

        User user = new User();
        user.setLogin("Cascados");
        userService.saveToDatabase(user);

        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("Cascados Group");
        userGroup.getUsers().add(userService.search("FROM User WHERE login = 'Cascados' ORDER BY id DESC").get(0));
        userGroupService.saveToDatabase(userGroup);

        UserGroup foundUserGroup = userGroupService.search("FROM UserGroup WHERE title = 'Cascados Group'").get(0);
        assertEquals("Additional user was not inserted in database!", "Cascados Group", foundUserGroup.getTitle());

        userGroupService.removeFromDatabase(foundUserGroup);
        int size = userGroupService.search("FROM UserGroup WHERE title = 'Cascados Group'").size();
        assertEquals("Additional user was not removed from database!", 0, size);

        size = userService.search("FROM User WHERE login = 'Cascados'").size();
        assertEquals("User was removed from database!", 1, size);

        userService.removeFromDatabase(userService.search("FROM User WHERE login = 'Cascados'").get(0));
    }

    @Test
    public void shouldFindById() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        JSONObject userGroup = userGroupService.findById(1);
        JSONObject jsonObject = (JSONObject) userGroup.get("_source");
        String actual = (String) jsonObject.get("title");
        String expected = "Admin";
        assertEquals("User group was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        List<JSONObject> userGroups = userGroupService.findByTitle("Admin", true);
        Integer actual = userGroups.size();
        Integer expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByTitle("none", true);
        actual = userGroups.size();
        expected = 0;
        assertEquals("User group was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByPermission() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        List<JSONObject> userGroups = userGroupService.findByPermission(1);
        Integer actual = userGroups.size();
        Integer expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByPermission(4);
        actual = userGroups.size();
        expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByPermission(5);
        actual = userGroups.size();
        expected = 0;
        assertEquals("User group was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByUserId() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        List<JSONObject> userGroups = userGroupService.findByUserId(1);
        Integer actual = userGroups.size();
        Integer expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByUserId(3);
        actual = userGroups.size();
        expected = 0;
        assertEquals("User groups were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByUserLogin() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        List<JSONObject> userGroups = userGroupService.findByUserLogin("kowal");
        Integer actual = userGroups.size();
        Integer expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByUserLogin("dora");
        actual = userGroups.size();
        expected = 0;
        assertEquals("User groups were found in index!", expected, actual);
    }

    @Test
    public void shouldGetPermissionAsString() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        String actual = userGroupService.getPermissionAsString(userGroup);
        assertEquals("Permission string doesn't match to given plain text!", "1", actual);

        userGroup = userGroupService.find(3);
        actual = userGroupService.getPermissionAsString(userGroup);
        assertEquals("Permission string doesn't match to given plain text!", "4", actual);
    }

    @Test
    public void shouldGetTasksSize() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        int actual = userGroupService.getTasksSize(userGroup);
        assertEquals("Tasks size is not equal to given value!", 2, actual);
    }
}
