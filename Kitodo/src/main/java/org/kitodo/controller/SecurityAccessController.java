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

    public boolean hasAuthorityForProject(String module, String authorityTitle, int projectId) {
        return securityAccessService.hasAuthorityForProject(authorityTitle, projectId);
    }

    public boolean hasAuthorityForClient(String module, String authorityTitle, int clientId) {
        return securityAccessService.hasAuthorityForClient(authorityTitle, clientId);
    }

}
