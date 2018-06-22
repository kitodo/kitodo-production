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

import java.io.File;
import java.util.Collections;
import java.util.Map;

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
