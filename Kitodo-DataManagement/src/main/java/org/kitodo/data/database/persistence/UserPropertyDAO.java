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

import org.kitodo.data.database.beans.UserProperty;
import org.kitodo.data.database.exceptions.DAOException;

public class UserPropertyDAO extends BaseDAO {

    private static final long serialVersionUID = 834210840673022351L;

    /**
     * Find user property object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public UserProperty find(Integer id) throws DAOException {
        UserProperty result = (UserProperty) retrieveObject(UserProperty.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all users from the database.
     *
     * @return all persisted users' properties
     */
    @SuppressWarnings("unchecked")
    public List<UserProperty> findAll() {
        return retrieveAllObjects(UserProperty.class);
    }

    public UserProperty save(UserProperty userProperty) throws DAOException {
        storeObject(userProperty);
        return (UserProperty) retrieveObject(UserProperty.class, userProperty.getId());
    }

    /**
     * The function remove() removes a user property
     *
     * @param userProperty
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(UserProperty userProperty) throws DAOException {
        removeObject(userProperty);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
