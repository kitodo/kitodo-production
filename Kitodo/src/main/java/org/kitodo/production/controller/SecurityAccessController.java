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

package org.kitodo.production.controller;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.security.SecurityAccessService;

/**
 * Controller for checking authorities of current user.
 */
@Named("SecurityAccessController")
@RequestScoped
public class SecurityAccessController {
    private SecurityAccessService securityAccessService = ServiceManager.getSecurityAccessService();

    /**
     * Check if the current user has a specified authority globally or for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForClient(String authorityTitle) {
        return securityAccessService.hasAuthorityGlobalOrForClient(authorityTitle);
    }

    /**
     * Checks if the current user has a specified authority globally.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobal(String authorityTitle) {
        return securityAccessService.hasAuthorityGlobal(authorityTitle);
    }

    /**
     * Check if the current user has a specified authority for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return true if the current user has the specified authority
     */
    public boolean hasAuthorityForClient(String authorityTitle) {
        return securityAccessService.hasAuthorityForClient(authorityTitle);
    }

    /**
     * Checks if the current user has any of the specified authorities globally or
     * for client.
     *
     * @param authorityTitles
     *            the authority title
     * @return True if the current user has the specified authority.
     */
    public boolean hasAnyAuthorityGlobalOrForClient(String authorityTitles) {
        return securityAccessService.hasAnyAuthorityGlobalOrForClient(authorityTitles);
    }

    /**
     * Check if the current user has any of the specified authorities for client.
     *
     * @param authorityTitles
     *            the authority title
     * @return true if the current user has the specified authority
     */
    public boolean hasAnyAuthorityForClient(String authorityTitles) {
        return securityAccessService.hasAnyAuthorityForClient(authorityTitles);
    }

    /**
     * Check if the current user has the authority to add the batch.
     *
     * @return true if the current user has the authority to add the batch
     */
    public boolean hasAuthorityToAddBatch() {
        return securityAccessService.hasAuthorityToAddBatch();
    }

    /**
     * Check if the current user has the authority to add the process.
     *
     * @return true if the current user has the authority to add the process
     */
    public boolean hasAuthorityToAddProcess() {
        return securityAccessService.hasAuthorityToAddProcess();
    }

    /**
     * Check if the current user has the authority to add the project.
     *
     * @return true if the current user has the authority to add the project
     */
    public boolean hasAuthorityToAddProject() {
        return securityAccessService.hasAuthorityToAddProject();
    }

    /**
     * Check if the current user has the authority to add the template.
     *
     * @return true if the current user has the authority to add the template
     */
    public boolean hasAuthorityToAddTemplate() {
        return securityAccessService.hasAuthorityToAddTemplate();
    }

    /**
     * Check if the current user has the authority to add the workflow.
     *
     * @return true if the current user has the authority to add the workflow
     */
    public boolean hasAuthorityToAddWorkflow() {
        return securityAccessService.hasAuthorityToAddWorkflow();
    }

    /**
     * Checks if the current user has the authority to add the docket.
     *
     * @return true if the current user has the authority to add the docket
     */
    public boolean hasAuthorityToAddDocket() {
        return securityAccessService.hasAuthorityToAddDocket();
    }

    /**
     * Check if the current user has the authority to add the ruleset.
     *
     * @return true if the current user has the authority to add the ruleset
     */
    public boolean hasAuthorityToAddRuleset() {
        return securityAccessService.hasAuthorityToAddRuleset();
    }

    /**
     * Check if the current user has the authority to add the user.
     *
     * @return True if the current user has the authority to add the user
     */
    public boolean hasAuthorityToAddUser() {
        return securityAccessService.hasAuthorityToAddUser();
    }

    /**
     * Check if the current user has the authority to add the role.
     *
     * @return true if the current user has the authority to add the role
     */
    public boolean hasAuthorityToAddRole() {
        return securityAccessService.hasAuthorityToAddRole();
    }

    /**
     * Check if the current user has the authority to add the client.
     *
     * @return true if the current user has the authority to add the client
     */
    public boolean hasAuthorityToAddClient() {
        return securityAccessService.hasAuthorityToAddClient();
    }

    /**
     * Check if the current user has the authority to add the authority.
     *
     * @return true if the current user has the authority to add the authority
     */
    public boolean hasAuthorityToAddAuthority() {
        return securityAccessService.hasAuthorityToAddAuthority();
    }

