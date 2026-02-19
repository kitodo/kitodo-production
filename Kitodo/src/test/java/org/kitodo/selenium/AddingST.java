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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.xebialabs.restito.server.StubServer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.UrlParameter;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.generators.LdapGroupGenerator;
import org.kitodo.selenium.testframework.generators.ProjectGenerator;
import org.kitodo.selenium.testframework.generators.UserGenerator;
import org.kitodo.selenium.testframework.pages.ImportConfigurationEditPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.RoleEditPage;
import org.kitodo.selenium.testframework.pages.UserEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.test.utils.ProcessTestUtils;

public class AddingST extends BaseTestSelenium {

    private static StubServer server;
    private static ProcessesPage processesPage;
    private static ProjectsPage projectsPage;
    private static UsersPage usersPage;
    private static RoleEditPage roleEditPage;
    private static UserEditPage userEditPage;
    private static ImportConfigurationEditPage importConfigurationEditPage;
    private static final String TEST_METADATA_FILE = "testMultiVolumeWorkMeta.xml";
    private static int secondProcessId = -1;
    private static final String PICA_PPN = "pica.ppn";
    private static final String PICA_XML = "picaxml";
    private static final String TEST_FILE_PATH = "src/test/resources/sruTestRecord.xml";

    @BeforeAll
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
        usersPage = Pages.getUsersPage();
        userEditPage = Pages.getUserEditPage();
        roleEditPage = Pages.getRoleEditPage();
        importConfigurationEditPage = Pages.getImportConfigurationEditPage();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();

