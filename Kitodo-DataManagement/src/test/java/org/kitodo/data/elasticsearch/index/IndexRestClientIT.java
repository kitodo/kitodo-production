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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.Netty4Plugin;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.ExtendedNode;
import org.kitodo.data.elasticsearch.MockEntity;
import org.kitodo.data.elasticsearch.search.Searcher;

/**
 * Test class for IndexRestClient.
 */
public class IndexRestClientIT {

    private static IndexRestClient restClient;
    private static Node node;
    private static String testIndexName;
    private static String port;
    private static Searcher searcher = new Searcher("indexer");
    private static final String HTTP_TRANSPORT_PORT = "9305";

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void startElasticSearch() throws Exception {
        final String nodeName = "indexernode";
        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        port = ConfigMain.getParameter("elasticsearch.port", "9205");
        restClient = initializeRestClient();

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
        JSONObject response = searcher.findDocument(1);
        assertTrue("Document exists!", !isFound(response.toJSONString()));

        restClient.addDocument(MockEntity.createEntities().get(1), 1);

        response = searcher.findDocument(1);
        assertTrue("Add of document has failed!", isFound(response.toJSONString()));
    }

    @Test
    public void shouldAddType() throws Exception {
        JSONObject response = searcher.findDocument(1);
        assertTrue("Document exists!", !isFound(response.toJSONString()));
        response = searcher.findDocument(2);
        assertTrue("Document exists!", !isFound(response.toJSONString()));

        restClient.addType(MockEntity.createEntities());

        response = searcher.findDocument(1);
        assertTrue("Add of type has failed - document id 1!", isFound(response.toJSONString()));
        response = searcher.findDocument(2);
        assertTrue("Add of type has failed - document id 2!", isFound(response.toJSONString()));

    }

    @Test
    public void shouldDeleteDocument() throws Exception {
        restClient.addType(MockEntity.createEntities());

        JSONObject response = searcher.findDocument(1);
        assertTrue("Document doesn't exist!", isFound(response.toJSONString()));

        restClient.deleteDocument(1);
        response = searcher.findDocument(1);
        assertTrue("Delete of document has failed!", !isFound(response.toJSONString()));

        // remove even if document doesn't exist should be possible
        restClient.deleteDocument(100);
        response = searcher.findDocument(100);
        assertTrue("Delete of document has failed!", !isFound(response.toJSONString()));
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
