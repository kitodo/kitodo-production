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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.kitodo.data.elasticsearch.KitodoRestClient;

/**
 * Implementation of Elastic Search REST Client for index package.
 */
public class IndexRestClient extends KitodoRestClient {

    /**
     * Add document to the index. This method will be used for add or update of
     * single document.
     *
     * @param entity
     *            with document which is going to be indexed
     * @param id
     *            of document - equal to the id from table in database
     * @return status code of the response from the server
     */
    public boolean addDocument(HttpEntity entity, Integer id) throws IOException {
        Response indexResponse = restClient.performRequest("PUT",
                "/" + this.getIndex() + "/" + this.getType() + "/" + id, Collections.<String, String>emptyMap(),
                entity);
        int statusCode = indexResponse.getStatusLine().getStatusCode();
        return statusCode == 200 || statusCode == 201;
    }

    /**
     * Add list of documents to the index. This method will be used for add
     * whole table to the index. It performs asynchronous request.
     *
     * @param documentsToIndex
     *            list of json documents to the index
     */
    public String addType(HashMap<Integer, HttpEntity> documentsToIndex) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(documentsToIndex.size());
        final StringBuilder output = new StringBuilder();

        for (Map.Entry<Integer, HttpEntity> entry : documentsToIndex.entrySet()) {
            restClient.performRequestAsync("PUT", "/" + this.getIndex() + "/" + this.getType() + "/" + entry.getKey(),
                    Collections.<String, String>emptyMap(), entry.getValue(), new ResponseListener() {
                        @Override
                        // problem with return type - it should be String
                        // dirty hack private variable ArrayResult
                        public void onSuccess(Response response) {
                            output.append(response.toString());
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            latch.countDown();
                        }
                    });
        }
        latch.await();

        return output.toString();
    }

    /**
     * Delete document from the index.
     *
     * @param id
     *            of the document
     * @return status code of the response from server
     */
    public boolean deleteDocument(Integer id) throws IOException {
        Response indexResponse = restClient.performRequest("DELETE",
                "/" + this.getIndex() + "/" + this.getType() + "/" + id);
        return indexResponse.getStatusLine().getStatusCode() == 200;
    }

    /**
     * Delete all documents of certain type from the index.
     *
     * @return response from server
     */
    public boolean deleteType() throws IOException {
        String query = "{\n" + "  \"query\": {\n" + "    \"match_all\": {}\n" + "  }\n" + "}";
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Response indexResponse = restClient.performRequest("POST",
                "/" + this.getIndex() + "/" + this.getType() + "/_delete_by_query?conflicts=proceed",
                Collections.<String, String>emptyMap(), entity);
        return indexResponse.getStatusLine().getStatusCode() == 200;
    }
}
