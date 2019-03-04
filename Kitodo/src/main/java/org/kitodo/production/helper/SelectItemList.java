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

package org.kitodo.production.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.enums.WorkflowStatus;
import org.kitodo.production.services.ServiceManager;

public class SelectItemList {

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
     * @param clients
     *            to convert to select list
     * @return list of clients as SelectItem list
     */
    public static List<SelectItem> getClients(List<Client> clients) {
        List<SelectItem> selectItems = new ArrayList<>();
        clients.sort(Comparator.comparing(Client::getName));
        for (Client client : clients) {
            selectItems.add(new SelectItem(client, client.getName(), null));
        }
        return selectItems;
    }

    /**
     * Get dockets for select list.
     *
     * @param dockets
     *            to convert to select list
     * @return list of dockets as SelectItem list
     */
    public static List<SelectItem> getDockets(List<Docket> dockets) {
        List<SelectItem> selectItems = new ArrayList<>();
        dockets.sort(Comparator.comparing(Docket::getTitle));
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
        List<LdapGroup> ldapGroups = ServiceManager.getLdapGroupService().getByQuery("from LdapGroup ORDER BY title");
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
        User currentUser = ServiceManager.getUserService().getCurrentUser();
        for (Project project : currentUser.getProjects()) {
            processes.addAll(project.getProcesses());
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
        processes.sort(Comparator.comparing(Process::getTitle));
        for (Process process : processes) {
            selectItems.add(new SelectItem(process, process.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get list of projects for select list.
     *
     * @param projects
     *            to convert to select list
     * @return list of SelectItem objects
     */
    public static List<SelectItem> getProjects(List<Project> projects) {
        List<SelectItem> selectItems = new ArrayList<>();
        projects.sort(Comparator.comparing(Project::getTitle));
        for (Project project : projects) {
            selectItems.add(new SelectItem(project, project.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get rulesets for select list.
     *
     * @param rulesets
     *            to convert to select list
     * @return list of rulesets as SelectItem list
     */
    public static List<SelectItem> getRulesets(List<Ruleset> rulesets) {
        List<SelectItem> selectItems = new ArrayList<>();
        rulesets.sort(Comparator.comparing(Ruleset::getTitle));
        for (Ruleset ruleset : rulesets) {
            selectItems.add(new SelectItem(ruleset, ruleset.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get task statuses for select list.
     *
     * @return list of task statuses as SelectItem list
     */
    public static List<SelectItem> getTaskStatuses() {
        List<SelectItem> selectItems = new ArrayList<>();
        selectItems.add(new SelectItem(TaskStatus.LOCKED, Helper.getTranslation("locked"), null));
        selectItems.add(new SelectItem(TaskStatus.OPEN, Helper.getTranslation("statusOpen"), null));
        selectItems.add(new SelectItem(TaskStatus.INWORK, Helper.getTranslation("statusInProcessing"), null));
        selectItems.add(new SelectItem(TaskStatus.DONE, Helper.getTranslation("statusDone"), null));
        return selectItems;
    }

    /**
     * Get list of workflows for select list.
     *
     * @param workflows
     *            to convert to select list
     * @return list of SelectItem objects
     */
    public static List<SelectItem> getWorkflows(List<Workflow> workflows) {
        List<SelectItem> selectItems = new ArrayList<>();
        workflows.sort(Comparator.comparing(Workflow::getTitle));
        for (Workflow workflow : workflows) {
            selectItems.add(new SelectItem(workflow, workflow.getTitle(), null));
        }
        return selectItems;
    }

    /**
     * Get list of workflow statues for select list.
     *
     * @return list of SelectItem objects
     */
    // TODO: exact text for description is needed
    public static List<SelectItem> getWorkflowStatuses() {
        List<SelectItem> selectItems = new ArrayList<>();
        selectItems.add(new SelectItem(WorkflowStatus.DRAFT, Helper.getTranslation("draft"), null));
        selectItems.add(new SelectItem(WorkflowStatus.ACTIVE, Helper.getTranslation("active"), null));
        // TODO: get information if it is possible to change from draft to archived
        selectItems.add(new SelectItem(WorkflowStatus.ARCHIVED, Helper.getTranslation("archived"), null));
        return selectItems;
    }
}
