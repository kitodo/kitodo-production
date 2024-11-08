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

package org.kitodo.production.services.data;

import static org.kitodo.constants.StringConstants.COMMA_DELIMITER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RoleDAO;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortOrder;

public class RoleService extends BaseBeanService<Role, RoleDAO> {

    private static volatile RoleService instance = null;

    private static final String CLIENT_ID = "clientId";

    /**
     * Constructor.
     */
    private RoleService() {
        super(new RoleDAO());
    }

    /**
     * Return singleton variable of type RoleService.
     *
     * @return unique instance of RoleService
     */
    public static RoleService getInstance() {
        RoleService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (RoleService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new RoleService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM Role");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        if (((Boolean)filters.get("allClients"))
                && ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList()) {
            return count();
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewRoleList()) {
            return count("SELECT COUNT(*) FROM Role AS r INNER JOIN r.client AS c WITH c.id = :clientId",
                    Collections.singletonMap(CLIENT_ID, ServiceManager.getUserService().getSessionClientId()));
        }
        return 0L;
    }

    /**
     * Get list of all objects for selected client from database.
     *
     * @return list of all objects for selected client from database
     */
    public List<Role> getAllForSelectedClient() {
        return dao.getByQuery("SELECT r FROM Role AS r INNER JOIN r.client AS c WITH c.id = :clientId",
            Collections.singletonMap(CLIENT_ID, ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Role> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        if (((Boolean)filters.get("allClients"))
                && ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList()) {
            return dao.getByQuery("FROM Role"  + getSort(sortField, sortOrder), Collections.emptyMap(), first,
                    pageSize);
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewRoleList()) {
            return dao.getByQuery("SELECT r FROM Role AS r INNER JOIN r.client AS c WITH c.id = :clientId"
                            + getSort(sortField, sortOrder),
                Collections.singletonMap(CLIENT_ID, ServiceManager.getUserService().getSessionClientId()), first,
                pageSize);
        }
        return new ArrayList<>();
    }

    /**
     * Get all roles available to assign to the edited user. It will be displayed
     * in the addRolesPopup.
     *
     * @param user
     *            id of user which is going to be edited
     * @return list of all matching roles
     */
    public List<Role> getAllAvailableForAssignToUser(User user) throws DAOException {
        if (user.getClients().isEmpty()) {
            return getAll();
        }
        List<Role> roles = dao.getAllAvailableForAssignToUser(user.getClients());
        roles.removeAll(user.getRoles());
        return roles;


    }

    @Override
    public void refresh(Role role) {
        dao.refresh(role);
    }

    /**
     * Get authorizations for given role.
     *
     * @param role
     *            object
     * @return authorizations as list of Strings
     */
    public List<String> getAuthorizationsAsString(Role role) {
        List<Authority> authorities = role.getAuthorities();
        List<String> stringAuthorizations = new ArrayList<>();
        for (Authority authority : authorities) {
            stringAuthorizations.add(authority.getTitle());
        }
        return stringAuthorizations;
    }

    /**
     * Get all user roles for the selected client.
     *
     * @param clientId
     *            the selected client id
     * @return The list of all user roles for the given client IDs
     */
    public List<Role> getAllRolesByClientId(int clientId) {
        return dao.getAllRolesByClientId(clientId);
    }

    /**
     * Create and return String containing the titles of all given roles joined by a ", ".
     * @param roles list of roles
     * @return String containing role titles
     */
    public static String getRoleTitles(List<Role> roles) {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList()) {
            return roles.stream().map(Role::getTitle).collect(Collectors.joining(COMMA_DELIMITER));
        } else {
            Client currentClient = ServiceManager.getUserService().getSessionClientOfAuthenticatedUser();
            return roles.stream().filter(role -> role.getClient().equals(currentClient)).map(Role::getTitle)
                    .collect(Collectors.joining(COMMA_DELIMITER));
        }
    }

    /**
     * Get number of roles of session client of currently authenticated user.
     *
     * @return number of roles assigned to session client of currently authenticated user
     *
     * @throws DAOException when retrieving number of roles from database fails
     */
    public int getNumberOfRolesOfCurrentClient() throws DAOException {
        Client currentSessionClient = ServiceManager.getUserService().getSessionClientOfAuthenticatedUser();
        return (int) ServiceManager.getRoleService().getAll().stream()
                .filter(role -> role.getClient().equals(currentSessionClient)).count();

    }
}
