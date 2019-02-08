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

import java.util.Map;
import java.util.Objects;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
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
    private static QueryBuilder query = QueryBuilders.matchAllQuery();
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
        AggregationBuilder aggregation = AggregationBuilders.sum("amount").field("amount");

        await().untilAsserted(() -> assertTrue("Incorrect result - amount doesn't match to given number!",
            ((Sum) searcher.aggregateDocuments(query, aggregation).get("amount")).getValue() == 8.0));

        /*QueryBuilder matchQuery = QueryBuilders.matchQuery("type", "");
        await().untilAsserted(() -> assertTrue("Incorrect result - amount doesn't match to given number!",
            ((Sum) searcher.aggregateDocuments(matchQuery, aggregation).get("amount")).getValue() == 6.0));*/
    }

    @Test
    public void shouldAggregateDocumentsAccordingToQueryCount() {
        AggregationBuilder aggregation = AggregationBuilders.count("count").field("amount");
        await().untilAsserted(() -> assertEquals("Incorrect result - count doesn't match to given number!", 4,
            ((ValueCount) searcher.aggregateDocuments(query, aggregation).get("count")).getValue()));

        /*QueryBuilder matchQuery = QueryBuilders.matchQuery("type", "");
        await().untilAsserted(() -> assertEquals("Incorrect result - count doesn't match to given number!", 3,
            ((ValueCount) searcher.aggregateDocuments(matchQuery, aggregation).get("count")).getValue()));*/
    }

    @Test
    public void shouldFindDocumentById() {
        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given plain text!", 1,
            getIdFromJSONObject(searcher.findDocument(1)).longValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - title doesn't match to given plain text!",
            "Batch1", searcher.findDocument(1).get("title")));
    }

    @Test
    public void shouldFindDocumentByQuery() {
        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given number!", 1,
            getIdFromJSONObject(searcher.findDocument(query)).intValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - title doesn't match to given plain text!",
            "Batch1", searcher.findDocument(query).get("title")));

        QueryBuilder queryMatch = QueryBuilders.matchQuery("title", "Batch1");
        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given plain text!", 1,
            getIdFromJSONObject(searcher.findDocument(queryMatch)).intValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - title doesn't match to given plain text!",
            "Batch1", searcher.findDocument(queryMatch).get("title")));

        QueryBuilder queryNonexistent = QueryBuilders.matchQuery("title", "Nonexistent");
        await().untilAsserted(() -> assertEquals("Incorrect result - id has another value than null!",
            Integer.valueOf(0), getIdFromJSONObject(searcher.findDocument(queryNonexistent))));
    }

    @Test
    public void shouldFindDocumentByQueryAndSort() {
        SortBuilder sort = new FieldSortBuilder("title").order(SortOrder.DESC);
        await().untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given number!", 2,
            getIdFromJSONObject(searcher.findDocument(query, sort)).intValue()));

        await().untilAsserted(() -> assertEquals("Incorrect result - title doesn't match to given plain text!", "Sort",
            searcher.findDocument(query, sort).get("title")));
    }

    @Test
    public void shouldFindDocumentsByQuery() {
        await().ignoreExceptions()
                .untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 1,
                    getIdFromJSONObject(searcher.findDocuments(query).get(0)).intValue()));

        await().ignoreExceptions()
                .untilAsserted(() -> assertEquals("Incorrect result - size doesn't match to given int value!", 4,
                    searcher.findDocuments(query).size()));

        QueryBuilder queryMatch = QueryBuilders.matchQuery("title", "Batch1");
        await().ignoreExceptions()
                .untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 1,
                    getIdFromJSONObject(searcher.findDocuments(queryMatch).get(0)).intValue()));

        await().ignoreExceptions()
                .untilAsserted(() -> assertEquals("Incorrect result - size doesn't match to given int value!", 1,
                    searcher.findDocuments(queryMatch).size()));

        QueryBuilder queryNonexistent = QueryBuilders.matchQuery("title", "Nonexistent");
        await().ignoreExceptions().untilAsserted(() -> assertEquals("Incorrect result - size is bigger than 0!", 0,
            searcher.findDocuments(queryNonexistent).size()));
    }

    @Test
    public void shouldFindDocumentsByQueryAndSort() {
        SortBuilder sort = new FieldSortBuilder("title").order(SortOrder.DESC);
        await().ignoreExceptions()
                .untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 2,
                    getIdFromJSONObject(searcher.findDocuments(query, sort).get(0)).intValue()));
    }

    @Test
    public void shouldFindDocumentsByQueryAndPagination() {
        await().ignoreExceptions()
                .untilAsserted(() -> assertEquals("Incorrect result - id doesn't match to given int values!", 2,
                    getIdFromJSONObject(searcher.findDocuments(query, 1, 2).get(0)).intValue()));
    }

    @Test
    public void shouldFindDocumentsByQuerySortAndPagination() {
        SortBuilder sort = new FieldSortBuilder("title").order(SortOrder.DESC);

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
    private static Integer getIdFromJSONObject(Map<String, Object> jsonObject) {
        if (jsonObject.containsKey("id") && Objects.nonNull(jsonObject.get("id"))) {
            return Integer.valueOf((String) jsonObject.get("id"));
        }
        return 0;
    }
}
