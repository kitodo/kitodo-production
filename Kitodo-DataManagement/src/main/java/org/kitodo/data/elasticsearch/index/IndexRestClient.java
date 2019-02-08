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
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.kitodo.data.elasticsearch.KitodoRestClient;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;

/**
 * Implementation of Elastic Search REST Client for index package.
 */
public class IndexRestClient extends KitodoRestClient {

    /**
     * IndexRestClient singleton.
     */
    private static IndexRestClient instance = null;
    private final Object lock = new Object();

    private IndexRestClient() {
    }

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
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    public void addDocument(Map<String, Object> entity, Integer id, boolean forceRefresh)
            throws IOException, CustomResponseException {
        IndexRequest indexRequest = new IndexRequest(this.index, this.type, String.valueOf(id)).source(entity);
        if (forceRefresh) {
            indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        }

        IndexResponse indexResponse = highLevelClient.index(indexRequest);
        processStatusCode(indexResponse.status());
    }

    /**
     * Add list of documents to the index. This method will be used for add whole
     * table to the index. It performs asynchronous request.
     *
     * @param documentsToIndex
     *            list of json documents to the index
     */
    void addTypeSync(Map<Integer, Map<String, Object>> documentsToIndex) throws CustomResponseException {
        BulkRequest bulkRequest = prepareBulkRequest(documentsToIndex);

        try {
            BulkResponse bulkResponse = highLevelClient.bulk(bulkRequest);
            if (bulkResponse.hasFailures()) {
                throw new CustomResponseException(bulkResponse.buildFailureMessage());
            }
        } catch (IOException e) {
            throw new CustomResponseException(e);
        }
    }

    /**
     * Add list of documents to the index. This method will be used for add whole
     * table to the index. It performs asynchronous request.
     *
     * @param documentsToIndex
     *            list of json documents to the index
     */
    void addTypeAsync(Map<Integer, Map<String, Object>> documentsToIndex) {
        BulkRequest bulkRequest = prepareBulkRequest(documentsToIndex);

        ResponseListener responseListener = new ResponseListener(this.type, documentsToIndex.size());
        highLevelClient.bulkAsync(bulkRequest, responseListener);

        synchronized (lock) {
            while (Objects.isNull(responseListener.getBulkResponse())) {
                try {
                    lock.wait(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Delete document from the index.
     *
     * @param id
     *            of the document
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    void deleteDocument(Integer id, boolean forceRefresh) throws CustomResponseException, DataException {
        DeleteRequest deleteRequest = new DeleteRequest(this.index, this.type, String.valueOf(id));
        if (forceRefresh) {
            deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        }

        try {
            highLevelClient.delete(deleteRequest);
        } catch (ResponseException e) {
            handleResponseException(e);
        }  catch (IOException e) {
            throw new DataException(e);
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
        String query = "{\n \"properties\": {\n\"" + field + "\": {\n" + "      \"type\": \"text\",\n"
                + "      \"fielddata\": true,\n" + "      \"fields\": {\n" + "        \"raw\": {\n"
                + "          \"type\":  \"text\",\n" + "          \"index\": false}\n" + "    }\n" + "  }}}";
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Response indexResponse = client.performRequest(HttpMethod.PUT,
            "/" + this.getIndex() + "/_mapping/" + type + "?update_all_types", Collections.emptyMap(), entity);
        processStatusCode(indexResponse.getStatusLine());
    }

    private BulkRequest prepareBulkRequest(Map<Integer, Map<String, Object>> documentsToIndex) {
        BulkRequest bulkRequest = new BulkRequest();

        for (Map.Entry<Integer, Map<String, Object>> entry : documentsToIndex.entrySet()) {
            IndexRequest indexRequest = new IndexRequest(this.index, this.type, String.valueOf(entry.getKey()));
            bulkRequest.add(indexRequest.source(entry.getValue()));
        }

        return bulkRequest;
    }
}
