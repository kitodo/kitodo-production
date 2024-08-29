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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
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
    private static volatile SearchRestClient instance = null;

    private SearchRestClient() {
    }

    /**
     * Return singleton variable of type SearchRestClient.
     *
     * @return unique instance of SearchRestClient
     */
    public static SearchRestClient getInstance() {
        SearchRestClient localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (SearchRestClient.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new SearchRestClient();
                    localReference.initiateClient();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Count amount of documents responding to given query.
     *
     * @param type
     *            for which request is performed
     * @param query
     *            to find a document
     * @return http entity as String
     */
    String countDocuments(String type, QueryBuilder query) throws CustomResponseException, DataException {
        String wrappedQuery = "{\n \"query\": " + query.toString() + "\n}";
        HttpEntity entity = new NStringEntity(wrappedQuery, ContentType.APPLICATION_JSON);
        return performRequest(type, entity, HttpMethod.GET, "_count");
    }

    /**
     * Aggregate documents responding to given query and aggregation's conditions.
     * Possible aggregation types are sum, count or terms.
     *
     * @param type
     *            for which request is performed
     * @param query
     *            to find a document
     * @param aggregation
     *            conditions as String
     * @return http entity as String
     */
    Aggregations aggregateDocuments(String type, QueryBuilder query, AggregationBuilder aggregation)
            throws CustomResponseException, DataException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(query);
        sourceBuilder.aggregation(aggregation);

        SearchRequest searchRequest = new SearchRequest(this.indexBase + "_" + type);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
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
     * @param type
     *            for which request is performed
     * @param id
     *            of searched document
     * @return http entity as String
     */
    Map<String, Object> getDocument(String type, Integer id) throws CustomResponseException, DataException {
        try {
            GetRequest getRequest = new GetRequest(this.indexBase + "_" + type);
            getRequest.id(String.valueOf(id));
            GetResponse getResponse = highLevelClient.get(getRequest, RequestOptions.DEFAULT);
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
     * @param type
     *            for which request is performed
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
    SearchHits getDocument(String type, QueryBuilder query, SortBuilder sort, Integer offset, Integer size)
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
            sourceBuilder.size(10000);
        }

        SearchRequest searchRequest = new SearchRequest(this.indexBase + "_" + type);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            return response.getHits();
        } catch (ResponseException e) {
            handleResponseException(e);
            return SearchHits.empty();
        } catch (IOException e) {
            throw new DataException(e);
        }
    }

    /**
     * Retrieves a map of document IDs to their corresponding base type for the given list of IDs.
     *
     * @param type the type of documents being requested, used to determine the index.
     * @param ids  the list of document IDs to search for.
     * @return a map where each key is a document ID and the value is the corresponding base type of the document.
     */
    public Map<Integer, String> fetchIdToBaseTypeMap(String type, List<Integer> ids) throws CustomResponseException, DataException {
        Map<Integer, String> idToBaseTypeMap = new HashMap<>();

        try {
            // Create a MultiGetRequest to fetch multiple documents with only baseType field
            MultiGetRequest multiGetRequest = new MultiGetRequest();
            for (Integer id : ids) {
                MultiGetRequest.Item item = new MultiGetRequest.Item(this.indexBase + "_" + type, String.valueOf(id));
                // Only fetch baseType field
                item.fetchSourceContext(new FetchSourceContext(true, new String[]{"baseType"}, null));
                multiGetRequest.add(item);
            }
            MultiGetResponse multiGetResponse = highLevelClient.mget(multiGetRequest, RequestOptions.DEFAULT);
            for (MultiGetItemResponse itemResponse : multiGetResponse.getResponses()) {
                if (!itemResponse.isFailed() && itemResponse.getResponse().isExists()) {
                    String baseType = (String) itemResponse.getResponse().getSourceAsMap().get("baseType");
                    Integer id = Integer.parseInt(itemResponse.getResponse().getId());
                    idToBaseTypeMap.put(id, baseType);
                }
            }
        } catch (ResponseException e) {
            handleResponseException(e);
        } catch (IOException | NumberFormatException e) {
            throw new DataException(e);
        }

        return idToBaseTypeMap;
    }

    private String performRequest(String type, HttpEntity entity, String httpMethod, String urlRequest)
            throws CustomResponseException, DataException {
        String output = "";
        try {
            Request request = new Request(httpMethod, "/" + indexBase + "_" + type + "/" + urlRequest);
            request.addParameter("pretty", "true");
            request.setEntity(entity);
            Response response = client.performRequest(request);
            output = EntityUtils.toString(response.getEntity());
        } catch (ResponseException e) {
            handleResponseException(e);
        } catch (IOException e) {
            throw new DataException(e);
        }
        return output;
    }
}
