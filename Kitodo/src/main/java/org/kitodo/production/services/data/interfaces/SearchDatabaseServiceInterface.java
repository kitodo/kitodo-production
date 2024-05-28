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

package org.kitodo.production.services.data.interfaces;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.primefaces.model.SortOrder;

/**
 * Specifies the functions of the Search Database Service.
 * 
 * @param <T>
 *            type of database objects
 */
public interface SearchDatabaseServiceInterface<T extends BaseBean> {
    /**
     * Returns the number of objects of the implementing type in the database.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function counts all data records in the database for all clients.
     *
     * @return the number of objects
     */
    Long countDatabaseRows() throws DAOException;

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
    Long countResults(Map<?, String> filters) throws DAOException, DataException;

    /**
     * Unlinks the object from the primary Hibernate cache, so it can be garbage
     * collected.
     *
     * @param baseBean
     *            bean to evict
     */
    void evict(T baseBean);

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
    List<T> getAll() throws DAOException;

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
    public List<T> getAll(int offset, int size) throws DAOException;

    /**
     * Gets an object by its database record number.
     *
     * @param id
     *            record number
     * @return object
     */
    T getById(Integer id) throws DAOException;

    /**
     * Gets a set of objects based on a search query.
     *
     * @param query
     *            query in Hibernate Query Language
     * @return list of exact bean objects
     */
    public List<T> getByQuery(String query);

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
    public List<T> getByQuery(String query, Map<String, Object> parameters, int max);

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
    List<T> loadData(int offset, int limit, String sortField, SortOrder sortOrder, Map<?, String> filters)
            throws DataException;

    /**
     * Removes an object from the database.
     *
     * @param baseIndexedBean
     *            object to remove
     */
    public void removeFromDatabase(T baseIndexedBean) throws DAOException;

    /**
     * Stores an object in the database.
     *
     * @param baseIndexedBean
     *            object to save
     */
    void saveToDatabase(T baseIndexedBean) throws DAOException;

    // === alternative functions that are no longer required ===

    /**
     * Count all objects in index.
     *
     * @return amount of all objects
     * @deprecated Use {@link #countDatabaseRows()}.
     */
    @Deprecated
    default Long count() throws DataException {
        try {
            return countDatabaseRows();
        } catch (DAOException e) {
            throw new DataException(e);
        }
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
    default List<T> getAllNotIndexed(int offset, int size) throws DAOException {
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
    default void remove(T baseIndexedBean) throws DataException {
        try {
            removeFromDatabase(baseIndexedBean);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    /**
     * calls save method with default updateRelatedObjectsInIndex=false.
     * 
     * @param object
     *            the object to save
     * @deprecated Use {@link #saveToDatabase(BaseBean)}.
     */
    @Deprecated
    public default void save(T object) throws DataException {
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
    default void save(T baseIndexedBean, boolean updateRelatedObjectsInIndex) throws DataException {
        save(baseIndexedBean);
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
    default void addAllObjectsToIndex(List<T> baseIndexedBeans)
            throws CustomResponseException, DAOException, IOException {
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
    default List<Integer> findAllIDs(Long startIndex, int limit) throws DataException {
        return Collections.emptyList();
    }

    /**
     * Method removes document from the index of Elastic Search.
     *
     * @param baseIndexedBean
     *            object
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     * @deprecated Does nothing anymore and can be deleted.
     */
    @Deprecated
    default void removeFromIndex(T baseIndexedBean, boolean forceRefresh)
            throws CustomResponseException, DataException, IOException {
    }

    /**
     * Method removes document from the index of Elastic Search by given id.
     *
     * @param id
     *            of object
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     * @deprecated Does nothing anymore and can be deleted.
     */
    @Deprecated
    default void removeFromIndex(Integer id, boolean forceRefresh) throws CustomResponseException, DataException {
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
    default void removeLooseIndexData(List<Integer> baseIndexedBeansId) throws DataException, CustomResponseException {
    }

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
    default void saveToIndex(T baseIndexedBean, boolean forceRefresh)
            throws CustomResponseException, DataException, IOException {
    }
}
