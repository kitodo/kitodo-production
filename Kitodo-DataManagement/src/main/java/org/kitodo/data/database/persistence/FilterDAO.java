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
public class FilterDAO extends BaseDAO<Filter> {

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
        Filter result = retrieveObject(Filter.class, id);
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
    public List<Filter> findAll() {
        return retrieveAllObjects(Filter.class);
    }

    /**
     * Retrieves all filters in given range.
     *
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    public List<Filter> getAll(int first, int max) throws DAOException {
        return retrieveObjects("FROM Filter ORDER BY id ASC", first, max);
    }

    /**
     * Find properties by query.
     *
     * @param query
     *            as String
     * @return list of properties
     */
    public List<Filter> search(String query) {
        return retrieveObjects(query);
    }

    /**
     * Save filter object to database.
     * 
     * @param filter
     *            object to be saved
     * @return saved object
     */
    public Filter save(Filter filter) throws DAOException {
        storeObject(filter);
        return retrieveObject(Filter.class, filter.getId());
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
        removeObject(Filter.class, id);
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
