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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.xebialabs.restito.server.StubServer;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
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
    private static final String TEST_METADATA_FILE = "testMetadataFileServiceTest.xml";
    private static int secondProcessId = -1;
    private static final String PICA_PPN = "pica.ppn";
    private static final String PICA_XML = "picaxml";
    private static final String TEST_FILE_PATH = "src/test/resources/sruTestRecord.xml";

    @BeforeClass
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
        assertTrue("Should find exactly one second process!", secondProcessId > 0);
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
    @AfterClass
    public static void removeUnsuitableParentTestProcess() throws DAOException {
        ProcessTestUtils.removeTestProcess(secondProcessId);
        server.stop();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void addBatchTest() throws Exception {
        processesPage.createNewBatch();
        await().untilAsserted(() -> assertEquals("Batch was inserted!", 1,
            ServiceManager.getBatchService().getByQuery("FROM Batch WHERE title = 'SeleniumBatch'").size()));
    }

    @Test
    public void addProjectTest() throws Exception {
        Project project = ProjectGenerator.generateProject();
        projectsPage.createNewProject();
        assertEquals("Header for create new project is incorrect", "Neues Projekt",
            Pages.getProjectEditPage().getHeaderText());

        Pages.getProjectEditPage().insertProjectData(project).save();
        assertTrue("Redirection after save was not successful", projectsPage.isAt());

        boolean projectAvailable = Pages.getProjectsPage().getProjectsTitles().contains(project.getTitle());
        assertTrue("Created Project was not listed at projects table!", projectAvailable);
    }

    @Test
    public void addTemplateTest() throws Exception {
        Template template = new Template();
        template.setTitle("MockTemplate");
        projectsPage.createNewTemplate();
        assertEquals("Header for create new template is incorrect", "Neue Produktionsvorlage",
            Pages.getTemplateEditPage().getHeaderText());

        Pages.getTemplateEditPage().insertTemplateData(template).save();
        await().until(() -> projectsPage.countListedTemplates() == 3);
        boolean templateAvailable = projectsPage.getTemplateTitles().contains(template.getTitle());
        assertTrue("Created Template was not listed at templates table!", templateAvailable);
    }

    @Test
    public void addProcessesTest() throws Exception {
        projectsPage.createNewProcess();
        assertEquals("Header for create new process is incorrect", "Einen neuen Vorgang anlegen (Produktionsvorlage: 'First template')",
            Pages.getProcessFromTemplatePage().getHeaderText());

        String generatedTitle = Pages.getProcessFromTemplatePage().createProcess();
        boolean processAvailable = processesPage.getProcessTitles().contains(generatedTitle);
        assertTrue("Created Process was not listed at processes table!", processAvailable);

        ProcessService processService = ServiceManager.getProcessService();
        Optional<Process> optionalProcess = processService.getAll().stream().filter(process -> generatedTitle
                .equals(process.getTitle())).findAny();
        assertTrue("Generated process not found in database", optionalProcess.isPresent());
        Process generatedProcess = optionalProcess.get();
        assertNull("Created Process unexpectedly got a parent!", generatedProcess.getParent());

        projectsPage.createNewProcess();
        String generatedChildTitle = Pages.getProcessFromTemplatePage()
                .createProcessAsChild(generatedProcess.getTitle());

        boolean childProcessAvailable = processesPage.getProcessTitles().contains(generatedChildTitle);
        assertTrue("Created Process was not listed at processes table!", childProcessAvailable);

        Optional<Process> optionalChildProcess = processService.getAll().stream().filter(process -> generatedChildTitle
                .equals(process.getTitle())).findAny();
        assertTrue("Generated child process not found in database", optionalChildProcess.isPresent());
        Process generatedChildProcess = optionalChildProcess.get();
        assertEquals("Created Process has a wrong parent!", generatedProcess, generatedChildProcess.getParent());
        ProcessTestUtils.removeTestProcess(generatedProcess.getId());
    }

    @Test
    public void addProcessAsChildNotPossible() throws Exception {
        projectsPage.createNewProcess();
        boolean errorMessageShowing = Pages.getProcessFromTemplatePage().createProcessAsChildNotPossible();
        assertTrue("There was no error!", errorMessageShowing);
        Pages.getProcessFromTemplatePage().cancel();
    }

    @Test
    public void addProcessFromCatalogTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS);

        projectsPage.createNewProcess();
        assertEquals("Header for create new process is incorrect", "Einen neuen Vorgang anlegen (Produktionsvorlage: 'First template')",
                Pages.getProcessFromTemplatePage().getHeaderText());

        String generatedTitle = Pages.getProcessFromTemplatePage().createProcessFromCatalog();
        boolean processAvailable = processesPage.getProcessTitles().contains(generatedTitle);
        assertTrue("Created Process was not listed at processes table!", processAvailable);
        int index = processesPage.getProcessTitles().indexOf(generatedTitle);
        assertTrue("Process table does not contain ID or new process", index >= 0);
        int processId = Integer.parseInt(processesPage.getProcessIds().get(index));
        ProcessTestUtils.removeTestProcess(processId);
    }

    @Test
    public void addWorkflowTest() throws Exception {
        Workflow workflow = new Workflow();
        workflow.setTitle("testWorkflow");
        projectsPage.createNewWorkflow();
        assertEquals("Header for create new workflow is incorrect", "Neuen Workflow anlegen",
            Pages.getWorkflowEditPage().getHeaderText());

        Pages.getWorkflowEditPage().insertWorkflowData(workflow).save();

        assertTrue("Redirection after save was not successful", AddingST.projectsPage.isAt());
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .untilAsserted(() -> assertEquals("There should be no processes found", 3,
                    AddingST.projectsPage.getWorkflowTitles().size()));
        List<String> workflowTitles = AddingST.projectsPage.getWorkflowTitles();
        boolean workflowAvailable = workflowTitles.contains("testWorkflow");
        assertTrue("Created Workflow was not listed at workflows table!", workflowAvailable);

        new File("src/test/resources/diagrams/testWorkflow.bpmn20.xml").delete();
        new File("src/test/resources/diagrams/testWorkflow.svg").delete();
    }

    @Test
    public void addDocketTest() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("MockDocket");
        projectsPage.createNewDocket();
        assertEquals("Header for create new docket is incorrect", "Neuen Laufzettel anlegen",
            Pages.getDocketEditPage().getHeaderText());

        Pages.getDocketEditPage().insertDocketData(docket).save();
        assertTrue("Redirection after save was not successful", projectsPage.isAt());

        List<String> docketTitles = projectsPage.getDocketTitles();
        boolean docketAvailable = docketTitles.contains(docket.getTitle());
        assertTrue("Created Docket was not listed at dockets table!", docketAvailable);
    }

    @Test
    public void addRulesetTest() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("MockRuleset");
        projectsPage.createNewRuleset();
        assertEquals("Header for create new ruleset is incorrect", "Neuen Regelsatz anlegen",
            Pages.getRulesetEditPage().getHeaderText());

        Pages.getRulesetEditPage().insertRulesetData(ruleset).save();
        assertTrue("Redirection after save was not successful", projectsPage.isAt());

        List<String> rulesetTitles = projectsPage.getRulesetTitles();
        boolean rulesetAvailable = rulesetTitles.contains(ruleset.getTitle());
        assertTrue("Created Ruleset was not listed at rulesets table!", rulesetAvailable);
    }

    @Ignore("broken: this test often causes unintentional javascript warning popups when adding roles to the user")
    @Test
    public void addUserTest() throws Exception {
        User user = UserGenerator.generateUser();
        usersPage.createNewUser();
        assertEquals("Header for create new user is incorrect", "Neuen Benutzer anlegen",
                userEditPage.getHeaderText());

        userEditPage.insertUserData(user);
        userEditPage.addUserToRole(ServiceManager.getRoleService().getById(2).getTitle());
        userEditPage.addUserToClient(ServiceManager.getClientService().getById(2).getName());
        userEditPage.save();
        assertTrue("Redirection after save was not successful", usersPage.isAt());

        User insertedUser = ServiceManager.getUserService().getByLogin(user.getLogin());

        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(insertedUser);
        Pages.getTopNavigation().selectSessionClient(1);
        assertEquals(ServiceManager.getClientService().getById(2).getName(),
            Pages.getTopNavigation().getSessionClient());
    }

    @Test
    public void addLdapGroupTest() throws Exception {
        LdapGroup ldapGroup = LdapGroupGenerator.generateLdapGroup();
        usersPage.createNewLdapGroup();
        assertEquals("Header for create new LDAP group is incorrect", "Neue LDAP-Gruppe anlegen",
            Pages.getLdapGroupEditPage().getHeaderText());

        Pages.getLdapGroupEditPage().insertLdapGroupData(ldapGroup).save();
        assertTrue("Redirection after save was not successful", usersPage.isAt());

        boolean ldapGroupAvailable = usersPage.getLdapGroupNames().contains(ldapGroup.getTitle());
        assertTrue("Created ldap group was not listed at ldap group table!", ldapGroupAvailable);

        LdapGroup actualLdapGroup = usersPage.editLdapGroup(ldapGroup.getTitle()).readLdapGroup();
        assertEquals("Saved ldap group is giving wrong data at edit page!", ldapGroup, actualLdapGroup);
    }

    @Test
    public void addClientTest() throws Exception {
        Client client = new Client();
        client.setName("MockClient");
        usersPage.createNewClient();
        assertEquals("Header for create new client is incorrect", "Neuen Mandanten anlegen",
            Pages.getClientEditPage().getHeaderText());

        Pages.getClientEditPage().insertClientData(client).save();
        assertTrue("Redirection after save was not successful", usersPage.isAt());

        boolean clientAvailable = usersPage.getClientNames().contains(client.getName());
        assertTrue("Created Client was not listed at clients table!", clientAvailable);
    }

    @Test
    public void addRoleTest() throws Exception {
        Role role = new Role();
        role.setTitle("MockRole");

        usersPage.createNewRole();
        assertEquals("Header for create new role is incorrect", "Neue Rolle anlegen",
                roleEditPage.getHeaderText());

        roleEditPage.setRoleTitle(role.getTitle()).assignAllGlobalAuthorities()
                .assignAllClientAuthorities();
        roleEditPage.save();
        assertTrue("Redirection after save was not successful", usersPage.isAt());
        List<String> roleTitles = usersPage.getRoleTitles();
        assertTrue("New role was not saved", roleTitles.contains(role.getTitle()));

        int availableGlobalAuthorities = ServiceManager.getAuthorityService().getAllAssignableGlobal().size();
        int assignedGlobalAuthorities = usersPage.editRole(role.getTitle())
                .countAssignedGlobalAuthorities();
        assertEquals("Assigned authorities of the new role were not saved!", availableGlobalAuthorities,
                assignedGlobalAuthorities);
        String actualTitle = Pages.getRoleEditPage().getRoleTitle();
        assertEquals("New Name of role was not saved", role.getTitle(), actualTitle);

        int availableClientAuthorities = ServiceManager.getAuthorityService().getAllAssignableToClients().size();
        int assignedClientAuthorities = usersPage.editRole(role.getTitle())
                .countAssignedClientAuthorities();
        assertEquals("Assigned client authorities of the new role were not saved!", availableClientAuthorities,
            assignedClientAuthorities);
    }

    @Test
    public void addCustomImportconfigurationWithUrlParameters() throws Exception {
        projectsPage.createNewImportConfiguration();
        importConfigurationEditPage.insertImportConfigurationDataWithUrlParameters();
        importConfigurationEditPage.save();
        ImportConfiguration importConfiguration = ServiceManager.getImportConfigurationService().getById(4);
        List<UrlParameter> urlParameters = importConfiguration.getUrlParameters();
        assertEquals("Wrong number of custom URL parameters", 1, urlParameters.size());
        assertEquals("Wrong URL parameter key", "testkey", urlParameters.get(0).getParameterKey());
        assertEquals("Wrong URL parameter value", "testvalue", urlParameters.get(0).getParameterValue());
    }
}
