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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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

    @BeforeClass
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

    @AfterClass
    public static void cleanIndex() throws Exception {
        searchRestClient.deleteIndex(testTypeName);
        node.close();
    }

    @Test
    public void shouldCountDocuments() {
        await().untilAsserted(() -> assertTrue("Count of documents has failed!",
            searchRestClient.countDocuments(testTypeName, query).contains("\"count\" : 4")));
    }

    @Test
    public void shouldGetDocumentById() {
        await().untilAsserted(() -> assertFalse("Get of document has failed - source is empty!",
            searchRestClient.getDocument(testTypeName, 1).isEmpty()));

        await().untilAsserted(() -> assertEquals("Get of document has failed - id is incorrect!", 1,
                Integer.parseInt((String) searchRestClient.getDocument(testTypeName, 1).get("id"))));
    }

    @Test
    public void shouldGetDocumentByQuery() {
        await().untilAsserted(() -> assertEquals("Get of document has failed!", 4,
            searchRestClient.getDocument(testTypeName, query, null, null, null).getHits().length));
    }

    @Test
    public void shouldGetDocumentByQueryWithSize() {
        await().untilAsserted(() -> assertEquals("Get of document has failed!", 3,
            searchRestClient.getDocument(testTypeName, query, null, null, 3).getHits().length));
    }

    @Test
    public void shouldGetDocumentByQueryWithOffsetAndSize() {
        await().untilAsserted(() -> assertEquals("Get of document has failed!", 2,
            searchRestClient.getDocument(testTypeName, query, null, 2, 3).getHits().length));
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
