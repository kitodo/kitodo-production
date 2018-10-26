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
     * Checks if the current user has the authority to edit the user
     * with the specified id.
     *
     * @param userId
     *            The user id.
     * @return True if the current user has the authority to edit the
     *         user with the specified id.
     */
    public boolean hasAuthorityToEditUser(int userId) {
        return securityAccessService.hasAuthorityToEditUser(userId);
    }

    /**
     * Checks if the current user is admin or has the authority to edit the role
     * with the specified id.
     *
     * @param roleId
     *            the role id
     * @return True if the current user is admin or has the authority to edit the
     *         role with the specified id.
     */
    public boolean hasAuthorityToEditRole(int roleId) {
        return securityAccessService.hasAuthorityToEditRole(roleId);
    }
}
