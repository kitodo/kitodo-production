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

import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for RestClientImplementation.
 */
public class RestClientImplementationTest {

    private static RestClientImplementation initializeRestClient() {
        RestClientImplementation restClient = new RestClientImplementation();

        restClient.initiateClient("localhost", 9200, "http");
        restClient.setIndex("kitodo");
        restClient.setType("test");

        return restClient;
    }

    private static HashMap<Integer, HttpEntity> createEntities() {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();

        String jsonString = "{\"title\":\"Batch1\",\"type\":\"LOGISTIC\",\"processes\":[{\"id\":\"1\"},{\"id\":\"2\"}]}";
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(1, entity);

        jsonString = "{\"title\":\"Batch2\",\"type\":\"null\",\"processes\":[]}";
        entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(2, entity);

        return documents;
    }

    @Test
    public void shouldAddDocument() throws Exception {
        RestClientImplementation restClient = initializeRestClient();
        String result = restClient.addDocument(createEntities().get(1), 1);

        boolean created = result.contains("\"created\":true");
        //if document already exists it is updated and in that case check if update successful
        boolean ok = result.contains("\"successful\":1");
        boolean condition = created || ok;
        assertTrue("Add of document has failed!", condition);
    }

    @Test
    public void shouldAddType() throws Exception {
        RestClientImplementation restClient = initializeRestClient();
        String result = restClient.addType(createEntities());

        boolean created = result.contains("requestLine=PUT /kitodo/test/1 HTTP/1.1, host=http://localhost:9200, response=HTTP/1.1 201 Created");
        boolean ok = result.contains("requestLine=PUT /kitodo/test/1 HTTP/1.1, host=http://localhost:9200, response=HTTP/1.1 200 OK");
        boolean condition = created || ok;
        assertTrue("Add of type has failed - document id 1!", condition);

        created = result.contains("requestLine=PUT /kitodo/test/2 HTTP/1.1, host=http://localhost:9200, response=HTTP/1.1 201 Created");
        ok = result.contains("requestLine=PUT /kitodo/test/2 HTTP/1.1, host=http://localhost:9200, response=HTTP/1.1 200 OK");
        condition = created || ok;
        assertTrue("Add of type has failed - document id 2!", condition);
    }

    @Test
    public void shouldDeleteDocument() throws Exception {
        RestClientImplementation restClient = initializeRestClient();
        restClient.addType(createEntities());

        String result = restClient.deleteDocument(1);
        boolean condition = result.contains("HTTP/1.1 200 OK");
        assertTrue("Delete of document has failed!", condition);
    }

    @Test
    public void shouldDeleteType() throws Exception {
        RestClientImplementation restClient = initializeRestClient();
        restClient.addType(createEntities());

        String result = restClient.deleteType();
        boolean condition = result.contains("HTTP/1.1 200 OK");
        assertTrue("Delete of type has failed!", condition);
    }
}