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

import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * DAO class for Filter bean.
 */
public class FilterDAO extends BaseDAO {

    private static final long serialVersionUID = 234210246673032251L;

    /**
     * Find filter object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public Filter find(Integer id) throws DAOException {
        Filter result = (Filter) retrieveObject(Filter.class, id);
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
    public List<Filter> findAll() {
        return retrieveAllObjects(Filter.class);
    }

    /**
     * Find properties by query.
     *
     * @param query
     *            as String
     * @return list of properties
     */
    @SuppressWarnings("unchecked")
    public List<Filter> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    public Filter save(Filter filter) throws DAOException {
        storeObject(filter);
        return (Filter) retrieveObject(Filter.class, filter.getId());
    }

    /**
     * The function remove() removes a filter
     *
     * @param filter
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Filter filter) throws DAOException {
        removeObject(filter);
    }

    /**
     * The function remove() removes a filter
     *
     * @param id
     *            of filter to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Integer id) throws DAOException {
        removeObject(id);
    }

    /**
     * Count filter objects in table.
     *
     * @param query
     *            as String
     * @return amount of objects as Long
     */
    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
