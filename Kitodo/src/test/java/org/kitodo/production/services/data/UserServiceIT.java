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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

/**
 * Tests for UserService class.
 */
public class UserServiceIT {

    private static final FileService fileService = ServiceManager.getFileService();
    private static final UserService userService = ServiceManager.getUserService();

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();

        fileService.createDirectory(URI.create(""), "users");
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();

        fileService.delete(URI.create("users"));
    }

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
        Long amount = userService.count();
        assertEquals(Long.valueOf(7), amount, "Users were not counted correctly!");
    }

    @Test
    public void shouldGetUser() throws Exception {
        User user = userService.getById(1);
        boolean condition = user.getName().equals("Jan") && user.getSurname().equals("Kowalski");
        assertTrue(condition, "User was not found in database!");

        assertEquals(3, user.getProcessingTasks().size(), "User was found but tasks were not inserted!");
    }

    @Test
    public void shouldGetAllUsers() throws Exception {
        List<User> users = userService.getAll();
        assertEquals(7, users.size(), "Not all users were found in database!");
    }

    @Test
    public void shouldGetAllUsersInGivenRange() throws Exception {
        List<User> users = userService.getAll(2, 10);
        assertEquals(5, users.size(), "Not all users were found in database!");
    }

    @Test
    public void shouldRemoveUser() throws Exception {
        User user = new User();
        user.setLogin("Remove");
        userService.save(user);
        User foundUser = userService.getByQuery("FROM User WHERE login = 'Remove' ORDER BY id DESC").getFirst();
        assertEquals("Remove", foundUser.getLogin(), "Additional user was not inserted in database!");

        userService.remove(foundUser);
        foundUser = userService.getById(foundUser.getId());
        assertNull(foundUser.getLogin(), "Additional user was not removed from database!");

        user = new User();
        user.setLogin("remove");
        userService.save(user);
        foundUser = userService.getByQuery("FROM User WHERE login = 'remove' ORDER BY id DESC").getFirst();
        assertEquals("remove", foundUser.getLogin(), "Additional user was not inserted in database!");

        userService.remove(foundUser.getId());
        foundUser = userService.getById(foundUser.getId());
        assertNull(foundUser.getLogin(), "Additional user was not removed from database!");
    }

    @Test
    public void shouldRemoveUserButNotRole() throws Exception {
        RoleService roleService = ServiceManager.getRoleService();

        Role role = new Role();
        role.setTitle("Cascade Group");
        roleService.save(role);

        User user = new User();
        user.setLogin("Cascade");
        user.getRoles().add(roleService.getByQuery("FROM Role WHERE title = 'Cascade Group' ORDER BY id DESC").getFirst());
        userService.save(user);
        User foundUser = userService.getByQuery("FROM User WHERE login = 'Cascade'").getFirst();
        assertEquals("Cascade", foundUser.getLogin(), "Additional user was not inserted in database!");

        userService.remove(foundUser);
        int size = userService.getByQuery("FROM User WHERE login = 'Cascade'").size();
        assertEquals(0, size, "Additional user was not removed from database!");

        size = roleService.getByQuery("FROM Role WHERE title = 'Cascade Group'").size();
        assertEquals(1, size, "Role was removed from database!");

        roleService.remove(roleService.getByQuery("FROM Role WHERE title = 'Cascade Group'").getFirst());
    }

    @Test
    public void shouldGetTableSize() throws Exception {
        User user = userService.getById(1);
        int actual = user.getTableSize();
        assertEquals(20, actual, "Table size is incorrect!");

        user = userService.getById(2);
        actual = user.getTableSize();
        assertEquals(10, actual, "Table size is incorrect!");
    }

    @Test
    public void shouldGetFullName() throws Exception {
        User user = userService.getById(1);
        boolean condition = userService.getFullName(user).equals("Kowalski, Jan");
        assertTrue(condition, "Full name of user is incorrect!");
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
        assertTrue(condition, "Home directory of user is incorrect!");

        user = userService.getById(2);
        homeDirectoryForUser = userService.getHomeDirectory(user);
        condition = homeDirectoryForUser.getRawPath().contains(user.getLogin());
        assertTrue(condition, "Home directory of user is incorrect!");

        ExecutionPermission.setNoExecutePermission(script);
    }

    @Test
    public void shouldGetAuthorityOfUser() throws Exception {
        Authority authority = userService.getByLogin("kowal").getRoles().getFirst().getAuthorities().get(1);
        assertEquals("viewClient_globalAssignable", authority.getTitle(), "Authority title is incorrect!");
    }

    @Test
    public void shouldNotSaveUserWithSameLogin() throws Exception {
        User newUser = new User();
        newUser.setLogin("kowal");
        assertThrows(DAOException.class, () -> userService.save(newUser));
    }

    @Test
    public void shouldGetLdapServerOfUser() throws DAOException {
        User user = userService.getById(2);
        assertEquals("FirstLdapServer", user.getLdapGroup().getLdapServer().getTitle(), "LdapServer title is incorrect!");
    }

    @Test
    public void shouldGetUserByLdapLogin() throws DAOException {
        User user = userService.getByLdapLoginOrLogin("kowalLDP");
        assertEquals("Kowalski", user.getSurname(), "User surname is incorrect!");
    }

    @Test
    public void shouldGetUserTasksInProgress() throws DAOException {
        User user = userService.getByLdapLoginOrLogin("nowakLDP");
        List<Task> tasks = ServiceManager.getTaskService().getTasksInProgress(user);
        assertEquals(1, tasks.size(), "Number of tasks in process is incorrect!");
        assertEquals("Progress", tasks.getFirst().getTitle(), "Title of task is incorrect!");
    }

    @Test
    public void shouldGetAuthenticatedUser() throws DAOException {
        SecurityTestUtils.addUserDataToSecurityContext(userService.getById(1), 1);
        User authenticatedUser = userService.getAuthenticatedUser();
        assertEquals("kowal", authenticatedUser.getLogin(), "Returned authenticated user was wrong");
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void returnCorrectUserIndependentOfLoginOrLdapLoginByLogin() {
        User user = userService.getByLdapLoginOrLogin("verylast");
        assertEquals("User, Very last", user.getFullName(), "Returned user was wrong");
    }

    @Test
    public void returnCorrectUserIndependentOfLoginOrLdapLoginByLdapLogin() {
        User user = userService.getByLdapLoginOrLogin("doraLDP");
        assertEquals("Dora, Anna", user.getFullName(), "Returned user was wrong");
    }

    @Test
    public void shouldLoadSameRolesIndividuallyAndInBulk() throws DAOException {
        List<User> users = ServiceManager.getUserService().getAll();
        List<Integer> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        Map<Integer, List<String>> bulkRoles =
                ServiceManager.getUserService().loadRolesForUsers(userIds);
        for (User user : users) {
            List<String> expectedRoles = user.getRoles().stream()
                    .map(Role::getTitle)
                    .sorted()
                    .collect(Collectors.toList());
            List<String> actualRoles = bulkRoles
                    .getOrDefault(user.getId(), Collections.emptyList())
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());

            assertEquals(
                    expectedRoles,
                    actualRoles,
                    "Roles mismatch for user " + user.getId()
            );
        }
    }

    @Test
    public void shouldLoadSameClientsIndividuallyAndInBulk() throws Exception {
        List<User> users = userService.getAll();
        List<Integer> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        Map<Integer, List<String>> bulkClients = userService.loadClientsForUsers(userIds);
        for (User user : users) {
            List<String> expected = user.getClients().stream()
                    .map(Client::getName)
                    .sorted()
                    .collect(Collectors.toList());
            List<String> actual = bulkClients
                    .getOrDefault(user.getId(), Collections.emptyList())
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());

            assertEquals(
                    expected,
                    actual,
                    "Clients mismatch for user " + user.getId()
            );
        }
    }

    @Test
    public void shouldLoadSameProjectsIndividuallyAndInBulk() throws Exception {
        List<User> users = userService.getAll();
        List<Integer> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        Map<Integer, List<String>> bulkProjects =
                userService.loadProjectsForUsers(userIds);
        for (User user : users) {
            List<String> expectedProjects = user.getProjects().stream()
                    .map(Project::getTitle)
                    .sorted()
                    .collect(Collectors.toList());
            List<String> actualProjects = bulkProjects
                    .getOrDefault(user.getId(), Collections.emptyList())
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());

            assertEquals(
                    expectedProjects,
                    actualProjects,
                    "Projects mismatch for user " + user.getId()
            );
        }
    }

    @Test
    public void shouldLoadSameTasksInProgressIndividuallyAndInBulk() throws Exception {
        List<User> users = userService.getAll();
        List<Integer> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        Map<Integer, Boolean> bulkTasksInProgress =
                userService.loadTasksInProgressForUsers(userIds);
        for (User user : users) {
            boolean expected = user.getProcessingTasks().stream()
                    .anyMatch(task ->
                            task.getProcessingStatus() == TaskStatus.INWORK
                                    && task.getProcess() != null
                    );
            boolean actual = bulkTasksInProgress
                    .getOrDefault(user.getId(), false);

            assertEquals(
                    expected,
                    actual,
                    "Tasks-in-progress mismatch for user " + user.getId()
            );
        }
    }
}
