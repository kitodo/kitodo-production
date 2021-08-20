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

package org.kitodo.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.*;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.UserEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;

public class RemovingST extends BaseTestSelenium {

    private static UsersPage usersPage;
    private static ProcessesPage processesPage;
    private static ProjectsPage projectsPage;

    @BeforeAll
    public static void setup() throws Exception {
        usersPage = Pages.getUsersPage();
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @AfterEach
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void removeBatchTest() throws Exception {
        int batchesDisplayed = processesPage.countListedBatches();
        long batchesInDatabase = ServiceManager.getBatchService().count();
        assertTrue(batchesDisplayed > 0 && batchesInDatabase > 0, "Batch list is empty");
        processesPage.deleteBatch();
        assertTrue(processesPage.countListedBatches() == batchesDisplayed - 1
                && ServiceManager.getBatchService().count() == batchesInDatabase - 1,
            "Removal of batch was not successful!");
    }

    @Test
    public void removeUserTest() throws Exception {
        int usersDisplayed = usersPage.countListedUsers();
        long usersInDatabase = ServiceManager.getUserService().count();
        assertTrue(usersDisplayed > 0 && usersInDatabase > 0, "User list is empty");
        usersPage.deleteRemovableUser();
        assertTrue(usersPage.countListedUsers() == usersDisplayed - 1
                && ServiceManager.getUserService().count() == usersInDatabase - 1,
            "Removal of first user was not successful!");
    }

    @Test
    public void removeRoleTest() throws Exception {
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        int rolesDisplayed = usersPage.countListedRoles();
        long clientRoles = ServiceManager.getRoleService().count();
        assertTrue(rolesDisplayed > 0 && clientRoles > 0, "Role list or umber of current clients roles is empty");
        usersPage.deleteRemovableRole();
        assertTrue(usersPage.countListedRoles() == rolesDisplayed - 1
                && ServiceManager.getRoleService().count() == clientRoles - 1, "Removal of first role was not successful!");
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void removeClientTest() throws Exception {
        int clientsDisplayed = usersPage.countListedClients();
        long clientsInDatabase = ServiceManager.getClientService().count();
        assertTrue(clientsDisplayed > 0 && clientsInDatabase > 0, "Client list is empty");
        usersPage.deleteRemovableClient();
        assertTrue(usersPage.countListedClients() == clientsDisplayed - 1
                && ServiceManager.getClientService().count() == clientsInDatabase - 1,
            "Removal of first client was not successful!");
    }

    @Test
    public void removeDocketTest() throws Exception {
        int docketsDisplayed = projectsPage.countListedDockets();
        long docketsInDatabase = ServiceManager.getDocketService().count();
        assertTrue(docketsDisplayed > 0 && docketsInDatabase > 0, "Docket list is empty");
        projectsPage.deleteDocket();
        assertTrue(projectsPage.countListedDockets() == docketsDisplayed - 1
                && ServiceManager.getDocketService().count() == docketsInDatabase - 1,
            "Removal of first docket was not successful!");
    }

    @Test
    public void removeRulesetTest() throws Exception {
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        long rulesetsInDatabase = ServiceManager.getRulesetService().count();
        assertTrue(rulesetsDisplayed > 0 && rulesetsInDatabase > 0, "Ruleset list is empty");
        projectsPage.deleteRuleset();
        assertTrue(projectsPage.countListedRulesets() == rulesetsDisplayed - 1
                && ServiceManager.getRulesetService().count() == rulesetsInDatabase - 1,
            "Removal of ruleset was not successful!");
    }

    /**
     * Verifies that removing a client from a user who is still assigned to projects of that client opens a
     * confirmation dialog listing these projects. Ensures that cancelling the dialog leaves the user unchanged and that
     * confirming the dialog removes the client together with its projects and roles from the user.
     */
    @Test
    public void removeClientWithAssignedProjectsFromUserTest() throws Exception {
        // user "Nowak, Adam" is assigned to "First client" and "Second client" and to projects and roles of both
        final int userId = 2;
        final int clientId = 2;
        User user = ServiceManager.getUserService().getById(userId);
        String clientName = ServiceManager.getClientService().getById(clientId).getName();

        List<String> clientProjectTitles = getProjectTitlesOfUser(userId, clientId, true);
        List<String> otherProjectTitles = getProjectTitlesOfUser(userId, clientId, false);
        assertFalse(clientProjectTitles.isEmpty(), "User is not assigned to any project of client '" + clientName + "'");
        assertFalse(otherProjectTitles.isEmpty(), "User is not assigned to any project of other clients");
        assertTrue(countRolesOfUserAndClient(userId, clientId) > 0,
                "User is not assigned to any role of client '" + clientName + "'");

        UserEditPage userEditPage = Pages.getUserEditPage().goTo(userId);
        assertTrue(userEditPage.getClientNames().contains(clientName),
                "Client '" + clientName + "' is not listed for user");

        // clicking 'remove' opens the confirmation dialog listing the user's projects of the client
        userEditPage.clickRemoveClient(clientName);
        userEditPage.waitForRemoveClientDialog();
        String dialogMessage = userEditPage.getRemoveClientDialogMessage();
        assertTrue(dialogMessage.contains(clientName), "Dialog message does not mention client: " + dialogMessage);
        assertTrue(dialogMessage.contains(user.getFullName()), "Dialog message does not mention user: " + dialogMessage);
        assertEquals(clientProjectTitles.stream().sorted().toList(),
                userEditPage.getRemoveClientDialogProjectTitles().stream().sorted().toList(),
                "Remove client dialog lists wrong projects");

        // cancelling the dialog leaves clients and projects of the user untouched
        userEditPage.cancelRemoveClient();
        assertFalse(userEditPage.isRemoveClientDialogDisplayed(), "Remove client dialog was not closed");
        assertTrue(userEditPage.getClientNames().contains(clientName),
                "Client was removed from user although removal was cancelled");
        assertTrue(userEditPage.getProjectTitles().containsAll(clientProjectTitles),
                "Projects were removed from user although removal was cancelled");

        // confirming the dialog removes client, client projects and client roles from the user
        userEditPage.clickRemoveClient(clientName);
        userEditPage.waitForRemoveClientDialog();
        userEditPage.confirmRemoveClient();
        pollAssertTrue(() -> !userEditPage.getClientNames().contains(clientName));
        List<String> remainingProjectTitles = userEditPage.getProjectTitles();
        assertTrue(remainingProjectTitles.stream().noneMatch(clientProjectTitles::contains),
                "Projects of removed client are still listed for user: " + remainingProjectTitles);
        assertTrue(remainingProjectTitles.containsAll(otherProjectTitles),
                "Projects of other clients were removed from user: " + remainingProjectTitles);
        assertFalse(userEditPage.getRoleClientNames().contains(clientName),
                "Roles of removed client are still listed for user");

        // changes are persisted after saving the user
        userEditPage.save();
        assertTrue(usersPage.isAt(), "Redirection after save was not successful");
        assertTrue(getProjectTitlesOfUser(userId, clientId, true).isEmpty(),
                "Projects of removed client are still assigned to user in database");
        assertEquals(otherProjectTitles.stream().sorted().toList(),
                getProjectTitlesOfUser(userId, clientId, false).stream().sorted().toList(),
                "Projects of other clients were changed in database");
        assertEquals(0, countRolesOfUserAndClient(userId, clientId),
                "Roles of removed client are still assigned to user in database");
        assertTrue(ServiceManager.getClientService().getByQuery(
                "SELECT c FROM Client AS c INNER JOIN c.users AS u WHERE u.id = :userId AND c.id = :clientId",
                Map.of("userId", userId, "clientId", clientId)).isEmpty(),
                "Client is still assigned to user in database");
    }

    /**
     * Verifies that cancelling the confirmation dialog for removing a client from a user does not reset the user's
     * tasks in progress belonging to that client when the user is saved afterward.
     */
    @Test
    public void cancelRemoveClientFromUserKeepsTasksInProgressTest() throws Exception {
        // user "Nowak, Adam" is processing task "Progress" of "First process" which belongs to "First client"
        final int userId = 2;
        final int clientId = 1;
        String clientName = ServiceManager.getClientService().getById(clientId).getName();
        int tasksInProgress = countTasksInProgressOfUserAndClient(userId, clientId);
        assertTrue(tasksInProgress > 0, "User has no tasks in progress of client '" + clientName + "'");

        UserEditPage userEditPage = Pages.getUserEditPage().goTo(userId);
        userEditPage.clickRemoveClient(clientName);
        userEditPage.waitForRemoveClientDialog();
        userEditPage.cancelRemoveClient();
        assertTrue(userEditPage.getClientNames().contains(clientName),
                "Client was removed from user although removal was cancelled");

        userEditPage.save();
        assertTrue(usersPage.isAt(), "Redirection after save was not successful");
        assertEquals(tasksInProgress, countTasksInProgressOfUserAndClient(userId, clientId),
                "Tasks in progress were reset although removal of client was cancelled");
        assertFalse(ServiceManager.getClientService().getByQuery(
                "SELECT c FROM Client AS c INNER JOIN c.users AS u WHERE u.id = :userId AND c.id = :clientId",
                Map.of("userId", userId, "clientId", clientId)).isEmpty(),
                "Client was removed from user in database although removal was cancelled");
    }

    /**
     * Verifies that saving a user resets the user's tasks in progress of every client that was removed from the user,
     * while tasks of a client whose removal was canceled in the confirmation dialog stay untouched. Covers the case
     * of a confirmed removal followed by a canceled removal before the user is saved.
     */
    @Test
    public void removeClientFromUserResetsTasksInProgressTest() throws Exception {
        // create a dedicated user to keep this test independent of the other tests modifying users' clients.
        final int firstClientId = 1;
        final int secondClientId = 2;
        String firstClientName = ServiceManager.getClientService().getById(firstClientId).getName();
        String secondClientName = ServiceManager.getClientService().getById(secondClientId).getName();
        User user = new User();
        user.setName("Client");
        user.setSurname("Remover");
        user.setLogin("clientremover");
        user.setLanguage("de");
        user.setTableSize(20);
        user.getClients().add(ServiceManager.getClientService().getById(firstClientId));
        user.getClients().add(ServiceManager.getClientService().getById(secondClientId));
        // "First project" belongs to "First client", "Inactive project" belongs to "Second client"
        user.getProjects().add(ServiceManager.getProjectService().getById(1));
        user.getProjects().add(ServiceManager.getProjectService().getById(3));
        ServiceManager.getUserService().save(user);
        final int userId = user.getId();

        // one task in progress per client, in processes of "First template"/"First project" and
        // "Second template"/"Inactive project"
        int firstClientTaskId = insertTaskInProgress(user, MockDatabase.addProcess("Client remover first", 1, 1));
        int secondClientTaskId = insertTaskInProgress(user, MockDatabase.addProcess("Client remover second", 3, 2));
        assertEquals(1, countTasksInProgressOfUserAndClient(userId, firstClientId),
                "User should have exactly one task in progress of client '" + firstClientName + "'");
        assertEquals(1, countTasksInProgressOfUserAndClient(userId, secondClientId),
                "User should have exactly one task in progress of client '" + secondClientName + "'");

        UserEditPage userEditPage = Pages.getUserEditPage().goTo(userId);

        // confirm removal of second client
        userEditPage.clickRemoveClient(secondClientName);
        userEditPage.waitForRemoveClientDialog();
        userEditPage.confirmRemoveClient();
        pollAssertTrue(() -> !userEditPage.getClientNames().contains(secondClientName));

        // cancel removal of first client afterward
        userEditPage.clickRemoveClient(firstClientName);
        userEditPage.waitForRemoveClientDialog();
        userEditPage.cancelRemoveClient();
        assertTrue(userEditPage.getClientNames().contains(firstClientName),
                "Client was removed from user although removal was cancelled");

        userEditPage.save();
        assertTrue(usersPage.isAt(), "Redirection after save was not successful");

        Task secondClientTask = getTask(secondClientTaskId);
        assertEquals(TaskStatus.OPEN, secondClientTask.getProcessingStatus(),
                "Task in progress of removed client was not reset to 'open'");
        assertNull(secondClientTask.getProcessingUser(),
                "Task in progress of removed client is still assigned to user");

        Task firstClientTask = getTask(firstClientTaskId);
        assertEquals(TaskStatus.INWORK, firstClientTask.getProcessingStatus(),
                "Task in progress was reset although removal of its client was cancelled");
        assertEquals(userId, firstClientTask.getProcessingUser().getId(),
                "Task was unassigned from user although removal of its client was cancelled");

        List<Integer> assignedClientIds = ServiceManager.getClientService().getByQuery(
                "SELECT c FROM Client AS c INNER JOIN c.users AS u WHERE u.id = :userId",
                Map.of("userId", userId)).stream().map(BaseBean::getId).toList();
        assertEquals(List.of(firstClientId), assignedClientIds, "User has wrong clients assigned in database");
    }

    private List<String> getProjectTitlesOfUser(int userId, int clientId, boolean ofClient) {
        String clientCondition = ofClient ? "p.client.id = :clientId" : "p.client.id <> :clientId";
        return ServiceManager.getProjectService().getByQuery(
                "SELECT p FROM Project AS p INNER JOIN p.users AS u WHERE u.id = :userId AND " + clientCondition,
                Map.of("userId", userId, "clientId", clientId)).stream().map(Project::getTitle).toList();
    }

    private int countRolesOfUserAndClient(int userId, int clientId) {
        return ServiceManager.getRoleService().getByQuery(
                "SELECT r FROM Role AS r INNER JOIN r.users AS u WHERE u.id = :userId AND r.client.id = :clientId",
                Map.of("userId", userId, "clientId", clientId)).size();
    }

    private int countTasksInProgressOfUserAndClient(int userId, int clientId) {
        return ServiceManager.getTaskService().getByQuery(
                "SELECT t FROM Task AS t WHERE t.processingUser.id = :userId AND t.processingStatus = :status "
                        + "AND t.process.project.client.id = :clientId",
                Map.of("userId", userId, "status", TaskStatus.INWORK, "clientId", clientId)).size();
    }

    private int insertTaskInProgress(User user, Process process) throws Exception {
        Task task = new Task();
        task.setTitle("In progress (" + process.getTitle() + ")");
        task.setOrdering(1);
        task.setEditType(TaskEditType.MANUAL_SINGLE);
        task.setProcessingStatus(TaskStatus.INWORK);
        task.setProcessingUser(user);
        task.setProcess(process);
        ServiceManager.getTaskService().save(task);
        return task.getId();
    }

    private Task getTask(int taskId) {
        return ServiceManager.getTaskService().getByQuery("SELECT t FROM Task AS t WHERE t.id = :taskId",
                Map.of("taskId", taskId)).getFirst();
    }
}
