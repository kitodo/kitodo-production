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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonObject;

import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.Netty4Plugin;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.ExtendedNode;
import org.kitodo.data.elasticsearch.MockEntity;
import org.kitodo.data.elasticsearch.index.IndexRestClient;

/**
 * Test class for Searcher.
 */
public class SearcherIT {

    private static Node node;
    private static IndexRestClient indexRestClient;
    private static String testIndexName;
    private static String port;
    private static final String HTTP_TRANSPORT_PORT = "9305";

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void prepareIndex() throws Exception {
        final String nodeName = "searchernode";
        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        port = ConfigMain.getParameter("elasticsearch.port", "9205");
        indexRestClient = initializeIndexRestClient();

        Map settingsMap = MockEntity.prepareNodeSettings(port, HTTP_TRANSPORT_PORT, nodeName);

        removeOldDataDirectories("target/" + nodeName);

        Settings settings = Settings.builder().put(settingsMap).build();
        node = new ExtendedNode(settings, Collections.singleton(Netty4Plugin.class));
        node.start();
    }

    private static void removeOldDataDirectories(String dataDirectory) throws Exception {
        File dataDir = new File(dataDirectory);
        if (dataDir.exists()) {
            FileSystemUtils.deleteSubDirectories(dataDir.toPath());
        }
    }

    @AfterClass
    public static void cleanIndex() throws Exception {
        node.close();
    }

    @Before
    public void createIndex() throws Exception {
        indexRestClient.createIndex();
        indexRestClient.addDocument(MockEntity.createEntities().get(1), 1, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(2), 2, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(3), 3, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(4), 4, false);
        indexRestClient.enableSortingByTextField("testsearch", "title");
    }

    @After
    public void deleteIndex() throws Exception {
        indexRestClient.deleteIndex();
    }

