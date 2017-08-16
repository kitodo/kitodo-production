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
import java.util.Collections;

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
     * Count amount of documents responding to given query.
     *
     * @param query
     *            to find a document
     * @return http entity as String
     */
    public String countDocuments(String query) throws DataException {
        String output = "";
        String wrappedQuery = "{\n \"query\": " + query + "\n}";
        HttpEntity entity = new NStringEntity(wrappedQuery, ContentType.APPLICATION_JSON);
        try {
            Response response = restClient.performRequest("GET", "/" + index + "/" + type + "/_count",
                    Collections.singletonMap("pretty", "true"), entity);
            output = EntityUtils.toString(response.getEntity());
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                logger.debug(e.getMessage());
            } else {
                throw new DataException(e);
            }
        } catch (IOException e) {
            throw new DataException(e);
        }
        return output;
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
            Response response = restClient.performRequest("GET", "/" + index + "/" + type + "/" + id.toString(),
                    Collections.singletonMap("pretty", "true"));
            output = EntityUtils.toString(response.getEntity());
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                logger.debug(e.getMessage());
            } else {
                throw new DataException(e);
            }
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
     * @param size as Integer
     * @return http entity as String
     */
    String getDocument(String query, String sort, Integer offset, Integer size) throws DataException {
        String output = "";
        String wrappedQuery;
        if (sort != null && offset != null && size != null) {
            String wrappedPagination = "\"from\":" + offset + ",\"size\":" + size;
            String wrappedSort = ",\n \"sort\": [" + sort + "]\n";
            wrappedQuery = "{\n" + wrappedPagination + wrappedSort + ",\n \"query\": " + query + "\n}";
        } else if (sort != null && offset == null && size == null) {
            String wrappedSort = "\n \"sort\": [" + sort + "]\n";
            wrappedQuery = "{\n" + wrappedSort + ",\n \"query\": " + query + "\n}";
        } else if (sort == null && offset != null && size != null) {
            String wrappedPagination = "\"from\":" + offset + ",\"size\":" + size;
            wrappedQuery = "{\n" + wrappedPagination + ",\n \"query\": " + query + "\n}";
        } else {
            wrappedQuery = "{\n \"query\": " + query + "\n}";
        }
        HttpEntity entity = new NStringEntity(wrappedQuery, ContentType.APPLICATION_JSON);
        try {
            Response response = restClient.performRequest("GET", "/" + index + "/" + type + "/_search",
                    Collections.singletonMap("pretty", "true"), entity);
            output = EntityUtils.toString(response.getEntity());
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                logger.debug(e.getMessage());
            } else {
                throw new DataException(e);
            }
        } catch (IOException e) {
            throw new DataException(e);
        }
        return output;
    }
}
