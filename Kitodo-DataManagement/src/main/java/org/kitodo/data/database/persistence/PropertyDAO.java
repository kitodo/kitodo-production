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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.PersistenceException;

import org.hibernate.Session;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;

public class PropertyDAO extends BaseDAO<Property> {

    @Override
    public Property getById(Integer id) throws DAOException {
        Property property = retrieveObject(Property.class, id);
        if (property == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return property;
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
    public void remove(Integer propertyId) throws DAOException {
        removeObject(Property.class, propertyId);
    }

    /**
     * Retrieve and return distinct Property titles from database.
     *
     * @return list of distinct Property titles sorted alphabetically.
     */
    public List<String> retrieveDistinctTitles() {
        try (Session session = HibernateUtil.getSession()) {
            List<?> titles = session.createQuery("SELECT DISTINCT title FROM Property").list();
            return titles.stream().map(Object::toString).sorted().collect(Collectors.toList());
        } catch (PersistenceException e) {
            return Collections.emptyList();
        }
    }
}
