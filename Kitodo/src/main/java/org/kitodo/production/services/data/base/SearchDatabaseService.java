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

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Collections;
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
import org.primefaces.model.SortOrder;

public abstract class SearchDatabaseService<T extends BaseBean, S extends BaseDAO<T>> {

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

    /**
     * Provides a window onto the data objects of the implementing type. This
     * makes it possible to navigate through the data objects page by page,
     * without having to load <i>all</i> objects into memory.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function filters the data according to the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * For {@code SearchDatabaseServiceInterface<Template>}, this must take into
     * account the value from
     * {@link DatabaseTemplateServiceInterface#setShowInactiveTemplates(boolean)}.
     * For {@link DatabaseProcessServiceInterface} and
     * {@link DatabaseTaskServiceInterface}, this function is unused. They
     * declare their own {@code loadData()} with additional parameters.
     * 
     * @param offset
     *            number of objects to be skipped at the list head
     * @param limit
     *            maximum number of objects to return
     * @param sortField
     *            by which column the data should be sorted. Must not be
     *            {@code null} or empty.<br>
     *            One of:<br>
     *            <ul>
     *            <li>"title.keyword": Title [docket, ruleset, project, process
     *            template, and workflow]</li>
     *            <li>"file.keyword": File [docket, and ruleset]</li>
     *            <li>"metsRightsOwner.keyword": METS rights owner
     *            [project]</li>
     *            <li>"orderMetadataByRuleset": Order metadata as in ruleset
     *            (otherwise alphabetically) [only ruleset]</li>
     *            <li>"ruleset.title.keyword": Ruleset [only process
     *            template]</li>
     *            <li>"active": Active [project, process template]</li>
     *            <li>"status": Status [only workflow]</li>
     *            </ul>
     * @param sortOrder
     *            sort ascending or descending?
     * @param filters
     *            a map with exactly one entry, only the value is important, in
     *            which the content of the filter field is passed
     *
     * @return the data objects to be displayed
     * @throws DataException
     *             if processes cannot be loaded from search index
     */
    public abstract List loadData(int offset, int limit, String sortField, SortOrder sortOrder, Map<?, String> filters)
            throws DataException;

    /**
     * Stores an object in the database.
     *
     * @param baseIndexedBean
     *            object to save
     */
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

    /**
     * Removes an object from the database.
     *
     * @param baseIndexedBean
     *            object to remove
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
     * Returns the number of objects of the implementing type in the database.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function counts all data records in the database for all clients.
     *
     * @return the number of objects
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

    /**
     * Returns the number of objects of the implementing type that the filter
     * matches.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function counts the data records for the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * For {@code SearchDatabaseServiceInterface<Template>}, this must take into
     * account the value from
     * {@link DatabaseTemplateServiceInterface#setShowInactiveTemplates(boolean)}.
     * For {@link DatabaseProcessServiceInterface} and
     * {@link DatabaseTaskServiceInterface}, this function is unused. They
     * declare their own {@code countResults()} with additional parameters.
     * 
     * @param filters
     *            a map with exactly one entry, only the value is important, in
     *            which the content of the filter field is passed
     * @return the number of matching objects
     * @throws DAOException
     *             that can be caused by Hibernate
     * @throws DataException
     *             that can be caused by ElasticSearch
     */
    public abstract Long countResults(Map<?, String> filters) throws DAOException, DataException;

    /**
     * Gets an object by its database record number.
     *
     * @param id
     *            record number
     * @return object
     */
    public T getById(Integer id) throws DAOException {
        return dao.getById(id);
    }

    /**
     * Gets a set of objects based on a search query.
     *
     * @param query
     *            query in Hibernate Query Language
     * @return list of exact bean objects
     */
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

    /**
     * Gets a set of objects based on a search query.
     *
     * @param query
     *            query in Hibernate Query Language
     * @param parameters
     *            used in query. If the query string contains a placeholder
     *            "{@code :joker}", this mapping must contain a mapping for the
     *            string "{@code joker}" to a value for it. A replacement for
     *            injection into the SQL query, to prevent attacks.
     * @param max
     *            maximum count of beans to return
     * @return list of exact bean objects
     */
    public List<T> getByQuery(String query, Map<String, Object> parameters, int max) {
        debugLogQuery(query, parameters, 0, max);
        return dao.getByQuery(query, parameters, 0, max);
    }

    /**
     * Returns all objects of the implementing type from the database.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This method actually returns all objects of all clients and is therefore
     * more suitable for operational purposes, rather not for display purposes.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * Use this with caution, only if the number of objects is manageable.
     *
     * @return all objects of the implementing type
     */
    public List<T> getAll() throws DAOException {
        return dao.getAll();
    }

