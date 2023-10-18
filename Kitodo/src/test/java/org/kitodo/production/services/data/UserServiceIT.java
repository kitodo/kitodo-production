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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

/**
 * Tests for UserService class.
 */
public class UserServiceIT {

    private static final FileService fileService = ServiceManager.getFileService();
    private static final UserService userService = ServiceManager.getUserService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();

        fileService.createDirectory(URI.create(""), "users");
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();

        fileService.delete(URI.create("users"));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountUsersAccordingToQuery() {
        /*BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.mustNot(matchQuery("_id", "1"));
        boolQuery.must(matchQuery("login", "kowal"));
        await().untilAsserted(() -> assertEquals("User was found!", Long.valueOf(0), userService.count(boolQuery)));

        await().untilAsserted(() -> assertEquals("User was found!", Long.valueOf(0),
            userService.getAmountOfUsersWithExactlyTheSameLogin("1", "kowal")));

        BoolQueryBuilder boolQuerySecond = new BoolQueryBuilder();
        boolQuerySecond.must(matchQuery("_id", "1"));
        boolQuerySecond.must(matchQuery("login", "kowal"));
        await().untilAsserted(
            () -> assertEquals("User was not found!", Long.valueOf(1), userService.count(boolQuerySecond)));

        await().untilAsserted(() -> assertEquals("User was not found!", Long.valueOf(1),
            userService.getAmountOfUsersWithExactlyTheSameLogin(null, "kowal")));

        await().untilAsserted(() -> assertEquals("User was not found!", Long.valueOf(1),
            userService.getAmountOfUsersWithExactlyTheSameLogin("2", "kowal")));*/
    }

    @Test
    public void shouldCountAllDatabaseRowsForUsers() throws Exception {
        Long amount = userService.countDatabaseRows();
        assertEquals("Users were not counted correctly!", Long.valueOf(7), amount);
    }

    @Test
    public void shouldGetUser() throws Exception {
        User user = userService.getById(1);
        boolean condition = user.getName().equals("Jan") && user.getSurname().equals("Kowalski");
        assertTrue("User was not found in database!", condition);

        assertEquals("User was found but tasks were not inserted!", 3, user.getProcessingTasks().size());
    }

    @Test
    public void shouldGetAllUsers() throws Exception {
        List<User> users = userService.getAll();
        assertEquals("Not all users were found in database!", 7, users.size());
    }

    @Test
    public void shouldGetAllUsersInGivenRange() throws Exception {
        List<User> users = userService.getAll(2, 10);
        assertEquals("Not all users were found in database!", 5, users.size());
    }

    @Test
    public void shouldRemoveUser() throws Exception {
        User user = new User();
        user.setLogin("Remove");
        userService.saveToDatabase(user);
        User foundUser = userService.getByQuery("FROM User WHERE login = 'Remove' ORDER BY id DESC").get(0);
        assertEquals("Additional user was not inserted in database!", "Remove", foundUser.getLogin());

        userService.removeFromDatabase(foundUser);
        foundUser = userService.getById(foundUser.getId());
        assertNull("Additional user was not removed from database!", foundUser.getLogin());

        user = new User();
        user.setLogin("remove");
        userService.saveToDatabase(user);
        foundUser = userService.getByQuery("FROM User WHERE login = 'remove' ORDER BY id DESC").get(0);
        assertEquals("Additional user was not inserted in database!", "remove", foundUser.getLogin());

        userService.removeFromDatabase(foundUser.getId());
        foundUser = userService.getById(foundUser.getId());
        assertNull("Additional user was not removed from database!", foundUser.getLogin());
    }

    @Test
    public void shouldRemoveUserButNotRole() throws Exception {
        RoleService roleService = ServiceManager.getRoleService();

        Role role = new Role();
        role.setTitle("Cascade Group");
        roleService.saveToDatabase(role);

        User user = new User();
        user.setLogin("Cascade");
        user.getRoles().add(roleService.getByQuery("FROM Role WHERE title = 'Cascade Group' ORDER BY id DESC").get(0));
        userService.saveToDatabase(user);
        User foundUser = userService.getByQuery("FROM User WHERE login = 'Cascade'").get(0);
        assertEquals("Additional user was not inserted in database!", "Cascade", foundUser.getLogin());

        userService.removeFromDatabase(foundUser);
        int size = userService.getByQuery("FROM User WHERE login = 'Cascade'").size();
        assertEquals("Additional user was not removed from database!", 0, size);

        size = roleService.getByQuery("FROM Role WHERE title = 'Cascade Group'").size();
        assertEquals("Role was removed from database!", 1, size);

        roleService.removeFromDatabase(roleService.getByQuery("FROM Role WHERE title = 'Cascade Group'").get(0));
    }

    @Test
    public void shouldGetTableSize() throws Exception {
        User user = userService.getById(1);
        int actual = user.getTableSize();
        assertEquals("Table size is incorrect!", 20, actual);

        user = userService.getById(2);
        actual = user.getTableSize();
        assertEquals("Table size is incorrect!", 10, actual);
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
        String homeDirectory = ConfigCore.getParameter(ParameterCore.DIR_USERS);

        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
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
    public void shouldGetAuthorityOfUser() throws Exception {
        Authority authority = userService.getByLogin("kowal").getRoles().get(0).getAuthorities().get(1);
        assertEquals("Authority title is incorrect!", "viewClient_globalAssignable", authority.getTitle());
    }

    @Test
    public void shouldNotSaveUserWithSameLogin() throws Exception {
        User newUser = new User();
        newUser.setLogin("kowal");
        exception.expect(DAOException.class);
        userService.saveToDatabase(newUser);
    }

    @Test
    public void shouldGetLdapServerOfUser() throws DAOException {
        User user = userService.getById(2);
        assertEquals("LdapServer title is incorrect!", "FirstLdapServer",
            user.getLdapGroup().getLdapServer().getTitle());
    }

    @Test
    public void shouldGetUserByLdapLogin() throws DAOException {
        User user = userService.getByLdapLoginOrLogin("kowalLDP");
        assertEquals("User surname is incorrect!", "Kowalski", user.getSurname());
    }

    @Test
    public void shouldGetUserTasksInProgress() throws DAOException {
        User user = userService.getByLdapLoginOrLogin("nowakLDP");
        List<Task> tasks = userService.getTasksInProgress(user);
        assertEquals("Number of tasks in process is incorrect!", 1, tasks.size());
        assertEquals("Title of task is incorrect!", "Progress", tasks.get(0).getTitle());
    }

    @Test
    public void shouldGetAuthenticatedUser() throws DAOException {
        SecurityTestUtils.addUserDataToSecurityContext(userService.getById(1), 1);
        User authenticatedUser = userService.getAuthenticatedUser();
        assertEquals("Returned authenticated user was wrong", "kowal", authenticatedUser.getLogin());
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void returnCorrectUserIndependentOfLoginOrLdapLoginByLogin() {
        User user = userService.getByLdapLoginOrLogin("verylast");
        assertEquals("Returned user was wrong", "User, Very last", user.getFullName());
    }

    @Test
    public void returnCorrectUserIndependentOfLoginOrLdapLoginByLdapLogin() {
        User user = userService.getByLdapLoginOrLogin("doraLDP");
        assertEquals("Returned user was wrong", "Dora, Anna", user.getFullName());
    }
}
