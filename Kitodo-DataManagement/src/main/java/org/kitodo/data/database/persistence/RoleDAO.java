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

package org.kitodo.data.database.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;

public class RoleDAO extends BaseDAO<Role> {

    @Override
    public Role getById(Integer id) throws DAOException {
        Role role = retrieveObject(Role.class, id);
        if (role == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return role;
    }

    @Override
    public List<Role> getAll() throws DAOException {
        return retrieveAllObjects(Role.class);
    }

    @Override
    public List<Role> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Role ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Role> getAllNotIndexed(int offset, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Role.class, id);
    }

    /**
     * Get all user roles assigned to selected client for current user.
     *
     * @param clientId
     *            selected client id for current user
     * @return list of user roles
     */
    public List<Role> getAllRolesByClientId(int clientId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", clientId);
        return getByQuery("SELECT r FROM Role AS r JOIN r.client AS c WHERE c.id = :clientId GROUP BY r.id",
            parameters);
    }

    /**
     * Get all roles available to assign to the edited user. It will be displayed
     * in the addRolesPopup.
     *
     * @param clients
     *            list of clients to which edited user is assigned
     * @return list of all matching roles
     */
    public List<Role> getAllAvailableForAssignToUser(List<Client> clients) {
        List<Integer> clientIds = new ArrayList<>();

        for (Client client : clients) {
            clientIds.add(client.getId());
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientIds", clientIds);
        return getByQuery("SELECT r FROM Role AS r JOIN r.client AS c WITH c.id IN :clientIds",
                parameters);
    }
}
