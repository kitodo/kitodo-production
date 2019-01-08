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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.kitodo.data.elasticsearch.KitodoRestClient;
import org.kitodo.data.exceptions.DataException;

/**
 * Extension of KitodoRestClient for search package.
 */
public class SearchRestClient extends KitodoRestClient {

    private static final Logger logger = LogManager.getLogger(SearchRestClient.class);

    /**
     * SearchRestClient singleton.
     */
    private static SearchRestClient instance = null;

    private SearchRestClient() {}

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
    String countDocuments(String query) throws DataException {
        String wrappedQuery = "{\n \"query\": " + query + "\n}";
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
    String aggregateDocuments(String query, String aggregation) throws DataException {
        String wrappedQuery = "{\n \"query\": " + query + "\n,\n \"aggs\": " + aggregation + "\n}";
        HttpEntity entity = new NStringEntity(wrappedQuery, ContentType.APPLICATION_JSON);
        return performRequest(entity, HttpMethod.POST, "_search?size=0");
    }

    /**
     * Get document by id.
     *
     * @param id
     *            of searched document
     * @return http entity as String
     */
    String getDocument(Integer id) throws DataException {
        String output = "";
        try {
            Response response = restClient.performRequest(HttpMethod.GET, "/" + index + "/" + type + "/" + id.toString(),
                    getParameter());
            output = EntityUtils.toString(response.getEntity());
        } catch (ResponseException e) {
            handleResponseException(e);
        } catch (IOException e) {
            throw new DataException(e);
        }
        return output;
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
    String getDocument(String query, String sort, Integer offset, Integer size) throws DataException {
        String output = "";
        String wrappedQuery;
        Map<String, String> parameters;
        if (Objects.nonNull(sort)) {
            String wrappedSort = "\n \"sort\": [" + sort + "]\n";
            wrappedQuery = "{\n" + wrappedSort + ",\n \"query\": " + query + "\n}";
        } else {
            wrappedQuery = "{\n \"query\": " + query + "\n}";
        }

        if ( Objects.nonNull(offset) && Objects.nonNull(size)) {
            parameters = getParameter(offset.toString(), size.toString());
        } else if (Objects.isNull(offset) && Objects.nonNull(size)) {
            parameters = getParameter(null, size.toString());
        } else if (Objects.nonNull(offset) && Objects.isNull(size)) {
            parameters = getParameter(offset.toString(),"10000");
        } else {
            parameters = getParameter(null, "10000");
        }

        HttpEntity entity = new NStringEntity(wrappedQuery, ContentType.APPLICATION_JSON);
        try {
            Response response = restClient.performRequest(HttpMethod.GET, "/" + index + "/" + type + "/_search?",
                    parameters, entity);
            output = EntityUtils.toString(response.getEntity());
        } catch (ResponseException e) {
            handleResponseException(e);
        } catch (IOException e) {
            throw new DataException(e);
        }
        return output;
    }

    private String performRequest(HttpEntity entity, String httpMethod, String urlRequest) throws DataException {
        String output = "";
        try {
            Response response = restClient.performRequest(httpMethod, "/" + index + "/" + type + "/" + urlRequest,
                    getParameter(), entity);
            output = EntityUtils.toString(response.getEntity());
        } catch (ResponseException e) {
            handleResponseException(e);
        } catch (IOException e) {
            throw new DataException(e);
        }
        return output;
    }

    private Map<String, String> getParameter() {
        return getParameter(null, null);
    }

    private Map<String, String> getParameter(String offset, String size) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("pretty", "true");

        if (Objects.nonNull(offset) && Objects.nonNull(size)) {
            parameters.put("from", offset);
            parameters.put("size", size);
            return parameters;
        } else if (Objects.isNull(offset) && Objects.nonNull(size)) {
            parameters.put("size", size);
            return parameters;
        } else {
            return parameters;
        }
    }

    private void handleResponseException(ResponseException e) throws DataException {
        if (e.getResponse().getStatusLine().getStatusCode() == 404) {
            logger.debug(e.getMessage());
        } else {
            throw new DataException(e);
        }
    }
}
