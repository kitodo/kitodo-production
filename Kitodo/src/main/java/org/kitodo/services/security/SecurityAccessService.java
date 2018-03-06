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

    private Collection<? extends GrantedAuthority> getAuthoritiesOfCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        } else {
            return new ArrayList<>();
        }
    }

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
    public boolean hasAuthorityGlobalOrForProject(String authorityTitle, int projectId) {
        if (hasGlobalAuthority(authorityTitle)) {
            return true;
        } else {
            String titleOfRequiredAuthority = authorityTitle + "_PROJECT_" + projectId;
            return hasAuthority(titleOfRequiredAuthority);
        }
    }

    /**
     * Checks if the current user is admin or has a specified authority globally or
     * in relation to a project id.
     *
     * @param authorityTitle
     *            The authority title.
     * @param projectId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForProject(String authorityTitle, int projectId) {
        if (isAdmin()) {
            return true;
        } else {
            return hasAuthorityGlobalOrForProject(authorityTitle, projectId);
        }
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
    public boolean hasAuthorityGlobalOrForClient(String authorityTitle, int clientId) {
        if (hasGlobalAuthority(authorityTitle)) {
            return true;
        } else {
            String titleOfRequiredAuthority = authorityTitle + "_CLIENT_" + clientId;
            return hasAuthority(titleOfRequiredAuthority);
        }
    }

    /**
     * Checks if the current user is admin or has a specified authority globally or
     * in relation to a client id.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForClient(String authorityTitle, int clientId) {
        if (isAdmin()) {
            return true;
        } else {
            return hasAuthorityGlobalOrForClient(authorityTitle, clientId);
        }
    }

    private boolean hasAuthority(String authorityTitle) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityTitle);
        Collection<? extends GrantedAuthority> authorities = getAuthoritiesOfCurrentAuthentication();
        return authorities.contains(authority);
    }

    // private boolean hasAnyAuthority(String authorityTitlesComplete) {
    // String[] authorityTitles = getStringArray(authorityTitlesComplete);
    // for (String authorityTitle : authorityTitles) {
    // if (hasAuthority(authorityTitle)) {
    // return true;
    // }
    // }
    // return false;
    // }

    private String[] getStringArray(String strings) {
        strings = strings.replaceAll("\\s+", ""); // remove whitespaces
        return strings.split(",");
    }

    /**
     * Checks if the current user is admin
     * 
     * @return True if the current user has the admin authority
     */
    public boolean isAdmin() {
        return hasGlobalAuthority("admin");
    }

    /**
     * Checks if the current user has a specified authority globally.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasGlobalAuthority(String authorityTitle) {
        return hasAuthority(authorityTitle + "_GLOBAL");
    }

    /**
     * Checks if the current user has a specified authority globally.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasGlobalAuthority(String authorityTitle) {
        if (isAdmin()) {
            return true;
        }
        return hasGlobalAuthority(authorityTitle);
    }

    /**
     * Checks if the current user has a specified authority globally, for any client
     * or for any project.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForAnyClientOrForAnyProject(String authorityTitle) {
        if (hasAuthority(authorityTitle + "_GLOBAL")) {
            return true;
        }
        if (hasAuthority(authorityTitle + "_ANYCLIENT")) {
            return true;
        }
        return hasAuthority(authorityTitle + "_ANYPROJECT");
    }

    /**
     * Checks if the current user has a specified authority globally, for any client
     * or for any project.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForAnyClientOrForAnyProject(String authorityTitle) {
        if (isAdmin()) {
            return true;
        }
        return hasAuthorityGlobalOrForAnyClientOrForAnyProject(authorityTitle);
    }

    /**
     * Checks if the current user has one of the specified authorities globally, for
     * any client or for any project.
     *
     * @param authorityTitlesComplete
     *            The authority titles separated with commas.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAnyAuthorityGlobalOrForAnyClientOrForAnyProject(String authorityTitlesComplete) {
        if (isAdmin()) {
            return true;
        }
        String[] authorityTitles = getStringArray(authorityTitlesComplete);
        for (String authorityTitle : authorityTitles) {
            if (hasAuthorityGlobalOrForAnyClientOrForAnyProject(authorityTitle)) {
                return true;
            }
        }
        return false;
    }
}