    /**
     * Check if current user has authority to add anything on user page. It returns
     * true if user has at least one of below given authorities.
     *
     * @return true if user has authority 'addUser' or 'addRole' or 'addClient' or
     *         'addLdapGroup' or 'addLdapServer' globally or for client
     */
    public boolean hasAuthorityToAddOnUserPage() {
        return securityAccessService.hasAuthorityToAddOnUserPage();
    }

    /**
     * Check if current user has authority to add anything on project page. It
     * returns true if user has at least one of below given authorities.
     *
     * @return true if user has authority 'addProject' or 'addTemplate' or
     *         'addWorkflow' or 'addDocket' or 'addRuleset' for client
     */
    public boolean hasAuthorityToAddOnProjectPage() {
        return securityAccessService.hasAuthorityToAddOnProjectPage();
    }

    /**
     * Check if the current user has the authority to delete the batch.
     *
     * @return true if the current user has the authority to delete the batch
     */
    public boolean hasAuthorityToDeleteBatch() {
        return securityAccessService.hasAuthorityToDeleteBatch();
    }

    /**
     * Check if the current user has the authority to delete the process.
     *
     * @return true if the current user has the authority to delete the process
     */
    public boolean hasAuthorityToDeleteProcess() {
        return securityAccessService.hasAuthorityToDeleteProcess();
    }

    /**
     * Check if the current user has the authority to delete the tak.
     *
     * @return true if the current user has the authority to delete the task
     */
    public boolean hasAuthorityToDeleteTask() {
        return securityAccessService.hasAuthorityToDeleteTask();
    }

    /**
     * Check if the current user has the authority to delete the project.
     *
     * @return true if the current user has the authority to delete the project
     */
    public boolean hasAuthorityToDeleteProject() {
        return securityAccessService.hasAuthorityToDeleteProject();
    }

    /**
     * Check if the current user has the authority to delete the template.
     *
     * @return true if the current user has the authority to delete the template
     */
    public boolean hasAuthorityToDeleteTemplate() {
        return securityAccessService.hasAuthorityToDeleteTemplate();
    }

    /**
     * Check if the current user has the authority to edit the workflow.
     *
     * @return true if the current user has the authority to edit the workflow
     */
    public boolean hasAuthorityToDeleteWorkflow() {
        return securityAccessService.hasAuthorityToDeleteWorkflow();
    }

    /**
     * Check if the current user has the authority to delete the docket.
     *
     * @return true if the current user has the authority to delete the docket
     */
    public boolean hasAuthorityToDeleteDocket() {
        return securityAccessService.hasAuthorityToDeleteDocket();
    }

    /**
     * Check if the current user has the authority to delete the ruleset.
     *
     * @return true if the current user has the authority to delete the ruleset
     */
    public boolean hasAuthorityToDeleteRuleset() {
        return securityAccessService.hasAuthorityToDeleteRuleset();
    }

    /**
     * Check if the current user has the authority to delete the user.
     *
     * @return true if the current user has the authority to delete the user
     */
    public boolean hasAuthorityToDeleteUser() {
        return securityAccessService.hasAuthorityToDeleteUser();
    }

    /**
     * Check if the current user has the authority to delete the role.
     *
     * @return true if the current user has the authority to delete the role
     */
    public boolean hasAuthorityToDeleteRole() {
        return securityAccessService.hasAuthorityToDeleteRole();
    }

    /**
     * Check if the current user has the authority to delete the client.
     *
     * @return true if the current user has the authority to delete the client
     */
    public boolean hasAuthorityToDeleteClient() {
        return securityAccessService.hasAuthorityToDeleteClient();
    }

    /**
     * Check if the current user has the authority to delete the authority.
     *
     * @return true if the current user has the authority to delete the authority
     */
    public boolean hasAuthorityToDeleteAuthority() {
        return securityAccessService.hasAuthorityToDeleteAuthority();
    }

    /**
     * Check if the current user has the authority to edit the task.
     *
     * @return true if the current user has the authority to edit the task
     */
    public boolean hasAuthorityToEditTask() {
        return securityAccessService.hasAuthorityToEditTask();
    }