    @Test
    public void shouldCountDocuments() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        Long result = searcher.countDocuments(query);
        Long expected = Long.valueOf("4");
        assertEquals("Amount of documents doesn't match to given number!", expected, result);
    }

    @Test
    public void shouldAggregateDocumentsAccordingToQuery() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        String aggregation = "{\"amount\" : {\"sum\" : { \"field\" : \"amount\" }}}";
        JsonObject result = searcher.aggregateDocuments(query, aggregation);
        JsonObject jsonObject = result.getJsonObject("amount");
        Double amount = jsonObject.getJsonNumber("value").doubleValue();
        assertEquals("Incorrect result - amount doesn't match to given number!", Double.valueOf(8.0), amount);

        query = "{\n\"match\" : {\n\"type\" : \"null\"}\n}";
        result = searcher.aggregateDocuments(query, aggregation);
        jsonObject = result.getJsonObject("amount");
        amount = jsonObject.getJsonNumber("value").doubleValue();
        assertEquals("Incorrect result - amount doesn't match to given number!", Double.valueOf(6.0), amount);

        query = "{\n\"match_all\" : {}\n}";
        aggregation = "{\"count\" : {\"value_count\" : { \"field\" : \"amount\" }}}";
        result = searcher.aggregateDocuments(query, aggregation);
        jsonObject = result.getJsonObject("count");
        Long count = jsonObject.getJsonNumber("value").longValue();
        assertEquals("Incorrect result - count doesn't match to given number!", Long.valueOf(4), count);

        query = "{\n\"match\" : {\n\"type\" : \"null\"}\n}";
        result = searcher.aggregateDocuments(query, aggregation);
        jsonObject = result.getJsonObject("count");
        count = jsonObject.getJsonNumber("value").longValue();
        assertEquals("Incorrect result - count doesn't match to given number!", Long.valueOf(3), count);
    }

    @Test
    public void shouldFindDocumentById() throws Exception {
        Searcher searcher = new Searcher("testsearch");
        JsonObject result = searcher.findDocument(1);
        JsonObject jsonObject = result.getJsonObject("_source");
        String actual = jsonObject.getString("title");

        boolean condition = getIdFromJSONObject(result).equals(1);
        assertTrue("Incorrect result - id doesn't match to given plain text!", condition);

        condition = actual.equals("Batch1");
        assertTrue("Incorrect result - title doesn't match to given plain text!", condition);
    }

    @Test
    public void shouldFindDocumentByQuery() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        JsonObject result = searcher.findDocument(query);
        JsonObject jsonObject = result.getJsonObject("_source");
        Integer id = getIdFromJSONObject(result);
        assertEquals("Incorrect result - id doesn't match to given number!", 1, id.intValue());
        String title = jsonObject.getString("title");
        assertEquals("Incorrect result - title doesn't match to given plain text!", "Batch1", title);

        query = "{\n\"match\" : {\n\"title\" : \"Batch1\"}\n}";
        result = searcher.findDocument(query);
        jsonObject = result.getJsonObject("_source");
        id = getIdFromJSONObject(result);
        assertEquals("Incorrect result - id doesn't match to given plain text!", 1, id.intValue());
        title = jsonObject.getString("title");
        assertEquals("Incorrect result - title doesn't match to given plain text!", "Batch1", title);

        query = "{\n\"match\" : {\n\"title\" : \"Nonexistent\"}\n}";
        result = searcher.findDocument(query);
        id = getIdFromJSONObject(result);
        assertEquals("Incorrect result - id has another value than null!", Integer.valueOf(0), id);
    }

    @Test
    public void shouldFindDocumentByQueryAndSort() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        String sort = "{\"title\" : {\"order\" : \"desc\"}}";
        JsonObject result = searcher.findDocument(query, sort);
        JsonObject jsonObject = result.getJsonObject("_source");
        Integer id = getIdFromJSONObject(result);
        assertEquals("Incorrect result - id doesn't match to given number!", 2, id.intValue());
        String title = jsonObject.getString("title");
        assertEquals("Incorrect result - title doesn't match to given plain text!", "Sort", title);
    }

    @Test
    public void shouldFindDocumentsByQuery() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        List<JsonObject> result = searcher.findDocuments(query);
        Integer id = getIdFromJSONObject(result.get(0));
        assertEquals("Incorrect result - id doesn't match to given int values!", 1, id.intValue());

        int size = result.size();
        assertEquals("Incorrect result - size doesn't match to given int value!", 4, size);

        query = "{\n\"match\" : {\n\"title\" : \"Batch1\"}\n}";
        result = searcher.findDocuments(query);
        id = getIdFromJSONObject(result.get(0));
        assertEquals("Incorrect result - id doesn't match to given int values!", 1, id.intValue());
        size = result.size();
        assertEquals("Incorrect result - size doesn't match to given int value!", 1, size);

        query = "{\n\"match\" : {\n\"title\" : \"Nonexistent\"}\n}";
        result = searcher.findDocuments(query);
        size = result.size();
        assertEquals("Incorrect result - size is bigger than 0!", 0, size);
    }

    @Test
    public void shouldFindDocumentsByQueryAndSort() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        String sort = "{\"title\" : {\"order\" : \"desc\"}}";
        List<JsonObject> result = searcher.findDocuments(query, sort);
        Integer id = getIdFromJSONObject(result.get(0));
        assertEquals("Incorrect result - id doesn't match to given int values!", 2, id.intValue());
    }

    @Test
    public void shouldFindDocumentsByQueryAndPagination() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        Integer offset = 1;
        Integer size = 2;
        List<JsonObject> result = searcher.findDocuments(query, offset, size);
        Integer id = getIdFromJSONObject(result.get(0));
        assertEquals("Incorrect result - id doesn't match to given int values!", 2, id.intValue());
    }

    @Test
    public void shouldFindDocumentsByQuerySortAndPagination() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        String sort = "{\"title\" : {\"order\" : \"desc\"}}";
        Integer offset = 1;
        Integer size = 2;
        List<JsonObject> result = searcher.findDocuments(query, sort, offset, size);
        Integer id = getIdFromJSONObject(result.get(0));
        assertEquals("Incorrect result - id doesn't match to given int values!", 4, id.intValue());

        id = getIdFromJSONObject(result.get(1));
        assertEquals("Incorrect result - id doesn't match to given int values!", 3, id.intValue());
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
