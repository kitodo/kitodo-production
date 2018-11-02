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
}
