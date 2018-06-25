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

import static org.junit.Assert.assertTrue;

import javax.json.JsonObject;

import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.MockEntity;
import org.kitodo.data.elasticsearch.search.Searcher;

/**
 * Test class for IndexRestClient.
 */
public class IndexRestClientIT {

    private static IndexRestClient restClient;
    private static Node node;
    private static String testIndexName;
    private static Searcher searcher = new Searcher("indexer");

    @BeforeClass
    public static void startElasticSearch() throws Exception {
        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        restClient = initializeRestClient();

        node = MockEntity.prepareNode();
        node.start();
    }

    @AfterClass
    public static void stopElasticSearch() throws Exception {
        node.close();
    }

    @Before
    public void createIndex() throws Exception {
        restClient.createIndex();
    }

    @After
    public void deleteIndex() throws Exception {
        restClient.deleteIndex();
    }

    @Test
    public void shouldAddDocument() throws Exception {
        JsonObject response = searcher.findDocument(1);
        assertTrue("Document exists!", !isFound(response.toString()));

        restClient.addDocument(MockEntity.createEntities().get(1), 1, false);

        response = searcher.findDocument(1);
        assertTrue("Add of document has failed!", isFound(response.toString()));
    }

    @Test
    public void shouldAddType() throws Exception {
        JsonObject response = searcher.findDocument(1);
        assertTrue("Document exists!", !isFound(response.toString()));
        response = searcher.findDocument(2);
        assertTrue("Document exists!", !isFound(response.toString()));

        restClient.addType(MockEntity.createEntities());

        response = searcher.findDocument(1);
        assertTrue("Add of type has failed - document id 1!", isFound(response.toString()));
        response = searcher.findDocument(2);
        assertTrue("Add of type has failed - document id 2!", isFound(response.toString()));

    }

    @Test
    public void shouldDeleteDocument() throws Exception {
        restClient.addType(MockEntity.createEntities());

        JsonObject response = searcher.findDocument(1);
        assertTrue("Document doesn't exist!", isFound(response.toString()));

        restClient.deleteDocument(1, false);
        response = searcher.findDocument(1);
        assertTrue("Delete of document has failed!", !isFound(response.toString()));

        // remove even if document doesn't exist should be possible
        restClient.deleteDocument(100, false);
        response = searcher.findDocument(100);
        assertTrue("Delete of document has failed!", !isFound(response.toString()));
    }

    private static IndexRestClient initializeRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndex(testIndexName);
        restClient.setType("indexer");
        return restClient;
    }

    private static boolean isFound(String response) {
        return response.contains("found");
    }
}
