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

import org.kitodo.data.exceptions.DataException;
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
    private static final String PROJECT_IDENTIFIER = "PROJECT";

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
    public Authentication getCurrentAuthentication() {
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
     * project.
     * 
     * @param authorityTitle
     *            The authority title.
     * @param projectId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForProject(String authorityTitle, int projectId) {
        return hasAuthorityGlobal(authorityTitle) || hasAuthorityForProject(authorityTitle, projectId);
    }

    /**
     * Checks if the current user has a specified authority for a project.
     *
     * @param authorityTitle
     *            The authority title.
     * @param projectId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityForProject(String authorityTitle, int projectId) {
        String titleOfRequiredAuthority = authorityTitle + "_" + PROJECT_IDENTIFIER + "_" + projectId;
        return hasAuthority(titleOfRequiredAuthority);
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
        return isAdmin() || hasAuthorityGlobalOrForProject(authorityTitle, projectId);
    }

    /**
     * Checks if the current user has a specified authority globally or for a
     * client.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The client id.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityGlobalOrForClient(String authorityTitle, int clientId) {
        return hasAuthorityGlobal(authorityTitle) || hasAuthorityForClient(authorityTitle, clientId);
    }

    /**
     * Checks if the current user has a specified authority for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The client id.
     * @return True if the current user has the specified authority.
     */
    public boolean hasAuthorityForClient(String authorityTitle, int clientId) {
        String titleOfRequiredAuthority = authorityTitle + "_" + CLIENT_IDENTIFIER + "_" + clientId;
        return hasAuthority(titleOfRequiredAuthority);
    }

    /**
     * Checks if the current user is admin or has a specified authority globally or
     * for a client.
     *
     * @param authorityTitle
     *            The authority title.
     * @param clientId
     *            The project id.
     * @return True if the current user has the specified authority.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForClient(String authorityTitle, int clientId) {
        return isAdmin() || hasAuthorityGlobalOrForClient(authorityTitle, clientId);
    }

    /**
     * Checks if the current user is admin.
     * 
     * @return True if the current user has the admin authority
     */
    public boolean isAdmin() {
        return hasAuthorityGlobal("admin");
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
     * Checks if the current user has any authority globally.
     * 
     * @return True if the current user has any authority globally.
     */
    public boolean hasAnyAuthorityGlobal() {
        Collection<? extends GrantedAuthority> authorities = getAuthoritiesOfCurrentAuthentication();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().contains(GLOBAL_IDENTIFIER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current user is admin or has a specified authority globally.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user is admin or has the specified authority
     *         globally.
     */
    public boolean isAdminOrHasAuthorityGlobal(String authorityTitle) {
        return isAdmin() || hasAuthorityGlobal(authorityTitle);
    }

    /**
     * Checks if the current user is admin or has any of the specified authorities
     * globally.
     *
     * @param authorityTitles
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return True if the current user is admin or has any of the specified
     *         authorities globally
     */
    public boolean isAdminOrHasAnyAuthorityGlobal(String authorityTitles) {
        return isAdmin() || hasAnyAuthorityGlobal(authorityTitles);
    }

    /**
     * Checks if the current user has a specified authority globally, for any client
     * or for any project.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority globally, for
     *         any client or for any project.
     */
    public boolean hasAuthorityGlobalOrForAnyClientOrForAnyProject(String authorityTitle) {
        return hasAuthorityGlobal(authorityTitle) || hasAuthorityForAnyClient(authorityTitle)
                || hasAuthorityForAnyProject(authorityTitle);
    }

    /**
     * Checks if the current user is admin or has a specified authority globally or
     * for any client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current is admin or user has the specified authority
     *         globally or for any client.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForAnyClient(String authorityTitle) {
        return isAdmin() || hasAuthorityGlobalOrForAnyClient(authorityTitle);
    }

    /**
     * Checks if the current user has a specified authority globally or for any
     * client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority globally or for
     *         any project.
     */
    public boolean hasAuthorityGlobalOrForAnyClient(String authorityTitle) {
        return hasAuthorityGlobal(authorityTitle) || hasAuthorityForAnyClient(authorityTitle);
    }

    /**
     * Checks if the current user has a specified authority for any client.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority for any client.
     */
    public boolean hasAuthorityForAnyClient(String authorityTitle) {
        return hasAuthority(authorityTitle + "_" + CLIENT_IDENTIFIER + "_ANY");
    }

    /**
     * Checks if the current user has a specified authority for any project.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user has the specified authority for any project.
     */
    public boolean hasAuthorityForAnyProject(String authorityTitle) {
        return hasAuthority(authorityTitle + "_" + PROJECT_IDENTIFIER + "_ANY");
    }

    /**
     * Checks if the current user is admin or has a specified authority globally,
     * for any client or for any project.
     *
     * @param authorityTitle
     *            The authority title.
     * @return True if the current user is admin or has the specified authority for
     *         any client or project.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForAnyClientOrForAnyProject(String authorityTitle) {
        return isAdmin() || hasAuthorityGlobalOrForAnyClientOrForAnyProject(authorityTitle);
    }

    /**
     * Checks if the current user is admin or has one of the specified authorities globally, for
     * any client or for any project.
     *
     * @param authorityTitlesComplete
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return True if the current user is admin or has any of the specified
     *         authorities for any client or project.
     */
    public boolean isAdminOrHasAnyAuthorityGlobalOrForAnyClientOrForAnyProject(String authorityTitlesComplete) {
        if (isAdmin()) {
            return true;
        }
        return hasAnyAuthorityGlobalOrForAnyClientOrForAnyProject(authorityTitlesComplete);
    }

    /**
     * Checks if the current user has one of the specified authorities globally, for
     * any client or for any project.
     *
     * @param authorityTitlesComplete
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return True if the current user is admin or has any of the specified
     *         authorities for any client or project.
     */
    public boolean hasAnyAuthorityGlobalOrForAnyClientOrForAnyProject(String authorityTitlesComplete) {
        String[] authorityTitles = getStringArray(authorityTitlesComplete);
        for (String authorityTitle : authorityTitles) {
            if (hasAuthorityGlobalOrForAnyClientOrForAnyProject(authorityTitle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current user is admin or has the specified authorities
     * globally, for the specified project or for the client which is related to the
     * project.
     *
     * @param authorityTitle
     *            The authority title.
     * 
     * @param projectId
     *            The project id.
     * @return True if the current user is admin or has the specified authority
     *         global, for the specified project or for the client of the project.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForProjectOrForRelatedClient(String authorityTitle, int projectId)
            throws DataException {
        int clientId = serviceManager.getClientService()
                .getIdFromJSONObject(serviceManager.getClientService().findByProjectId(projectId));
        return isAdminOrHasAuthorityGlobalOrForClientOrForProject(authorityTitle, clientId, projectId);
    }

    /**
     * Checks if the current user is admin or has the specified authorities
     * globally, for the specified client or for the specified project.
     *
     * @param authorityTitle
     *            The authority titles separated with commas e.g. "authority1,
     *            authority2, authority3".
     * @return True if the current user is admin or has any of the specified
     *         authorities for the specified client or the specified project.
     */
    public boolean isAdminOrHasAuthorityGlobalOrForClientOrForProject(String authorityTitle, int clientId,
            int projectId) {
        return isAdmin() || hasAuthorityGlobal(authorityTitle) || hasAuthorityForClient(authorityTitle, clientId)
                || hasAuthorityForProject(authorityTitle, projectId);
    }

    /**
     * Get list of client id for given authority title.
     * 
     * @param authorityTitle
     *            as String
     * @return list of Client ids
     */
    public List<Integer> getClientIdListForAuthority(String authorityTitle) {
        List<Integer> clientIdList = new ArrayList<>();
        if (hasAuthorityForAnyClient(authorityTitle)) {
            Collection<? extends GrantedAuthority> authorities = getAuthoritiesOfCurrentAuthentication();
            clientIdList = getIdsOfAuthoritiesByFilter(authorities,authorityTitle + "_" + CLIENT_IDENTIFIER + "_");
        }
        return clientIdList;
    }

    private List<Integer> getIdsOfAuthoritiesByFilter(Collection<? extends GrantedAuthority> authorities, String filter) {
        List<Integer> idList = new ArrayList<>();
        for (GrantedAuthority authority : authorities) {
            String currentAuthority = authority.getAuthority();
            if (currentAuthority.contains(filter) && !currentAuthority.contains(filter + "ANY")) {
                int id = getNumberAfterLastUnderscore(currentAuthority);
                if (!idList.contains(id)) {
                    idList.add(getNumberAfterLastUnderscore(currentAuthority));
                }
            }
        }
        return idList;
    }

    /**
     * Returns the last number of a string which is separated by an underscore "_"
     * as integer. In case of more than on underscore, the last one is used.
     * 
     * @param string
     *            The string which needs to have at least one underscore, e.g.
     *            "test_123"
     * @return The last number as int
     */
    private int getNumberAfterLastUnderscore(String string) {
        String id = string.substring(string.lastIndexOf("_") + 1);
        return Integer.valueOf(id);
    }

    /**
     * Checks if the current user is admin or has the authority to view the user
     * with the specified id.
     * 
     * @param userId
     *            The user id.
     * @return True if the current user is admin or has the authority to view the
     *         user with the specified id.
     */
    public boolean isAdminOrHasAuthorityToViewUser(int userId) {
        String authorityTitle = "viewUser";
        if (isAdminOrHasAuthorityGlobal(authorityTitle)) {
            return true;
        }
        return hasAuthorityForUser(authorityTitle, userId);
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
        String authorityTitle = "editUser";
        if (isAdminOrHasAuthorityGlobal(authorityTitle)) {
            return true;
        }
        return hasAuthorityForUser(authorityTitle, userId);
    }

    private boolean hasAuthorityForUser(String authorityTitle, int userId) {
        List<Integer> clientIdListForAuthority = getClientIdListForAuthority(authorityTitle);
        if (!clientIdListForAuthority.isEmpty()) {
            List<Integer> allActiveUserIdsVisibleForCurrentUser = serviceManager.getUserService()
                    .getAllActiveUserIdsByClientIds(clientIdListForAuthority);
            return allActiveUserIdsVisibleForCurrentUser.contains(userId);
        }
        return false;
    }

    /**
     * Checks if the current user is admin or has the authority to edit the user group
     * with the specified id.
     *
     * @param userGroupId
     *            The user group id.
     * @return True if the current user is admin or has the authority to edit the
     *         user group with the specified id.
     */
    public boolean isAdminOrHasAuthorityToEditUserGroup(int userGroupId) {
        String authorityTitle = "editUserGroup";
        if (isAdminOrHasAuthorityGlobal(authorityTitle)) {
            return true;
        }
        return hasAuthorityForUserGroup(authorityTitle, userGroupId);
    }

    /**
     * Checks if the current user is admin or has the authority to view the user group
     * with the specified id.
     *
     * @param userGroupId
     *            The user group id.
     * @return True if the current user is admin or has the authority to view the
     *         user group with the specified id.
     */
    public boolean isAdminOrHasAuthorityToViewUserGroup(int userGroupId) {
        String authorityTitle = "viewUserGroup";
        if (isAdminOrHasAuthorityGlobal(authorityTitle)) {
            return true;
        }
        return hasAuthorityForUserGroup(authorityTitle, userGroupId);
    }

    private boolean hasAuthorityForUserGroup(String authorityTitle, int userId) {
        List<Integer> clientIdListForAuthority = getClientIdListForAuthority(authorityTitle);
        if (!clientIdListForAuthority.isEmpty()) {
            List<Integer> allActiveUserGroupIdsVisibleForCurrentUser = serviceManager.getUserGroupService()
                .getAllUserGroupIdsByClientIds(clientIdListForAuthority);
            return allActiveUserGroupIdsVisibleForCurrentUser.contains(userId);
        }
        return false;
    }
}