    /**
     * Check if the current user has the authority to edit the task.
     *
     * @param taskId the specific taskId
     * @return true if the current user has the authority to edit the task
     */
    public boolean hasAuthorityToEditTask(int taskId) throws DataException {
        return securityAccessService.hasAuthorityToEditTask(taskId);
    }
    /**
     * Check if the current user has the authority to edit the batch.
     *
     * @return true if the current user has the authority to edit the batch
     */
    public boolean hasAuthorityToEditBatch() {
        return securityAccessService.hasAuthorityToEditBatch();
    }

    /**
     * Check if the current user has the authority to edit the process.
     *
     * @return true if the current user has the authority to edit the process
     */
    public boolean hasAuthorityToEditProcess() {
        return securityAccessService.hasAuthorityToEditProcess();
    }

    /**
     * Check if the current user has the authority to edit the process.
     *
     * @param processId the specific processId
     * @return true if the current user has the authority to edit the process
     */
    public boolean hasAuthorityToEditProcess(int processId) throws DataException {
        return securityAccessService.hasAuthorityToEditProcess(processId);
    }

    /**
     * Check if the current user has the authority to edit the project.
     *
     * @return true if the current user has the authority to edit the project
     */
    public boolean hasAuthorityToEditProject() {
        return securityAccessService.hasAuthorityToEditProject();
    }

    /**
     * Check if the current user has the authority to edit the project.
     *
     * @param projectId the specific projectId
     * @return true if the current user has the authority to edit the project
     */
    public boolean hasAuthorityToEditProject(int projectId) {
        return securityAccessService.hasAuthorityToEditProject(projectId);
    }

    /**
     * Check if the current user has the authority to edit the template.
     *
     * @return true if the current user has the authority to edit the template
     */
    public boolean hasAuthorityToEditTemplate() {
        return securityAccessService.hasAuthorityToEditTemplate();
    }

    /**
     * Check if the current user has the authority to edit the workflow.
     *
     * @return true if the current user has the authority to edit the workflow
     */
    public boolean hasAuthorityToEditWorkflow() {
        return securityAccessService.hasAuthorityToEditWorkflow();
    }

    /**
     * Check if the current user has the authority to edit the docket.
     *
     * @return true if the current user has the authority to edit the docket
     */
    public boolean hasAuthorityToEditDocket() {
        return securityAccessService.hasAuthorityToEditDocket();
    }

    /**
     * Check if the current user has the authority to edit the ruleset.
     *
     * @return true if the current user has the authority to edit the ruleset
     */
    public boolean hasAuthorityToEditRuleset() {
        return securityAccessService.hasAuthorityToEditRuleset();
    }

    /**
     * Check if the current user has the authority to edit the user.
     *
     * @return True if the current user has the authority to edit the user
     */
    public boolean hasAuthorityToEditUser() {
        return securityAccessService.hasAuthorityToEditUser();
    }

    /**
     * Check if the current user has the authority to edit the role.
     *
     * @return true if the current user has the authority to edit the role
     */
    public boolean hasAuthorityToEditRole() {
        return securityAccessService.hasAuthorityToEditRole();
    }

    /**
     * Check if the current user has the authority to edit the client.
     *
     * @return true if the current user has the authority to edit the client
     */
    public boolean hasAuthorityToEditClient() {
        return securityAccessService.hasAuthorityToEditClient();
    }

    /**
     * Check if the current user has the authority to edit the authority.
     *
     * @return true if the current user has the authority to edit the authority
     */
    public boolean hasAuthorityToEditAuthority() {
        return securityAccessService.hasAuthorityToEditAuthority();
    }

    /**
     * Check if the current user has the authority to edit the index.
     *
     * @return true if the current user has the authority to edit the index
     */
    public boolean hasAuthorityToEditIndex() {
        return securityAccessService.hasAuthorityToEditIndex();
    }

    /**
     * Check if the current user has the authority to export process.
     *
     * @return true if the current user has the authority to export process
     */
    public boolean hasAuthorityToExportProcess() {
        return securityAccessService.hasAuthorityToExportProcess();
    }

    /**
     * Check if the current user has the authority to view the process.
     *
     * @return true if the current user has the authority to view the process
     */
    public boolean hasAuthorityToViewProcess() {
        return securityAccessService.hasAuthorityToViewProcess();
    }

    /**
     * Check if the current user has the authority to view the process.
     *
     * @param processId the specific processId
     * @return true if the current user has the authority to view the process
     */
    public boolean hasAuthorityToViewProcess(int processId) throws DataException {
        return securityAccessService.hasAuthorityToViewProcess(processId);
    }

