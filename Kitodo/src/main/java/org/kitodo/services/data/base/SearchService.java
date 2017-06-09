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

package org.kitodo.services.data.base;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.parser.ParseException;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;

/**
 * Class for implementing methods used by all service classes which search in
 * ElasticSearch index.
 */
public abstract class SearchService<T extends BaseBean> {

    private static final Logger logger = LogManager.getLogger(SearchService.class);
    protected Searcher searcher;
    protected Indexer indexer;

    /**
     * Constructor necessary to use searcher in child classes.
     *
     * @param searcher
     *            for executing queries
     */
    public SearchService(Searcher searcher) {
        this.searcher = searcher;
    }

    /**
     * Method saves object to database.
     *
     * @param baseBean
     *            object
     */
    public abstract void saveToDatabase(T baseBean) throws DAOException;

    /**
     * Method saves document to the index of Elastic Search.
     *
     * @param baseBean
     *            object
     */
    public abstract void saveToIndex(T baseBean) throws CustomResponseException, IOException;

    /**
     * Method necessary for get from database object by id. It is used in
     * removeById method.
     *
     * @param id
     *            of object
     * @return object
     */
    public abstract T find(Integer id) throws DAOException;

    /**
     * Method necessary for conversion of SearchResult objects to exact bean
     * objects called from database.
     *
     * @param query
     *            as String
     * @return list of exact bean objects
     */
    public abstract List<T> search(String query) throws DAOException;

    /**
     * Method removes object from database.
     *
     * @param baseBean
     *            object
     */
    public abstract void removeFromDatabase(T baseBean) throws DAOException;

    /**
     * Method removes object from database.
     *
     * @param id
     *            of object
     */
    public abstract void removeFromDatabase(Integer id) throws DAOException;

    /**
     * Method removes document from the index of Elastic Search.
     *
     * @param baseBean
     *            object
     */
    public abstract void removeFromIndex(T baseBean) throws CustomResponseException, IOException;

