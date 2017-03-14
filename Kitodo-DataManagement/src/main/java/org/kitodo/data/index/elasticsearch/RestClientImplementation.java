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

package org.kitodo.data.index.elasticsearch;

import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;

import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;

import org.kitodo.data.index.api.ClientInterface;

/**
 * Implementation of Elastic Search REST Client for Index Module.
 */
public class RestClientImplementation implements ClientInterface {

    private String index;
    private String type;
    private RestClient restClient;

    /**
     * Create REST client.
     *
     * @param host default host is localhost
     * @param port default port ist 9200
     * @param protocol default protocol is http
     */
    public void initiateClient(String host, Integer port, String protocol) {
        restClient = RestClient.builder(new HttpHost(host, port, protocol)).build();
    }

    /**
     * Get information about client server.
     *
     * @return information about the server
     */
    public String getServerInformation() throws IOException {
        Response response = restClient.performRequest("GET", "/",
                Collections.singletonMap("pretty", "true"));
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Close REST Client.
     *
     * @throws IOException add description
     */
    public void closeClient() throws IOException {
        restClient.close();
    }

    /**
     * Add document to the index. This method will be used for add or update of single document.
     *
     * @param entity with document which is going to be indexed
     * @param id of document - equal to the id from table in database
     * @return response from the server
     */
    public String addDocument(HttpEntity entity, Integer id)
            throws IOException {
        Response indexResponse = restClient.performRequest(
                "PUT",
                "/" + this.getIndex() + "/" + this.getType() + "/" + id,
                Collections.<String, String>emptyMap(),
                entity);

        return IOUtils.toString(indexResponse.getEntity().getContent(), "UTF-8");
    }

    /**
     * Add list of documents to the index. This method will be used for add whole table to the index.
     * It performs asynchronous request.
     *
     * @param documentsToIndex list of json documents to the index
     */
    public String addType(HashMap<Integer, HttpEntity> documentsToIndex) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(documentsToIndex.size());
        final StringBuilder output = new StringBuilder();

        for (Map.Entry<Integer, HttpEntity> entry : documentsToIndex.entrySet()) {
            restClient.performRequestAsync(
                    "PUT",
                    "/" + this.getIndex() + "/" + this.getType() + "/" + entry.getKey(),
                    Collections.<String, String>emptyMap(),
                    entry.getValue(),
                    new ResponseListener() {
                        @Override
                        //problem with return type - it should be String
                        //dirty hack private variable ArrayResult
                        public void onSuccess(Response response) {
                            output.append(response.toString());
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            latch.countDown();
                        }
                    }
            );
        }
        latch.await();

        return output.toString();
    }

    /**
     * Delete document from the index.
     *
     * @param id of the document
     * @return response from server
     */
    public String deleteDocument(Integer id) throws IOException {
        Response indexResponse = restClient.performRequest(
                "DELETE",
                "/" + this.getIndex() + "/" + this.getType() + "/" + id);
        return indexResponse.toString();
    }

    /**
     * Delete all documents of certain type from the index.
     *
     * @return response from server
     */
    public String deleteType() throws IOException {
        String query = "{\n"
                + "  \"query\": {\n"
                + "    \"match_all\": {}\n"
                + "  }\n"
                + "}";
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Response indexResponse = restClient.performRequest(
                "POST",
                "/" + this.getIndex() + "/" + this.getType() +  "/_delete_by_query",
                Collections.<String, String>emptyMap(),
                entity);
        return indexResponse.toString();
    }

    public String getIndex() {
        return index;
    }

    /**
     * Setter for index.
     *
     * @param index - equal to the name of database, default kitodo
     */
    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    /**
     * Setter for type.
     *
     * @param type - equal to the name of table in database
     */
    public void setType(String type) {
        this.type = type;
    }
}
