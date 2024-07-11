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

import io.reactivex.annotations.CheckReturnValue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.data.interfaces.SearchDatabaseServiceInterface;
import org.primefaces.model.SortOrder;

public abstract class SearchDatabaseService<T extends BaseBean, S extends BaseDAO<T>>
        implements SearchDatabaseServiceInterface<T> {

    protected static final EnumMap<SortOrder, String> SORT_ORDER_MAPPING;

    static {
        SORT_ORDER_MAPPING = new EnumMap<>(SortOrder.class);
        SORT_ORDER_MAPPING.put(SortOrder.ASCENDING, "ASC");
        SORT_ORDER_MAPPING.put(SortOrder.DESCENDING, "DESC");
    }

    private static final Logger logger = LogManager.getLogger(SearchDatabaseService.class);
    private static final Pattern PARAMETER_PATTERN = Pattern.compile(":(\\w+)");

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

    @Override
    public abstract List loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map<?, String> filters)
            throws DataException;

    @Override
    public void saveToDatabase(T baseIndexedBean) throws DAOException {
        dao.save(baseIndexedBean);
    }

    /**
     * Method saves objects to database.
     *
     * @param baseIndexedBeans
     *            beans object to store as indexed
     */
    public void saveAsIndexed(List<T> baseIndexedBeans) throws DAOException {
        dao.saveAsIndexed(baseIndexedBeans);
    }

    @Override
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

    @Override
    public abstract Long countDatabaseRows() throws DAOException;

    /**
     * Count rows in database according to given query.
     *
     * @param query
     *            for database search
     * @return amount of rows in database according to given query
     */
    public Long countDatabaseRows(String query) throws DAOException {
        logger.debug(query);
        return dao.count(query);
    }

    /**
     * Count rows in database according to given query.
     *
     * @param query
     *            for database search
     * @param parameters
     *            for query
     * @return amount of rows in database according to given query
     */
    public Long countDatabaseRows(String query, Map<String, Object> parameters) throws DAOException {
        debugLogQuery(query, parameters);
        return dao.count(query, parameters);
    }

    @Override
    public abstract Long countResults(Map<?, String> filters) throws DAOException, DataException;

    @Override
    public T getById(Integer id) throws DAOException {
        return dao.getById(id);
    }

    @Override
    public List<T> getByQuery(String query) {
        logger.debug(query);
        return dao.getByQuery(query);
    }

    /**
     * Retrieves BaseBean objects from database by given query.
     *
     * @param query
     *            as String
     * @param parameters
     *            for query
     * @return list of beans objects
     */
    public List<T> getByQuery(String query, Map<String, Object> parameters) {
        debugLogQuery(query, parameters);
        return dao.getByQuery(query, parameters);
    }

    public List<T> getByQuery(String query, Map<String, Object> parameters, int begin, int max) {
        debugLogQuery(query, parameters, begin, max);
        return dao.getByQuery(query, parameters, begin, max);
    }

    @Override
    public List<T> getByQuery(String query, Map<String, Object> parameters, int max) {
        debugLogQuery(query, parameters, 0, max);
        return dao.getByQuery(query, parameters, 0, max);
    }

    @Override
    public List<T> getAll() throws DAOException {
        return dao.getAll();
    }

    @Override
    public List<T> getAll(int offset, int size) throws DAOException {
        return dao.getAll(offset, size);
    }

    @Override
    public void evict(T baseBean) {
        this.dao.evict(baseBean);
    }

    @CheckReturnValue
    public T merge(T baseBean) {
        return this.dao.merge(baseBean);
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

    protected String getSort(String sortField, SortOrder sortOrder) {
        if (!Objects.equals(sortField, null) && Objects.equals(sortOrder, SortOrder.ASCENDING)) {
            return " ORDER BY " + sortField + " ASC";
        } else if (!Objects.equals(sortField, null) && Objects.equals(sortOrder, SortOrder.DESCENDING)) {
            return " ORDER BY " + sortField + " DESC";
        } else {
            return "";
        }
    }

    /**
     * Enters a search query into the log when it is running in debug level.
     * Placeholders are replaced with their parameter values.
     * 
     * @param query
     *            search query
     * @param parameters
     *            parameter values
     */
    private static void debugLogQuery(String query, Map<String, Object> parameters) {
        debugLogQuery(query, parameters, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    /**
     * Enters a search query into the log when it is running in debug level.
     * Placeholders are replaced with their parameter values.
     * 
     * @param query
     *            search query
     * @param parameters
     *            parameter values
     * @param initPointer
     *            can initialize the object pointer to a later object (sets
     *            {@linkplain Query#setFirstResult(int)})
     * @param stopCount
     *            the search stops after count hits (sets
     *            {@linkplain Query#setMaxResults(int)})
     */
    private static void debugLogQuery(String query, Map<String, Object> parameters, int initPointer, int stopCount) {
        if (logger.isDebugEnabled()) {
            String resolved = PARAMETER_PATTERN.matcher(query).replaceAll(matchResult -> {
                Object parameter = parameters.get(matchResult.group(1));
                if (Objects.isNull(parameter)) {
                    return matchResult.group();
                }
                if (parameter instanceof String) {
                    return '\'' + ((String) parameter) + '\'';
                }
                return Objects.toString(parameter);
            });
            if (initPointer != Integer.MIN_VALUE || stopCount != Integer.MIN_VALUE) {
                if (stopCount != Integer.MIN_VALUE) {
                    resolved = String.format("%s (limit=%d)", resolved, stopCount);
                } else {
                    resolved = String.format("%s (limit=%d, offset=%d)", resolved, stopCount, initPointer);
                }
            }
            logger.debug(resolved);
        }
    }
}