    /**
     * Method removes document from the index of Elastic Search.
     *
     * @param id
     *            of object
     */
    public void removeFromIndex(Integer id) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(id);
    }

    /**
     * Method saves relations which can be potentially modified together with
     * object.
     *
     * @param baseBean
     *            object
     */
    protected void saveDependenciesToIndex(T baseBean) throws CustomResponseException, IOException {

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
     * </p>
     *
     * @param baseBean
     *            object
     */
    public void save(T baseBean) throws CustomResponseException, DAOException, IOException {
        try {
            baseBean.setIndexAction(IndexAction.INDEX);
            saveToDatabase(baseBean);
            saveToIndex(baseBean);
            saveDependenciesToIndex(baseBean);
            baseBean.setIndexAction(IndexAction.DONE);
            saveToDatabase(baseBean);
        } catch (DAOException e) {
            logger.debug(e);
            throw new DAOException(e);
        } catch (CustomResponseException | IOException e) {
            int count = 0;
            int maxTries = 5;
            while (true) {
                try {
                    saveToIndex(baseBean);
                    saveDependenciesToIndex(baseBean);
                    baseBean.setIndexAction(IndexAction.DONE);
                    saveToDatabase(baseBean);
                    break;
                } catch (CustomResponseException cre) {
                    logger.debug(e);
                    if (++count >= maxTries) {
                        throw new CustomResponseException(cre.getMessage());
                    }
                } catch (IOException ioe) {
                    logger.debug(e);
                    if (++count >= maxTries) {
                        throw new IOException(ioe.getMessage());
                    }
                } catch (DAOException daoe) {
                    logger.debug("Index was updated but flag in database not... " + e);
                    throw new DAOException(daoe.getMessage());
                }
            }
        }
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws CustomResponseException, DAOException, IOException {
        T baseBean = find(id);
        remove(baseBean);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param baseBean
     *            object
     */
    public void remove(T baseBean) throws CustomResponseException, DAOException, IOException {
        try {
            baseBean.setIndexAction(IndexAction.DELETE);
            saveToDatabase(baseBean);
            removeFromIndex(baseBean);
            removeFromDatabase(baseBean);
        } catch (DAOException e) {
            logger.debug(e);
            throw new DAOException(e);
        } catch (CustomResponseException | IOException e) {
            int count = 0;
            int maxTries = 5;
            while (true) {
                try {
                    removeFromIndex(baseBean);
                    removeFromDatabase(baseBean);
                    break;
                } catch (CustomResponseException cre) {
                    logger.debug(e);
                    if (++count >= maxTries) {
                        throw new CustomResponseException(cre.getMessage());
                    }
                } catch (IOException ioe) {
                    logger.debug(e);
                    if (++count >= maxTries) {
                        throw new IOException(ioe.getMessage());
                    }
                } catch (DAOException daoe) {
                    logger.debug("Remove from index was successful but..." + e);
                    throw new DAOException(daoe.getMessage());
                }
            }
        }
    }

    /**
     * Display all documents for exact type.
     *
     * @return list of all documents
     */
    public List<SearchResult> findAllDocuments() throws CustomResponseException, IOException, ParseException {
        QueryBuilder queryBuilder = matchAllQuery();
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Find user with exact id.
     *
     * @param id
     *            of the searched user
     * @return search result
     */
    public SearchResult findById(Integer id) throws CustomResponseException, IOException, ParseException {
        return searcher.findDocument(id);
    }

    /**
     * Convert list of SearchResult object to list of bean objects.
     *
     * @param searchResults
     *            list of results from ElasticSearch
     * @param table
     *            name
     * @return list of beans
     */
    public List<? extends BaseBean> convertSearchResultsToObjectList(List<SearchResult> searchResults, String table)
            throws DAOException {
        StringBuilder query = new StringBuilder();
        query.append("FROM ");
        query.append(table);
        query.append(" WHERE id IN (");
        for (SearchResult searchResult : searchResults) {
            query.append(searchResult.getId());
            query.append(",");
        }
        query.deleteCharAt(query.length() - 1);
        query.append(")");
        return search(query.toString());
    }

    /**
     * Convert SearchResult object to bean object.
     *
     * @param searchResult
     *            result from ElasticSearch
     * @return bean object
     */
    public T convertSearchResultToObject(SearchResult searchResult) throws DAOException {
        if (searchResult.getId() == null) {
            // TODO: maybe here could be used some instancing of generic
            // class...
            return null;
        } else {
            return find(searchResult.getId());
        }
    }

    /**
     * Used for cases where operator is not necessary to create query - checking
     * only for one parameter.
     *
     * @param key
     *            JSON key for searched object
     * @param id
     *            id value for searched object or some object related to
     *            searched object
     * @param contains
     *            determine if results should contain given value or should not
     *            contain given value
     * @return query
     */
    protected QueryBuilder createSimpleQuery(String key, Integer id, boolean contains) {
        if (contains && id != null) {
            return matchQuery(key, id);
        } else if (!contains && id != null) {
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            return boolQuery.mustNot(matchQuery(key, id));
        } else {
            return matchQuery(key, 0);
        }
    }

    /**
     * Used for cases where operator is not necessary to create query - checking
     * only for one parameter.
     *
     * @param key
     *            JSON key for searched object
     * @param value
     *            JSON value for searched object
     * @param contains
     *            determine if results should contain given value or should not
     *            contain given value
     * @return query
     */
    protected QueryBuilder createSimpleQuery(String key, String value, boolean contains) {
        if (contains) {
            return matchQuery(key, value);
        } else {
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            return boolQuery.mustNot(matchQuery(key, value));
        }
    }

    /**
     * Used for cases where operator is necessary to create query - checking for
     * more than one parameter.
     *
     * @param key
     *            JSON key for searched object
     * @param value
     *            JSON value for searched object
     * @param contains
     *            determine if results should contain given value or should not
     *            contain given value
     * @param operator
     *            as Operator AND or OR - useful when value contains more than
     *            one word
     * @return query
     */
    protected QueryBuilder createSimpleQuery(String key, String value, boolean contains, Operator operator) {
        if (operator == null) {
            operator = Operator.OR;
        }

        if (contains) {
            return matchQuery(key, value).operator(operator);
        } else {
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            return boolQuery.mustNot(matchQuery(key, value).operator(operator));
        }
    }

    /**
     * Method for comparing dates.
     *
     * @param key
     *            as String
     * @param date
     *            as Date
     * @param searchCondition
     *            as SearchCondition - bigger, smaller and so on
     * @return query for searching for date in exact range
     */
    protected QueryBuilder createSimpleCompareDateQuery(String key, Date date, SearchCondition searchCondition) {
        QueryBuilder query = null;
        switch (searchCondition) {
            case EQUAL:
                query = matchQuery(key, formatDate(date));
                break;
            case EQUAL_OR_BIGGER:
                query = rangeQuery(key).gte(formatDate(date));
                break;
            case EQUAL_OR_SMALLER:
                query = rangeQuery(key).lte(formatDate(date));
                break;
            case BIGGER:
                query = rangeQuery(key).gt(formatDate(date));
                break;
            case SMALLER:
                query = rangeQuery(key).lt(formatDate(date));
                break;
            default:
                assert false : searchCondition;
        }
        return query;
    }

    /**
     * Method for comparing Integer values.
     *
     * @param key
     *            as String
     * @param value
     *            as Integer
     * @param searchCondition
     *            as SearchCondition - bigger, smaller and so on
     * @return query for searching for numbers in exact range
     */
    protected QueryBuilder createSimpleCompareQuery(String key, Integer value, SearchCondition searchCondition) {
        QueryBuilder query = null;
        switch (searchCondition) {
            case EQUAL:
                query = matchQuery(key, value);
                break;
            case EQUAL_OR_BIGGER:
                query = rangeQuery(key).gte(value);
                break;
            case EQUAL_OR_SMALLER:
                query = rangeQuery(key).lte(value);
                break;
            case BIGGER:
                query = rangeQuery(key).gt(value);
                break;
            case SMALLER:
                query = rangeQuery(key).lt(value);
                break;
            default:
                assert false : searchCondition;
        }
        return query;
    }

    /**
     * Format date according to format used during the indexing of documents.
     *
     * @param date
     *            as Date
     * @return formatted date
     */
    protected String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
}
