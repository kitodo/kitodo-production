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

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.exceptions.DataException;

/**
 * Tests for UserService class.
 */
public class UserServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, DataException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void shouldFindUser() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = user.getName().equals("Jan") && user.getSurname().equals("Kowalski");
        assertTrue("User was not found in database!", condition);
    }

    @Test
    public void shouldFindAllUsers() {
        UserService userService = new UserService();

        List<User> users = userService.findAll();
        boolean result = users.size() == 3 || users.size() == 4 || users.size() == 5 || users.size() == 6;
        assertTrue("Not all users were found in database!", result);
    }

    @Test
    public void shouldRemoveUser() throws Exception {
        UserService userService = new UserService();

        User user = new User();
        user.setLogin("Remove");
        userService.save(user);
        Thread.sleep(1000);
        User foundUser = userService.convertSearchResultToObject(userService.findByLogin("Remove"));
        assertEquals("Additional user was not inserted in database!", "Remove", foundUser.getLogin());

        userService.remove(foundUser);
        foundUser = userService.find(foundUser.getId());
        assertEquals("Additional user was not removed from database!", null, foundUser.getLogin());

        user = new User();
        user.setLogin("remove");
        userService.save(user);
        Thread.sleep(1000);
        foundUser = userService.convertSearchResultToObject(userService.findByLogin("remove"));
        assertEquals("Additional user was not inserted in database!", "remove", foundUser.getLogin());

        userService.remove(foundUser.getId());
        foundUser = userService.find(foundUser.getId());
        assertEquals("Additional user was not removed from database!", null, foundUser.getLogin());
    }

    @Test
    public void shouldRemoveUserButNotUserGroup() throws Exception {
        UserService userService = new UserService();
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("Cascade Group");
        userGroupService.saveToDatabase(userGroup);

        User user = new User();
        user.setLogin("Cascade");
        user.getUserGroups()
                .add(userGroupService.search("FROM UserGroup WHERE title = 'Cascade Group' ORDER BY id DESC").get(0));
        userService.saveToDatabase(user);
        User foundUser = userService.search("FROM User WHERE login = 'Cascade'").get(0);
        assertEquals("Additional user was not inserted in database!", "Cascade", foundUser.getLogin());

        userService.removeFromDatabase(foundUser);
        int size = userService.search("FROM User WHERE login = 'Cascade'").size();
        assertEquals("Additional user was not removed from database!", 0, size);

        size = userGroupService.search("FROM UserGroup WHERE title = 'Cascade Group'").size();
        assertEquals("User Group was removed from database!", 1, size);

        userGroupService
                .removeFromDatabase(userGroupService.search("FROM UserGroup WHERE title = 'Cascade Group'").get(0));
    }

    @Test
    public void shouldFindById() throws Exception {
        UserService userService = new UserService();

        SearchResult user = userService.findById(1);
        String actual = user.getProperties().get("login");
        String expected = "kowal";
        assertEquals("User was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByName() throws Exception {
        UserService userService = new UserService();

        List<SearchResult> users = userService.findByName("Jan");
        Integer actual = users.size();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findByName("Jannik");
        actual = users.size();
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindBySurname() throws Exception {
        UserService userService = new UserService();

        List<SearchResult> users = userService.findBySurname("Kowalski");
        Integer actual = users.size();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findBySurname("Müller");
        actual = users.size();
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByFullName() throws Exception {
        UserService userService = new UserService();

        List<SearchResult> users = userService.findByFullName("Jan", "Kowalski");
        Integer actual = users.size();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findByFullName("Jannik", "Müller");
        actual = users.size();
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByLogin() throws Exception {
        UserService userService = new UserService();

        SearchResult user = userService.findByLogin("kowal");
        Integer actual = user.getId();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        user = userService.findByLogin("random");
        actual = user.getId();
        expected = null;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByLdapLogin() throws Exception {
        UserService userService = new UserService();

        SearchResult user = userService.findByLdapLogin("kowalLDP");
        Integer actual = user.getId();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        user = userService.findByLdapLogin("random");
        actual = user.getId();
        expected = null;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByLocation() throws Exception {
        UserService userService = new UserService();

        List<SearchResult> users = userService.findByLocation("Dresden");
        Integer actual = users.size();
        Integer expected = 2;
        assertEquals("Users were not found in index!", expected, actual);

        users = userService.findByLocation("Leipzig");
        actual = users.size();
        expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findByLocation("Wroclaw");
        actual = users.size();
        expected = 0;
        assertEquals("Users were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByActive() throws Exception {
        UserService userService = new UserService();

        List<SearchResult> users = userService.findByActive(true);
        boolean result = users.size() == 2 || users.size() == 3 || users.size() == 4 || users.size() == 5;
        assertTrue("Users were not found in index!", result);

        users = userService.findByActive(false);
        result = users.size() == 1 || users.size() == 2 || users.size() == 3 || users.size() == 4;
        assertTrue("Users were found in index!", result);
    }

    @Test
    public void shouldFindByUserGroupId() throws Exception {
        UserService userService = new UserService();

        List<SearchResult> users = userService.findByUserGroupId(1);
        Integer actual = users.size();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findByUserGroupId(3);
        actual = users.size();
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByUserGroupTitle() throws Exception {
        UserService userService = new UserService();

        List<SearchResult> users = userService.findByUserGroupTitle("Admin");
        Integer actual = users.size();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findByUserGroupTitle("None");
        actual = users.size();
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProperty() throws Exception {
        UserService userService = new UserService();

        List<SearchResult> users = userService.findByProperty("FirstUserProperty", "first value");
        Integer actual = users.size();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findByProperty("firstTemplate title", "first value");
        actual = users.size();
        expected = 0;
        assertEquals("User was not found in index!", expected, actual);
    }

    @Test
    public void shouldGetTableSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getTableSize(user);
        assertEquals("Table size is incorrect!", 20, actual);

        user = userService.find(2);
        actual = userService.getTableSize(user);
        assertEquals("Table size is incorrect!", 10, actual);
    }

    @Test
    public void shouldGetSessionTimeout() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getSessionTimeout(user);
        assertEquals("Session timeout is incorrect!", 7200, actual);

        user = userService.find(2);
        actual = userService.getSessionTimeout(user);
        assertEquals("Session timeout is incorrect!", 9000, actual);
    }

    @Test
    public void shouldGetSessionTimeoutInMinutes() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getSessionTimeoutInMinutes(user);
        assertEquals("Session timeout in minutes is incorrect!", 120, actual);
    }

    @Test
    public void shouldGetCss() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getCss(user).equals("/css/fancy.css");
        assertTrue("Css file is incorrect!", condition);

        user = userService.find(2);
        condition = userService.getCss(user).equals("/css/default.css");
        assertTrue("Css file is incorrect!", condition);
    }

    @Test
    public void shouldGetUserGroupSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getUserGroupSize(user);
        assertEquals("User groups' size is incorrect!", 1, actual);
    }

    @Test
    public void shouldGetTasksSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(2);
        int actual = userService.getTasksSize(user);
        assertEquals("Tasks' size is incorrect!", 2, actual);

        user = userService.find(3);
        actual = userService.getTasksSize(user);
        assertEquals("Tasks' size is incorrect!", 1, actual);
    }

    @Test
    public void shouldGetProcessingTasksSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getProcessingTasksSize(user);
        assertEquals("Processing tasks' size is incorrect!", 1, actual);
    }

    @Test
    public void shouldGetProjectsSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getProjectsSize(user);
        assertEquals("Projects' size is incorrect!", 2, actual);

        user = userService.find(2);
        actual = userService.getProjectsSize(user);
        assertEquals("Projects' size is incorrect!", 1, actual);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getPropertiesSize(user);
        assertEquals("Properties' size is incorrect!", 2, actual);
    }

    @Ignore("not sure how method works")
    @Test
    public void shouldCheckIfIsPasswordCorrect() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.isPasswordCorrect(user, "test");
        assertTrue("User's password is incorrect!", condition);
    }

    @Test
    public void shouldGetFullName() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getFullName(user).equals("Kowalski, Jan");
        assertTrue("Full name of user is incorrect!", condition);
    }

    @Ignore
    @Test
    public void shouldGetHomeDirectory() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        String homeDirectory = ConfigCore.getParameter("dir_Users");
        boolean condition = userService.getHomeDirectory(user).equals(homeDirectory + "kowal" + File.separator);
        System.out.println("1. Home directory: " + user.getLogin() + userService.getHomeDirectory(user));
        assertTrue("Home directory of user is incorrect!", condition);

        // probably here home directory should look differently (depending on
        // LDAP group)
        // but not sure how to test because it depends on config.properties
        // ldap_use
        user = userService.find(2);
        condition = userService.getHomeDirectory(user).toString().contains("nowak");
        System.out.println("2. Home directory: " + user.getLogin() + userService.getHomeDirectory(user));
        assertTrue("Home directory of user is incorrect!", condition);
    }
}
