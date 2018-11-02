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

package org.kitodo.services.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.kitodo.security.SecurityUserDetails;
import org.kitodo.services.ServiceManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAccessService {

    private static SecurityAccessService instance = null;
    private static ServiceManager serviceManager = new ServiceManager();
    private static final String GLOBAL_IDENTIFIER = "GLOBAL";
    private static final String CLIENT_IDENTIFIER = "CLIENT";

    /**
     * Return singleton variable of type SecurityAccessService.
     *
     * @return unique instance of SecurityAccessService
     */
    public static SecurityAccessService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (SecurityAccessService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new SecurityAccessService();
                }
            }
        }
        return instance;
    }

    private Collection<? extends GrantedAuthority> getAuthoritiesOfCurrentAuthentication() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Gets Authentication object of current threads security context.
     * 
     * @return authentication object
     */
    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Gets the current authenticated user of current threads security context.
     *
     * @return The SecurityUserDetails object or null if no user is authenticated.
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
     * Checks if there is currently an authenticated user.
     * 
     * @return true if there is currently an authenticated user
     */
    public boolean isAuthenticated() {
        Authentication currentAuthentication = getCurrentAuthentication();
        if (currentAuthentication != null) {
            return currentAuthentication.isAuthenticated();
        }
        return false;
    }

    private boolean hasAuthority(String authorityTitle) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityTitle);
        Collection<? extends GrantedAuthority> authorities = getAuthoritiesOfCurrentAuthentication();
        return authorities.contains(authority);
    }

    private String[] getStringArray(String strings) {
        strings = strings.replaceAll("\\s+", ""); // remove whitespaces
        return strings.split(",");
    }

    /**
     * Checks if the current user has a specified authority globally or for a
     * client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForClient(String authorityTitle) {
        return hasAuthorityGlobal(authorityTitle) || hasAuthorityForClient(authorityTitle);
    }

    /**
     * Checks if the current user has a specified authority for a client.
     *
     * @param authorityTitles
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAnyAuthorityForClient(String authorityTitles) {
        String[] authorityTitlesArray = getStringArray(authorityTitles);
        for (String authorityTitle : authorityTitlesArray) {
            if (hasAuthorityForClient(authorityTitle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current user has a specified authority for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityForClient(String authorityTitle) {
        String clientAuthority = authorityTitle + "_" + CLIENT_IDENTIFIER + "_"
                + serviceManager.getUserService().getSessionClientId();
        return hasAuthority(clientAuthority);
    }

    /**
     * Checks if the current user has a specified authority globally.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobal(String authorityTitle) {
        return hasAuthority(authorityTitle + "_" + GLOBAL_IDENTIFIER);
    }

    /**
     * Checks if the current user has any of the specified authorities globally.
     *
     * @param authorityTitles
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return True if the current user has any of the specified authorities
     *         globally
     */
    public boolean hasAnyAuthorityGlobal(String authorityTitles) {
        String[] authorityTitlesArray = getStringArray(authorityTitles);
        for (String authorityTitle : authorityTitlesArray) {
            if (hasAuthorityGlobal(authorityTitle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current user has any of the specified authorities globally or
     * for client.
     *
     * @param authorityTitles
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return true if the current user has any of the specified authorities
     *         globally or for client
     */
    public boolean hasAnyAuthorityGlobalOrForClient(String authorityTitles) {
        return hasAnyAuthorityGlobal(authorityTitles) || hasAnyAuthorityForClient(authorityTitles);
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
     * Check if the current user has the authority to edit the task.
     *
     * @return true if the current user has the authority to edit the task
     */
    public boolean hasAuthorityToEditTask() {
        return hasAuthorityForClient("editTask");
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
     * Check if the current user has the authority to edit the project.
     *
     * @return true if the current user has the authority to edit the project
     */
    public boolean hasAuthorityToEditProject() {
        return hasAuthorityForClient("editProject");
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
     * Checks if the current user has the authority to edit the user.
     *
     * @return true if the current user has the authority to edit the user
     */
    public boolean hasAuthorityToEditUser() {
        return hasAuthorityGlobalOrForClient("editUser");
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
     * Check if the current user has the authority to edit the index.
     *
     * @return true if the current user has the authority to edit the index
     */
    public boolean hasAuthorityToEditIndex() {
        return hasAuthorityGlobal("editIndex");
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
     * Check if the current user has the authority to view the project. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the project
     */
    public boolean hasAuthorityToViewProject() {
        return hasAnyAuthorityForClient("viewProject, addProject, editProject");
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
        return hasAnyAuthorityForClient("viewDocket, addWorkflow, editDocket");
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
     * Check if the current user has the authority to view the user. Add and edit
     * authorities include also view.
     *
     * @return true if the current user has the authority to view the user
     */
    public boolean hasAuthorityToViewUser() {
        return hasAnyAuthorityGlobalOrForClient("viewUser, addUser, editUser");
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
     * Check if the current user has the authority to view the index. Edit authority
     * includes also view.
     *
     * @return true if the current user has the authority to view the index
     */
    public boolean hasAuthorityToViewIndex() {
        return hasAnyAuthorityGlobal("viewIndex, editIndex");
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
     * Check if current user has authority to view user list. It returns true if
     * user has "viewAllUsers" authority for client.
     *
     * @return true if user has authority 'viewAllUsers' globally or for client
     */
    public boolean hasAuthorityToViewUserList() {
        return hasAuthorityGlobalOrForClient("viewAllUsers");
    }

    /**
     * Check if current user has authority to view role list. It returns true if
     * user has "viewAllRoles" authority for client.
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
     * Check if current user has authority to view LDAP group list. It returns true
     * if user has "viewAllLdapGroups" authority for client.
     *
     * @return true if user has authority 'viewAllLdapGroups' globally
     */
    public boolean hasAuthorityToViewLdapGroupList() {
        return hasAuthorityGlobal("viewAllLdapGroups");
    }

    /**
     * Check if current user has authority to view LDAP server list. It returns true
     * if user has "viewAllLdapServers" authority for client.
     *
     * @return true if user has authority 'viewAllLdapServers' globally
     */
    public boolean hasAuthorityToViewLdapServerList() {
        return hasAuthorityForClient("viewAllLdapServers");
    }
}