        for (Process process : ServiceManager.getProcessService().getAll()) {
            if ("Second process".equals(process.getTitle())) {
                secondProcessId = process.getId();
                break;
            }
        }
        assertTrue(secondProcessId > 0, "Should find exactly one second process!");
        ProcessTestUtils.copyTestMetadataFile(secondProcessId, TEST_METADATA_FILE);
        server = new StubServer(MockDatabase.PORT).run();
        setupServer();
    }

    private static void setupServer() throws IOException {
        // REST endpoint for testing metadata import
        MockDatabase.addRestEndPointForSru(server, PICA_PPN + "=test", TEST_FILE_PATH, PICA_XML, 1);
    }

    /**
     * Remove unsuitable parent test process.
     * @throws DAOException when test process cannot be removed
     */
    @AfterAll
    public static void removeUnsuitableParentTestProcess() throws DAOException {
        ProcessTestUtils.removeTestProcess(secondProcessId);
        server.stop();
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
    public void addBatchTest() throws Exception {
        processesPage.createNewBatch();
        await().untilAsserted(() -> assertEquals(1, ServiceManager.getBatchService().getByQuery("FROM Batch WHERE title = 'SeleniumBatch'").size(), "Batch was inserted!"));
    }

    @Test
    public void addProjectTest() throws Exception {
        Project project = ProjectGenerator.generateProject();
        projectsPage.createNewProject();
        assertEquals("Neues Projekt", Pages.getProjectEditPage().getHeaderText(), "Header for create new project is incorrect");

        Pages.getProjectEditPage().insertProjectData(project).save();
        assertTrue(projectsPage.isAt(), "Redirection after save was not successful");

        boolean projectAvailable = Pages.getProjectsPage().getProjectsTitles().contains(project.getTitle());
        assertTrue(projectAvailable, "Created Project was not listed at projects table!");
    }

    @Test
    public void addTemplateTest() throws Exception {
        Template template = new Template();
        template.setTitle("MockTemplate");
        projectsPage.createNewTemplate();
        assertEquals("Neue Produktionsvorlage", Pages.getTemplateEditPage().getHeaderText(), "Header for create new template is incorrect");

        Pages.getTemplateEditPage().insertTemplateData(template).save();
        await().until(() -> projectsPage.countListedTemplates() == 3);
        boolean templateAvailable = projectsPage.getTemplateTitles().contains(template.getTitle());
        assertTrue(templateAvailable, "Created Template was not listed at templates table!");
    }

    @Test
    public void addProcessesTest() throws Exception {
        projectsPage.createNewProcess();
        assertEquals("Einen neuen Vorgang anlegen (Produktionsvorlage: 'First template')", Pages.getProcessFromTemplatePage().getHeaderText(), "Header for create new process is incorrect");

        String generatedTitle = Pages.getProcessFromTemplatePage().createProcess();
        boolean processAvailable = processesPage.getProcessTitles().contains(generatedTitle);
        assertTrue(processAvailable, "Created Process was not listed at processes table!");

        ProcessService processService = ServiceManager.getProcessService();
        Optional<Process> optionalProcess = processService.getAll().stream().filter(process -> generatedTitle
                .equals(process.getTitle())).findAny();
        assertTrue(optionalProcess.isPresent(), "Generated process not found in database");
        Process generatedProcess = optionalProcess.get();
        assertNull(generatedProcess.getParent(), "Created Process unexpectedly got a parent!");

        projectsPage.createNewProcess();
        String generatedChildTitle = Pages.getProcessFromTemplatePage()
                .createProcessAsChild(generatedProcess.getTitle());

        boolean childProcessAvailable = processesPage.getProcessTitles().contains(generatedChildTitle);
        assertTrue(childProcessAvailable, "Created Process was not listed at processes table!");

        Optional<Process> optionalChildProcess = processService.getAll().stream().filter(process -> generatedChildTitle
                .equals(process.getTitle())).findAny();
        assertTrue(optionalChildProcess.isPresent(), "Generated child process not found in database");
        Process generatedChildProcess = optionalChildProcess.get();
        assertEquals(generatedProcess, generatedChildProcess.getParent(), "Created Process has a wrong parent!");
        ProcessTestUtils.removeTestProcess(generatedProcess.getId());
    }

    @Test
    public void addProcessAsChildNotPossible() throws Exception {
        projectsPage.createNewProcess();
        boolean errorMessageShowing = Pages.getProcessFromTemplatePage().createProcessAsChildNotPossible();
        assertTrue(errorMessageShowing, "There was no error!");
        Pages.getProcessFromTemplatePage().cancel();
    }

    @Test
    public void addProcessFromCatalogTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS);

        projectsPage.createNewProcess();
        assertEquals("Einen neuen Vorgang anlegen (Produktionsvorlage: 'First template')", Pages.getProcessFromTemplatePage().getHeaderText(), "Header for create new process is incorrect");

        String generatedTitle = Pages.getProcessFromTemplatePage().createProcessFromCatalog();
        boolean processAvailable = processesPage.getProcessTitles().contains(generatedTitle);
        assertTrue(processAvailable, "Created Process was not listed at processes table!");
        int index = processesPage.getProcessTitles().indexOf(generatedTitle);
        assertTrue(index >= 0, "Process table does not contain ID or new process");
        int processId = Integer.parseInt(processesPage.getProcessIds().get(index));
        ProcessTestUtils.removeTestProcess(processId);
    }

    @Test
    public void addWorkflowTest() throws Exception {
        Workflow workflow = new Workflow();
        workflow.setTitle("testWorkflow");
        projectsPage.createNewWorkflow();
        assertEquals("Neuen Workflow anlegen", Pages.getWorkflowEditPage().getHeaderText(), "Header for create new workflow is incorrect");

        Pages.getWorkflowEditPage().insertWorkflowData(workflow).save();

        assertTrue(AddingST.projectsPage.isAt(), "Redirection after save was not successful");
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .untilAsserted(() -> assertEquals(3, AddingST.projectsPage.getWorkflowTitles().size(), "There should be no processes found"));
        List<String> workflowTitles = AddingST.projectsPage.getWorkflowTitles();
        boolean workflowAvailable = workflowTitles.contains("testWorkflow");
        assertTrue(workflowAvailable, "Created Workflow was not listed at workflows table!");

        new File("src/test/resources/diagrams/testWorkflow.bpmn20.xml").delete();
        new File("src/test/resources/diagrams/testWorkflow.svg").delete();
    }

    @Test
    public void addDocketTest() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("MockDocket");
        projectsPage.createNewDocket();
        assertEquals("Neuen Laufzettel anlegen", Pages.getDocketEditPage().getHeaderText(), "Header for create new docket is incorrect");

        Pages.getDocketEditPage().insertDocketData(docket).save();
        assertTrue(projectsPage.isAt(), "Redirection after save was not successful");

        List<String> docketTitles = projectsPage.getDocketTitles();
        boolean docketAvailable = docketTitles.contains(docket.getTitle());
        assertTrue(docketAvailable, "Created Docket was not listed at dockets table!");
    }

    @Test
    public void addRulesetTest() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("MockRuleset");
        projectsPage.createNewRuleset();
        assertEquals("Neuen Regelsatz anlegen", Pages.getRulesetEditPage().getHeaderText(), "Header for create new ruleset is incorrect");

        Pages.getRulesetEditPage().insertRulesetData(ruleset).save();
        assertTrue(projectsPage.isAt(), "Redirection after save was not successful");

        List<String> rulesetTitles = projectsPage.getRulesetTitles();
        boolean rulesetAvailable = rulesetTitles.contains(ruleset.getTitle());
        assertTrue(rulesetAvailable, "Created Ruleset was not listed at rulesets table!");
    }

    @Disabled("broken: this test often causes unintentional javascript warning popups when adding roles to the user")
    @Test
    public void addUserTest() throws Exception {
        User user = UserGenerator.generateUser();
        usersPage.createNewUser();
        assertEquals("Neuen Benutzer anlegen", userEditPage.getHeaderText(), "Header for create new user is incorrect");

        userEditPage.insertUserData(user);
        userEditPage.addUserToRole(ServiceManager.getRoleService().getById(2).getTitle());
        userEditPage.addUserToClient(ServiceManager.getClientService().getById(2).getName());
        userEditPage.save();
        assertTrue(usersPage.isAt(), "Redirection after save was not successful");

        User insertedUser = ServiceManager.getUserService().getByLogin(user.getLogin());

        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(insertedUser);
        Pages.getTopNavigation().selectSessionClient(1);
        assertEquals(ServiceManager.getClientService().getById(2).getName(), Pages.getTopNavigation().getSessionClient());
    }

    @Test
    public void addLdapGroupTest() throws Exception {
        LdapGroup ldapGroup = LdapGroupGenerator.generateLdapGroup();
        usersPage.createNewLdapGroup();
        assertEquals("Neue LDAP-Gruppe anlegen", Pages.getLdapGroupEditPage().getHeaderText(), "Header for create new LDAP group is incorrect");

        Pages.getLdapGroupEditPage().insertLdapGroupData(ldapGroup).save();
        assertTrue(usersPage.isAt(), "Redirection after save was not successful");

        boolean ldapGroupAvailable = usersPage.getLdapGroupNames().contains(ldapGroup.getTitle());
        assertTrue(ldapGroupAvailable, "Created ldap group was not listed at ldap group table!");

        LdapGroup actualLdapGroup = usersPage.editLdapGroup(ldapGroup.getTitle()).readLdapGroup();
        assertEquals(ldapGroup, actualLdapGroup, "Saved ldap group is giving wrong data at edit page!");
    }

    @Test
    public void addClientTest() throws Exception {
        Client client = new Client();
        client.setName("MockClient");
        usersPage.createNewClient();
        assertEquals("Neuen Mandanten anlegen", Pages.getClientEditPage().getHeaderText(), "Header for create new client is incorrect");

        Pages.getClientEditPage().insertClientData(client).save();
        assertTrue(usersPage.isAt(), "Redirection after save was not successful");

        boolean clientAvailable = usersPage.getClientNames().contains(client.getName());
        assertTrue(clientAvailable, "Created Client was not listed at clients table!");
    }

    @Test
    public void addRoleTest() throws Exception {
        Role role = new Role();
        role.setTitle("MockRole");

        usersPage.createNewRole();
        assertEquals("Neue Rolle anlegen", roleEditPage.getHeaderText(), "Header for create new role is incorrect");

        roleEditPage.setRoleTitle(role.getTitle()).assignAllGlobalAuthorities()
                .assignAllClientAuthorities();
        roleEditPage.save();
        assertTrue(usersPage.isAt(), "Redirection after save was not successful");
        List<String> roleTitles = usersPage.getRoleTitles();
        assertTrue(roleTitles.contains(role.getTitle()), "New role was not saved");

        int availableGlobalAuthorities = ServiceManager.getAuthorityService().getAllAssignableGlobal().size();
        int assignedGlobalAuthorities = usersPage.editRole(role.getTitle())
                .countAssignedGlobalAuthorities();
        assertEquals(availableGlobalAuthorities, assignedGlobalAuthorities, "Assigned authorities of the new role were not saved!");
        String actualTitle = Pages.getRoleEditPage().getRoleTitle();
        assertEquals(role.getTitle(), actualTitle, "New Name of role was not saved");

        int availableClientAuthorities = ServiceManager.getAuthorityService().getAllAssignableToClients().size();
        int assignedClientAuthorities = usersPage.editRole(role.getTitle())
                .countAssignedClientAuthorities();
        assertEquals(availableClientAuthorities, assignedClientAuthorities, "Assigned client authorities of the new role were not saved!");
    }

    @Test
    public void addCustomImportconfigurationWithUrlParameters() throws Exception {
        projectsPage.createNewImportConfiguration();
        importConfigurationEditPage.insertImportConfigurationDataWithUrlParameters();
        importConfigurationEditPage.save();
        ImportConfiguration importConfiguration = ServiceManager.getImportConfigurationService().getById(4);
        List<UrlParameter> urlParameters = importConfiguration.getUrlParameters();
        assertEquals(1, urlParameters.size(), "Wrong number of custom URL parameters");
        assertEquals("testkey", urlParameters.getFirst().getParameterKey(), "Wrong URL parameter key");
        assertEquals("testvalue", urlParameters.getFirst().getParameterValue(), "Wrong URL parameter value");
    }
}
