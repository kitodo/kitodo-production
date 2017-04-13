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

package org.kitodo;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.index.IndexRestClient;

import java.io.IOException;
import java.util.HashMap;

/**
 * Mock entities.
 */
public class MockEntity {

    public static void cleanIndex() throws IOException, ResponseException {
        IndexRestClient restClient = new IndexRestClient();
        restClient.initiateClient();
        restClient.deleteIndex();
    }

    public static HashMap<Integer, HttpEntity> createBatchEntities() {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();

        String jsonString = "{\"title\":\"Batch1\",\"type\":\"LOGISTIC\",\"processes\":[{\"id\":\"1\"},{\"id\":\"2\"}]}";
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(1, entity);

        jsonString = "{\"title\":\"Batch2\",\"type\":\"null\",\"processes\":[]}";
        entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(2, entity);

        return documents;
    }
}
