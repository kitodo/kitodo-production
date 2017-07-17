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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

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
        node = new ExtendedNode(settings, asList(Netty4Plugin.class));
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
        indexRestClient.addDocument(MockEntity.createEntities().get(1), 1);
        indexRestClient.addDocument(MockEntity.createEntities().get(2), 2);
    }

    @After
    public void deleteIndex() throws Exception {
        indexRestClient.deleteIndex();
    }

    @Test
    public void shouldCountDocuments() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"query\" : {\n\"match_all\" : {}\n}\n}";
        Long result = searcher.countDocuments(query);
        Long expected = Long.valueOf("2");
        assertEquals("Amount of documents doesn't match to given number!", expected, result);
    }

    @Test
        public void shouldFindDocumentById() throws Exception {
        Searcher searcher = new Searcher("testsearch");
        SearchResult result = searcher.findDocument(1);

        boolean condition = result.getId().equals(1) == result.getProperties().get("title").equals("Batch1");
        assertTrue("Incorrect result - id or title don't match to given plain text!", condition);
    }

    @Test
    public void shouldFindDocumentByQuery() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        SearchResult result = searcher.findDocument(query);
        Integer id = result.getId();
        assertEquals("Incorrect result - id doesn't match to given number!", 1, id.intValue());
        String title = (String) result.getProperties().get("title");
        assertEquals("Incorrect result - title doesn't match to given plain text!", "Batch1", title);

        query = "{\n\"match\" : {\n\"title\" : \"Batch1\"}\n}";
        result = searcher.findDocument(query);
        id = result.getId();
        assertEquals("Incorrect result - id doesn't match to given plain text!", 1, id.intValue());
        title = (String) result.getProperties().get("title");
        assertEquals("Incorrect result - title doesn't match to given plain text!", "Batch1", title);

        query = "{\n\"match\" : {\n\"title\" : \"Nonexistent\"}\n}";
        result = searcher.findDocument(query);
        id = result.getId();
        assertEquals("Incorrect result - id has another value than null!", null, id);
    }

    @Test
    public void shouldFindDocumentsByQuery() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testsearch");

        String query = "{\n\"match_all\" : {}\n}";
        List<SearchResult> result = searcher.findDocuments(query);
        Integer id = result.get(0).getId();
        assertEquals("Incorrect result - id doesn't match to given int values!", 1, id.intValue());

        int size = result.size();
        assertEquals("Incorrect result - size doesn't match to given int value!", 2, size);

        query = "{\n\"match\" : {\n\"title\" : \"Batch1\"}\n}";
        result = searcher.findDocuments(query);
        id = result.get(0).getId();
        assertEquals("Incorrect result - id doesn't match to given int values!", 1, id.intValue());
        size = result.size();
        assertEquals("Incorrect result - size doesn't match to given int value!", 1, size);

        query = "{\n\"match\" : {\n\"title\" : \"Nonexistent\"}\n}";
        result = searcher.findDocuments(query);
        size = result.size();
        assertEquals("Incorrect result - size is bigger than 0!", 0, size);
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = new IndexRestClient();
        restClient.initiateClient();
        restClient.setIndex(testIndexName);
        restClient.setType("testsearch");
        return restClient;
    }
}
