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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.BaseType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.BaseDTO;
import org.kitodo.helper.RelatedProperty;

/**
 * Class for implementing methods used by all service classes which search in
 * ElasticSearch index.
 */
public abstract class SearchService<T extends BaseIndexedBean, S extends BaseDTO, V extends BaseDAO<T>>
        extends SearchDatabaseService<T, V> {

    private static final Logger logger = LogManager.getLogger(SearchService.class);
    protected Searcher searcher;
    protected Indexer indexer;
    protected BaseType type;

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
    public SearchService(V dao, BaseType type, Indexer indexer, Searcher searcher) {
        super(dao);
        this.searcher = searcher;
        this.indexer = indexer;
        this.type = type;
    }

    /**
     * Method converts JSON object object to DTO. Necessary for displaying in the
     * frontend.
     *
     * @param jsonObject
     *            return from find methods
     * @param related
     *            true or false
     * @return DTO object
     */
    public abstract S convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException;

    /**
     * Get all DTO objects from index an convert them for frontend wit all
     * relations.
     *
     * @return List of DTO objects
     */
    public List<S> findAll() throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(), false);
    }

    /**
     * Get all DTO objects from index an convert them for frontend.
     *
     * @param related
     *            true or false
     * @return List of DTO objects
     */
    public List<S> findAll(boolean related) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(), related);
    }

    /**
     * Find list of all objects from ES.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return list of all objects from ES
     */
    public List<S> findAll(String sort) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(sort), false);
    }

    /**
     * Find list of all objects from ES.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return list of all objects from ES
     */
    public List<S> findAll(String sort, Integer offset, Integer size) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(sort, offset, size), false);
    }

    /**
     * Find list of all objects from ES.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return list of all objects from ES
     */
    public List<S> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(sort, offset, size), false);
    }

    /**
     * Find list of all objects from ES.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @param related
     *            true or false
     * @return list of all objects from ES
     */
    public List<S> findAll(String sort, Integer offset, Integer size, boolean related) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(sort, offset, size), related);
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
     * @throws DataException
     *             that can be caused by ElasticSearch
     */
    public String createCountQuery(Map filters) throws DataException {
        return null;
    }

    /**
     * Method saves document to the index of Elastic Search.
     *
     * @param baseIndexedBean
     *            object
     */
    @SuppressWarnings("unchecked")
    public void saveToIndex(T baseIndexedBean) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (baseIndexedBean != null) {
            indexer.performSingleRequest(baseIndexedBean, type);
        }
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     *
     * @param baseIndexedBeans
     *            List of BaseIndexedBean objects
     */
    @SuppressWarnings("unchecked")
    public void addAllObjectsToIndex(List<T> baseIndexedBeans)
            throws CustomResponseException, DAOException, InterruptedException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(baseIndexedBeans, type);
        for (T baseIndexedBean : baseIndexedBeans) {
            baseIndexedBean.setIndexAction(IndexAction.DONE);
            dao.save(baseIndexedBean);
        }
    }

    /**
     * Method removes document from the index of Elastic Search.
     *
     * @param baseIndexedBean
     *            object
     */
    @SuppressWarnings("unchecked")
    public void removeFromIndex(T baseIndexedBean) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (baseIndexedBean != null) {
            indexer.performSingleRequest(baseIndexedBean, type);
        }
    }

    /**
     * Method removes document from the index of Elastic Search by given id.
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
     * @param baseIndexedBean
     *            object
     */
    protected void manageDependenciesForIndex(T baseIndexedBean)
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
     * @param baseIndexedBean
     *            object
     */
    public void save(T baseIndexedBean) throws DataException {
        try {
            baseIndexedBean.setIndexAction(IndexAction.INDEX);
            T savedBean = saveToDatabase(baseIndexedBean);
            saveToIndex(savedBean);
            manageDependenciesForIndex(savedBean);
            waitForIndexUpdate();
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
                    saveToIndex(baseIndexedBean);
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
    public void remove(T baseIndexedBean) throws DataException {
        try {
            baseIndexedBean.setIndexAction(IndexAction.DELETE);
            T savedBean = saveToDatabase(baseIndexedBean);
            removeFromIndex(savedBean);
            manageDependenciesForIndex(savedBean);
            waitForIndexUpdate();
            removeFromDatabase(savedBean);
        } catch (DAOException e) {
            logger.debug(e);
            throw new DataException(e);
        } catch (CustomResponseException | IOException e) {
            int count = 0;
            int maxTries = 5;
            while (true) {
                try {
                    removeFromIndex(baseIndexedBean);
                    removeFromDatabase(baseIndexedBean);
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

    // TODO: search for some more elegant way
    private void waitForIndexUpdate() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
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
     * @return amount of objects according to given query or 0 if query is null
     */
    public Long count(String query) throws DataException {
        return searcher.countDocuments(query);
    }

    /**
     * Display all documents for exact type.
     *
     * @return list of all documents
     */
    public List<JsonObject> findAllDocuments() throws DataException {
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
    public List<JsonObject> findAllDocuments(String sort) throws DataException {
        QueryBuilder queryBuilder = matchAllQuery();
        return searcher.findDocuments(queryBuilder.toString(), sort);
    }

    /**
     * Display all documents for exact type with exact sorting.
     *
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return sorted list of all documents
     */
    public List<JsonObject> findAllDocuments(Integer offset, Integer size) throws DataException {
        QueryBuilder queryBuilder = matchAllQuery();
        return searcher.findDocuments(queryBuilder.toString(), offset, size);
    }

    /**
     * Display all documents for exact type with exact sorting.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return sorted list of all documents
     */
    public List<JsonObject> findAllDocuments(String sort, Integer offset, Integer size) throws DataException {
        QueryBuilder queryBuilder = matchAllQuery();
        return searcher.findDocuments(queryBuilder.toString(), sort, offset, size);
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
     *            findById(Integer id).
     * @return related DTO object
     */
    public S findById(Integer id, boolean related) throws DataException {
        return convertJSONObjectToDTO(searcher.findDocument(id), related);
    }

    /**
     * Find list of DTO objects by query.
     *
     * @param query
     *            as QueryBuilder object
     * @param related
     *            determines if converted object is related to some other object (if
     *            so, objects related to it are not included in conversion)
     * @return list of found DTO objects
     */
    public List<S> findByQuery(QueryBuilder query, boolean related) throws DataException {
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString()), related);
    }

    /**
     * Find list of DTO objects by query.
     *
     * @param query
     *            as QueryBuilder object
     * @param sort
     *            as String
     * @param related
     *            determines if converted object is related to some other object (if
     *            so, objects related to it are not included in conversion)
     * @return list of found DTO objects
     */
    public List<S> findByQuery(QueryBuilder query, String sort, boolean related) throws DataException {
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString(), sort), related);
    }

    /**
     * Find list of sorted DTO objects by query with defined offset and size of
     * results.
     *
     * @param query
     *            as QueryBuilder object
     * @param sort
     *            as String
     * @param offset
     *            as Integer
     * @param size
     *            as Integer
     * @param related
     *            determines if converted object is related to some other object (if
     *            so, objects related to it are not included in conversion)
     * @return list of found DTO objects
     */
    public List<S> findByQuery(QueryBuilder query, String sort, Integer offset, Integer size, boolean related)
            throws DataException {
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString(), sort, offset, size), related);
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
    public List<S> convertJSONObjectsToDTOs(List<JsonObject> jsonObjects, boolean related) throws DataException {
        List<S> results = new ArrayList<>();

        for (JsonObject jsonObject : jsonObjects) {
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
    public T convertJSONObjectToBean(JsonObject jsonObject) throws DAOException {
        Integer id = getIdFromJSONObject(jsonObject);
        if (id == 0) {
            // TODO: maybe here could be used some instancing of generic
            // class...
            return null;
        } else {
            return getById(id);
        }
    }

    protected <O extends BaseDTO> List<O> convertListIdToDTO(List<Integer> listId,
            SearchService<?, O, ?> service) throws DataException {
        return service.findByQuery(createSetQueryForIds(listId), true);
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
    protected <O extends BaseDTO> List<O> convertRelatedJSONObjectToDTO(JsonObject jsonObject, String key,
            SearchService<?, O, ?> service) throws DataException {
        List<Integer> ids = getRelatedPropertyForDTO(jsonObject, key);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return service.findByQuery(createSetQueryForIds(ids), true);
    }

    private QueryBuilder createSetQueryForIds(List<Integer> ids) {
        return termsQuery("_id", ids);
    }

    /**
     * Get id from JSON object returned form ElasticSearch.
     *
     * @param jsonObject
     *            returned form ElasticSearch
     * @return id as Integer
     */
    public Integer getIdFromJSONObject(JsonObject jsonObject) {
        if (jsonObject.containsKey("_id")) {
            String id = jsonObject.getString("_id");
            if (Objects.nonNull(id)) {
                return Integer.valueOf(id);
            }
        }
        return 0;
    }

    /**
     * Create query for set of data.
     *
     * @param key
     *            JSON key for searched object
     * @param values
     *            set of values for searched objects or some objects related to
     *            searched object
     * @param contains
     *            determine if results should contain given value or should not
     *            contain given value
     * @return query
     */
    protected QueryBuilder createSetQuery(String key, Set<? extends Object> values, boolean contains) {
        if (contains && !values.isEmpty()) {
            return termsQuery(key, values);
        } else if (!contains && values != null) {
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            return boolQuery.mustNot(termsQuery(key, values));
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
        JsonObject jsonObject = searcher.aggregateDocuments(query, createCountAggregation(field));
        JsonObject count = jsonObject.getJsonObject(field);
        return count.getJsonNumber("value").longValue();
    }

    protected Double findSumAggregation(String query, String field) throws DataException {
        JsonObject jsonObject = searcher.aggregateDocuments(query, createSumAggregation(field));
        JsonObject sum = jsonObject.getJsonObject(field);
        return sum.getJsonNumber("value").doubleValue();
    }

    /**
     * Find distinct values sorted by terms. Returned values are stored as Strings.
     *
     * @param query
     *            for searched values to aggregation
     * @param field
     *            by which aggregation is going to be performed
     * @param sort
     *            asc true or false
     * @return sorted list of distinct values
     */
    protected List<String> findDistinctValues(String query, String field, boolean sort) throws DataException {
        List<String> distinctValues = new ArrayList<>();
        JsonObject jsonObject = searcher.aggregateDocuments(query, createTermAggregation(field, sort));
        JsonObject aggregations = (JsonObject) jsonObject.get(field);
        JsonArray buckets = aggregations.getJsonArray("buckets");
        for (Object bucket : buckets) {
            JsonObject document = (JsonObject) bucket;
            distinctValues.add(document.getString("key"));
        }
        return distinctValues;
    }

    private String createAvgAggregation(String field) {
        return XContentHelper.toString(AggregationBuilders.avg(field).field(field));
    }

    private String createCountAggregation(String field) {
        return XContentHelper.toString(AggregationBuilders.count(field).field(field));
    }

    private String createSumAggregation(String field) {
        return XContentHelper.toString(AggregationBuilders.sum(field).field(field));
    }

    private String createTermAggregation(String field, boolean sort) {
        return XContentHelper
                .toString(AggregationBuilders.terms(field).field(field).order(Terms.Order.aggregation("_term", sort)));
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
     *            JSONObject
     * @return display properties as list of Integers
     */
    private List<Integer> getRelatedPropertyForDTO(JsonObject object, String key) {
        if (object != null) {
            JsonArray jsonArray = object.getJsonArray(key);
            List<Integer> ids = new ArrayList<>();
            for (JsonValue singleObject : jsonArray) {
                ids.add(singleObject.asJsonObject().getInt("id"));
            }
            return ids;
        }
        return new ArrayList<>();
    }

    /**
     * Converts properties' values returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject
     * @param key
     *            to access JSONArray
     * @param subKeys
     *            to access specified values in objects of JSONArray
     * @return display properties as list of Integers
     */
    protected List<RelatedProperty> getRelatedArrayPropertyForDTO(JsonObject object, String key, List<String> subKeys) {
        if (object != null) {
            JsonArray jsonArray = (JsonArray) object.get(key);
            List<RelatedProperty> relatedProperties = new ArrayList<>();
            for (Object singleObject : jsonArray) {
                JsonObject jsonObject = (JsonObject) singleObject;
                RelatedProperty relatedProperty = new RelatedProperty();
                relatedProperty.setId(jsonObject.getInt("id"));
                ArrayList<String> values = new ArrayList<>();
                for (String subKey : subKeys) {
                    values.add(jsonObject.getString(subKey));
                }
                relatedProperty.setValues(values);
                relatedProperties.add(relatedProperty);
            }
            return relatedProperties;
        }
        return new ArrayList<>();
    }

    /**
     * Get size of related objects returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject
     * @param key
     *            of property which need to be counted
     * @return size of array with related objects
     */
    protected int getSizeOfRelatedPropertyForDTO(JsonObject object, String key) {
        if (object != null) {
            JsonArray jsonArray = (JsonArray) object.get(key);
            return jsonArray.size();
        }
        return 0;
    }
}
