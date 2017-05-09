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

import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;

public class PropertyDAO extends BaseDAO {

    private static final long serialVersionUID = 834210846673022251L;

    /**
     * Find property object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public Property find(Integer id) throws DAOException {
        Property result = (Property) retrieveObject(Property.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all properties from the database.
     *
     * @return all persisted templates' properties
     */
    @SuppressWarnings("unchecked")
    public List<Property> findAll() {
        return retrieveAllObjects(Property.class);
    }

    /**
     * Find properties by query.
     * 
     * @param query
     *            as String
     * @return list of properties
     */
    @SuppressWarnings("unchecked")
    public List<Property> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    public Property save(Property property) throws DAOException {
        storeObject(property);
        return (Property) retrieveObject(Property.class, property.getId());
    }

    /**
     * The function remove() removes a property
     *
     * @param property
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Property property) throws DAOException {
        removeObject(property);
    }

    /**
     * Count property objects in table.
     * 
     * @param query
     *            as String
     * @return amount of objects as Long
     */
    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
