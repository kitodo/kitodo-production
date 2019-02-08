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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.kitodo.data.elasticsearch.KitodoRestClient;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;

/**
 * Extension of KitodoRestClient for search package.
 */
public class SearchRestClient extends KitodoRestClient {

    /**
     * SearchRestClient singleton.
     */
    private static SearchRestClient instance = null;

    private SearchRestClient() {
    }

    /**
     * Return singleton variable of type SearchRestClient.
     *
     * @return unique instance of SearchRestClient
     */
    public static SearchRestClient getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (SearchRestClient.class) {
                if (Objects.equals(instance, null)) {
                    instance = new SearchRestClient();
                    instance.initiateClient();
                }
            }
        }
        return instance;
    }

    /**
     * Count amount of documents responding to given query.
     *
     * @param query
     *            to find a document
     * @return http entity as String
     */
    String countDocuments(QueryBuilder query) throws CustomResponseException, DataException {
        String wrappedQuery = "{\n \"query\": " + query.toString() + "\n}";
        HttpEntity entity = new NStringEntity(wrappedQuery, ContentType.APPLICATION_JSON);
        return performRequest(entity, HttpMethod.GET, "_count");
    }

    /**
     * Aggregate documents responding to given query and aggregation's conditions.
     * Possible aggregation types are sum, count or terms.
     *
     * @param query
     *            to find a document
     * @param aggregation
     *            conditions as String
     * @return http entity as String
     */
    Aggregations aggregateDocuments(QueryBuilder query, AggregationBuilder aggregation)
            throws CustomResponseException, DataException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(query);
        sourceBuilder.aggregation(aggregation);

        SearchRequest searchRequest = new SearchRequest(this.index);
        searchRequest.types(this.type);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = highLevelClient.search(searchRequest);
            return response.getAggregations();
        } catch (ResponseException e) {
            handleResponseException(e);
            return new Aggregations(new ArrayList<>());
        } catch (IOException e) {
            throw new DataException(e);
        }
    }

    /**
     * Get document by id.
     *
     * @param id
     *            of searched document
     * @return http entity as String
     */
    Map<String, Object> getDocument(Integer id) throws CustomResponseException, DataException {
        try {
            GetRequest getRequest = new GetRequest(this.index, this.type, String.valueOf(id));
            GetResponse getResponse = highLevelClient.get(getRequest);
            if (getResponse.isExists()) {
                Map<String, Object> response = getResponse.getSourceAsMap();
                response.put("id", getResponse.getId());
                return response;
            }
        } catch (ResponseException e) {
            handleResponseException(e);
        } catch (IOException e) {
            throw new DataException(e);
        }
        return Collections.emptyMap();
    }

    /**
     * Get document by query with possible sort of results.
     *
     * @param query
     *            to find a document
     * @param sort
     *            as String with sort conditions
     * @param offset
     *            as Integer
     * @param size
     *            as Integer
     * @return http entity as String
     */
    SearchHits getDocument(QueryBuilder query, SortBuilder sort, Integer offset, Integer size)
            throws CustomResponseException, DataException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(query);
        if (Objects.nonNull(sort)) {
            sourceBuilder.sort(sort);
        }
        if (Objects.nonNull(offset)) {
            sourceBuilder.from(offset);
        }
        if (Objects.nonNull(size)) {
            sourceBuilder.size(size);
        } else {
            sourceBuilder.size(1000);
        }

        SearchRequest searchRequest = new SearchRequest(this.index);
        searchRequest.types(this.type);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = highLevelClient.search(searchRequest);
            return response.getHits();
        } catch (ResponseException e) {
            handleResponseException(e);
            return SearchHits.empty();
        } catch (IOException e) {
            throw new DataException(e);
        }
    }

    private String performRequest(HttpEntity entity, String httpMethod, String urlRequest)
            throws CustomResponseException, DataException {
        String output = "";
        try {
            Response response = client.performRequest(httpMethod, "/" + index + "/" + type + "/" + urlRequest,
                Collections.singletonMap("pretty", "true"), entity);
            output = EntityUtils.toString(response.getEntity());
        } catch (ResponseException e) {
            handleResponseException(e);
        } catch (IOException e) {
            throw new DataException(e);
        }
        return output;
    }
}
