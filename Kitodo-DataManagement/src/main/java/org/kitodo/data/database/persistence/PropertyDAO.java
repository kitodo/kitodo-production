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

public class PropertyDAO extends BaseDAO<Property> {

    private static final long serialVersionUID = 834210846673022251L;

    @Override
    public Property getById(Integer id) throws DAOException {
        Property result = retrieveObject(Property.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Property> getAll() throws DAOException {
        return retrieveAllObjects(Property.class);
    }

    @Override
    public List<Property> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Property ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Property> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Property WHERE indexAction = 'INDEX' OR indexAction IS NULL ORDER BY id ASC",
            offset, size);
    }

    @Override
    public Property save(Property property) throws DAOException {
        storeObject(property);
        return retrieveObject(Property.class, property.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Property.class, id);
    }
}
