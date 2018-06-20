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

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;

public class UserGroupDAO extends BaseDAO<UserGroup> {

    private static final long serialVersionUID = 4987176626562271217L;

    @Override
    public UserGroup getById(Integer id) throws DAOException {
        UserGroup result = retrieveObject(UserGroup.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<UserGroup> getAll() throws DAOException {
        return retrieveAllObjects(UserGroup.class);
    }

    @Override
    public List<UserGroup> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM UserGroup ORDER BY id ASC", offset, size);
    }

    @Override
    public UserGroup save(UserGroup userGroup) throws DAOException {
        storeObject(userGroup);
        return retrieveObject(UserGroup.class, userGroup.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(UserGroup.class, id);
    }

    /**
     * Refresh user's group object after some changes.
     *
     * @param userGroup
     *            object
     */
    public void refresh(UserGroup userGroup) {
        refreshObject(userGroup);
    }

    /**
     * Get all user groups visible for current user - user groups of users assigned
     * to projects with certain clients.
     *
     * @param clientIdList
     *            list of client ids assigned to which current user is assigned
     * @return list of user groups
     */
    public List<UserGroup> getAllUserGroupsByClientIds(List<Integer> clientIdList) {
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllActiveUsersByClientIds(clientIdList);
        List<Integer> userIdList = new ArrayList<>();
        for (User user : users) {
            userIdList.add(user.getId());
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userIdList", userIdList);
        return getByQuery("SELECT ug FROM UserGroup AS ug JOIN ug.users AS u WHERE u.id IN :userIdList GROUP BY ug.id",
            parameters);
    }
}
