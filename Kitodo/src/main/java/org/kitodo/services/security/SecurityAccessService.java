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
import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.Client;
import org.kitodo.security.SecurityUserDetails;
import org.kitodo.services.ServiceManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAccessService {

    private static SecurityAccessService instance = null;
    private static ServiceManager serviceManager = new ServiceManager();
    private static final String GLOBAL_IDENTIFIER = "GLOBAL";
    private static final String CLIENT_IDENTIFIER = "CLIENT";

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
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Gets Authentication object of current threads security context.
     * 
     * @return authentication object
     */
    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Gets the current authenticated user of current threads security context.
     *
     * @return The SecurityUserDetails object or null if no user is authenticated.
     */
    public SecurityUserDetails getAuthenticatedSecurityUserDetails() {
        if (isAuthenticated()) {
            Object principal = getCurrentAuthentication().getPrincipal();
            if (principal instanceof SecurityUserDetails) {
                return (SecurityUserDetails) principal;
            }
        }
        return null;
    }

    /**
     * Checks if there is currently an authenticated user.
     * 
     * @return true if there is currently an authenticated user
     */
    public boolean isAuthenticated() {
        Authentication currentAuthentication = getCurrentAuthentication();
        if (currentAuthentication != null) {
            return currentAuthentication.isAuthenticated();
        }
        return false;
    }

    private boolean hasAuthority(String authorityTitle) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityTitle);
        Collection<? extends GrantedAuthority> authorities = getAuthoritiesOfCurrentAuthentication();
        return authorities.contains(authority);
    }

    private String[] getStringArray(String strings) {
        strings = strings.replaceAll("\\s+", ""); // remove whitespaces
        return strings.split(",");
    }

    /**
     * Checks if the current user has a specified authority globally or for a
     * client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForClient(String authorityTitle) {
        return hasAuthorityGlobal(authorityTitle) || hasAuthorityForClient(authorityTitle);
    }

    /**
     * Checks if the current user has a specified authority for a client.
     *
     * @param authorityTitles
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAnyAuthorityForClient(String authorityTitles) {
        Client selectedClient = serviceManager.getUserService().getSessionClientOfAuthenticatedUser();
        if (Objects.nonNull(selectedClient)) {
            String[] authorityTitlesArray = getStringArray(authorityTitles);
            for (String authorityTitle : authorityTitlesArray) {
                if (hasAuthorityForClient(authorityTitle)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Checks if the current user has a specified authority for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityForClient(String authorityTitle) {
        Client selectedClient = serviceManager.getUserService().getSessionClientOfAuthenticatedUser();
        if (Objects.nonNull(selectedClient)) {
            String titleOfRequiredAuthority = authorityTitle + "_" + CLIENT_IDENTIFIER + "_" + selectedClient.getId();
            return hasAuthority(titleOfRequiredAuthority);
        }
        return false;
    }

    /**
     * Checks if the current user has a specified authority globally.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobal(String authorityTitle) {
        return hasAuthority(authorityTitle + "_" + GLOBAL_IDENTIFIER);
    }

    /**
     * Checks if the current user has any of the specified authorities globally.
     *
     * @param authorityTitles
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return True if the current user has any of the specified authorities
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
     * Checks if the current user has any of the specified authorities globally or
     * for client.
     *
     * @param authorityTitles
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return true if the current user has any of the specified authorities
     *         globally or for client
     */
    public boolean hasAnyAuthorityGlobalOrForClient(String authorityTitles) {
        return hasAnyAuthorityGlobal(authorityTitles) || hasAnyAuthorityForClient(authorityTitles);
    }

    /**
     * Checks if the current user has the authority to view the user with the
     * specified id.
     * 
     * @param userId
     *            The user id.
     * @return true if the current user has the authority to view the user with the
     *         specified id.
     */
    public boolean hasAuthorityToViewUser(int userId) {
        String authorityTitle = "viewUser";
        if (hasAuthorityGlobal(authorityTitle)) {
            return true;
        }
        return hasAuthorityForUser(authorityTitle, userId);
    }

    /**
     * Checks if the current user has the authority to edit the user with the
     * specified id.
     *
     * @param userId
     *            The user id.
     * @return true if the current user has the authority to edit the user with the
     *         specified id.
     */
    public boolean hasAuthorityToEditUser(int userId) {
        String authorityTitle = "editUser";
        if (hasAuthorityGlobal(authorityTitle)) {
            return true;
        }
        return hasAuthorityForUser(authorityTitle, userId);
    }

    private boolean hasAuthorityForUser(String authorityTitle, int userId) {
        if (hasAuthorityForClient(authorityTitle)) {
            List<Integer> allActiveUserIdsVisibleForCurrentUser = serviceManager.getUserService()
                    .getAllActiveUserIdsByClientId(
                        serviceManager.getUserService().getSessionClientOfAuthenticatedUser().getId());
            return allActiveUserIdsVisibleForCurrentUser.contains(userId);
        }
        return false;
    }

    /**
     * Checks if the current user has the authority to edit the role with the
     * specified id.
     *
     * @param roleId
     *            the role id
     * @return true if the current user has the authority to edit the role with the
     *         specified id.
     */
    public boolean hasAuthorityToEditRole(int roleId) {
        String authorityTitle = "editRole";
        if (hasAuthorityGlobal(authorityTitle)) {
            return true;
        }
        return hasAuthorityForRole(authorityTitle, roleId);
    }

    /**
     * Checks if the current user has the authority to view the role with the
     * specified id.
     *
     * @param roleId
     *            the role id
     * @return true if the current user has the authority to view the role with the
     *         specified id.
     */
    public boolean hasAuthorityToViewRole(int roleId) {
        String authorityTitle = "viewRole";
        if (hasAuthorityGlobal(authorityTitle)) {
            return true;
        }
        return hasAuthorityForRole(authorityTitle, roleId);
    }

    private boolean hasAuthorityForRole(String authorityTitle, int userId) {
        if (hasAuthorityForClient(authorityTitle)) {
            List<Integer> allActiveUserGroupIdsVisibleForCurrentUser = serviceManager.getRoleService()
                    .getAllRolesIdsByClientId();
            return allActiveUserGroupIdsVisibleForCurrentUser.contains(userId);
        }
        return false;
    }
}
