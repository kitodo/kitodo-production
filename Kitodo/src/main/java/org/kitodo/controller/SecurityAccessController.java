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
     * Checks if the current user has a specified authority globally or in relation
     * to a project id.
     *
     * @param authorityTitle
     *            The authority title.
     * @param projectId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityForProjectOrGlobally(String authorityTitle, int projectId) {
        return securityAccessService.hasAuthorityForProject(authorityTitle, projectId);
    }

    /**
     * Checks if the current user has a specified authority globally or in relation
     * to a client id.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityForClientOrGlobally(String authorityTitle, int clientId) {
        return securityAccessService.hasAuthorityForClient(authorityTitle, clientId);
    }

    /**
     * Checks if the current user has a specified authority globally.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobally(String authorityTitle) {
        return securityAccessService.hasGlobalAuthority(authorityTitle);
    }

}
