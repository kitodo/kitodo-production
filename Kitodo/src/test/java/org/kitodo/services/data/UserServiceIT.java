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

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.json.JsonObject;

import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.UserDTO;
import org.kitodo.services.ServiceManager;

/**
 * Tests for UserService class.
 */
public class UserServiceIT {

    private static final Logger logger = LogManager.getLogger(UserServiceIT.class);
    private static final UserService userService = new ServiceManager().getUserService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
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
    public void shouldCountAllUsers() throws Exception {
        Long amount = userService.count();
        assertEquals("Users were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldCountUsersAccordingToQuery() throws Exception {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.mustNot(matchQuery("_id", "1"));
        boolQuery.must(matchQuery("login", "kowal"));
        Long amount = userService.count(boolQuery.toString());
        assertEquals("User was found!", Long.valueOf(0), amount);

        amount = userService.getAmountOfUsersWithExactlyTheSameLogin("1", "kowal");
        assertEquals("User was found!", Long.valueOf(0), amount);

        BoolQueryBuilder boolQuerySecond = new BoolQueryBuilder();
        boolQuerySecond.must(matchQuery("_id", "1"));
        boolQuerySecond.must(matchQuery("login", "kowal"));
        amount = userService.count(boolQuerySecond.toString());
        assertEquals("User was not found!", Long.valueOf(1), amount);

        amount = userService.getAmountOfUsersWithExactlyTheSameLogin(null, "kowal");
        assertEquals("User was not found!", Long.valueOf(1), amount);

        amount = userService.getAmountOfUsersWithExactlyTheSameLogin("2", "kowal");
        assertEquals("User was not found!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForUserGroups() throws Exception {
        Long amount = userService.countDatabaseRows();
        assertEquals("Users were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldFindUser() throws Exception {
        User user = userService.getById(1);
        boolean condition = user.getName().equals("Jan") && user.getSurname().equals("Kowalski");
        assertTrue("User was not found in database!", condition);
    }

    @Test
    public void shouldFindAllUsers() {
        List<User> users = userService.getAll();
        assertEquals("Not all users were found in database!", 3, users.size());
    }

    @Test
    public void shouldGetAllUsersInGivenRange() throws Exception {
        List<User> users = userService.getAll(2,10);
        assertEquals("Not all users were found in database!", 1, users.size());
    }

    @Test
    public void shouldRemoveUser() throws Exception {
        User user = new User();
        user.setLogin("Remove");
        userService.save(user);
        Thread.sleep(1000);
        User foundUser = userService.convertJSONObjectToBean(userService.findByLogin("Remove"));
        assertEquals("Additional user was not inserted in database!", "Remove", foundUser.getLogin());

        userService.remove(foundUser);
        foundUser = userService.getById(foundUser.getId());
        assertNull("Additional user was not removed from database!", foundUser.getLogin());

        user = new User();
        user.setLogin("remove");
        userService.save(user);
        Thread.sleep(1000);
        foundUser = userService.convertJSONObjectToBean(userService.findByLogin("remove"));
        assertEquals("Additional user was not inserted in database!", "remove", foundUser.getLogin());

        userService.remove(foundUser.getId());
        foundUser = userService.getById(foundUser.getId());
        assertNull("Additional user was not removed from database!", foundUser.getLogin());
    }

    @Test
    public void shouldRemoveUserButNotUserGroup() throws Exception {
        UserGroupService userGroupService = new ServiceManager().getUserGroupService();

        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("Cascade Group");
        userGroupService.saveToDatabase(userGroup);

        User user = new User();
        user.setLogin("Cascade");
        user.getUserGroups()
                .add(userGroupService.getByQuery("FROM UserGroup WHERE title = 'Cascade Group' ORDER BY id DESC").get(0));
        userService.saveToDatabase(user);
        User foundUser = userService.getByQuery("FROM User WHERE login = 'Cascade'").get(0);
        assertEquals("Additional user was not inserted in database!", "Cascade", foundUser.getLogin());

        userService.removeFromDatabase(foundUser);
        int size = userService.getByQuery("FROM User WHERE login = 'Cascade'").size();
        assertEquals("Additional user was not removed from database!", 0, size);

        size = userGroupService.getByQuery("FROM UserGroup WHERE title = 'Cascade Group'").size();
        assertEquals("User Group was removed from database!", 1, size);

        userGroupService
                .removeFromDatabase(userGroupService.getByQuery("FROM UserGroup WHERE title = 'Cascade Group'").get(0));
    }

    @Test
    public void shouldFindById() throws Exception {
        String actual = userService.findById(1).getLogin();
        String expected = "kowal";
        assertEquals("User was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByName() throws Exception {
        List<JsonObject> users = userService.findByName("Jan");
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
        List<JsonObject> users = userService.findBySurname("Kowalski");
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
        List<JsonObject> users = userService.findByFullName("Jan", "Kowalski");
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
        JsonObject user = userService.findByLogin("kowal");
        Integer actual = userService.getIdFromJSONObject(user);
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        user = userService.findByLogin("random");
        actual = userService.getIdFromJSONObject(user);
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByLdapLogin() throws Exception {
        JsonObject user = userService.findByLdapLogin("kowalLDP");
        Integer actual = userService.getIdFromJSONObject(user);
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        user = userService.findByLdapLogin("random");
        actual = userService.getIdFromJSONObject(user);
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByLocation() throws Exception {
        List<JsonObject> users = userService.findByLocation("Dresden");
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
        List<JsonObject> users = userService.findByActive(true);
        boolean result = users.size() == 2 || users.size() == 3 || users.size() == 4 || users.size() == 5;
        assertTrue("Users were not found in index!", result);

        users = userService.findByActive(false);
        result = users.size() == 1 || users.size() == 2 || users.size() == 3 || users.size() == 4;
        assertTrue("Users were found in index!", result);
    }

    @Test
    public void shouldFindByUserGroupId() throws Exception {
        List<JsonObject> users = userService.findByUserGroupId(1);
        Integer actual = users.size();
        Integer expected = 2;
        assertEquals("Users were not found in index!", expected, actual);

        users = userService.findByUserGroupId(3);
        actual = users.size();
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByUserGroupTitle() throws Exception {
        List<JsonObject> users = userService.findByUserGroupTitle("Admin");
        Integer actual = users.size();
        Integer expected = 2;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findByUserGroupTitle("None");
        actual = users.size();
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByFilter() throws Exception {
        List<JsonObject> users = userService.findByFilter("\"id:1\"");
        Integer actual = users.size();
        Integer expected = 1;
        assertEquals("User was not found in index!", expected, actual);

        users = userService.findByFilter("\"id:5\"");
        actual = users.size();
        expected = 0;
        assertEquals("User was found in index!", expected, actual);
    }

    @Test
    public void shouldGetTableSize() throws Exception {
        User user = userService.getById(1);
        int actual = userService.getTableSize(user);
        assertEquals("Table size is incorrect!", 20, actual);

        user = userService.getById(2);
        actual = userService.getTableSize(user);
        assertEquals("Table size is incorrect!", 10, actual);
    }

    @Test
    public void shouldGetCss() throws Exception {
        User user = userService.getById(1);
        boolean condition = userService.getCss(user).equals("old/userStyles/classic.css");
        assertTrue("Css file is incorrect!", condition);

        user = userService.getById(2);
        condition = userService.getCss(user).equals("old/userStyles/default.css");
        assertTrue("Css file is incorrect!", condition);
    }

    @Test
    public void shouldGetUserGroupSize() throws Exception {
        UserDTO user = userService.findById(1);
        int actual = user.getUserGroupSize();
        assertEquals("User groups' size is incorrect!", 1, actual);

        user = userService.findById(1, true);
        actual = user.getUserGroupSize();
        assertEquals("User groups' size is incorrect!", 1, actual);

        String title = user.getUserGroups().get(0).getTitle();
        assertEquals("User group's title is incorrect!", "Admin", title);
    }

    @Test
    public void shouldGetTasksSize() throws Exception {
        UserDTO user = userService.findById(2);
        int actual = user.getTasksSize();
        assertEquals("Tasks' size is incorrect!", 2, actual);

        user = userService.findById(3);
        actual = user.getTasksSize();
        assertEquals("Tasks' size is incorrect!", 1, actual);
    }

    @Test
    public void shouldGetProcessingTasksSize() throws Exception {
        UserDTO user = userService.findById(1);
        int actual = user.getProcessingTasksSize();
        assertEquals("Processing tasks' size is incorrect!", 1, actual);
    }

    @Test
    public void shouldGetProjectsSize() throws Exception {
        UserDTO user = userService.findById(1);
        int actual = user.getProjectsSize();
        assertEquals("Projects' size is incorrect!", 2, actual);

        user = userService.findById(2);
        actual = user.getProjectsSize();
        assertEquals("Projects' size is incorrect!", 1, actual);

        user = userService.findById(2, true);
        actual = user.getProjectsSize();
        assertEquals("Projects' size is incorrect!", 1, actual);

        String title = user.getProjects().get(0).getTitle();
        assertEquals("Project's title is incorrect!", "First project", title);
    }

    @Test
    public void shouldGetFiltersSize() throws Exception {
        UserDTO user = userService.findById(1);
        int actual = user.getFiltersSize();
        assertEquals("Properties' size is incorrect!", 2, actual);
    }

    @Ignore("not sure how method works")
    @Test
    public void shouldCheckIfIsPasswordCorrect() throws Exception {
        User user = userService.getById(1);
        boolean condition = userService.isPasswordCorrect(user, "test");
        assertTrue("User's password is incorrect!", condition);
    }

    @Test
    public void shouldGetFullName() throws Exception {
        User user = userService.getById(1);
        boolean condition = userService.getFullName(user).equals("Kowalski, Jan");
        assertTrue("Full name of user is incorrect!", condition);
    }

    @Test
    public void shouldGetHomeDirectory() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        User user = userService.getById(1);
        String homeDirectory = ConfigCore.getParameter("dir_Users");

        File script = new File(ConfigCore.getParameter("script_createDirUserHome"));
        ExecutionPermission.setExecutePermission(script);

        URI homeDirectoryForUser = userService.getHomeDirectory(user);
        boolean condition = homeDirectoryForUser.getRawPath().contains(homeDirectory + user.getLogin());
        assertTrue("Home directory of user is incorrect!", condition);

        user = userService.getById(2);
        homeDirectoryForUser = userService.getHomeDirectory(user);
        condition = homeDirectoryForUser.getRawPath().contains(user.getLogin());
        assertTrue("Home directory of user is incorrect!", condition);

        ExecutionPermission.setNoExecutePermission(script);
    }

    @Test
    public void shouldFindAllVisibleUsers() throws Exception {
        List<UserDTO> allVisibleUsers = userService.findAllVisibleUsers();
        assertEquals("Size of users is incorrect!", 3, allVisibleUsers.size());

        allVisibleUsers = userService.findAllVisibleUsersWithRelations();
        assertEquals("Size of users is incorrect!", 3, allVisibleUsers.size());
    }

    @Test
    public void shouldFindAllActiveUsers() throws Exception {
        List<UserDTO> allActiveUsers = userService.findAllActiveUsers();
        assertEquals("Size of users is incorrect!", 2, allActiveUsers.size());

        allActiveUsers = userService.findAllActiveUsersWithRelations();
        assertEquals("Size of users is incorrect!", 2, allActiveUsers.size());
    }

    @Test
    public void shouldFindActiveUsersByName() throws Exception {
        List<UserDTO> allActiveUsers = userService.findActiveUsersByName("Jan");
        int actual = allActiveUsers.size();
        int expected = 1;
        assertEquals("Size of users is incorrect!", expected, actual);

        allActiveUsers = userService.findActiveUsersByName("owa");
        assertEquals(2, allActiveUsers.size());
        actual = allActiveUsers.get(0).getId();
        expected = 1;
        assertEquals("Id of first user is incorrect!", expected, actual);

        allActiveUsers = userService.findActiveUsersByName("owa");
        actual = allActiveUsers.size();
        expected = 2;
        assertEquals("Size of users is incorrect!", expected, actual);
    }

    @Test
    public void shouldGetAuthorizationOfUser()throws Exception {
        Authority authority = userService.getByLogin("kowal").getUserGroups().get(0).getGlobalAuthorities().get(0);
        assertEquals("Authority title is incorrect!", "viewAllClients", authority.getTitle());
    }

    @Test
    public void shouldNotSaveUserWithSameLogin() throws DataException {
        User newUser = new User();
        newUser.setLogin("kowal");
        exception.expect(DataException.class);
        userService.save(newUser);
    }

    @Test
    public void shouldGetLdapServerOfUser() throws DAOException {
        User user = userService.getById(2);
        assertEquals("LdapServer title is incorrect!", "FirstLdapServer",
            user.getLdapGroup().getLdapServer().getTitle());
    }

    @Test
    public void shouldGetUserByLdapLogin() throws DAOException {
        User user = userService.getByLdapLogin("kowalLDP");
        assertEquals("User surname is incorrect!", "Kowalski", user.getSurname());
    }

    @Test
    public void shouldGetUserTasksInProgress() throws DAOException {
        User user = userService.getByLdapLogin("kowalLDP");
        List<Task> tasks = userService.getTasksInProgress(user);
        assertEquals("Number of tasks in process is incorrect!", 1, tasks.size());
        assertEquals("Title of task is incorrect!", tasks.get(0).getTitle(), "Testing");
    }
}