    /**
     * Check if the current user has the authority to view the project.
     *
     * @return true if the current user has the authority to view the project
     */
    public boolean hasAuthorityToViewProject() {
        return securityAccessService.hasAuthorityToViewProject();
    }

    /**
     * Check if the current user has the authority to view the project.
     *
     * @param projectId the specific projectId
     * @return true if the current user has the authority to view the project
     */
    public boolean hasAuthorityToViewProject(int projectId) {
        return securityAccessService.hasAuthorityToViewProject(projectId);
    }

    /**
     * Check if the current user has the authority to view the template.
     *
     * @return true if the current user has the authority to view the template
     */
    public boolean hasAuthorityToViewTemplate() {
        return securityAccessService.hasAuthorityToViewTemplate();
    }

    /**
     * Check if the current user has the authority to view the workflow.
     *
     * @return true if the current user has the authority to view the workflow
     */
    public boolean hasAuthorityToViewWorkflow() {
        return securityAccessService.hasAuthorityToViewWorkflow();
    }

    /**
     * Check if the current user has the authority to view the docket.
     *
     * @return true if the current user has the authority to view the docket
     */
    public boolean hasAuthorityToViewDocket() {
        return securityAccessService.hasAuthorityToViewDocket();
    }

    /**
     * Check if the current user has the authority to view the ruleset.
     *
     * @return true if the current user has the authority to view the ruleset
     */
    public boolean hasAuthorityToViewRuleset() {
        return securityAccessService.hasAuthorityToViewRuleset();
    }

    /**
     * Check if the current user has the authority to view the user.
     *
     * @return true if the current user has the authority to view the user
     */
    public boolean hasAuthorityToViewUser() {
        return securityAccessService.hasAuthorityToViewUser();
    }

    /**
     * Check if the current user has the authority to change the user config.
     *
     * @param userId
     *            id of the viewed user
     * @return true if the current user has the authority to change the user config
     */
    public boolean hasAuthorityToConfigUser(int userId) {
        return securityAccessService.hasAuthorityToConfigUser(userId);
    }

    /**
     * Check if the current user has the authority to view the role.
     *
     * @return true if the current user has the authority to view the role
     */
    public boolean hasAuthorityToViewRole() {
        return securityAccessService.hasAuthorityToViewRole();
    }

    /**
     * Check if the current user has the authority to view the client.
     *
     * @return true if the current user has the authority to view the client
     */
    public boolean hasAuthorityToViewClient() {
        return securityAccessService.hasAuthorityToViewClient();
    }

    /**
     * Check if the current user has the authority to view the authority.
     *
     * @return true if the current user has the authority to view the authority
     */
    public boolean hasAuthorityToViewAuthority() {
        return securityAccessService.hasAuthorityToViewAuthority();
    }

    /**
     * Check if the current user has the authority to view the index.
     *
     * @return true if the current user has the authority to view the index
     */
    public boolean hasAuthorityToViewIndex() {
        return securityAccessService.hasAuthorityToViewIndex();
    }

    /**
     * Check if current user has authority to view process page. It returns true if
     * user has at least one of below given authorities.
     *
     * @return true if user has authority 'viewAllProcesses' or 'viewAllBatches' for
     *         client
     */
    public boolean hasAuthorityToViewProcessPage() {
        return securityAccessService.hasAnyAuthorityForClient("viewAllProcesses, viewAllBatches");
    }

    /**
     * Check if current user has authority to view project page. It returns true if
     * user has at least one of below given authorities.
     *
     * @return true if user has authority 'viewAllProjects' or 'viewAllTemplates' or
     *         'viewAllWorkflows' or 'viewAllDockets' or 'viewAllRulesets' for
     *         client
     */
    public boolean hasAuthorityToViewProjectPage() {
        return securityAccessService.hasAnyAuthorityForClient(
            "viewAllProjects, viewAllTemplates, viewAllWorkflows, viewAllDockets, viewAllRulesets");
    }

    /**
     * Check if current user has authority to view system page. It returns true if
     * user has at least one of below given authorities.
     *
     * @return true if user has authority 'viewIndex' or 'viewIndex' globally
     */
    public boolean hasAuthorityToViewSystemPage() {
        return securityAccessService.hasAuthorityToViewSystemPage();
    }

