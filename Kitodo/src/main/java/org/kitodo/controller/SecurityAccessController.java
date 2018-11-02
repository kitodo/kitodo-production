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

package org.kitodo.controller;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.kitodo.services.ServiceManager;
import org.kitodo.services.security.SecurityAccessService;

/**
 * Controller for checking authorities of current user.
 */
@Named("SecurityAccessController")
@RequestScoped
public class SecurityAccessController {
    private SecurityAccessService securityAccessService = new ServiceManager().getSecurityAccessService();

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
     * Checks if the current user has any of the specified authorities globally.
     *
     * @param authorityTitles
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAnyAuthorityGlobal(String authorityTitles) {
        return securityAccessService.hasAnyAuthorityGlobal(authorityTitles);
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
     * Checks if the current user has the authority to edit the user with the
     * specified id.
     *
     * @param userId
     *            The user id.
     * @return True if the current user has the authority to edit the user with the
     *         specified id.
     */
    public boolean hasAuthorityToEditUser(int userId) {
        return securityAccessService.hasAuthorityToEditUser(userId);
    }

    /**
     * Check if the current user has the authority to edit the role with the
     * specified id.
     *
     * @param roleId
     *            the role id
     * @return true if the current user has the authority to edit the role with the
     *         specified id
     */
    public boolean hasAuthorityToEditRole(int roleId) {
        return securityAccessService.hasAuthorityToEditRole(roleId);
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
     *         'viewAllWorkflows' or 'viewAllDockets' or 'viewAllRulestes' for
     *         client
     */
    public boolean hasAuthorityToViewProjectPage() {
        return securityAccessService.hasAnyAuthorityForClient(
            "viewAllProjects, viewAllTemplates, viewAllWorkflows, viewAllDockets, viewAllRulestes");
    }

    /**
     * Check if current user has authority to view system page. It returns true if
     * user has at least one of below given authorities.
     *
     * @return true if user has authority 'viewIndex' or 'viewIndex' globally
     */
    public boolean hasAuthorityToViewSystemPage() {
        return securityAccessService.hasAnyAuthorityGlobal("viewIndex, viewIndex");
    }

    /**
     * Check if current user has authority to view task page. It returns true if
     * user has "viewAllTasks" authority for client.
     *
     * @return true if user has authority 'viewAllTasks' for client
     */
    public boolean hasAuthorityToViewTaskPage() {
        return securityAccessService.hasAuthorityForClient("viewAllTasks");
    }

    /**
     * Check if current user has authority to view user page. It returns true if
     * user has at least one of below given authorities.
     *
     * @return true if user has authority 'viewAllUsers' or 'viewAllUsers' or
     *         'viewAllClients' or 'viewAllLdapGroups' or 'viewAllLdapServers'
     *         globally or for client
     */
    public boolean hasAuthorityToViewUserPage() {
        return securityAccessService.hasAnyAuthorityGlobalOrForClient(
            "viewAllUsers, viewAllUsers, viewAllClients, viewAllLdapGroups, viewAllLdapServers");
    }

    /**
     * Check if current user has authority to view task list. It returns true if
     * user has "viewAllTasks" authority for client. It is exactly the same
     * authority as task page so it uses the same method.
     *
     * @return true if user has authority 'viewAllTasks' for client
     */
    public boolean hasAuthorityToViewTaskList() {
        return hasAuthorityToViewTaskPage();
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
}
