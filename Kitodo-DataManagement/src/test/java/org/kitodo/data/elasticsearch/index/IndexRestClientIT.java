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

import java.util.Map;

import static org.junit.Assert.assertTrue;

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
        Map<String, Object> response = searcher.findDocument(1);
        assertTrue("Document exists!", !isFound(response));

        restClient.addDocument(MockEntity.createEntities().get(1), 1, false);

        response = searcher.findDocument(1);
        assertTrue("Add of document has failed!", isFound(response));
    }

    @Test
    public void shouldAddTypeAsync() throws Exception {
        Map<String, Object> response = searcher.findDocument(1);
        assertTrue("Document exists!", !isFound(response));
        response = searcher.findDocument(2);
        assertTrue("Document exists!", !isFound(response));

        restClient.addTypeSync(MockEntity.createEntities());

        response = searcher.findDocument(1);
        assertTrue("Add of type has failed - document id 1!", isFound(response));
        response = searcher.findDocument(2);
        assertTrue("Add of type has failed - document id 2!", isFound(response));

    }

    @Test
    public void shouldAddTypeSync() throws Exception {
        Map<String, Object> response = searcher.findDocument(1);
        assertTrue("Document exists!", !isFound(response));
        response = searcher.findDocument(2);
        assertTrue("Document exists!", !isFound(response));

        restClient.addTypeAsync(MockEntity.createEntities());

        response = searcher.findDocument(1);
        assertTrue("Add of type has failed - document id 1!", isFound(response));
        response = searcher.findDocument(2);
        assertTrue("Add of type has failed - document id 2!", isFound(response));

    }

    @Test
    public void shouldDeleteDocument() throws Exception {
        restClient.addTypeSync(MockEntity.createEntities());

        Map<String, Object> response = searcher.findDocument(1);
        assertTrue("Document doesn't exist!", isFound(response));

        restClient.deleteDocument(1, false);
        response = searcher.findDocument(1);
        assertTrue("Delete of document has failed!", !isFound(response));

        // remove even if document doesn't exist should be possible
        restClient.deleteDocument(100, false);
        response = searcher.findDocument(100);
        assertTrue("Delete of document has failed!", !isFound(response));
    }

    @Test
    public void shouldGetServerInfo() throws Exception {
        System.out.println(restClient.getServerInformation());
        System.out.println(restClient.getServerInfo());
    }

    private static IndexRestClient initializeRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndex(testIndexName);
        restClient.setType("indexer");
        return restClient;
    }

    private static boolean isFound(Map<String, Object> response) {
        return !response.isEmpty();
    }
}
