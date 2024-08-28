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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.DataInterface;

/**
 * Class for implementing methods used by all service classes which search in
 * ElasticSearch index.
 */
public abstract class SearchService<T extends BaseIndexedBean, S extends DataInterface, V extends BaseDAO<T>>
        extends SearchDatabaseService<T, V> {

    private static final Logger logger = LogManager.getLogger(SearchService.class);
    protected static final String WILDCARD = "*";

    /**
     * Constructor necessary to use searcher in child classes.
     *
     * @param dao
     *            DAO object for executing operations on database
     * @param type
     *            Type object for ElasticSearch
     * @param indexer
     *            for executing insert / updates to ElasticSearch
     * @param searcher
     *            for executing queries to ElasticSearch
     */
    public SearchService(V dao, Object type, Object indexer, Object searcher) {
        super(dao);
    }

    /**
     * Method converts JSON object object to Interface. Necessary for displaying in the
     * frontend.
     *
     * @param jsonObject
     *            return from find methods
     * @param related
     *            true or false
     * @return Interface object
     */
    public abstract S convertJSONObjectTo(Map<String, Object> jsonObject, boolean related) throws DataException;

    /**
     * Count all not indexed rows in database. Not indexed means that row has index
     * action INDEX or NULL.
     *
     * @return amount of all not indexed rows
     */
    public abstract Long countNotIndexedDatabaseRows() throws DAOException;

    /**
     * Get all not indexed objects from database. Not indexed means that row has
     * index action INDEX or NULL.
     *
     * @return list of all not indexed objects
     */
    public abstract List<T> getAllNotIndexed();

    /**
     * Get all not indexed objects from database in given range. Not indexed means
     * that row has index action INDEX or NULL.
     *
     * @param offset
     *            result - important, numeration starts since 0
     * @param size
     *            amount of results
     * @return list of all not indexed objects from database in given range
     */
    @Override
    public List<T> getAllNotIndexed(int offset, int size) throws DAOException {
        return dao.getAllNotIndexed(offset, size);
    }

    /**
     * Method saves relations which can be potentially modified together with
     * object.
     *
     * @param baseIndexedBean
     *            object
     */
    protected void manageDependenciesForIndex(T baseIndexedBean)
            throws CustomResponseException, DAOException, DataException, IOException {
    }

    /**
     * calls save method with default updateRelatedObjectsInIndex=false.
     * @param object the object to save
     */
    @Override
    public void save(T object) throws DataException {
        save(object, false);
    }

    /**
     * Method saves object to database and document to the index of Elastic Search.
     * This method binds three other methods: save to database, save to index and
     * save dependencies to index.
     *
     * <p>
     * First step sets up the flag indexAction to state Index and saves to database.
     * It informs that object was updated in database but not yet in index. If this
     * step fails, method breaks. If it is successful, method saves changes to
     * index, first document and next its dependencies. If one of this steps fails,
     * method retries up to 5 times operations on index. If it continues to fail,
     * method breaks. If save to index was successful, indexAction flag is changed
     * to Done and database is again updated. There is possibility that last step
     * fails and in that case, even if index is up to date, in some point of the
     * future it will be reindexed by administrator.
     *
     * @param baseIndexedBean
     *            object
     *
     * @param updateRelatedObjectsInIndex if relatedObjects need to be updated in Index
     */
    @Override
    public void save(T baseIndexedBean, boolean updateRelatedObjectsInIndex) throws DataException {
        try {
            baseIndexedBean.setIndexAction(IndexAction.INDEX);
            saveToDatabase(baseIndexedBean);
            // TODO: find out why properties lists are save double
            T savedBean = getById(baseIndexedBean.getId());
            saveToIndex(savedBean, true);
            if (updateRelatedObjectsInIndex) {
                manageDependenciesForIndex(savedBean);
            }
            savedBean.setIndexAction(IndexAction.DONE);
            saveToDatabase(savedBean);
        } catch (DAOException e) {
            logger.debug(e);
            throw new DataException(e);
        } catch (CustomResponseException | IOException e) {
            int count = 0;
            int maxTries = 5;
            while (true) {
                try {
                    saveToIndex(baseIndexedBean, true);
                    manageDependenciesForIndex(baseIndexedBean);
                    baseIndexedBean.setIndexAction(IndexAction.DONE);
                    saveToDatabase(baseIndexedBean);
                    break;
                } catch (CustomResponseException | IOException ee) {
                    logger.debug(ee);
                    if (++count >= maxTries) {
                        throw new DataException(ee);
                    }
                } catch (DAOException daoe) {
                    logger.debug("Index was updated but flag in database not... {}", daoe.getMessage());
                    throw new DataException(daoe);
                }
            }
        }
    }

    /**
     * Method removes object from database and document from the index of Elastic
     * Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws DataException {
        try {
            T baseIndexedBean = getById(id);
            remove(baseIndexedBean);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    /**
     * Method removes object from database and document from the index of Elastic
     * Search.
     *
     * @param baseIndexedBean
     *            object
     */
    @Override
    public void remove(T baseIndexedBean) throws DataException {
        try {
            baseIndexedBean.setIndexAction(IndexAction.DELETE);
            saveToDatabase(baseIndexedBean);
            T savedBean = getById(baseIndexedBean.getId());
            removeFromIndex(savedBean, true);
            manageDependenciesForIndex(savedBean);
            removeFromDatabase(savedBean);
        } catch (DAOException e) {
            logger.debug(e);
            throw new DataException(e);
        } catch (CustomResponseException | IOException e) {
            int count = 0;
            int maxTries = 5;
            while (true) {
                try {
                    removeFromIndex(baseIndexedBean, true);
                    removeFromDatabase(baseIndexedBean);
                    break;
                } catch (CustomResponseException | IOException ee) {
                    logger.debug(ee);
                    if (++count >= maxTries) {
                        throw new DataException(ee);
                    }
                } catch (DAOException daoe) {
                    logger.debug("Remove from index was successful but...{}", daoe.getMessage());
                    throw new DataException(daoe);
                }
            }
        }
    }

    /**
     * Convert list of JSONObject object to list of Interface objects.
     *
     * @param jsonObjects
     *            list of SearchResult objects
     * @param related
     *            determines if converted object is related to some other object (if
     *            so, objects related to it are not included in conversion)
     * @return list of Interface object
     */
    protected List<S> convertJSONObjectsToInterfaces(List<Map<String, Object>> jsonObjects, boolean related)
            throws DataException {
        List<S> results = new ArrayList<>();

        for (Map<String, Object> jsonObject : jsonObjects) {
            results.add(convertJSONObjectTo(jsonObject, related));
        }

        return results;
    }

    /**
     * Get id from JSON object returned form ElasticSearch.
     *
     * @param jsonObject
     *            returned form ElasticSearch
     * @return id as Integer
     */
    public Integer getIdFromJSONObject(Map<String, Object> jsonObject) {
        if (jsonObject.containsKey("id")) {
            String id = (String) jsonObject.get("id");
            if (Objects.nonNull(id)) {
                return Integer.valueOf(id);
            }
        }
        return 0;
    }

    /**
     * Removes all objects from index, which are no longer in Database.
     * @param baseIndexedBeansId the list of beans to check for missing db eintries.
     *
     */
    @Override
    public void removeLooseIndexData(List<Integer> baseIndexedBeansId) throws DataException, CustomResponseException {
        for (Integer baseIndexedBeanId : baseIndexedBeansId) {
            try {
                getById(baseIndexedBeanId);
            } catch (DAOException e) {
                removeFromIndex(baseIndexedBeanId,true);
            }
        }
    }
}
