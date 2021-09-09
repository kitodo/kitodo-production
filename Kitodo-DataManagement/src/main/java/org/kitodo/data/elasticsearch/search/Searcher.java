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

package org.kitodo.data.elasticsearch.search;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.sort.SortBuilder;
import org.kitodo.data.elasticsearch.Index;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;

/**
 * Implementation of ElasticSearch Searcher for Kitodo - Data Management
 * Module.
 */
public class Searcher extends Index {

    /**
     * Constructor for searcher with type names equal to table names.
     *
     * @param beanClass
     *            as Class
     */
    public Searcher(Class<?> beanClass) {
        super(beanClass);
    }

    /**
     * Constructor for searcher with tableName names not equal to table names.
     *
     * @param tableName
     *            as String
     */
    public Searcher(String tableName) {
        super(tableName);
    }

    /**
     * Count amount of all documents stored in index.
     *
     * @return amount of all documents
     */
    public Long countDocuments() throws CustomResponseException, DataException {
        return countDocuments(QueryBuilders.matchAllQuery());
    }

    /**
     * Count amount of documents responding to given query.
     *
     * @param query
     *            of searched documents
     * @return amount of documents as Long
     */
    public Long countDocuments(QueryBuilder query) throws CustomResponseException, DataException {
        SearchRestClient restClient = initiateRestClient();

        String response = restClient.countDocuments(this.type, query);
        if (!response.isEmpty()) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
                JsonObject result = jsonReader.readObject();
                return result.getJsonNumber("count").longValue();
            }
        } else {
            return 0L;
        }
    }

    /**
     * Aggregate documents responding to given query.
     *
     * @param query
     *            of searched documents
     * @param aggregation
     *            condition as String
     * @return aggregate documents as JSONObject
     */
    public Aggregations aggregateDocuments(QueryBuilder query, AggregationBuilder aggregation)
            throws CustomResponseException, DataException {
        SearchRestClient restClient = initiateRestClient();
        return restClient.aggregateDocuments(this.type, query, aggregation);
    }

    /**
     * Find document by id.
     *
     * @param id
     *            of searched document
     * @return JSONObject
     */
    public Map<String, Object> findDocument(Integer id) throws CustomResponseException, DataException {
        SearchRestClient restClient = initiateRestClient();
        return restClient.getDocument(this.type, id);
    }

    /**
     * Find document by query. It returns only first found document (last
     * inserted!).
     *
     * @param query
     *            as String
     * @return search result
     */
    public Map<String, Object> findDocument(QueryBuilder query) throws CustomResponseException, DataException {
        return findDocument(query, null);
    }

    /**
     * Find document by query. It returns only first found document (last inserted
     * or first matching to sort condition!).
     *
     * @param query
     *            as String
     * @return search result
     */
    public Map<String, Object> findDocument(QueryBuilder query, SortBuilder sort)
            throws CustomResponseException, DataException {
        SearchRestClient restClient = initiateRestClient();

        SearchHits searchHits = restClient.getDocument(this.type, query, sort, 0, 1);
        if (searchHits.getHits().length > 0) {
            SearchHit searchHit = searchHits.getAt(0);
            if (Objects.nonNull(searchHit)) {
                Map<String, Object> response = searchHit.getSourceAsMap();
                response.put("id", searchHit.getId());
                return response;
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Find many documents by query.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<Map<String, Object>> findDocuments(QueryBuilder query) throws CustomResponseException, DataException {
        return findDocuments(query, null, null, null);
    }

    /**
     * Find many documents by query and sort condition.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<Map<String, Object>> findDocuments(QueryBuilder query, SortBuilder sort)
            throws CustomResponseException, DataException {
        return findDocuments(query, sort, null, null);
    }

    /**
     * Find many documents by query, offset and size of results.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<Map<String, Object>> findDocuments(QueryBuilder query, Integer offset, Integer size)
            throws CustomResponseException, DataException {
        return findDocuments(query, null, offset, size);
    }

    /**
     * Find many documents by query, sort condition, offset and size of result set.
     *
     * @param query
     *            as String
     * @param sort
     *            as String
     * @return list of JSON objects
     */
    public List<Map<String, Object>> findDocuments(QueryBuilder query, SortBuilder sort, Integer offset, Integer size)
            throws CustomResponseException, DataException {
        SearchRestClient restClient = initiateRestClient();
        List<Map<String, Object>> searchResults = new ArrayList<>();

        SearchHits hits = restClient.getDocument(this.type, query, sort, offset, size);
        for (SearchHit hit : hits.getHits()) {
            Map<String,Object> result = hit.getSourceAsMap();
            result.put("id", hit.getId());
            searchResults.add(result);
        }
        return searchResults;
    }

    private SearchRestClient initiateRestClient() {
        SearchRestClient restClient = SearchRestClient.getInstance();
        restClient.setIndexBase(index);
        return restClient;
    }
}
