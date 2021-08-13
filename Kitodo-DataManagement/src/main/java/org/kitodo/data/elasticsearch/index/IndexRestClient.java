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
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.kitodo.data.elasticsearch.KitodoRestClient;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;

/**
 * Implementation of ElasticSearch REST Client for index package.
 */
public class IndexRestClient extends KitodoRestClient {

    /**
     * IndexRestClient singleton.
     */
    private static volatile IndexRestClient instance = null;
    private final Object lock = new Object();

    private IndexRestClient() {
    }

    /**
     * Return singleton variable of type IndexRestClient.
     *
     * @return unique instance of IndexRestClient
     */
    public static IndexRestClient getInstance() {
        IndexRestClient localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (IndexRestClient.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new IndexRestClient();
                    localReference.initiateClient();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Add document to the index. This method will be used for add or update of
     * single document.
     *
     * @param type
     *            for which request is performed
     * @param entity
     *            with document which is going to be indexed
     * @param id
     *            of document - equal to the id from table in database
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    public void addDocument(String type, Map<String, Object> entity, Integer id, boolean forceRefresh)
            throws IOException, CustomResponseException {
        IndexRequest indexRequest = new IndexRequest(this.indexBase + "_" + type).source(entity);
        indexRequest.id(String.valueOf(id));
        if (forceRefresh) {
            indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        }

        IndexResponse indexResponse = highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        processStatusCode(indexResponse.status());
    }

    /**
     * Add list of documents to the index. This method will be used for add whole
     * table to the index. It performs asynchronous request.
     *
     * @param type
     *            for which request is performed
     * @param documentsToIndex
     *            list of json documents to the index
     */
    void addTypeSync(String type, Map<Integer, Map<String, Object>> documentsToIndex) throws CustomResponseException {
        BulkRequest bulkRequest = prepareBulkRequest(type, documentsToIndex);

        try {
            BulkResponse bulkResponse = highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
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
     * @param type
     *            for which request is performed
     * @param documentsToIndex
     *            list of json documents to the index
     */
    void addTypeAsync(String type, Map<Integer, Map<String, Object>> documentsToIndex) {
        BulkRequest bulkRequest = prepareBulkRequest(type, documentsToIndex);

        ResponseListener responseListener = new ResponseListener(type, documentsToIndex.size());
        highLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, responseListener);

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
     * Delete document from type specific index.
     *
     * @param type
     *            for which request is performed
     * @param id
     *            of the document
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    void deleteDocument(String type, Integer id, boolean forceRefresh) throws CustomResponseException, DataException {
        DeleteRequest deleteRequest = new DeleteRequest(this.indexBase + "_" + type);
        deleteRequest.id(String.valueOf(id));
        if (forceRefresh) {
            deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        }

        try {
            highLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (ResponseException e) {
            handleResponseException(e);
        }  catch (IOException e) {
            throw new DataException(e);
        }
    }

    /**
     * Enable sorting by text field.
     *
     * @param field
     *            as String
     * @param mappingType
     *            as String
     */
    public void enableSortingByTextField(String field, String mappingType) throws IOException, CustomResponseException {
        String query = "{\n \"properties\": {\n\"" + field + "\": {\n" + "      \"type\": \"text\",\n"
                + "      \"fielddata\": true,\n" + "      \"fields\": {\n" + "        \"raw\": {\n"
                + "          \"type\":  \"text\",\n" + "          \"index\": false}\n" + "    }\n" + "  }}}";
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Request request = new Request(HttpMethod.PUT,
                "/" + this.getIndexBase() + "_" + mappingType + "/_mappings");
        request.setEntity(entity);
        Response indexResponse = client.performRequest(request);
        processStatusCode(indexResponse.getStatusLine());
    }

    private BulkRequest prepareBulkRequest(String type, Map<Integer, Map<String, Object>> documentsToIndex) {
        BulkRequest bulkRequest = new BulkRequest();

        for (Map.Entry<Integer, Map<String, Object>> entry : documentsToIndex.entrySet()) {
            IndexRequest indexRequest = new IndexRequest(this.indexBase + "_" + type);
            indexRequest.id(String.valueOf(entry.getKey()));
            bulkRequest.add(indexRequest.source(entry.getValue()));
        }

        return bulkRequest;
    }
}
