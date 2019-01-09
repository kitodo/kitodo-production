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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RoleDAO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class RoleService extends SearchDatabaseService<Role, RoleDAO> {

    private static RoleService instance = null;

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
        if (Objects.equals(instance, null)) {
            synchronized (RoleService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new RoleService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Role");
    }

    @Override
    public String createCountQuery(Map filters) {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList()) {
            return "SELECT COUNT(*) FROM Role";
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewRoleList()) {
            return createQueryRolesForCurrentUser(filters).toString();
        }
        return "SELECT COUNT(*) FROM Role";
    }

    @Override
    public List<Role> getAllForSelectedClient() {
        return dao.getByQuery("SELECT r FROM Role AS r INNER JOIN r.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public List<Role> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList()) {
            return getAll(sort, offset, size, false);
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewRoleList()) {
            return getAllForSelectedClient(createQueryRolesForCurrentUser(filters).toString(), sort, offset, size), false);
        }
        return new ArrayList<>();
    }

    /**
     * Refresh user's role object after update.
     *
     * @param role
     *            object
     */
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
}
