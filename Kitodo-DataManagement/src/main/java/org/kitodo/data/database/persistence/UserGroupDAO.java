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

public class UserGroupDAO extends BaseDAO {

    private static final long serialVersionUID = 4987176626562271217L;

    /**
     * Find user group object by id.
     *
     * @param id of searched object
     * @return result
     * @throws DAOException an exception that can be thrown from the underlying find() procedure failure.
     */
    public UserGroup find(Integer id) throws DAOException {
        UserGroup result = (UserGroup) retrieveObject(UserGroup.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    public UserGroup save(UserGroup userGroup) throws DAOException {
        storeObject(userGroup);
        return (UserGroup) retrieveObject(UserGroup.class, userGroup.getId());
    }

    /**
     * The function remove() removes a user group from database.
     *
     * @param userGroup to be removed
     * @throws DAOException an exception that can be thrown from the underlying save() procedure upon database
     * 				failure.
     */
    public void remove(UserGroup userGroup) throws DAOException {
        if (userGroup.getId() != null) {
            removeObject(userGroup);
        }
    }

    public void remove(Integer id) throws DAOException {
        removeObject(UserGroup.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<UserGroup> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
