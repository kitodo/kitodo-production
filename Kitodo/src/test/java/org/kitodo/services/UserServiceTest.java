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

import org.junit.BeforeClass;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for UserService class.
 */
public class UserServiceTest {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertUsers();
        MockDatabase.insertUserGroups();
        MockDatabase.insertBatches();
        MockDatabase.insertDockets();
        MockDatabase.insertProjects();
        MockDatabase.insertRulesets();
        MockDatabase.insertProcesses();
        MockDatabase.insertTasks();
    }

    @Test
    public void shouldGetTableSize() throws Exception {
        UserService userService = new UserService();

        User firstUser = userService.find(1);
        boolean firstCondition = userService.getTableSize(firstUser) == 20;
        assertTrue("Table size is incorrect!", firstCondition);

        User secondUser = userService.find(2);
        boolean secondCondition = userService.getTableSize(secondUser) == 10;
        assertTrue("Table size is incorrect!", secondCondition);
    }

    @Test
    public void shouldGetSessionTimeout() throws Exception {
        UserService userService = new UserService();

        User firstUser = userService.find(1);
        boolean firstCondition = userService.getSessionTimeout(firstUser) == 7200;
        assertTrue("Session timeout is incorrect!", firstCondition);

        User secondUser = userService.find(2);
        boolean secondCondition = userService.getSessionTimeout(secondUser) == 9000;
        assertTrue("Session timeout is incorrect!", secondCondition);
    }

    @Test
    public void shouldGetSessionTimeoutInMinutes() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getSessionTimeoutInMinutes(user) == 120;
        assertTrue("Session timeout in minutes is incorrect!", condition);
    }

    @Test
    public void shouldGetCss() throws Exception {
        UserService userService = new UserService();

        User firstUser = userService.find(1);
        boolean firstCondition = userService.getCss(firstUser).equals("/css/fancy.css");
        assertTrue("Css file is incorrect!", firstCondition);

        User secondUser = userService.find(2);
        boolean secondCondition = userService.getCss(secondUser).equals("/css/default.css");
        assertTrue("Css file is incorrect!", secondCondition);
    }

    @Test
    public void shouldGetUserGroupSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getUserGroupSize(user) == 1;
        assertTrue("User groups' size is incorrect!", condition);
    }

    @Test
    public void shouldGetTasksSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getTasksSize(user) == 1;
        System.out.println("User: " + user.getLogin());
        System.out.println("Tasks: " + userService.getTasksSize(user));
        assertTrue("Tasks' size is incorrect!", condition);
    }

    @Test
    public void shouldGetProcessingTasksSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getProcessingTasksSize(user) == 1;
        System.out.println("Processes: " + userService.getProcessingTasksSize(user));
        assertTrue("Processing tasks' size is incorrect!", condition);
    }

    @Test
    public void shouldGetProjectsSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getProjectsSize(user) == 1;
        System.out.println("Projects: " + userService.getProjectsSize(user));
        assertTrue("Projects' size is incorrect!", condition);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getPropertiesSize(user) == 1;
        System.out.println("Properties: " + userService.getPropertiesSize(user));
        assertTrue("Properties' size is incorrect!", condition);
    }

    @Test
    public void shouldGetFullName() throws Exception {
        UserService userService = new UserService();

        User user = userService.find(1);
        boolean condition = userService.getFullName(user).equals("Kowalski, Jan");
        assertTrue("Full name of user is incorrect!", condition);
    }
}