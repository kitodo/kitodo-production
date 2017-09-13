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

import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;

public class UserGroupDAO extends BaseDAO<UserGroup> {

    private static final long serialVersionUID = 4987176626562271217L;

    /**
     * Find user group object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public UserGroup find(Integer id) throws DAOException {
        UserGroup result = retrieveObject(UserGroup.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all users' groups from the database.
     *
     * @return all persisted dockets
     */
    public List<UserGroup> findAll() {
        return retrieveAllObjects(UserGroup.class);
    }

    /**
     * Retrieves all user's groups in given range.
     *
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    public List<UserGroup> getAll(int first, int max) throws DAOException {
        return retrieveObjects("FROM UserGroup", first, max);
    }

    public UserGroup save(UserGroup userGroup) throws DAOException {
        storeObject(userGroup);
        return retrieveObject(UserGroup.class, userGroup.getId());
    }

    /**
     * The function remove() removes a user group from database.
     *
     * @param userGroup
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(UserGroup userGroup) throws DAOException {
        if (userGroup.getId() != null) {
            removeObject(userGroup);
        }
    }

    public void remove(Integer id) throws DAOException {
        removeObject(UserGroup.class, id);
    }

    public List<UserGroup> search(String query) {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
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
}
