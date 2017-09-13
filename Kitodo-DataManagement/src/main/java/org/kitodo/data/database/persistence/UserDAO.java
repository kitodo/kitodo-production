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

import java.util.List;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;

public class UserDAO extends BaseDAO<User> {

    private static final long serialVersionUID = 834210840673022251L;

    /**
     * Find user object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public User find(Integer id) throws DAOException {
        User result = retrieveObject(User.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all users from the database.
     *
     * @return all persisted users
     */
    public List<User> findAll() {
        return retrieveObjects("FROM User WHERE deleted = 0");
    }

    /**
     * Retrieves all users in given range.
     *
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    public List<User> getAll(int first, int max) throws DAOException {
        return retrieveObjects("FROM User WHERE deleted = 0", first, max);
    }

    public User save(User user) throws DAOException {
        storeObject(user);
        return retrieveObject(User.class, user.getId());
    }

    /**
     * The function remove() removes a user from the environment. Since the user
     * ID may still be referenced somewhere, the user account is invalidated
     * instead.
     *
     * @param user
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(User user) throws DAOException {
        user.selfDestruct();
        save(user);
    }

    /**
     * The function remove() removes a user from the environment. Since the user
     * ID may still be referenced somewhere, the user account is invalidated
     * instead.
     *
     * @param id
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Integer id) throws DAOException {
        User user = find(id);
        user.selfDestruct();
        save(user);
    }

    public List<User> search(String query) {
        return retrieveObjects(query);
    }

    public List<User> search(String query, String parameter) throws DAOException {
        return retrieveObjects(query, parameter);
    }

    /**
     * Search for a list of users by a named parameter.
     *
     * @param query
     *            Search query
     * @param namedParameter
     *            Name of named parameter
     * @param parameter
     *            Parameter value
     * @return list of users
     * @throws DAOException
     *             if a HibernateException is thrown
     */
    public List<User> search(String query, String namedParameter, String parameter) throws DAOException {
        return retrieveObjects(query, namedParameter, parameter);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }

    /**
     * Refresh user object after some changes.
     *
     * @param user
     *            object
     */
    public void refresh(User user) {
        refreshObject(user);
    }

    /**
     * Get all active users sorted by surname and name.
     *
     * @return sorted list of all active users as User objects
     */
    public List<User> getAllActiveUsersSortedByNameAndSurname() {
        return search("FROM User WHERE active = 1 AND deleted = 0 ORDER BY surname ASC, name ASC");
    }
}
