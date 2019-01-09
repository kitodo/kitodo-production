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

package org.kitodo.production.services.data.base;

import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.exceptions.DataException;
import org.primefaces.model.SortOrder;

public abstract class SearchDatabaseService<T extends BaseBean, S extends BaseDAO<T>> {

    protected S dao;

    /**
     * Constructor necessary to use searcher in child classes.
     *
     * @param dao
     *            for executing queries
     */
    public SearchDatabaseService(S dao) {
        this.dao = dao;
    }

    /**
     * Get list of all objects for selected client from database.
     *
     * @return list of all objects for selected client from database
     */
    public abstract List<T> getAllForSelectedClient();

    /**
     *
     * @param first
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @param filters
     *
     * @return
     */
    public abstract List loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DAOException, DataException;

    /**
     * Method saves object to database.
     *
     * @param baseIndexedBean
     *            object
     */
    public T saveToDatabase(T baseIndexedBean) throws DAOException {
        return dao.save(baseIndexedBean);
    }

    /**
     * Method removes object from database.
     *
     * @param baseIndexedBean
     *            object
     */
    public void removeFromDatabase(T baseIndexedBean) throws DAOException {
        dao.remove(baseIndexedBean);
    }

    /**
     * Method removes object from database by given id.
     *
     * @param id
     *            of object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        dao.remove(id);
    }

    /**
     * Count all rows in database.
     *
     * @return amount of all rows
     */
    public abstract Long countDatabaseRows() throws DAOException;

    /**
     * Count rows in database according to given query.
     *
     * @param query
     *            for database search
     * @return amount of rows in database according to given query
     */
    public Long countDatabaseRows(String query) throws DAOException {
        return dao.count(query);
    }

    /**
     * This function can be overriden to implement specific filters e.g. in
     * ProcessService. Since there are no general filters at the moment this
     * function just returns null, but a query for general filters can be
     * implemented here in the future.
     *
     * @param filters
     *            Map of parameters used for filtering
     * @return null
     * @throws DAOException
     *             that can be caused by Hibernate
     * @throws DataException
     *             that can be caused by ElasticSearch
     */
    public String createCountQuery(Map filters) throws DAOException, DataException {
        return null;
    }

    /**
     * Method necessary for get from database object by id. It is used in removeById
     * method.
     *
     * @param id
     *            of object
     * @return object
     */
    public T getById(Integer id) throws DAOException {
        return dao.getById(id);
    }

    /**
     * Method necessary for conversion of JSON objects to exact bean objects called
     * from database.
     *
     * @param query
     *            as String
     * @return list of exact bean objects
     */
    public List<T> getByQuery(String query) {
        return dao.getByQuery(query);
    }

    /**
     * Get list of all objects from database.
     *
     * @return list of all objects from database
     */
    public List<T> getAll() throws DAOException {
        return dao.getAll();
    }

    /**
     * Get list of all objects from database in given range.
     *
     * @param offset
     *            result - important, numeration starts since 0
     * @param size
     *            amount of results
     * @return list of all objects from database in given range
     */
    public List<T> getAll(int offset, int size) throws DAOException {
        return dao.getAll(offset, size);
    }

    /**
     * Evict given bean object.
     *
     * @param baseBean
     *            bean to evict
     */
    public void evict(T baseBean) {
        this.dao.evict(baseBean);
    }

    /**
     * Refresh given bean object.
     *
     * @param baseBean
     *            bean object
     */
    public void refresh(T baseBean) {
        this.dao.refresh(baseBean);
    }
}
