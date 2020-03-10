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

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.json.JsonObject;
import javax.ws.rs.HttpMethod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.BaseType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.BaseDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.data.ProjectService;
import org.primefaces.model.SortOrder;

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
    public abstract S convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException;

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
    public List<T> getAllNotIndexed(int offset, int size) throws DAOException {
        return dao.getAllNotIndexed(offset, size);
    }

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
     * Get all ids from index
     *
     * @return List of ids
     */
    public List<Integer> findAllIDs() throws DataException {
        List<Map<String, Object>> allDocuments = findAllDocuments();
        List<Integer> allIds = new ArrayList<>();
        for (Map<String, Object> document : allDocuments) {
            allIds.add(Integer.parseInt((String) document.get("id")));
        }
        return allIds;
    }


    /**
     * Method saves document to the index of Elastic Search.
     *
     * @param baseIndexedBean
     *            object
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    @SuppressWarnings("unchecked")
    public void saveToIndex(T baseIndexedBean, boolean forceRefresh)
            throws CustomResponseException, DataException, IOException {

        indexer.setMethod(HttpMethod.PUT);
        if (Objects.nonNull(baseIndexedBean)) {
            indexer.performSingleRequest(baseIndexedBean, type, forceRefresh);
        }
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     *
     * @param baseIndexedBeans
     *            List of BaseIndexedBean objects
     */
    @SuppressWarnings("unchecked")
    public void addAllObjectsToIndex(List<T> baseIndexedBeans) throws CustomResponseException, DAOException {
        indexer.setMethod(HttpMethod.PUT);
        if (!baseIndexedBeans.isEmpty()) {
            indexer.performMultipleRequests(baseIndexedBeans, type, true);
        }
    }

    /**
     * Method removes document from the index of Elastic Search.
     *
     * @param baseIndexedBean
     *            object
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    @SuppressWarnings("unchecked")
    public void removeFromIndex(T baseIndexedBean, boolean forceRefresh)
            throws CustomResponseException, DataException, IOException {
        indexer.setMethod(HttpMethod.DELETE);
        if (Objects.nonNull(baseIndexedBean)) {
            indexer.performSingleRequest(baseIndexedBean, type, forceRefresh);
        }
    }

    /**
     * Method removes document from the index of Elastic Search by given id.
     *
     * @param id
     *            of object
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    public void removeFromIndex(Integer id, boolean forceRefresh) throws CustomResponseException, DataException {
        indexer.setMethod(HttpMethod.DELETE);
        indexer.performSingleRequest(id, forceRefresh);
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
     *
     * @param baseIndexedBean
     *            object
     */
    public void save(T baseIndexedBean) throws DataException {
        try {
            baseIndexedBean.setIndexAction(IndexAction.INDEX);
            saveToDatabase(baseIndexedBean);
            // TODO: find out why properties lists are save double
            T savedBean = getById(baseIndexedBean.getId());
            saveToIndex(savedBean, true);
            manageDependenciesForIndex(savedBean);
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
        try {
            return searcher.countDocuments();
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
    }

    /**
     * Count objects according to given query.
     *
     * @param query
     *            for index search
     * @return amount of objects according to given query or 0 if query is null
     */
    public Long count(QueryBuilder query) throws DataException {
        return countDocuments(query);
    }

    /**
     * Display all documents for exact type.
     *
     * @return list of all documents
     */
    public List<Map<String, Object>> findAllDocuments() throws DataException {
        QueryBuilder queryBuilder = matchAllQuery();
        try {
            return searcher.findDocuments(queryBuilder, null, null, Math.toIntExact(count()));
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
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
        try {
            return convertJSONObjectToDTO(searcher.findDocument(id), related);
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
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
        try {
            return convertJSONObjectsToDTOs(searcher.findDocuments(query), related);
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
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
    public List<S> findByQuery(QueryBuilder query, SortBuilder sort, boolean related) throws DataException {
        try {
            return convertJSONObjectsToDTOs(searcher.findDocuments(query, sort), related);
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
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
    public List<S> findByQuery(QueryBuilder query, SortBuilder sort, Integer offset, Integer size, boolean related)
            throws DataException {
        try {
            return convertJSONObjectsToDTOs(searcher.findDocuments(query, sort, offset, size), related);
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
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
    protected List<S> convertJSONObjectsToDTOs(List<Map<String, Object>> jsonObjects, boolean related)
            throws DataException {
        List<S> results = new ArrayList<>();

        for (Map<String, Object> jsonObject : jsonObjects) {
            results.add(convertJSONObjectToDTO(jsonObject, related));
        }

        return results;
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
    protected <O extends BaseDTO> List<O> convertRelatedJSONObjectToDTO(Map<String, Object> jsonObject, String key,
            SearchService<?, O, ?> service) throws DataException {
        List<Integer> ids = getRelatedPropertyForDTO(jsonObject, key);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        if (service instanceof ProjectService) {
            BoolQueryBuilder query = new BoolQueryBuilder();
            query.must(createSetQueryForIds(ids));
            query.must(((ProjectService)service).getProjectsForCurrentUserQuery());
            return service.findByQuery(query, true);
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
    public Integer getIdFromJSONObject(Map<String, Object> jsonObject) {
        if (jsonObject.containsKey("id")) {
            String id = (String) jsonObject.get("id");
            if (Objects.nonNull(id)) {
                return Integer.valueOf(id);
            }
        }
        return 0;
    }

    protected Long countDocuments(QueryBuilder query) throws DataException {
        try {
            return searcher.countDocuments(query);
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
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
    protected QueryBuilder createSetQuery(String key, Set<?> values, boolean contains) {
        if (contains && !values.isEmpty()) {
            return termsQuery(key, values);
        } else if (!contains && Objects.nonNull(values)) {
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            return boolQuery.mustNot(termsQuery(key, values));
        } else {
            return matchQuery(key, 0);
        }
    }

    protected QueryBuilder createSetQuery(String key, List<Map<String, Object>> values, boolean contains) {
        Set<Integer> valuesIds = new HashSet<>();
        for (Map<String, Object> value : values) {
            valuesIds.add(getIdFromJSONObject(value));
        }

        return createSetQuery(key, valuesIds, contains);
    }

    protected QueryBuilder createSetQueryForBeans(String key, List<? extends BaseBean> values, boolean contains) {
        Set<Integer> valuesIds = new HashSet<>();
        for (BaseBean value : values) {
            valuesIds.add(value.getId());
        }

        return createSetQuery(key, valuesIds, contains);
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
        if (contains && Objects.nonNull(id)) {
            return matchQuery(key, id);
        } else if (!contains && Objects.nonNull(id)) {
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
        if (contains && Objects.nonNull(condition)) {
            return matchQuery(key, condition);
        } else if (!contains && Objects.nonNull(condition)) {
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
        if (Objects.isNull(operator)) {
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
                query = matchQuery(key, Helper.getDateAsFormattedString(date));
                break;
            case EQUAL_OR_BIGGER:
                query = rangeQuery(key).gte(Helper.getDateAsFormattedString(date));
                break;
            case EQUAL_OR_SMALLER:
                query = rangeQuery(key).lte(Helper.getDateAsFormattedString(date));
                break;
            case BIGGER:
                query = rangeQuery(key).gt(Helper.getDateAsFormattedString(date));
                break;
            case SMALLER:
                query = rangeQuery(key).lt(Helper.getDateAsFormattedString(date));
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

    protected Long findCountAggregation(QueryBuilder query, String field) throws DataException {
        try {
            Aggregations jsonObject = searcher.aggregateDocuments(query, AggregationBuilders.count(field).field(field));
            JsonObject count = jsonObject.get(field);
            return count.getJsonNumber("value").longValue();
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
    }

    protected Double findSumAggregation(QueryBuilder query, String field) throws DataException {
        try {
            Aggregations jsonObject = searcher.aggregateDocuments(query, AggregationBuilders.count(field).field(field));
            JsonObject sum = jsonObject.get(field);
            return sum.getJsonNumber("value").doubleValue();
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
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
     * @param size
     *            number of rows returned by query
     * @return sorted list of distinct values
     */
    protected List<String> findDistinctValues(QueryBuilder query, String field, boolean sort, long size) throws DataException {
        List<String> distinctValues = new ArrayList<>();
        try {
            TermsAggregationBuilder termsAggregation = AggregationBuilders.terms(field).field(field)
                    .order(Terms.Order.aggregation("_term", sort));
            if (size > 0) {
                termsAggregation.size(Math.toIntExact(size));
            }
            Aggregations jsonObject = searcher.aggregateDocuments(query, termsAggregation);
            ParsedStringTerms stringTerms = jsonObject.get(field);
            List<? extends Terms.Bucket> buckets = stringTerms.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                distinctValues.add(bucket.getKeyAsString());
            }
            return distinctValues;
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
    }

    protected Map<String, Object> findDocument(QueryBuilder query) throws DataException {
        try {
            return searcher.findDocument(query);
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
    }

    protected List<Map<String, Object>> findDocuments(QueryBuilder query) throws DataException {
        return findDocuments(query, null);
    }

    protected List<Map<String, Object>> findDocuments(QueryBuilder query, SortBuilder sortBuilder) throws DataException {
        return findDocuments(query, sortBuilder, null, null);
    }

    protected List<Map<String, Object>> findDocuments(QueryBuilder query, SortBuilder sortBuilder, Integer offset, Integer size)
            throws DataException {
        try {
            return searcher.findDocuments(query, sortBuilder, offset, size);
        } catch (CustomResponseException e) {
            throw new DataException(e);
        }
    }

    /**
     * Converts properties' values returned from ElasticSearch index.
     *
     * @param object
     *            JSONObject
     * @return display properties as list of Integers
     */
    @SuppressWarnings("unchecked")
    private List<Integer> getRelatedPropertyForDTO(Map<String, Object> object, String key) {
        if (Objects.nonNull(object)) {
            List<Map<String, Object>> jsonArray = (List<Map<String, Object>>) object.get(key);
            List<Integer> ids = new ArrayList<>();
            for (Map<String, Object> singleObject : jsonArray) {
                ids.add((Integer) singleObject.get("id"));
            }
            return ids;
        }
        return new ArrayList<>();
    }

    protected SortBuilder getSortBuilder(String sortField, SortOrder sortOrder) {
        if (!Objects.equals(sortField, null) && Objects.equals(sortOrder, SortOrder.ASCENDING)) {
            return SortBuilders.fieldSort(sortField).order(org.elasticsearch.search.sort.SortOrder.ASC);
        } else if (!Objects.equals(sortField, null) && Objects.equals(sortOrder, SortOrder.DESCENDING)) {
            return SortBuilders.fieldSort(sortField).order(org.elasticsearch.search.sort.SortOrder.DESC);
        } else {
            return null;
        }
    }

    /**
     * Removes all objects from index, which are no longer in Database.
     * @param baseIndexedBeans the list of beans to check for missing db eintries.
     * 
     */
    public void removeLooseIndexData(List<S> baseIndexedBeans) throws DataException, CustomResponseException {
        for (S baseIndexedBean : baseIndexedBeans) {
            Integer baseIndexedBeanId = baseIndexedBean.getId();
            try {
                getById(baseIndexedBeanId);
            } catch (DAOException e) {
                removeFromIndex(baseIndexedBeanId,true);
            }
        }
    }
}
