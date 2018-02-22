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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAccessService {

    private static SecurityAccessService instance = null;

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

    private Collection<? extends GrantedAuthority> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Checks if the current user has a specified authority title in relation to a
     * project id.
     * 
     * @param authorityTitle
     *            The authority title.
     * @param projectId
     *            The project id.
     * @return
     */
    public boolean hasAuthorityForProject(String authorityTitle, int projectId) {
        String titleOfRequiredAuthority = authorityTitle + "_PROJECT_" + projectId;
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(titleOfRequiredAuthority);
        Collection<? extends GrantedAuthority> authorities = getAuthorities();
        return authorities.contains(authority);
    }

    /**
     * Checks if the current user has a specified authority title in relation to a
     * client id.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The project id.
     * @return
     */
    public boolean hasAuthorityForClient(String authorityTitle, int clientId) {
        String titleOfRequiredAuthority = authorityTitle + "_CLIENT_" + clientId;
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(titleOfRequiredAuthority);
        Collection<? extends GrantedAuthority> authorities = getAuthorities();
        return authorities.contains(authority);
    }

}
