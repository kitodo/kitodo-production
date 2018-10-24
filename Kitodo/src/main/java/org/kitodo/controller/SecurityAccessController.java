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
     * Checks if the current user has a specified authority globally or for a client
     * id.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The client id.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForClient(String authorityTitle, int clientId) {
        return securityAccessService.hasAuthorityGlobalOrForClient(authorityTitle, clientId);
    }

    /**
     * Checks if the current user is admin or has a specified authority globally or
     * for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The client id.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForClient(String authorityTitle, int clientId) {
        return securityAccessService.isAdminOrHasAuthorityGlobalOrForClient(authorityTitle, clientId);
    }

    /**
     * Checks if the current user is admin or has a specified authority globally or
     * for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForAnyClient(String authorityTitle) {
        return securityAccessService.isAdminOrHasAuthorityGlobalOrForAnyClient(authorityTitle);
    }

    /**
     * Checks if the current user has a specified authority globally or
     * for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForAnyClient(String authorityTitle) {
        return securityAccessService.hasAuthorityGlobalOrForAnyClient(authorityTitle);
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
     * Checks if the current user is admin or has a specified authority globally.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobal(String authorityTitle) {
        return securityAccessService.isAdminOrHasAuthorityGlobal(authorityTitle);
    }

    /**
     * Checks if the current user is admin or has any of the specified authorities globally.
     *
     * @param authorityTitles
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAnyAuthorityGlobal(String authorityTitles) {
        return securityAccessService.isAdminOrHasAnyAuthorityGlobal(authorityTitles);
    }

    /**
     * Checks if the current user is admin or has one of the specified authorities
     * globally, for any client or for any project.
     *
     * @param authorityTitles
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return True if the current user has one of the specified authorities or is
     *         admin.
     */
    public boolean isAdminOrHasAnyAuthorityGlobalOrForAnyClient(String authorityTitles) {
        return securityAccessService.isAdminOrHasAnyAuthorityGlobalOrForAnyClient(authorityTitles);
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
     * Checks if the current user has one of the specified authorities globally or for
     * any client.
     *
     * @param authorityTitlesComplete
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return True if the current user is admin or has any of the specified
     *         authorities for any client or project.
     */
    public boolean hasAnyAuthorityGlobalOrForAnyClient(String authorityTitlesComplete) {
        return securityAccessService.hasAnyAuthorityGlobalOrForAnyClient(authorityTitlesComplete);
    }

    /**
     * Checks if the current user is admin.
     * 
     * @return True if the current user is admin.
     */
    public boolean isAdmin() {
        return securityAccessService.isAdmin();
    }

    /**
     * Checks if the current user is admin or has the authority to edit the user
     * with the specified id.
     *
     * @param userId
     *            The user id.
     * @return True if the current user is admin or has the authority to edit the
     *         user with the specified id.
     */
    public boolean isAdminOrHasAuthorityToEditUser(int userId) {
        return securityAccessService.isAdminOrHasAuthorityToEditUser(userId);
    }

    /**
     * Checks if the current user is admin or has the authority to edit the user group
     * with the specified id.
     *
     * @param roleId
     *            The user group id.
     * @return True if the current user is admin or has the authority to edit the
     *         user group with the specified id.
     */
    public boolean isAdminOrHasAuthorityToEditRole(int roleId) {
        return securityAccessService.isAdminOrHasAuthorityToEditRole(roleId);
    }
}
