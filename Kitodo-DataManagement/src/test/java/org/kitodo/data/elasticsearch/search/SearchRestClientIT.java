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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.elasticsearch.MockEntity;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.index.IndexRestClient;

import static org.junit.Assert.assertTrue;

/**
 * Test class for SearchRestClient.
 */
public class SearchRestClientIT {

    @BeforeClass
    public static void prepareIndex() throws IOException, ResponseException {
        IndexRestClient indexRestClient = initializeIndexRestClient();
        indexRestClient.addDocument(MockEntity.createEntities().get(1), 1);
        indexRestClient.addDocument(MockEntity.createEntities().get(2), 2);
    }

    @AfterClass
    public static void cleanIndex() throws IOException, ResponseException {
        IndexRestClient restClient = initializeIndexRestClient();
        restClient.deleteIndex();
    }

    @Test
    public void shouldCountDocuments() throws Exception {
        Thread.sleep(2000);
        SearchRestClient searchRestClient = initializeSearchRestClient();
        String query = "{\n\"query\" : {\n\"match_all\" : {}\n}\n}";
        String result = searchRestClient.countDocuments(query);

        boolean condition = result.contains("\"count\" : 2");
        assertTrue("Count of documents has failed!", condition);
    }

    @Test
    public void shouldGetDocumentById() throws Exception {
        SearchRestClient searchRestClient = initializeSearchRestClient();
        String result = searchRestClient.getDocument(1);

        boolean condition = result.contains("\"found\" : true");
        assertTrue("Get of document has failed!", condition);
    }

    @Test
    public void shouldGetDocumentByQuery() throws Exception {
        Thread.sleep(2000);
        SearchRestClient searchRestClient = initializeSearchRestClient();
        String query = "{\n\"query\" : {\n\"match_all\" : {}\n}\n}";
        String result = searchRestClient.getDocument(query);

        boolean condition = result.contains("\"total\" : 2");
        assertTrue("Get of document has failed!", condition);
    }

    private static SearchRestClient initializeSearchRestClient() {
        SearchRestClient restClient = new SearchRestClient();
        restClient.initiateClient();
        restClient.setIndex("kitodo");
        restClient.setType("testGet");
        return restClient;
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = new IndexRestClient();
        restClient.initiateClient();
        restClient.setIndex("kitodo");
        restClient.setType("testGet");
        return restClient;
    }
}
