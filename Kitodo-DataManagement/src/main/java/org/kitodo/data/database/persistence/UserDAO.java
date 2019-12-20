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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;

public class UserDAO extends BaseDAO<User> {

    @Override
    public User getById(Integer userId) throws DAOException {
        User user = retrieveObject(User.class, userId);
        if (user == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        return getByQuery("FROM User WHERE deleted = 0");
    }

    @Override
    public List<User> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM User WHERE deleted = 0 ORDER BY id ASC", offset, size);
    }

    @Override
    public List<User> getAllNotIndexed(int offset, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(User user) throws DAOException {
        user.selfDestruct();
        save(user);
    }

    @Override
    public void remove(Integer userId) throws DAOException {
        User user = getById(userId);
        user.selfDestruct();
        save(user);
    }

    /**
     * Count amount of users with exactly the same login like given but different
     * id.
     *
     * @param id
     *            of user
     * @param login
     *            of user
     * @return list of users
     */
    public Long countUsersWithExactlyTheSameLogin(Integer id, String login) throws DAOException {
        Map<String, Object> parameters = new HashMap<>();
        if (Objects.nonNull(id)) {
            parameters.put("id", id);
            parameters.put("login", login);
            return count("SELECT COUNT(*) FROM User WHERE id != :id AND login = :login",
                    parameters);
        }

        parameters.put("login", login);
        return count("SELECT COUNT(*) FROM User WHERE login = :login", parameters);
    }

    /**
     * Get all active users sorted by surname and name.
     *
     * @return sorted list of all active users as User objects
     */
    public List<User> getAllActiveUsersSortedByNameAndSurname() {
        return getByQuery("FROM User WHERE active = 1 AND deleted = 0 ORDER BY surname ASC, name ASC");
    }
}