    /**
     * Check if current user has authority to view task page. It returns true if
     * user has "viewAllTasks" authority for client.
     *
     * @return true if user has authority 'viewAllTasks' for client
     */
    public boolean hasAuthorityToViewTaskPage() {
        return hasAuthorityToViewTaskList();
    }

    /**
     * Check if current user has authority to view user page. It returns true if
     * user has at least one of below given authorities.
     *
     * @return true if user has authority 'viewAllUsers' or 'viewAllRoles' or
     *         'viewAllClients' or 'viewAllLdapGroups' or 'viewAllLdapServers'
     *         globally or for client
     */
    public boolean hasAuthorityToViewUserPage() {
        return securityAccessService.hasAnyAuthorityGlobalOrForClient(
            "viewAllUsers, viewAllRoles, viewAllClients, viewAllLdapGroups, viewAllLdapServers");
    }

    /**
     * Check if current user has authority to view task list. It returns true if
     * user has "viewAllTasks" authority for client. It is exactly the same
     * authority as task page so it uses the same method.
     *
     * @return true if user has authority 'viewAllTasks' for client
     */
    public boolean hasAuthorityToViewTaskList() {
        return securityAccessService.hasAuthorityToViewTaskList();
    }

    /**
     * Check if current user has authority to view batch list. It returns true if
     * user has "viewAllBatches" authority for client.
     *
     * @return true if user has authority 'viewAllBatches' for client
     */
    public boolean hasAuthorityToViewBatchList() {
        return securityAccessService.hasAuthorityToViewBatchList();
    }

    /**
     * Check if current user has authority to view process list. It returns true if
     * user has "viewAllProcesses" authority for client.
     *
     * @return true if user has authority 'viewAllProcesses' for client
     */
    public boolean hasAuthorityToViewProcessList() {
        return securityAccessService.hasAuthorityToViewProcessList();
    }

    /**
     * Check if current user has authority to view project list. It returns true if
     * user has "viewAllProjects" authority for client.
     *
     * @return true if user has authority 'viewAllProjects' for client
     */
    public boolean hasAuthorityToViewProjectList() {
        return securityAccessService.hasAuthorityToViewProjectList();
    }

    /**
     * Check if current user has authority to view template list. It returns true if
     * user has "viewAllTemplates" authority for client.
     *
     * @return true if user has authority 'viewAllTemplates' for client
     */
    public boolean hasAuthorityToViewTemplateList() {
        return securityAccessService.hasAuthorityToViewTemplateList();
    }

    /**
     * Check if current user has authority to view workflow list. It returns true if
     * user has "viewAllWorkflows" authority for client.
     *
     * @return true if user has authority 'viewAllWorkflows' for client
     */
    public boolean hasAuthorityToViewWorkflowList() {
        return securityAccessService.hasAuthorityToViewWorkflowList();
    }

    /**
     * Check if current user has authority to view docket list. It returns true if
     * user has "viewAllDockets" authority for client.
     *
     * @return true if user has authority 'viewAllDockets' for client
     */
    public boolean hasAuthorityToViewDocketList() {
        return securityAccessService.hasAuthorityToViewDocketList();
    }

    /**
     * Check if current user has authority to view ruleset list. It returns true if
     * user has "viewAllRulesets" authority for client.
     *
     * @return true if user has authority 'viewAllRulesets' for client
     */
    public boolean hasAuthorityToViewRulesetList() {
        return securityAccessService.hasAuthorityToViewRulesetList();
    }

    /**
     * Check if current user has authority to view user list. It returns true if
     * user has "viewAllUsers" authority for client.
     *
     * @return true if user has authority 'viewAllUsers' globally or for client
     */
    public boolean hasAuthorityToViewUserList() {
        return securityAccessService.hasAuthorityToViewUserList();
    }

    /**
     * Check if current user has authority to view role list. It returns true if
     * user has "viewAllRoles" authority for client.
     *
     * @return true if user has authority 'viewAllRoles' globally or for client
     */
    public boolean hasAuthorityToViewRoleList() {
        return securityAccessService.hasAuthorityToViewRoleList();
    }

    /**
     * Check if current user has authority to view client list. It returns true if
     * user has "viewAllClients" authority for client.
     *
     * @return true if user has authority 'viewAllClients' globally or for client
     */
    public boolean hasAuthorityToViewClientList() {
        return securityAccessService.hasAuthorityToViewClientList();
    }