    /**
     * Returns a range of all objects of the implementing type from the
     * database. In this way the objects can be fetched in groups.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This method actually returns all objects of all clients and is therefore
     * more suitable for operational purposes, rather not for display purposes.
     *
     * @param offset
     *            number of objects to be skipped at the head
     * @param size
     *            maximum number of objects to return
     * @return objects in the given range
     */
    public List<T> getAll(int offset, int size) throws DAOException {
        return dao.getAll(offset, size);
    }

    /**
     * Unlinks the object from the primary Hibernate cache, so it can be garbage
     * collected.
     *
     * @param baseBean
     *            bean to evict
     */
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
                if (parameter instanceof AbstractCollection) {
                    return Objects.toString(parameter).replaceFirst("^\\[(.*)\\]$", "$1");
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

    // === functions no longer used ===
    
    /**
     * Method adds all object found in database to Elastic Search index.
     *
     * @param baseIndexedBeans
     *            List of BaseIndexedBean objects
     * @deprecated Does nothing anymore and can be deleted.
     */
    @Deprecated
    public void addAllObjectsToIndex(List<T> baseIndexedBeans) throws DAOException,
            IOException {
    }

    /**
     * Get all ids from index in a given range.
     *
     * @return List of ids in given range
     * @deprecated The function was only used in the context of the no longer
     *             existing function {@link #removeLooseIndexData(List)} and can
     *             therefore now also be omitted.
     */
    @Deprecated
    public List<Integer> findAllIDs(Long startIndex, int limit) throws DataException {
        return Collections.emptyList();
    }

    /**
     * Get all not indexed objects from database in given range. Not indexed
     * means that row has index action INDEX or NULL.
     *
     * @param offset
     *            result - important, numeration starts since 0
     * @param size
     *            amount of results
     * @return list of all not indexed objects from database in given range
     * @deprecated Use {@link #getAll(int, int)}.
     */
    @Deprecated
    public List<T> getAllNotIndexed(int offset, int size) throws DAOException {
        return getAll(offset, size);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param baseIndexedBean
     *            object
     * @deprecated Use {@link #removeFromDatabase(BaseBean)}.
     */
    @Deprecated
    public void remove(T baseIndexedBean) throws DataException {
        try {
            removeFromDatabase(baseIndexedBean);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    /**
     * Removes all objects from index, which are no longer in Database.
     * 
     * @param baseIndexedBeansId
     *            the list of beans to check for missing db eintries.
     *
     * @deprecated Does nothing anymore and can be deleted.
     */
    @Deprecated
    public void removeLooseIndexData(List<Integer> baseIndexedBeansId) throws DataException {
    }

    /**
     * calls save method with default updateRelatedObjectsInIndex=false.
     * 
     * @param object
     *            the object to save
     * @deprecated Use {@link #saveToDatabase(BaseBean)}.
     */
    @Deprecated
    public void save(T object) throws DataException {
        try {
            saveToDatabase(object);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    /**
     * Method saves object to database and document to the index of Elastic
     * Search. This method binds three other methods: save to database, save to
     * index and save dependencies to index.
     *
     * <p>
     * First step sets up the flag indexAction to state Index and saves to
     * database. It informs that object was updated in database but not yet in
     * index. If this step fails, method breaks. If it is successful, method
     * saves changes to index, first document and next its dependencies. If one
     * of this steps fails, method retries up to 5 times operations on index. If
     * it continues to fail, method breaks. If save to index was successful,
     * indexAction flag is changed to Done and database is again updated. There
     * is possibility that last step fails and in that case, even if index is up
     * to date, in some point of the future it will be reindexed by
     * administrator.
     *
     * @param baseIndexedBean
     *            object
     *
     * @param updateRelatedObjectsInIndex
     *            if relatedObjects need to be updated in Index
     * @deprecated Use {@link #saveToDatabase(BaseBean)}.
     */
    @Deprecated
    public void save(T baseIndexedBean, boolean updateRelatedObjectsInIndex) throws DataException {
        save(baseIndexedBean);
    }

    // === alternative functions that are no longer required ===

    /**
     * Count all objects in index.
     *
     * @return amount of all objects
     * @deprecated Use {@link #countDatabaseRows()}.
     */
    @Deprecated
    public Long count() throws DataException {
        try {
            return countDatabaseRows();
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    // === functions no longer used ===

    /**
     * Method saves document to the index of Elastic Search.
     *
     * @param baseIndexedBean
     *            object
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     * @deprecated Does nothing anymore and can be deleted.
     */
    @Deprecated
    public void saveToIndex(T baseIndexedBean, boolean forceRefresh) throws DataException, IOException {
    }
}
