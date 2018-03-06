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

package org.kitodo.data.elasticsearch.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.ResponseListener;
import org.kitodo.data.elasticsearch.KitodoRestClient;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;

/**
 * Implementation of Elastic Search REST Client for index package.
 */
public class IndexRestClient extends KitodoRestClient {

    private static final Logger logger = LogManager.getLogger(IndexRestClient.class);

    /**
     * IndexRestClient singleton.
     */
    private static IndexRestClient instance = null;

    private IndexRestClient() {}

    /**
     * Return singleton variable of type IndexRestClient.
     *
     * @return unique instance of IndexRestClient
     */
    public static IndexRestClient getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (IndexRestClient.class) {
                if (Objects.equals(instance, null)) {
                    instance = new IndexRestClient();
                    instance.initiateClient();
                }
            }
        }
        return instance;
    }

    /**
     * Add document to the index. This method will be used for add or update of
     * single document.
     *
     * @param entity
     *            with document which is going to be indexed
     * @param id
     *            of document - equal to the id from table in database
     */
    public void addDocument(HttpEntity entity, Integer id) throws IOException, CustomResponseException {
        Response indexResponse = restClient.performRequest("PUT",
                "/" + this.getIndex() + "/" + this.getType() + "/" + id, Collections.emptyMap(),
                entity);
        processStatusCode(indexResponse.getStatusLine());
    }

    /**
     * Add list of documents to the index. This method will be used for add
     * whole table to the index. It performs asynchronous request.
     *
     * @param documentsToIndex
     *            list of json documents to the index
     */
    public void addType(HashMap<Integer, HttpEntity> documentsToIndex)
            throws InterruptedException, CustomResponseException {
        final CountDownLatch latch = new CountDownLatch(documentsToIndex.size());
        final ArrayList<String> output = new ArrayList<>();

        for (Map.Entry<Integer, HttpEntity> entry : documentsToIndex.entrySet()) {
            restClient.performRequestAsync("PUT", "/" + this.getIndex() + "/" + this.getType() + "/" + entry.getKey(),
                    Collections.emptyMap(), entry.getValue(), new ResponseListener() {
                        @Override
                        public void onSuccess(Response response) {
                            output.add(response.toString());
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            output.add(exception.getMessage());
                            latch.countDown();
                        }
                    });
        }
        latch.await();
        filterAsynchronousResponses(output);
    }

    /**
     * Delete document from the index.
     *
     * @param id
     *            of the document
     */
    public void deleteDocument(Integer id) throws IOException, CustomResponseException {
        try {
            restClient.performRequest("DELETE",
                    "/" + this.getIndex() + "/" + this.getType() + "/" + id);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                logger.debug(e.getMessage());
            } else {
                throw new CustomResponseException(e.getMessage());
            }
        }
    }

    /**
     * Enable sorting by text field.
     *
     * @param type
     *            as String
     * @param field
     *            as String
     */
    public void enableSortingByTextField(String type, String field) throws IOException, CustomResponseException {
        String query = "{\n \"properties\": {\n\""
                + field
                + "\": {\n"
                + "      \"type\": \"text\",\n"
                + "      \"fielddata\": true,\n"
                + "      \"fields\": {\n"
                + "        \"raw\": {\n"
                + "          \"type\":  \"text\",\n"
                + "          \"index\": false}\n"
                + "    }\n"
                + "  }}}";
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Response indexResponse = restClient.performRequest("PUT",
                "/" + this.getIndex() + "/_mapping/" + type + "?update_all_types",
                Collections.emptyMap(), entity);
        processStatusCode(indexResponse.getStatusLine());
    }

    private void filterAsynchronousResponses(ArrayList<String> responses) throws CustomResponseException {
        if (responses.size() > 0) {
            for (String response : responses) {
                if (response == null || response.equals("")) {
                    throw new CustomResponseException(
                            "ElasticSearch failed to add one or more documents for unknown reason!");
                } else {
                    if (!(response.contains("HTTP/1.1 200") || response.contains("HTTP/1.1 201"))) {
                        throw new CustomResponseException(
                                "ElasticSearch failed to add one or more documents! Reason: " + response);
                    }
                }
            }
        } else {
            throw new CustomResponseException("ElasticSearch failed to add all documents for unknown reason!");
        }
    }
}
