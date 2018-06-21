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
import static org.junit.Assert.assertTrue;

import java.util.Objects;

import javax.json.JsonObject;

import org.elasticsearch.node.Node;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.MockEntity;
import org.kitodo.data.elasticsearch.index.IndexRestClient;

/**
 * Test class for Searcher.
 */
public class SearcherIT {

    private static Node node;
    private static IndexRestClient indexRestClient;
    private static String testIndexName;
    private static String query = "{\n\"match_all\" : {}\n}";
    private static Searcher searcher = new Searcher("testsearch");

    @BeforeClass
    public static void prepareIndex() throws Exception {
        MockEntity.setUpAwaitility();

        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        indexRestClient = initializeIndexRestClient();


        node = MockEntity.prepareNode();
        node.start();

        indexRestClient.createIndex();
        indexRestClient.addDocument(MockEntity.createEntities().get(1), 1, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(2), 2, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(3), 3, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(4), 4, false);
        indexRestClient.enableSortingByTextField("testsearch", "title");
    }

    @AfterClass
    public static void cleanIndex() throws Exception {
        indexRestClient.deleteIndex();
        node.close();
    }

    @Test
    public void shouldCountDocuments() {
        await().untilAsserted(() -> assertEquals("Amount of documents doesn't match to given number!", 4,
            searcher.countDocuments(query).longValue()));
    }

    @Test
    public void shouldAggregateDocumentsAccordingToQueryAmount() {
        String aggregation = "{\"amount\" : {\"sum\" : { \"field\" : \"amount\" }}}";
        await().untilAsserted(() -> assertTrue("Incorrect result - amount doesn't match to given number!",
            searcher.aggregateDocuments(query, aggregation).getJsonObject("amount").getJsonNumber("value").doubleValue() == 8.0));

        String query = "{\n\"match\" : {\n\"type\" : \"null\"}\n}";
        await().untilAsserted(() -> assertTrue("Incorrect result - amount doesn't match to given number!",
            searcher.aggregateDocuments(query, aggregation).getJsonObject("amount").getJsonNumber("value").doubleValue() == 6.0));
    }

    @Test
    public void shouldAggregateDocumentsAccordingToQueryCount() {
        String aggregation = "{\"count\" : {\"value_count\" : { \"field\" : \"amount\" }}}";
        await().untilAsserted(
            () -> assertEquals("Incorrect result - count doesn't match to given number!", 4, searcher.aggregateDocuments(SearcherIT.query, aggregation).getJsonObject("count").getJsonNumber("value").longValue()));

        String query = "{\n\"match\" : {\n\"type\" : \"null\"}\n}";
        await().untilAsserted(
            () -> assertEquals("Incorrect result - count doesn't match to given number!", 3, searcher.aggregateDocuments(query, aggregation).getJsonObject("count").getJsonNumber("value").longValue()));
    }

    @Test
    public void shouldFindDocumentById() {
        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given plain text!", 1,
            getIdFromJSONObject(searcher.findDocument(1)).longValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - title doesn't match to given plain text!",
            "Batch1", searcher.findDocument(1).getJsonObject("_source").getString("title")));
    }

    @Test
    public void shouldFindDocumentByQuery() {
        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given number!", 1,
            getIdFromJSONObject(searcher.findDocument(query)).intValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - title doesn't match to given plain text!",
            "Batch1", searcher.findDocument(query).getJsonObject("_source").getString("title")));

        String query = "{\n\"match\" : {\n\"title\" : \"Batch1\"}\n}";
        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given plain text!", 1,
            getIdFromJSONObject(searcher.findDocument(query)).intValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - title doesn't match to given plain text!",
            "Batch1", searcher.findDocument(query).getJsonObject("_source").getString("title")));

        String queryNonexistent = "{\n\"match\" : {\n\"title\" : \"Nonexistent\"}\n}";
        await().untilAsserted(() -> assertEquals("Incorrect result - id has another value than null!",
            Integer.valueOf(0), getIdFromJSONObject(searcher.findDocument(queryNonexistent))));
    }

    @Test
    public void shouldFindDocumentByQueryAndSort() {
        String sort = "{\"title\" : {\"order\" : \"desc\"}}";
        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given number!", 2,
            getIdFromJSONObject(searcher.findDocument(query, sort)).intValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - title doesn't match to given plain text!", "Sort",
            searcher.findDocument(query, sort).getJsonObject("_source").getString("title")));
    }

    @Test
    public void shouldFindDocumentsByQuery() {
        await().ignoreExceptions().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 1,
            getIdFromJSONObject(searcher.findDocuments(query).get(0)).intValue()));

        await().ignoreExceptions().untilAsserted(() -> assertEquals("Incorrect result - size doesn't match to given int value!", 4,
            searcher.findDocuments(query).size()));

        String queryMatch = "{\n\"match\" : {\n\"title\" : \"Batch1\"}\n}";
        await().ignoreExceptions().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 1,
            getIdFromJSONObject(searcher.findDocuments(queryMatch).get(0)).intValue()));

        await().ignoreExceptions().untilAsserted(() -> assertEquals("Incorrect result - size doesn't match to given int value!", 1,
            searcher.findDocuments(queryMatch).size()));

        String queryNonexistent = "{\n\"match\" : {\n\"title\" : \"Nonexistent\"}\n}";
        await().ignoreExceptions().untilAsserted(() -> assertEquals("Incorrect result - size is bigger than 0!", 0,
            searcher.findDocuments(queryNonexistent).size()));
    }

    @Test
    public void shouldFindDocumentsByQueryAndSort() {
        String sort = "{\"title\" : {\"order\" : \"desc\"}}";

        await().ignoreExceptions().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 2,
            getIdFromJSONObject(searcher.findDocuments(query, sort).get(0)).intValue()));
    }

    @Test
    public void shouldFindDocumentsByQueryAndPagination() {
        await().ignoreExceptions().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 2,
            getIdFromJSONObject(searcher.findDocuments(query, 1, 2).get(0)).intValue()));
    }

    @Test
    public void shouldFindDocumentsByQuerySortAndPagination() {
        String sort = "{\"title\" : {\"order\" : \"desc\"}}";

        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 4,
            getIdFromJSONObject(searcher.findDocuments(query, sort, 1, 2).get(0)).intValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 3,
            getIdFromJSONObject(searcher.findDocuments(query, sort, 1, 2).get(1)).intValue()));
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndex(testIndexName);
        restClient.setType("testsearch");
        return restClient;
    }

    /**
     * Get id from JSON object returned form ElasticSearch.
     * 
     * @param jsonObject
     *            returned form ElasticSearch
     * @return id as Integer
     */
    private static Integer getIdFromJSONObject(JsonObject jsonObject) {
        if (jsonObject.containsKey("_id") && Objects.nonNull(jsonObject.getString("_id"))) {
            return Integer.valueOf(jsonObject.getString("_id"));
        }
        return 0;
    }
}
