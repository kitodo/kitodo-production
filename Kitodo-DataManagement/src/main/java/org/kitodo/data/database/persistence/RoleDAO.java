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

import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;

public class RoleDAO extends BaseDAO<Role> {

    private static final long serialVersionUID = 4987176626562271217L;

    @Override
    public Role getById(Integer id) throws DAOException {
        Role result = retrieveObject(Role.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
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
    public List<Role> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Role WHERE indexAction = 'INDEX' OR indexAction IS NULL ORDER BY id ASC",
            offset, size);
    }

    @Override
    public Role save(Role role) throws DAOException {
        storeObject(role);
        return retrieveObject(Role.class, role.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Role.class, id);
    }

    /**
     * Get all user roles visible for current user - user roles of users assigned
     * to projects with certain clients.
     *
     * @param clientId
     *            selected client id for current user
     * @return list of user roles
     */
    public List<Role> getAllRolesByClientIds(int clientId) {
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllActiveUsersByClientId(clientId);
        List<Integer> userIdList = new ArrayList<>();
        for (User user : users) {
            userIdList.add(user.getId());
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userIdList", userIdList);
        return getByQuery("SELECT r FROM Role AS r JOIN r.users AS u WHERE u.id IN :userIdList GROUP BY r.id",
            parameters);
    }
}
