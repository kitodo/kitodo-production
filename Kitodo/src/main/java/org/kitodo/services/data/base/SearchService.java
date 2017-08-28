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
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.BaseDTO;

/**
 * Class for implementing methods used by all service classes which search
 * in ElasticSearch index.
 */
public abstract class SearchService<T extends BaseBean, S extends BaseDTO> {

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
     * Method necessary for get from database object by id. It is used in removeById
     * method.
     *
     * @param id
     *            of object
     * @return object
     */
    public abstract T getById(Integer id) throws DAOException;

    /**
     * Get list of all objects from database.
     * 
     * @return list of all objects from database
     */
    public abstract List<T> getAll();

    /**
     * Method necessary for conversion of JSON objects to exact bean objects called
     * from database.
     *
     * @param query
     *            as String
     * @return list of exact bean objects
     */
    public abstract List<T> getByQuery(String query) throws DAOException;

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
    public abstract Long countDatabaseRows(String query) throws DAOException;

    /**
     * Method converts JSON object object to DTO. Necessary for displaying in the
     * frontend.
     *
     * @param jsonObject
     *            return from find methods
     * @return DTO object
     */
    public abstract S convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException;

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
    protected void manageDependenciesForIndex(T baseBean)
            throws CustomResponseException, DAOException, DataException, IOException {

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
     * </p>
     *
     * @param baseBean
     *            object
     */
    public void save(T baseBean) throws DataException {
        try {
            baseBean.setIndexAction(IndexAction.INDEX);
            saveToDatabase(baseBean);
            saveToIndex(baseBean);
            manageDependenciesForIndex(baseBean);
            baseBean.setIndexAction(IndexAction.DONE);
            saveToDatabase(baseBean);
        } catch (DAOException e) {
            logger.debug(e);
            throw new DataException(e);
        } catch (CustomResponseException | IOException e) {
            int count = 0;
            int maxTries = 5;
            while (true) {
                try {
                    saveToIndex(baseBean);
                    manageDependenciesForIndex(baseBean);
                    baseBean.setIndexAction(IndexAction.DONE);
                    saveToDatabase(baseBean);
                    break;
                } catch (CustomResponseException | IOException ee) {
                    logger.debug(ee);
                    if (++count >= maxTries) {
                        throw new DataException(ee);
                    }
                } catch (DAOException daoe) {
                    logger.debug("Index was updated but flag in database not... " + daoe.getMessage());
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
            T baseBean = getById(id);
            remove(baseBean);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    /**
     * Method removes object from database and document from the index of Elastic
     * Search.
     *
     * @param baseBean
     *            object
     */
    public void remove(T baseBean) throws DataException {
        try {
            baseBean.setIndexAction(IndexAction.DELETE);
            saveToDatabase(baseBean);
            removeFromIndex(baseBean);
            manageDependenciesForIndex(baseBean);
            removeFromDatabase(baseBean);
        } catch (DAOException e) {
            logger.debug(e);
            throw new DataException(e);
        } catch (CustomResponseException | IOException e) {
            int count = 0;
            int maxTries = 5;
            while (true) {
                try {
                    removeFromIndex(baseBean);
                    removeFromDatabase(baseBean);
                    break;
                } catch (CustomResponseException | IOException ee) {
                    logger.debug(ee);
                    if (++count >= maxTries) {
                        throw new DataException(ee);
                    }
                } catch (DAOException daoe) {
                    logger.debug("Remove from index was successful but..." + daoe.getMessage());
                    throw new DataException(daoe);
                }
            }
        }
    }

    /**
     * Count all objects in index.
     *
     * @return amount of all objects
     */
    public Long count() throws DataException {
        return searcher.countDocuments();
    }

    /**
     * Count objects according to given query.
     *
     * @param query
     *            for index search
     * @return amount of objects according to given query
     */
    public Long count(String query) throws DataException {
        return searcher.countDocuments(query);
    }

    /**
     * Display all documents for exact type.
     *
     * @return list of all documents
     */
    public List<JSONObject> findAllDocuments() throws DataException {
        QueryBuilder queryBuilder = matchAllQuery();
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Display all documents for exact type with exact sorting.
     * 
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return sorted list of all documents
     */
    public List<JSONObject> findAllDocuments(String sort) throws DataException {
        QueryBuilder queryBuilder = matchAllQuery();
        return searcher.findDocuments(queryBuilder.toString(), sort);
    }

    /**
     * Find object in ES and convert it to DTO.
     * 
     * @param id
     *            object id
     * @return DTO object
     */
    public S findById(Integer id) throws DataException {
        return findById(id, false);
    }

    /**
     * Find object related to previously found object in ES and convert it to DTO.
     * 
     * @param id
     *            related object id
     * @param related
     *            this method should ba called only with true, if false call method
     *            getById(Integer id).
     * @return related DTO object
     */
    public S findById(Integer id, boolean related) throws DataException {
        return convertJSONObjectToDTO(searcher.findDocument(id), related);
    }

    /**
     * Convert list of JSONObject object to list of DTO objects.
     *
     * @param jsonObjects
     *            list of SearchResult objects
     * @param related
     *            determines if converted object is related to some other object (if
     *            so, objects related to it are not included in conversion)
     * @return list of DTO object
     */
    public List<S> convertJSONObjectsToDTOs(List<JSONObject> jsonObjects, boolean related) throws DataException {
        List<S> results = new ArrayList<>();

        for (JSONObject jsonObject : jsonObjects) {
            results.add(convertJSONObjectToDTO(jsonObject, related));
        }

        return results;
    }

    /**
     * Convert JSONObject object to bean object.
     *
     * @param jsonObject
     *            result from ElasticSearch
     * @return bean object
     */
    public T convertJSONObjectToBean(JSONObject jsonObject) throws DAOException {
        Integer id = getIdFromJSONObject(jsonObject);
        if (id == 0) {
            // TODO: maybe here could be used some instancing of generic
            // class...
            return null;
        } else {
            return getById(id);
        }
    }

    /**
     * Convert related JSONObject object to bean object.
     *
     * @param jsonObject
     *            result from ElasticSearch
     * @param key
     *            name of related property
     * @return bean object
     */
    protected <O extends BaseDTO> List<O> convertRelatedJSONObjectToDTO(JSONObject jsonObject, String key,
            SearchService<?, O> service) throws DataException {
        List<O> listDTO = new ArrayList<>();
        for (Integer id : getRelatedPropertyForDTO(jsonObject, key)) {
            listDTO.add(service.findById(id, true));
        }
        return listDTO;
    }

    /**
     * Get id from JSON object returned form ElasticSearch.
     * 
     * @param jsonObject
     *            returned form ElasticSearch
     * @return id as Integer
     */
    public Integer getIdFromJSONObject(JSONObject jsonObject) {
        Object id = jsonObject.get("_id");
        if (id != null) {
            return Integer.valueOf(id.toString());
        }
        return 0;
    }

    /**
     * Create query for set of data.
     * 
     * @param key
     *            JSON key for searched object
     * @param ids
     *            set of id values for searched objects or some objects related to
     *            searched object
     * @param contains
     *            determine if results should contain given value or should not
     *            contain given value
     * @return query
     */
    protected QueryBuilder createSetQuery(String key, Set<Integer> ids, boolean contains) {
        if (contains && ids.size() > 0) {
            return termsQuery(key, ids);
        } else if (!contains && ids != null) {
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            return boolQuery.mustNot(termsQuery(key, ids));
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
     * @param id
     *            id value for searched object or some object related to searched
     *            object
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
     * @param condition
     *            id value for searched object or some object related to searched
     *            object
     * @param contains
     *            determine if results should contain given value or should not
     *            contain given value
     * @return query
     */
    protected QueryBuilder createSimpleQuery(String key, Boolean condition, boolean contains) {
        if (contains && condition != null) {
            return matchQuery(key, condition);
        } else if (!contains && condition != null) {
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            return boolQuery.mustNot(matchQuery(key, condition));
        } else {
            return matchQuery(key, false);
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
     *            as Operator AND or OR - useful when value contains more than one
     *            word
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

    protected QueryBuilder createSimpleWildcardQuery(String key, String value) {
        return queryStringQuery(key + ": *" + value + "*");
    }

    protected Long findCountAggregation(String query, String field) throws DataException {
        JSONObject jsonObject = searcher.aggregateDocuments(query, createCountAggregation(field));
        JSONObject count = (JSONObject) jsonObject.get(field);
        return (Long) count.get("value");
    }

    protected Double findSumAggregation(String query, String field) throws DataException {
        JSONObject jsonObject = searcher.aggregateDocuments(query, createSumAggregation(field));
        JSONObject sum = (JSONObject) jsonObject.get(field);
        return (Double) sum.get("value");
    }

    protected List<String> findDistinctValues(String field, String sort) throws DataException {
        JSONObject jsonObject = searcher.aggregateDocuments(null, createTermAggregation(field));
        return new ArrayList<>();
    }

    private String createCountAggregation(String field) {
        return XContentHelper.toString(AggregationBuilders.count(field).field(field));
    }

    private String createSumAggregation(String field) {
        return XContentHelper.toString(AggregationBuilders.sum(field).field(field));
    }

    private String createTermAggregation(String field) {
        return XContentHelper.toString(AggregationBuilders.terms(field).field(field));
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

    /**
     * Converts properties' values returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject as Object
     * @return display properties as String
     */
    protected String getStringPropertyForDTO(Object object, String key) {
        JSONObject jsonObject = (JSONObject) object;
        jsonObject = (JSONObject) jsonObject.get("_source");
        if (jsonObject != null) {
            return (String) jsonObject.get(key);
        }
        return "";
    }

    /**
     * Converts properties' values returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject as Object
     * @return display properties as Integer
     */
    protected Integer getIntegerPropertyForDTO(Object object, String key) {
        JSONObject jsonObject = (JSONObject) object;
        jsonObject = (JSONObject) jsonObject.get("_source");
        if (jsonObject != null) {
            Long returned = (Long) jsonObject.get(key);
            if (returned != null) {
                return returned.intValue();
            }
        }
        return 0;
    }

    /**
     * Converts properties' values returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject as Object
     * @return display properties as list of Integers
     */
    protected List<Integer> getRelatedPropertyForDTO(Object object, String key) {
        JSONObject jsonObject = (JSONObject) object;
        jsonObject = (JSONObject) jsonObject.get("_source");
        if (jsonObject != null) {
            JSONArray jsonArray = (JSONArray) jsonObject.get(key);
            List<Integer> ids = new ArrayList<>();
            for (Object singleObject : jsonArray) {
                ids.add(convertIdForDTO(singleObject));
            }
            return ids;
        }
        return new ArrayList<>();
    }

    /**
     * Get size of related objects returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject as Object
     * @param key
     *            of property which need to be counted
     * @return size of array with related objects
     */
    protected int getSizeOfRelatedPropertyForDTO(Object object, String key) {
        JSONObject jsonObject = (JSONObject) object;
        jsonObject = (JSONObject) jsonObject.get("_source");
        if (jsonObject != null) {
            JSONArray jsonArray = (JSONArray) jsonObject.get(key);
            return jsonArray.size();
        }
        return 0;
    }

    /**
     * Converts id value returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject as Object
     * @return id as Integer
     */
    private Integer convertIdForDTO(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        Long longId = (Long) jsonObject.get("id");
        return longId.intValue();
    }

    /**
     * Return server information provided by the indexer and gathered by the rest
     * client.
     *
     * @return String information about the server
     */
    public String getServerInformation() {
        return indexer.getServerInformation();
    }
}
