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

package org.kitodo.helper;

import java.util.List;
import javax.faces.model.SelectItem;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.BatchService;
import org.kitodo.services.data.ClientService;
import org.kitodo.services.data.DocketService;
import org.kitodo.services.data.ProjectService;
import org.kitodo.services.data.RulesetService;
import org.kitodo.services.data.WorkflowService;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SelectItemListIT {

    private static ServiceManager serviceManager = new ServiceManager();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();

        SecurityTestUtils.addUserDataToSecurityContext(serviceManager.getUserService().getById(1), 1);
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        SecurityTestUtils.cleanSecurityContext();

        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetBatches() throws Exception {
        BatchService batchService = serviceManager.getBatchService();

        await().untilAsserted(() -> assertEquals("Incorrect amount of select items!", 4,
            SelectItemList.getBatches(batchService.getAll()).size()));

        List<SelectItem> selectItems = SelectItemList.getBatches(batchService.getAll());

        assertEquals("First item is not sorted correctly!", "First batch (1 processes) [LOGISTIC]",
            selectItems.get(0).getLabel());
        assertEquals("Second item is not sorted correctly!", "Second batch (0 processes) [LOGISTIC]",
            selectItems.get(1).getLabel());
        assertEquals("Third item is not sorted correctly!", "Third batch (2 processes) [NEWSPAPER]",
            selectItems.get(2).getLabel());
        assertEquals("Fourth item is not sorted correctly!", "Batch 4 (0 processes) [SERIAL]",
            selectItems.get(3).getLabel());

        assertThat("First item is not a Batch type!", selectItems.get(0).getValue(), instanceOf(Batch.class));
    }

    @Test
    public void shouldGetClients() throws Exception {
        ClientService clientService = serviceManager.getClientService();

        await().untilAsserted(
            () -> assertEquals("Incorrect amount of select items!", 4, SelectItemList.getClients(clientService.getAll()).size()));

        List<SelectItem> selectItems = SelectItemList.getClients(clientService.getAll());

        assertEquals("Second item is not sorted correctly!", "First client", selectItems.get(0).getLabel());
        assertEquals("Third item is not sorted correctly!", "Not used client", selectItems.get(1).getLabel());
        assertEquals("Fourth item is not sorted correctly!", "Removable client", selectItems.get(2).getLabel());
        assertEquals("Fifth item is not sorted correctly!", "Second client", selectItems.get(3).getLabel());

        assertThat("Second item is not a Client type!", selectItems.get(1).getValue(), instanceOf(Client.class));
    }

    @Test
    public void shouldGetDockets() throws Exception {
        DocketService docketService = serviceManager.getDocketService();

        await().untilAsserted(
            () -> assertEquals("Incorrect amount of select items!", 5, SelectItemList.getDockets(docketService.getAll()).size()));

        List<SelectItem> selectItems = SelectItemList.getDockets(docketService.getAll());

        assertEquals("First item is not sorted correctly!", "Removable docket", selectItems.get(0).getLabel());
        assertEquals("Second item is not sorted correctly!", "default", selectItems.get(1).getLabel());
        assertEquals("Third item is not sorted correctly!", "second", selectItems.get(2).getLabel());
        assertEquals("Fourth item is not sorted correctly!", "tester", selectItems.get(3).getLabel());
        assertEquals("Fifth item is not sorted correctly!", "third", selectItems.get(4).getLabel());

        assertThat("First item is not a Docket type!", selectItems.get(0).getValue(), instanceOf(Docket.class));
    }

    @Test
    public void shouldGetLdapGroups() {
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of select items!", 1, SelectItemList.getLdapGroups().size()));

        List<SelectItem> selectItems = SelectItemList.getLdapGroups();

        assertEquals("First item is not sorted correctly!", "LG", selectItems.get(0).getLabel());

        assertThat("First item is not a LdapGroup type!", selectItems.get(0).getValue(), instanceOf(LdapGroup.class));
    }

    @Test
    public void shouldGetProcessesForChoiceList() {
        assertEquals("Incorrect amount of select items!", 1, SelectItemList.getProcessesForChoiceList().size());

        List<SelectItem> selectItems = SelectItemList.getProcessesForChoiceList();

        assertEquals("First item is not sorted correctly!", "First process", selectItems.get(0).getLabel());

        assertThat("First item is not an Process type!", selectItems.get(0).getValue(), instanceOf(Process.class));
    }

    @Test
    public void shouldGetProjects() throws Exception {
        ProjectService projectService = serviceManager.getProjectService();

        await().untilAsserted(
            () -> assertEquals("Incorrect amount of select items!", 3, SelectItemList.getProjects(projectService.getAll()).size()));

        List<SelectItem> selectItems = SelectItemList.getProjects(projectService.getAll());

        assertEquals("First item is not sorted correctly!", "First project", selectItems.get(0).getLabel());
        assertEquals("Second item is not sorted correctly!", "Inactive project", selectItems.get(1).getLabel());
        assertEquals("Third item is not sorted correctly!", "Second project", selectItems.get(2).getLabel());

        assertThat("First item is not a Project type!", selectItems.get(0).getValue(), instanceOf(Project.class));
    }

    @Test
    public void shouldGetRulesets() throws Exception {
        RulesetService rulesetService = serviceManager.getRulesetService();

        await().untilAsserted(
            () -> assertEquals("Incorrect amount of select items!", 4, SelectItemList.getRulesets(rulesetService.getAll()).size()));

        List<SelectItem> selectItems = SelectItemList.getRulesets(rulesetService.getAll());

        assertEquals("First item is not sorted correctly!", "Removable ruleset", selectItems.get(0).getLabel());
        assertEquals("Second item is not sorted correctly!", "SLUBBB", selectItems.get(1).getLabel());
        assertEquals("Third item is not sorted correctly!", "SLUBDD", selectItems.get(2).getLabel());
        assertEquals("Fourth item is not sorted correctly!", "SLUBHH", selectItems.get(3).getLabel());

        assertThat("First item is not a Ruleset type!", selectItems.get(0).getValue(), instanceOf(Ruleset.class));
    }

    @Test
    public void shouldGetWorkflows() throws Exception {
        WorkflowService workflowService = serviceManager.getWorkflowService();

        await().untilAsserted(
            () -> assertEquals("Incorrect amount of select items!", 2, SelectItemList.getWorkflows(workflowService.getAll()).size()));

        List<SelectItem> selectItems = SelectItemList.getWorkflows(workflowService.getAll());

        assertEquals("First item is not sorted correctly!", "gateway", selectItems.get(0).getLabel());
        assertEquals("Second item is not sorted correctly!", "test", selectItems.get(1).getLabel());

        assertThat("First item is not a Workflow type!", selectItems.get(0).getValue(), instanceOf(Workflow.class));
    }
}
