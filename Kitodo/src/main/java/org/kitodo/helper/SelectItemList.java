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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.kitodo.data.database.beans.BaseTemplateBean;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.services.ServiceManager;

public class SelectItemList {

    private static ServiceManager serviceManager = new ServiceManager();

    /**
     * Private constructor to hide public one.
     */
    private SelectItemList() {

    }

    /**
     * Get batches for select list.
     *
     * @param batches
     *            to convert to select list
     * @return list of batches as SelectItem list
     */
    public static List<SelectItem> getBatches(List<Batch> batches) {
        List<SelectItem> selectItems = new ArrayList<>();
        for (Batch batch : batches) {
            selectItems.add(new SelectItem(batch, batch.toString(), null));
        }
        return selectItems;
    }

    /**
     * Get clients for select list.
     *
     * @return list of clients as SelectItem list
     */
    public static List<SelectItem> getClients() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Client> clients = serviceManager.getClientService().getByQuery("from Client ORDER BY name");
        for (Client client : clients) {
            selectItems.add(new SelectItem(client, client.getName(), null));
        }
        return selectItems;
    }

    /**
     * Get dockets for select list.
     *
     * @return list of dockets as SelectItem list
     */
    public static List<SelectItem> getDockets() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Docket> dockets = serviceManager.getDocketService().getByQuery("from Docket ORDER BY title");
        for (Docket docket : dockets) {
            selectItems.add(new SelectItem(docket, docket.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get LDAP groups for select list.
     * 
     * @return list of LDAP groups as SelectItem list
     */
    public static List<SelectItem> getLdapGroups() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<LdapGroup> ldapGroups = serviceManager.getLdapGroupService().getByQuery("from LdapGroup ORDER BY title");
        for (LdapGroup ldapGroup : ldapGroups) {
            selectItems.add(new SelectItem(ldapGroup, ldapGroup.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get list of processes for select list.
     * 
     * @return list of templates as SelectItem list
     */
    // TODO: check if this is still true - why take processes if it should be
    // templates
    public static List<SelectItem> getProcessesForChoiceList() {
        List<Process> processes = new ArrayList<>();
        User currentUser = serviceManager.getUserService().getAuthenticatedUser();
        if (Objects.nonNull(currentUser)) {
            for (Project project : currentUser.getProjects()) {
                processes.addAll(project.getProcesses());
            }
        }
        processes = processes.stream().filter(BaseTemplateBean::getInChoiceListShown).collect(Collectors.toList());

        return getProcesses(processes);
    }

    /**
     * Get processes for select list.
     *
     * @param processes
     *            to convert to select list
     * @return list of processes as SelectItem list
     */
    public static List<SelectItem> getProcesses(List<Process> processes) {
        List<SelectItem> selectItems = new ArrayList<>();
        for (Process process : processes) {
            selectItems.add(new SelectItem(process, process.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get list of projects for select list.
     *
     * @return list of SelectItem objects
     */
    public static List<SelectItem> getProjects() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Project> temp = serviceManager.getProjectService().getByQuery("from Project ORDER BY title");
        for (Project project : temp) {
            selectItems.add(new SelectItem(project, project.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get rulesets for select list.
     *
     * @return list of rulesets as SelectItem list
     */
    public static List<SelectItem> getRulesets() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Ruleset> rulesets = serviceManager.getRulesetService().getByQuery("from Ruleset ORDER BY title");
        for (Ruleset ruleset : rulesets) {
            selectItems.add(new SelectItem(ruleset, ruleset.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get list of workflows for select list.
     *
     * @return list of SelectItem objects
     */
    public static List<SelectItem> getWorkflows() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Workflow> workflows = serviceManager.getWorkflowService().getAvailableWorkflows();
        for (Workflow workflow : workflows) {
            selectItems.add(new SelectItem(workflow, workflow.getTitle(), null));
        }
        return selectItems;
    }
}
