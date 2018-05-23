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

@Named("SecurityAccessController")
@RequestScoped
public class SecurityAccessController {
    private SecurityAccessService securityAccessService = new ServiceManager().getSecurityAccessService();

    /**
     * Checks if the current user has a specified authority globally or for a
     * project.
     *
     * @param authorityTitle
     *            The authority title.
     * @param projectId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForProject(String authorityTitle, int projectId) {
        return securityAccessService.hasAuthorityGlobalOrForProject(authorityTitle, projectId);
    }

    /**
     * Checks if the current user is admin or has a specified authority globally or
     * for a project.
     *
     * @param authorityTitle
     *            The authority title.
     * @param projectId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForProject(String authorityTitle, int projectId) {
        return securityAccessService.isAdminOrHasAuthorityGlobalOrForProject(authorityTitle, projectId);
    }

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
     * for a client or for a project.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The client id.
     * @param projectId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForClientOrForProject(String authorityTitle, int clientId, int projectId) {
        return securityAccessService.isAdminOrHasAuthorityGlobalOrForClientOrForProject(authorityTitle, clientId, projectId);
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
     * Checks if the current user has a specified authority globally, for any client
     * or for any project.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityForAny(String authorityTitle) {
        return securityAccessService.hasAuthorityGlobalOrForAnyClientOrForAnyProject(authorityTitle);
    }

    /**
     * Checks if the current user is admin or has a specified authority globally,
     * for any client or for any project.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority or is admin.
     */
    public boolean isAdminOrHasAuthorityForAny(String authorityTitle) {
        return securityAccessService.isAdminOrHasAuthorityGlobalOrForAnyClientOrForAnyProject(authorityTitle);
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
    public boolean isAdminOrHasAnyAuthorityForAny(String authorityTitles) {
        return securityAccessService.isAdminOrHasAnyAuthorityGlobalOrForAnyClientOrForAnyProject(authorityTitles);
    }

    /**
     * Checks if the current user is admin.
     * 
     * @return True if the current user is admin.
     */
    public boolean isAdmin() {
        return securityAccessService.isAdmin();
    }
}
