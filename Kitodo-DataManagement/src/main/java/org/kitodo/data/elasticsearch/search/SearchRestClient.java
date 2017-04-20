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
import org.elasticsearch.client.Response;
import org.kitodo.data.elasticsearch.KitodoRestClient;

/**
 * Extension of KitodoRestClient for search package.
 */
public class SearchRestClient extends KitodoRestClient {

    /**
     * Count amount of documents responding to given query.
     *
     * @param query
     *            to find a document
     * @return http entity as String
     */
    public String countDocuments(String query) throws IOException {
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Response response = restClient.performRequest("GET", "/" + index + "/" + type + "/_count",
                Collections.singletonMap("pretty", "true"), entity);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Get document by id.
     *
     * @param id of searched document
     * @return http entity as String
     */
    public String getDocument(Integer id)
            throws IOException {
        Response response = restClient.performRequest(
                "GET",
                "/" + index + "/" + type + "/" + id.toString(),
                Collections.singletonMap("pretty", "true"));
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Get document by query.
     *
     * @param query to find a document
     * @return http entity as String
     */
    public String getDocument(String query)
            throws IOException {
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Response response = restClient.performRequest(
                "GET",
                "/" + index + "/" + type + "/_search",
                Collections.singletonMap("pretty", "true"),
                entity);
        return EntityUtils.toString(response.getEntity());
    }
}
