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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.MockEntity;
import org.kitodo.data.elasticsearch.index.IndexRestClient;

/**
 * Test class for SearchRestClient.
 */
public class SearchRestClientIT {

    private static Node node;
    private static SearchRestClient searchRestClient;
    private static String testIndexName;
    private static final String testTypeName = "testsearchclient";
    private static final QueryBuilder query = QueryBuilders.matchAllQuery();

    @BeforeAll
    public static void prepareIndex() throws Exception {
        MockEntity.setUpAwaitility();

        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        searchRestClient = initializeSearchRestClient();

        node = MockEntity.prepareNode();
        node.start();

        searchRestClient.createIndex(null, testTypeName);

        IndexRestClient indexRestClient = initializeIndexRestClient();
        indexRestClient.addDocument(testTypeName, MockEntity.createEntities().get(1), 1, false);
        indexRestClient.addDocument(testTypeName, MockEntity.createEntities().get(2), 2, false);
        indexRestClient.addDocument(testTypeName, MockEntity.createEntities().get(3), 3, false);
        indexRestClient.addDocument(testTypeName, MockEntity.createEntities().get(4), 4, false);
    }

    @AfterAll
    public static void cleanIndex() throws Exception {
        searchRestClient.deleteIndex(testTypeName);
        node.close();
    }

    @Test
    public void shouldCountDocuments() {
        await().untilAsserted(() ->
            assertTrue(searchRestClient.countDocuments(testTypeName, query).contains("\"count\" : 4"), "Count of documents has failed!"));
    }

    @Test
    public void shouldGetDocumentById() {
        await().untilAsserted(() ->
            assertFalse(searchRestClient.getDocument(testTypeName, 1).isEmpty(), "Get of document has failed - source is empty!"));

        await().untilAsserted(() ->
            assertEquals(1, Integer.parseInt((String) searchRestClient.getDocument(testTypeName, 1).get("id")), "Get of document has failed - id is incorrect!"));
    }

    @Test
    public void shouldGetDocumentByQuery() {
        await().untilAsserted(() ->
            assertEquals(4, searchRestClient.getDocument(testTypeName, query, null, null, null).getHits().length, "Get of document has failed!"));
    }

    @Test
    public void shouldGetDocumentByQueryWithSize() {
        await().untilAsserted(() ->
            assertEquals(3, searchRestClient.getDocument(testTypeName, query, null, null, 3).getHits().length, "Get of document has failed!"));
    }

    @Test
    public void shouldGetDocumentByQueryWithOffsetAndSize() {
        await().untilAsserted(() ->
            assertEquals(2, searchRestClient.getDocument(testTypeName, query, null, 2, 3).getHits().length, "Get of document has failed!"));
    }

    private static SearchRestClient initializeSearchRestClient() {
        SearchRestClient restClient = SearchRestClient.getInstance();
        restClient.setIndexBase(testIndexName);
        return restClient;
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndexBase(testIndexName);
        return restClient;
    }
}
