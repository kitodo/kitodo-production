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

package org.kitodo.services;

import de.sub.goobi.config.ConfigMain;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.kitodo.data.database.beans.User;

import static org.junit.Assert.*;

/**
 * Tests for UserService class.
 */
public class UserServiceTest {

    @Test
    public void shouldFindUser() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = user.getName().equals("Jan") && user.getSurname().equals("Kowalski");
        assertTrue("User was not found in database!", condition);
    }

    @Test
    public void shouldFindAllUsers() throws Exception {
        UserService userService = new UserService();

        List<User> users = userService.findAll();
        assertEquals("Not all users were found in database!", 3, users.size());
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

    @Ignore("problem with lazy fetching")
    @Test
    public void shouldGetTasksSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        User currentUser = userService.getCurrent(user);
        int actual = userService.getTasksSize(currentUser);
        assertEquals("Tasks' size is incorrect!", 1, actual);
    }

    @Ignore("problem with lazy fetching")
    @Test
    public void shouldGetProcessingTasksSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getProcessingTasksSize(user);
        assertEquals("Processing tasks' size is incorrect!", 1, actual);
    }

    @Ignore("problem with lazy fetching")
    @Test
    public void shouldGetProjectsSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getProjectsSize(user);
        assertEquals("Projects' size is incorrect!", 1, actual);
    }

    @Ignore("problem with lazy fetching")
    @Test
    public void shouldGetPropertiesSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        int actual = userService.getPropertiesSize(user);
        assertEquals("Properties' size is incorrect!", 1, actual);
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

    @Test
    public void shouldGetHomeDirectory() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        String homeDirectory = ConfigMain.getParameter("dir_Users");
        boolean condition = userService.getHomeDirectory(user).equals(homeDirectory + "kowal" + File.separator);
        System.out.println("1. Home directory: " + user.getLogin() +userService.getHomeDirectory(user));
        assertTrue("Home directory of user is incorrect!", condition);

        //probably here home directory should look differently (depending on  LDAP group)
        // but not sure how to test because it depends on config.properties ldap_use
        user = userService.find(2);
        condition = userService.getHomeDirectory(user).contains("nowak");
        System.out.println("2. Home directory: " + user.getLogin() + userService.getHomeDirectory(user));
        assertTrue("Home directory of user is incorrect!", condition);
    }
}