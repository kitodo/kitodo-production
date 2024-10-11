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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.opensearch.node.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
    private static final String testTypeName = "indexer";
    private static final Searcher searcher = new Searcher(testTypeName);
    private static final String DOCUMENT_EXISTS = "Document exists!";

    @BeforeAll
    public static void startElasticSearch() throws Exception {
        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        restClient = initializeRestClient();

        node = MockEntity.prepareNode();
        node.start();
    }

    @AfterAll
    public static void stopElasticSearch() throws Exception {
        node.close();
    }

    @BeforeEach
    public void createIndex() throws Exception {
        restClient.createIndex(null, testTypeName);
    }

    @AfterEach
    public void deleteIndex() throws Exception {
        restClient.deleteIndex(testTypeName);
    }

    @Test
    public void shouldAddDocument() throws Exception {
        Map<String, Object> response = searcher.findDocument(1);
        assertFalse(isFound(response), DOCUMENT_EXISTS);

        restClient.addDocument(testTypeName, MockEntity.createEntities().get(1), 1, false);

        response = searcher.findDocument(1);
        assertTrue(isFound(response), "Add of document has failed!");
    }

    @Test
    public void shouldAddTypeAsync() throws Exception {
        Map<String, Object> response = searcher.findDocument(1);
        assertFalse(isFound(response), DOCUMENT_EXISTS);
        response = searcher.findDocument(2);
        assertFalse(isFound(response), DOCUMENT_EXISTS);

        restClient.addTypeSync(testTypeName, MockEntity.createEntities());

        response = searcher.findDocument(1);
        assertTrue(isFound(response), "Add of type has failed - document id 1!");
        response = searcher.findDocument(2);
        assertTrue(isFound(response), "Add of type has failed - document id 2!");

    }

    @Test
    public void shouldAddTypeSync() throws Exception {
        Map<String, Object> response = searcher.findDocument(1);
        assertFalse(isFound(response), DOCUMENT_EXISTS);
        response = searcher.findDocument(2);
        assertFalse(isFound(response), DOCUMENT_EXISTS);

        restClient.addTypeAsync(testTypeName, MockEntity.createEntities());

        response = searcher.findDocument(1);
        assertTrue(isFound(response), "Add of type has failed - document id 1!");
        response = searcher.findDocument(2);
        assertTrue(isFound(response), "Add of type has failed - document id 2!");

    }

    @Test
    public void shouldDeleteDocument() throws Exception {
        restClient.addTypeSync(testTypeName, MockEntity.createEntities());

        Map<String, Object> response = searcher.findDocument(1);
        assertTrue(isFound(response), "Document doesn't exist!");

        restClient.deleteDocument(testTypeName, 1, false);
        response = searcher.findDocument(1);
        assertFalse(isFound(response), "Delete of document has failed!");

        // remove even if document doesn't exist should be possible
        restClient.deleteDocument(testTypeName, 100, false);
        response = searcher.findDocument(100);
        assertFalse(isFound(response), "Delete of document has failed!");
    }

    @Test
    public void shouldGetServerInfo() throws Exception {
        System.out.println(restClient.getServerInformation());
        System.out.println(restClient.getServerInfo());
    }

    private static IndexRestClient initializeRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndexBase(testIndexName);
        return restClient;
    }

    private static boolean isFound(Map<String, Object> response) {
        return !response.isEmpty();
    }
}