    /**
     * Check if current user has global authority to view authority list. It returns
     * true if user has "viewAllAuthorities" authority globally.
     *
     * @return true if user has authority 'viewAllAuthorities' globally
     */
    public boolean hasAuthorityToViewAuthorityList() {
        return securityAccessService.hasAuthorityToViewAuthorityList();
    }

    /**
     * Check if current user has authority to view LDAP group list. It returns true
     * if user has "viewAllLdapGroups" authority for client.
     *
     * @return true if user has authority 'viewAllLdapGroups' globally
     */
    public boolean hasAuthorityToViewLdapGroupList() {
        return securityAccessService.hasAuthorityToViewLdapGroupList();
    }

    /**
     * Check if current user has authority to view LDAP server list. It returns true
     * if user has "viewAllLdapServers" authority for client.
     *
     * @return true if user has authority 'viewAllLdapServers' globally
     */
    public boolean hasAuthorityToViewLdapServerList() {
        return securityAccessService.hasAuthorityToViewLdapServerList();
    }

    /**
     * Checks if current user has global authority for add or edit role. If yes,
     * current client is not assigned to created or edited role.
     *
     * @return true if current user has global authority for add or edit role
     */
    public boolean hasAuthorityGlobalToAddOrEditRole() {
        return securityAccessService.hasAuthorityGlobalToAddOrEditRole();
    }

    /**
     * Checks if current user has authority to configure displayed columns in list
     * views.
     *
     * @return true if current user has authority to configure columns
     */
    public boolean hasAuthorityToConfigureColumns() {
        return securityAccessService.hasAuthorityToConfigureColumns();
    }

    /**
     * Check if the current user has the authority to edit the process metadata.
     *
     * @return true if the current user has the authority to edit the process
     *         metadata
     */
    public boolean hasAuthorityToEditProcessMetaData() {
        return securityAccessService.hasAuthorityToEditProcessMetaData();
    }

    /**
     * Check if the current user has the authority to view the process metadata.
     *
     * @return true if the current user has the authority to view the process
     *         metadata
     */
    public boolean hasAuthorityToViewProcessMetaData() {
        return securityAccessService.hasAuthorityToViewProcessMetaData();
    }

    /**
     * Check if the current user has the authority to edit the process structure
     * data.
     *
     * @return true if the current user has the authority to edit the process
     *         structure data
     */
    public boolean hasAuthorityToEditProcessStructureData() {
        return securityAccessService.hasAuthorityToEditProcessStructureData();
    }

    /**
     * Check if the current user has the authority to view the process structure
     * data.
     *
     * @return true if the current user has the authority to view the process
     *         structure data
     */
    public boolean hasAuthorityToViewProcessStructureData() {
        return securityAccessService.hasAuthorityToViewProcessStructureData();
    }

    /**
     * Check if the current user has the authority to edit the process images.
     *
     * @return true if the current user has the authority to edit the process images
     */
    public boolean hasAuthorityToEditProcessImages() {
        return securityAccessService.hasAuthorityToEditProcessImages();
    }

    /**
     * Check if the current user has the authority to view the process images.
     *
     * @return true if the current user has the authority to view the process images
     */
    public boolean hasAuthorityToViewProcessImages() {
        return securityAccessService.hasAuthorityToViewProcessImages();
    }

    /**
     * Check if the current user has the authority to open the metadata editor.
     * Access to the metadata editor is granted if the user has the authority to
     * view or edit data in any part of the editor.
     *
     * @return true if the current user has the authority to view or edit any part
     *         of the data in the metadata editor
     */
    public boolean hasAuthorityToOpenMetadataEditor() {
        return securityAccessService.hasAuthorityToViewProcessMetaData()
                || securityAccessService.hasAuthorityToEditProcessMetaData()
                || securityAccessService.hasAuthorityToViewProcessStructureData()
                || securityAccessService.hasAuthorityToEditProcessStructureData()
                || securityAccessService.hasAuthorityToViewProcessImages()
                || securityAccessService.hasAuthorityToEditProcessImages();

    }

    /**
     * Check if the current user has the authority to view the database statistics.
     *
     * @return true if the current user has the authority to view database statistics
     */
    public boolean hasAuthorityToViewDatabaseStatistics() {
        return securityAccessService.hasAuthorityToViewDatabaseStatistic();
    }
}
