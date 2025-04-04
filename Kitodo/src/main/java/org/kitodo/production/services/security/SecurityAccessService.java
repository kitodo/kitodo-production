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

package org.kitodo.production.services.security;

import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.security.SecurityAccess;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAccessService extends SecurityAccess {

    private static volatile SecurityAccessService instance = null;

    /**
     * Return singleton variable of type SecurityAccessService.
     *
     * @return unique instance of SecurityAccessService
     */
    public static SecurityAccessService getInstance() {
        SecurityAccessService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (SecurityAccessService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new SecurityAccessService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public int getClientId() {
        return ServiceManager.getUserService().getSessionClientId();
    }

    /**
     * Get the current authenticated user of current threads security context.
     *
     * @return the SecurityUserDetails object or null if no user is authenticated
     */
    public SecurityUserDetails getAuthenticatedSecurityUserDetails() {
        if (isAuthenticated()) {
            Object principal = getCurrentAuthentication().getPrincipal();
            if (principal instanceof SecurityUserDetails) {
                return (SecurityUserDetails) principal;
            }
        }
        return null;
    }

    /**
     * Check if there is currently an authenticated user.
     *
     * @return true if there is currently an authenticated user
     */
    public boolean isAuthenticated() {
        Authentication currentAuthentication = getCurrentAuthentication();
        if (Objects.nonNull(currentAuthentication)) {
            return currentAuthentication.isAuthenticated();
        }
        return false;
    }

    /**
     * Update Authentication object of current threads security context
     * after changing current parameters of authenticated user.
     *
     * @param updatedUser as Object of the updated authenticated user
     */
    public void updateAuthentication(User updatedUser) {
        SecurityUserDetails userDetails = new SecurityUserDetails(updatedUser);
        userDetails.setSessionClient(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Check if the current user has the authority to add the batch.
     *
     * @return true if the current user has the authority to add the batch
     */
    public boolean hasAuthorityToAddBatch() {
        return hasAuthorityForClient("addBatch");
    }

    /**
     * Check if the current user has the authority to add the process.
     *
     * @return true if the current user has the authority to add the process
     */
    public boolean hasAuthorityToAddProcess() {
        return hasAuthorityForClient("addProcess");
    }

    /**
     * Check if the current user has the authority to add the project.
     *
     * @return true if the current user has the authority to add the project
     */
    public boolean hasAuthorityToAddProject() {
        return hasAuthorityForClient("addProject");
    }

    /**
     * Check if the current user has the authority to add the template.
     *
     * @return true if the current user has the authority to add the template
     */
    public boolean hasAuthorityToAddTemplate() {
        return hasAuthorityForClient("addTemplate");
    }

    /**
     * Check if the current user has the authority to edit the workflow.
     *
     * @return true if the current user has the authority to edit the workflow
     */
    public boolean hasAuthorityToAddWorkflow() {
        return hasAuthorityForClient("addWorkflow");
    }

    /**
     * Check if the current user has the authority to add the docket.
     *
     * @return true if the current user has the authority to add the docket
     */
    public boolean hasAuthorityToAddDocket() {
        return hasAuthorityForClient("addDocket");
    }

    /**
     * Check if the current user has the authority to add the ruleset.
     *
     * @return true if the current user has the authority to add the ruleset
     */
    public boolean hasAuthorityToAddRuleset() {
        return hasAuthorityForClient("addRuleset");
    }

    /**
     * Check if the current user has the authority to add new import configuration.
     *
     * @return true if the current user has the authority to add new import configuration.
     */
    public boolean hasAuthorityToAddImportConfiguration() {
        return hasAuthorityForClient("addImportConfiguration");
    }

    /**
     * Check if the current user has the authority to add new mapping file.
     *
     * @return true if the current user has the authority to add new mapping file.
     */
    public boolean hasAuthorityToAddMappingFile() {
        return hasAuthorityForClient("addMappingFile");
    }

    /**
     * Check if the current user has the authority to add the user.
     *
     * @return true if the current user has the authority to add the user
     */
    public boolean hasAuthorityToAddUser() {
        return hasAuthorityGlobalOrForClient("addUser");
    }

    /**
     * Check if the current user has the authority to add the role.
     *
     * @return true if the current user has the authority to add the role
     */
    public boolean hasAuthorityToAddRole() {
        return hasAuthorityGlobalOrForClient("addRole");
    }

    /**
     * Check if the current user has the authority to add the client.
     *
     * @return true if the current user has the authority to add the client
     */
    public boolean hasAuthorityToAddClient() {
        return hasAuthorityGlobal("addClient");
    }

    /**
     * Check if the current user has the authority to add the authority.
     *
     * @return true if the current user has the authority to add the authority
     */
    public boolean hasAuthorityToAddAuthority() {
        return hasAuthorityGlobal("addAuthority");
    }

    /**
     * Check if current user has authority to add anything on user page. It returns
     * true if user has at least one of below given authorities.
     *
     * @return true if user has authority 'addUser' or 'addRole' or 'addClient' or
     *         'addLdapGroup' or 'addLdapServer' globally or for client
     */
    public boolean hasAuthorityToAddOnUserPage() {
        return hasAnyAuthorityGlobalOrForClient("addUser, addRole, addClient, addLdapGroup, addLdapServer");
    }

    /**
     * Check if current user has authority to add anything on project page. It
     * returns true if user has at least one of below given authorities.
     *
     * @return true if user has authority 'addProject' or 'addTemplate' or
     *         'addWorkflow' or 'addDocket' or 'addRuleset' for client
     */
    public boolean hasAuthorityToAddOnProjectPage() {
        return hasAnyAuthorityForClient("addProject, addTemplate, addWorkflow, addDocket, addRuleset");
    }

    /**
     * Check if the current user has the authority to assign an OCR-D workflow to template or process.
     *
     * @return true if the current user has the authority to assign an OCR-D workflow
     */
    public boolean hasAuthorityToAssignOcrdWorkflow() {
        return hasAnyAuthorityForClient("assignOcrdWorkflow");
    }

    /**
     * Check if the current user has the authority to delete the batch.
     *
     * @return true if the current user has the authority to delete the batch
     */
    public boolean hasAuthorityToDeleteBatch() {
        return hasAuthorityForClient("deleteBatch");
    }

    /**
     * Check if the current user has the authority to delete the process.
     *
     * @return true if the current user has the authority to delete the process
     */
    public boolean hasAuthorityToDeleteProcess() {
        return hasAuthorityForClient("deleteProcess");
    }

    /**
     * Check if the current user has the authority to delete the task.
     *
     * @return true if the current user has the authority to delete the task
     */
    public boolean hasAuthorityToDeleteTask() {
        return hasAuthorityForClient("deleteTask");
    }

    /**
     * Check if the current user has the authority to delete the project.
     *
     * @return true if the current user has the authority to delete the project
     */
    public boolean hasAuthorityToDeleteProject() {
        return hasAuthorityForClient("deleteProject");
    }

    /**
     * Check if the current user has the authority to delete the template.
     *
     * @return true if the current user has the authority to delete the template
     */
    public boolean hasAuthorityToDeleteTemplate() {
        return hasAuthorityForClient("deleteTemplate");
    }

    /**
     * Check if the current user has the authority to edit the workflow.
     *
     * @return true if the current user has the authority to edit the workflow
     */
    public boolean hasAuthorityToDeleteWorkflow() {
        return hasAuthorityForClient("deleteWorkflow");
    }

    /**
     * Check if the current user has the authority to delete the docket.
     *
     * @return true if the current user has the authority to delete the docket
     */
    public boolean hasAuthorityToDeleteDocket() {
        return hasAuthorityForClient("deleteDocket");
    }

    /**
     * Check if the current user has the authority to delete the ruleset.
     *
     * @return true if the current user has the authority to delete the ruleset
     */
    public boolean hasAuthorityToDeleteRuleset() {
        return hasAuthorityForClient("deleteRuleset");
    }

    /**
     * Check if the current user has the authority to delete import configuration.
     *
     * @return true if the current user has the authority to delete the import configuration
     */
    public boolean hasAuthorityToDeleteImportConfiguration() {
        return hasAuthorityForClient("deleteImportConfiguration");
    }

    /**
     * Check if the current user has the authority to delete mapping file object.
     *
     * @return true if the current user has the authority to delete the mapping file object
     */
    public boolean hasAuthorityToDeleteMappingFile() {
        return hasAuthorityForClient("deleteMappingFile");
    }

    /**
     * Check if the current user has the authority to delete the user.
     *
     * @return true if the current user has the authority to delete the user
     */
    public boolean hasAuthorityToDeleteUser() {
        return hasAuthorityGlobalOrForClient("deleteUser");
    }

    /**
     * Check if the current user has the authority to delete the role.
     *
     * @return true if the current user has the authority to delete the role
     */
    public boolean hasAuthorityToDeleteRole() {
        return hasAuthorityGlobalOrForClient("deleteRole");
    }

    /**
     * Check if the current user has the authority to delete the client.
     *
     * @return true if the current user has the authority to delete the client
     */
    public boolean hasAuthorityToDeleteClient() {
        return hasAuthorityGlobal("deleteClient");
    }

    /**
     * Check if the current user has the authority to delete the authority.
     *
     * @return true if the current user has the authority to delete the authority
     */
    public boolean hasAuthorityToDeleteAuthority() {
        return hasAuthorityGlobalOrForClient("deleteAuthority");
    }

    /**
     * Check if the current user has the authority to edit the task.
     *
     * @return true if the current user has the authority to edit the task
     */
    public boolean hasAuthorityToEditTask() {
        return hasAuthorityForClient("editTask");
    }

    /**
     * Check if the current user has the authority to edit the task.
     *
     * @param taskId the specific taskId
     * @return true if the current user has the authority to edit the task
     */
    public boolean hasAuthorityToEditTask(int taskId) throws DAOException {
        return hasAuthorityForClient("editTask") && hasAuthorityForTask(taskId);
    }

    /**
     * Check if the current user has the authority to edit the batch.
     *
     * @return true if the current user has the authority to edit the batch
     */
    public boolean hasAuthorityToEditBatch() {
        return hasAuthorityForClient("editBatch");
    }

    /**
     * Check if the current user has the authority to edit the process.
     *
     * @return true if the current user has the authority to edit the process
     */
    public boolean hasAuthorityToEditProcess() {
        return hasAuthorityForClient("editProcess");
    }

    /**
     * Check if the current user has the authority to edit the process.
     *
     * @param processId
     *            the specific processId
     * @return true if the current user has the authority to edit the process
     */
    public boolean hasAuthorityToEditProcess(int processId) throws DAOException {
        return hasAuthorityForClient("editProcess") && hasAuthorityForProcess(processId);
    }

    /**
     * Check if the current user has the authority to edit the project.
     *
     * @return true if the current user has the authority to edit the project
     */
    public boolean hasAuthorityToEditProject() {
        return hasAuthorityForClient("editProject");
    }


    /**
     * Check if the current user has the authority to edit the project.
     *
     * @param projectId
     *            the specific projectId
     * @return true if the current user has the authority to edit the project
     */
    public boolean hasAuthorityToEditProject(int projectId) {
        return hasAuthorityForClient("editProject") && hasAuthorityForProject(projectId);
    }

    /**
     * Check if the current user has the authority to edit the template.
     *
     * @return true if the current user has the authority to edit the template
     */
    public boolean hasAuthorityToEditTemplate() {
        return hasAuthorityForClient("editTemplate");
    }

    /**
     * Check if the current user has the authority to edit the workflow.
     *
     * @return true if the current user has the authority to edit the workflow
     */
    public boolean hasAuthorityToEditWorkflow() {
        return hasAuthorityForClient("editWorkflow");
    }

    /**
     * Check if the current user has the authority to edit the docket.
     *
     * @return true if the current user has the authority to edit the docket
     */
    public boolean hasAuthorityToEditDocket() {
        return hasAuthorityForClient("editDocket");
    }

    /**
     * Check if the current user has the authority to edit the ruleset.
     *
     * @return true if the current user has the authority to edit the ruleset
     */
    public boolean hasAuthorityToEditRuleset() {
        return hasAuthorityForClient("editRuleset");
    }

    /**
     * Check if the current user has the authority to edit import configuration.
     *
     * @return true if the current user has the authority to edit import configuration
     */
    public boolean hasAuthorityToEditImportConfiguration() {
        return hasAuthorityForClient("editImportConfiguration");
    }

    /**
     * Check if the current user has the authority to edit import configuration.
     *
     * @return true if the current user has the authority to edit import configuration
     */
    public boolean hasAuthorityToEditMappingFile() {
        return hasAuthorityForClient("editMappingFile");
    }

    /**
     * Checks if the current user has the authority to edit the user.
     *
     * @return true if the current user has the authority to edit the user
     */
    public boolean hasAuthorityToEditUser() {
        return hasAuthorityGlobalOrForClient("editUser");
    }

    /**
     * Checks if the current user has the authority to unassign all tasks in progress of a user.
     *
     * @return true if the current user has the authority to unassign all tasks in progress of a user
     */
    public boolean hasAuthorityToUnassignTasks() {
        return hasAuthorityGlobalOrForClient("unassignTasks");
    }


    /**
     * Check if the current user has the authority to upload media in metadataeditor.
     *
     * @return true if the current user has the authority to to upload media in metadataeditor
     */
    public boolean hasAuthorityToUploadMedia() {
        return hasAuthorityGlobalOrForClient("uploadMedia");
    }

    /**
     * Checks if the current user has the authority to edit the role.
     *
     * @return true if the current user has the authority to edit the role
     */
    public boolean hasAuthorityToEditRole() {
        return hasAuthorityGlobalOrForClient("editRole");
    }

    /**
     * Check if the current user has the authority to edit the client.
     *
     * @return true if the current user has the authority to edit the client
     */
    public boolean hasAuthorityToEditClient() {
        return hasAuthorityGlobal("editClient");
    }

    /**
     * Check if the current user has the authority to edit the authority.
     *
     * @return true if the current user has the authority to edit the authority
     */
    public boolean hasAuthorityToEditAuthority() {
        return hasAuthorityGlobal("editAuthority");
    }

    /**
     * Check if the current user has the authority to edit the index.
     *
     * @return true if the current user has the authority to edit the index
     */
    public boolean hasAuthorityToEditIndex() {
        return hasAuthorityGlobal("editIndex");
    }

    /**
     * Check if the current user has the authority to export process.
     *
     * @return true if the current user has the authority to export process
     */
    public boolean hasAuthorityToExportProcess() {
        return hasAuthorityForClient("exportProcess");
    }

    /**
     * Check if the current user has the authority to view the process. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the process
     */
    public boolean hasAuthorityToViewProcess() {
        return hasAnyAuthorityForClient("viewProcess, addProcess, editProcess");
    }

    /**
     * Check if the current user has the authority to view the process. Add and edit
     * authorities include also view.
     *
     * @param processId
     *            the specific processId
     * @return true if the current user has the authority to view the process
     */
    public boolean hasAuthorityToViewProcess(int processId) throws DAOException {
        return hasAnyAuthorityForClient("viewProcess, addProcess, editProcess") && hasAuthorityForProcess(processId);
    }

    /**
     * Check if the current user has the authority to view the project. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the project
     */
    public boolean hasAuthorityToViewProject() {
        return hasAnyAuthorityForClient("viewProject, addProject, editProject");
    }

    /**
     * Check if the current user has the authority to view the project. Add and edit
     * authorities include also view.
     *
     * @param projectId
     *            the specific projectId
     * @return true if the current user has the authority to view the project
     */
    public boolean hasAuthorityToViewProject(int projectId) {
        return hasAnyAuthorityForClient("viewProject, addProject, editProject") && hasAuthorityForProject(projectId);
    }

    /**
     * Check if the current user has the authority to view the template. Add and
     * edit authorities include also view.
     *
     * @return true if the current user has the authority to view the template
     */
    public boolean hasAuthorityToViewTemplate() {
        return hasAnyAuthorityForClient("viewTemplate, addTemplate, editTemplate");
    }

    /**
     * Check if the current user has the authority to view the workflow. Add and
     * edit authorities include also view.
     *
     * @return true if the current user has the authority to view the workflow
     */
    public boolean hasAuthorityToViewWorkflow() {
        return hasAnyAuthorityForClient("viewWorkflow, addWorkflow, editWorkflow");
    }

    /**
     * Check if the current user has the authority to view the docket. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the docket
     */
    public boolean hasAuthorityToViewDocket() {
        return hasAnyAuthorityForClient("viewDocket, addDocket, editDocket");
    }

    /**
     * Check if the current user has the authority to view the ruleset. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the ruleset
     */
    public boolean hasAuthorityToViewRuleset() {
        return hasAnyAuthorityForClient("viewRuleset, addRuleset, editRuleset");
    }

    /**
     * Check if the current user has the authority to view import configuration. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view import configuration
     */
    public boolean hasAuthorityToViewImportConfiguration() {
        return hasAnyAuthorityForClient("viewImportConfiguration, editImportConfiguration, addImportConfiguration");
    }

    /**
     * Check if the current user has the authority to view mapping file.
     *
     * @return true if the current user has the authority to view mapping file
     */
    public boolean hasAuthorityToViewMappingFile() {
        return hasAnyAuthorityForClient("viewMappingFile, editMappingFile, addMappingFile");
    }

    /**
     * Check if the current user has the authority to view the user. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the user
     */
    public boolean hasAuthorityToViewUser() {
        return hasAnyAuthorityGlobalOrForClient("viewUser, addUser, editUser");
    }

    /**
     * Check if the current user has the authority to change the user config. It is
     * true if current user is logged user.
     *
     * @param userId
     *            id of the viewed user
     * @return true if the current user has the authority to change the user config
     */
    public boolean hasAuthorityToConfigUser(int userId) {
        return ServiceManager.getUserService().getAuthenticatedUser().getId().equals(userId);
    }

    /**
     * Check if the current user has the authority to view the role. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the role
     */
    public boolean hasAuthorityToViewRole() {
        return hasAnyAuthorityGlobalOrForClient("viewRole, addRole, editRole");
    }

    /**
     * Check if the current user has the authority to view the client. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the client
     */
    public boolean hasAuthorityToViewClient() {
        return hasAnyAuthorityGlobalOrForClient("viewClient, addClient, editClient");
    }

    /**
     * Check if the current user has the authority to view the authority. Add and
     * edit authorities include also view.
     *
     * @return true if the current user has the authority to view the authority
     */
    public boolean hasAuthorityToViewAuthority() {
        return hasAnyAuthorityGlobalOrForClient("viewAuthority, addAuthority, editAuthority");
    }

    /**
     * Check if current user has authority to view task list. It returns true if
     * user has "viewAllTasks" authority for client.
     *
     * @return true if user has authority 'viewAllTasks' for client
     */
    public boolean hasAuthorityToViewTaskList() {
        return hasAuthorityForClient("viewAllTasks");
    }

    /**
     * Check if current user has authority to view batch list. It returns true if
     * user has "viewAllBatches" authority for client.
     *
     * @return true if user has authority 'viewAllBatches' for client
     */
    public boolean hasAuthorityToViewBatchList() {
        return hasAuthorityForClient("viewAllBatches");
    }

    /**
     * Check if current user has authority to view process list. It returns true if
     * user has "viewAllProcesses" authority for client.
     *
     * @return true if user has authority 'viewAllProcesses' for client
     */
    public boolean hasAuthorityToViewProcessList() {
        return hasAuthorityForClient("viewAllProcesses");
    }

    /**
     * Check if current user has authority to view project list. It returns true if
     * user has "viewAllProjects" authority for client.
     *
     * @return true if user has authority 'viewAllProjects' for client
     */
    public boolean hasAuthorityToViewProjectList() {
        return hasAuthorityForClient("viewAllProjects");
    }

    /**
     * Check if current user has authority to view template list. It returns true if
     * user has "viewAllTemplates" authority for client.
     *
     * @return true if user has authority 'viewAllTemplates' for client
     */
    public boolean hasAuthorityToViewTemplateList() {
        return hasAuthorityForClient("viewAllTemplates");
    }

    /**
     * Check if current user has authority to view workflow list. It returns true if
     * user has "viewAllWorkflows" authority for client.
     *
     * @return true if user has authority 'viewAllWorkflows' for client
     */
    public boolean hasAuthorityToViewWorkflowList() {
        return hasAuthorityForClient("viewAllWorkflows");
    }

    /**
     * Check if current user has authority to view docket list. It returns true if
     * user has "viewAllDockets" authority for client.
     *
     * @return true if user has authority 'viewAllDockets' for client
     */
    public boolean hasAuthorityToViewDocketList() {
        return hasAuthorityForClient("viewAllDockets");
    }

    /**
     * Check if current user has authority to view ruleset list. It returns true if
     * user has "viewAllRulesets" authority for client.
     *
     * @return true if user has authority 'viewAllRulesets' for client
     */
    public boolean hasAuthorityToViewRulesetList() {
        return hasAuthorityForClient("viewAllRulesets");
    }

    /**
     * Check if current user has authority to view import configuration list. It returns true if
     * user has "viewAllImportConfigurations" authority for client.
     *
     * @return true if user has authority 'viewAllImportConfigurations' for client
     */
    public boolean hasAuthorityToViewImportConfigurationList() {
        return hasAuthorityForClient("viewAllImportConfigurations");
    }

    /**
     * Check if current user has authority to view mapping file list. It returns true if
     * user has "viewAllMappingFiles" authority for client.
     *
     * @return true if user has authority 'viewAllMappingFiles' for client
     */
    public boolean hasAuthorityToViewMappingFileList() {
        return hasAuthorityForClient("viewAllMappingFiles");
    }

    /**
     * Check if current user has authority to view user list. It returns true if
     * user has "viewAllUsers" authority globally or for client.
     *
     * @return true if user has authority 'viewAllUsers' globally or for client
     */
    public boolean hasAuthorityToViewUserList() {
        return hasAuthorityGlobalOrForClient("viewAllUsers");
    }

    /**
     * Check if current user has authority to view role list. It returns true if
     * user has "viewAllRoles" authority globally or for client.
     *
     * @return true if user has authority 'viewAllRoles' globally or for client
     */
    public boolean hasAuthorityToViewRoleList() {
        return hasAuthorityGlobalOrForClient("viewAllRoles");
    }

    /**
     * Check if current user has authority to view client list. It returns true if
     * user has "viewAllClients" authority for client.
     *
     * @return true if user has authority 'viewAllClients' globally or for client
     */
    public boolean hasAuthorityToViewClientList() {
        return hasAuthorityGlobalOrForClient("viewAllClients");
    }

    /**
     * Check if current user has global authority to view authority list. It returns
     * true if user has "viewAllAuthorities" authority globally.
     *
     * @return true if user has authority 'viewAllAuthorities' globally
     */
    public boolean hasAuthorityToViewAuthorityList() {
        return hasAuthorityGlobal("viewAllAuthorities");
    }

    /**
     * Check if current user has authority to view LDAP group list. It returns true
     * if user has "viewAllLdapGroups" authority globally.
     *
     * @return true if user has authority 'viewAllLdapGroups' globally
     */
    public boolean hasAuthorityToViewLdapGroupList() {
        return hasAuthorityGlobal("viewAllLdapGroups");
    }

    /**
     * Check if current user has authority to view LDAP server list. It returns true
     * if user has "viewAllLdapServers" authority globally.
     *
     * @return true if user has authority 'viewAllLdapServers' globally
     */
    public boolean hasAuthorityToViewLdapServerList() {
        return hasAuthorityGlobal("viewAllLdapServers");
    }

    /**
     * Checks if current user has global authority for add or edit role. If yes,
     * current client is not assigned to created or edited role.
     *
     * @return true if current user has global authority for add or edit role
     */
    public boolean hasAuthorityGlobalToAddOrEditRole() {
        return hasAnyAuthorityGlobal("addRole, editRole");
    }

    /**
     * Checks if current user has global authority for editing a role.
     *
     * @return true if current user has global authority for editing a role
     */
    public boolean hasAuthorityGlobalToEditRole() {
        return hasAnyAuthorityGlobal("editRole");
    }

    /**
     * Checks if current user has global authority for viewing a role.
     * Having the authority to edit a role also grants permission to view it inherently.
     *
     * @return true if current user has global authority for editing a role
     */
    public boolean hasAuthorityGlobalToViewRole() {
        return hasAnyAuthorityGlobal("viewRole, editRole");
    }

    /**
     * Check if current user has global authority to view role list. It returns true
     * if user has "viewAllRoles" authority globally.
     *
     * @return true if user has authority 'viewAllRoles' globally
     */
    public boolean hasAuthorityGlobalToViewRoleList() {
        return hasAuthorityGlobal("viewAllRoles");
    }

    /**
     * Check if current user has authority to view user list. It returns true if
     * user has "viewAllUsers" authority globally.
     *
     * @return true if user has authority 'viewAllUsers' globally
     */
    public boolean hasAuthorityGlobalToViewUserList() {
        return hasAuthorityGlobal("viewAllUsers");
    }

    /**
     * Check if current usr has authority to configure list views. It returns true
     * if the user has "configureColumns" authority globally or for a client.
     *
     * @return true if user has authority 'configureColumns' globally or for a
     *         client
     */
    public boolean hasAuthorityToConfigureColumns() {
        return hasAuthorityGlobalOrForClient("configureColumns");
    }

    /**
     * Check if the current user has the authority to edit the process metadata.
     *
     * @return true if the current user has the authority to edit the process
     *         metadata
     */
    public boolean hasAuthorityToEditProcessMetaData() {
        return hasAuthorityForClient("editProcessMetaData");
    }

    /**
     * Check if the current user has the authority to view the process metadata.
     *
     * @return true if the current user has the authority to view the process
     *         metadata
     */
    public boolean hasAuthorityToViewProcessMetaData() {
        return hasAuthorityForClient("viewProcessMetaData");
    }

    /**
     * Check if the current user has the authority to edit the process structure
     * data.
     *
     * @return true if the current user has the authority to edit the process
     *         structure data
     */
    public boolean hasAuthorityToEditProcessStructureData() {
        return hasAuthorityForClient("editProcessStructureData");
    }

    /**
     * Check if the current user has the authority to view the process structure
     * data.
     *
     * @return true if the current user has the authority to view the process
     *         structure data
     */
    public boolean hasAuthorityToViewProcessStructureData() {
        return hasAuthorityForClient("viewProcessStructureData");
    }

    /**
     * Check if the current user has the authority to edit the process images.
     *
     * @return true if the current user has the authority to edit the process images
     */
    public boolean hasAuthorityToEditProcessImages() {
        return hasAuthorityForClient("editProcessImages");
    }

    /**
     * Check if the current user has the authority to view the process images.
     *
     * @return true if the current user has the authority to view the process images
     */
    public boolean hasAuthorityToViewProcessImages() {
        return hasAuthorityForClient("viewProcessImages");
    }

    /**
     * Check if the current user has the authority to edit the process pagination.
     *
     * @return true if the current user has the authority to edit the process pagination
     */
    public boolean hasAuthorityToEditProcessPagination() {
        return hasAuthorityForClient("editProcessPagination");
    }

    /**
     * Check if the current user has the authority to view the process pagination.
     *
     * @return true if the current user has the authority to view the process pagination
     */
    public boolean hasAuthorityToViewProcessPagination() {
        return hasAuthorityForClient("viewProcessPagination");
    }

    /**
     * Check if the current user has the authority to view the database statistics.
     *
     * @return true if the current user has the authority to view database statistics
     */
    public boolean hasAuthorityToViewDatabaseStatistic() {
        return hasAuthorityGlobal("viewDatabaseStatistic");
    }

    /**
     * Check if the current user has the authority to view the index page.
     *
     * @return true if the current user has the authority to view the index page
     */
    public boolean hasAuthorityToViewIndexPage() {
        return hasAnyAuthorityGlobal("viewIndex, editIndex");
    }

    /**
     * Check if the current user has the authority to view the task manager page.
     *
     * @return true if the current user has the authority to view the task manager page
     */
    public boolean hasAuthorityToViewTaskManagerPage() {
        return hasAnyAuthorityGlobal("viewTaskManager");
    }

    /**
     * Check if the current user has the authority to view the terms page.
     *
     * @return true if the current user has the authority to view the terms page
     */
    public boolean hasAuthorityToViewTermsPage() {
        return hasAnyAuthorityGlobal("viewTerms");
    }

    /**
     * Check if the current user has the authority to view the migration page.
     *
     * @return true if the current user has the authority to view the migration page
     */
    public boolean hasAuthorityToViewMigrationPage() {
        return hasAnyAuthorityGlobal("viewMigration");
    }


    private boolean hasAuthorityForTask(int taskId) throws DAOException {
        Integer processId = ServiceManager.getTaskService().findById(taskId).getProcess().getId();
        return hasAuthorityForProcess(processId);
    }

    private boolean hasAuthorityForProcess(int processId) throws DAOException {
        Integer projectId = processId == 0 ? 0
                : ServiceManager.getProcessService().getById(processId).getProject().getId();
        return hasAuthorityForProject(projectId);
    }

    private boolean hasAuthorityForProject(Integer projectId) {
        List<Project> projects = ServiceManager.getUserService().getCurrentUser().getProjects();
        return projects.stream().anyMatch(project -> project.getId().equals(projectId)) || projectId == 0;
    }

    /**
     * Check if the current user has the authority to delete media in metadata editor.
     *
     * @return true if the current user has the authority to delete media in metadata editor
     */
    public boolean hasAuthorityToDeleteMedia() {
        return hasAuthorityGlobalOrForClient("deleteMedia");
    }

    /**
     * Check if the current user has the authority to run Kitodo scripts.
     *
     * @return true if the current user has the authority to run Kitodo scripts.
     */
    public boolean hasAuthorityToRunKitodoScripts() {
        return hasAnyAuthorityForClient("runKitodoScript");
    }

    /**
     * Check if the current user has the permission to rename media files.
     *
     * @return true if the current user has the permission to rename media files.
     */
    public boolean hasAuthorityToRenameMediaFiles() {
        return hasAnyAuthorityForClient("renameMedia");
    }

    /**
     * Check if the current user has the permission to assign import configurations to clients.
     *
     * @return true if the current user has the permission to assign import configurations to clients.
     */
    public boolean hasAuthorityToAssignImportConfigurationToClient() {
        return hasAuthorityGlobal("assignImportConfigurationToClient");
    }

    /**
     * Check if the current user has the permission to link processes to parent processes of unassigned projects.
     *
     * @return true if the current user has the permission to link processes to parent processes of unassigned projects.
     */
    public boolean hasAuthorityToLinkToProcessesOfUnassignedProjects() {
        return hasAnyAuthorityForClient("linkToProcessesOfUnassignedProjects");
    }

    /**
     * Check if the current user has the permission to use the mass import feature.
     *
     * @return true if the current use has the permission to use the mass import feature
     */
    public boolean hasAuthorityToUseMassImport() {
        return hasAnyAuthorityForClient("useMassImport");
    }

    /**
     * Check if the current user has the permission to set import configurations for processes.
     *
     * @return true if the current user has the permission to set import configurations for processes.
     */
    public boolean hasAuthorityToSetImportConfiguration() {
        return hasAuthorityForClient("setImportConfiguration");
    }

    /**
     * Check if the current user has the permission to re-import metadata in the metadata editor.
     *
     * @return true if the current user has the permission to re-import metadata in the metadata editor.
     */
    public boolean hasAuthorityToReimportMetadata() {
        return hasAuthorityForClient("reimportMetadata");
    }
}
