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

package org.kitodo.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * If module wants to use own roles, it needs to extend this class. Currently it
 * is usable only for global roles as client id is not available to modules.
 */
public abstract class SecurityAccess {

    private static final String GLOBAL_IDENTIFIER = "GLOBAL";
    private static final String CLIENT_IDENTIFIER = "CLIENT";

    private Collection<? extends GrantedAuthority> getAuthoritiesOfCurrentAuthentication() {
        Authentication authentication = getCurrentAuthentication();
        if (Objects.nonNull(authentication)) {
            return authentication.getAuthorities();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get client id for current session.
     *
     * @return value of client id for current session
     */
    // TODO: find a way for modules to get information about current client id
    public abstract int getClientId();

    /**
     * Check if the current user has a specified authority globally.
     *
     * @param authorityTitle
     *            the authority title
     * @return true if the current user has the specified authority
     */
    public boolean hasAuthorityGlobal(String authorityTitle) {
        return hasAuthority(authorityTitle + "_" + GLOBAL_IDENTIFIER);
    }

    /**
     * Check if the current user has a specified authority for a client.
     *
     * @param authorityTitle
     *            the authority title
     * @return true if the current user has the specified authority
     */
    public boolean hasAuthorityForClient(String authorityTitle) {
        String clientAuthority = authorityTitle + "_" + CLIENT_IDENTIFIER + "_" + getClientId();
        return hasAuthority(clientAuthority);
    }

    /**
     * Check if the current user has a specified authority globally or for a client.
     *
     * @param authorityTitle
     *            the authority title
     * @return true if the current user has the specified authority
     */
    public boolean hasAuthorityGlobalOrForClient(String authorityTitle) {
        return hasAuthorityGlobal(authorityTitle) || hasAuthorityForClient(authorityTitle);
    }

    /**
     * Check if the current user has any of the specified authorities globally.
     *
     * @param authorityTitles
     *            the authority titles separated with commas e.g. "authority1,
     *            authority2, authority3"
     * @return true if the current user has any of the specified authorities
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
     * Check if the current user has a specified authority for a client.
     *
     * @param authorityTitles
     *            the authority title
     * @return true if the current user has the specified authority
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
     * Check if the current user has any of the specified authorities globally or
     * for client.
     *
     * @param authorityTitles
     *            the authority titles separated with commas e.g. "authority1,
     *            authority2, authority3"
     * @return true if the current user has any of the specified authorities
     *         globally or for client
     */
    public boolean hasAnyAuthorityGlobalOrForClient(String authorityTitles) {
        return hasAnyAuthorityGlobal(authorityTitles) || hasAnyAuthorityForClient(authorityTitles);
    }

    /**
     * Get Authentication object of current threads security context.
     *
     * @return authentication object
     */
    protected Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasAuthority(String authorityTitle) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityTitle);
        Collection<? extends GrantedAuthority> authorities = getAuthoritiesOfCurrentAuthentication();
        return authorities.contains(authority);
    }

    private String[] getStringArray(String values) {
        // remove white spaces and split values
        return values.replaceAll("\\s+", "").split(",");
    }
}
