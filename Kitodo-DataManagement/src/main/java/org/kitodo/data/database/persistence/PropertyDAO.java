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

import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.Helper;

public class PropertyDAO extends BaseDAO<Property> {

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
        Property result = retrieveObject(Property.class, id);
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
    public List<Property> search(String query) {
        return retrieveObjects(query);
    }

    public Property save(Property property) throws DAOException {
        storeObject(property);
        return retrieveObject(Property.class, property.getId());
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
     * The function remove() removes a property
     *
     * @param id
     *            of property to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Integer id) throws DAOException {
        removeObject(Property.class, id);
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

    /**
     * Gets all titles from workpieceproperties.
     * 
     * @return a list of titles.
     */
    @SuppressWarnings("unchecked")
    public List<String> findWorkpiecePropertiesTitlesDistinct() {
        Session session = Helper.getHibernateSession();
        Query query = session
                .createSQLQuery("select distinct p.title from " + Property.class.getAnnotation(Table.class).name()
                        + " as p inner join workpiece_x_property wxp on p.id = wxp.property_id order by title");
        return query.list();
    }

    /**
     * Gets all titles from templateproperties.
     * 
     * @return a list of titles.
     */
    @SuppressWarnings("unchecked")
    public List<String> findTemplatePropertiesTitlesDistinct() {
        Session session = Helper.getHibernateSession();
        Query query = session
                .createSQLQuery("select distinct p.title from " + Property.class.getAnnotation(Table.class).name()
                        + " as p inner join template_x_property txp on p.id = txp.property_id order by title");
        return query.list();
    }

    /**
     * Gets all titles from processProperties.
     * 
     * @return a list of titles.
     */
    @SuppressWarnings("unchecked")
    public List<String> findProcessPropertiesTitlesDistinct() {
        Session session = Helper.getHibernateSession();
        Query query = session
                .createSQLQuery("select distinct p.title from " + Property.class.getAnnotation(Table.class).name()
                        + " as p inner join process_x_property pxp on p.id = pxp.property_id order by title");
        return query.list();
    }
}
