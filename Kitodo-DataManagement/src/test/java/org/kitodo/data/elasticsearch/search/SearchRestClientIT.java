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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
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
 * Test class for SearchRestClient.
 */
public class SearchRestClientIT {

    private static Node node;
    private static SearchRestClient searchRestClient;
    private static String testIndexName;
    private static String port;
    private static final String HTTP_TRANSPORT_PORT = "9305";

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void prepareIndex() throws Exception {
        final String nodeName = "searchernode";
        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        port = ConfigMain.getParameter("elasticsearch.port", "9205");
        searchRestClient = initializeSearchRestClient();

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
        searchRestClient.createIndex();
        IndexRestClient indexRestClient = initializeIndexRestClient();
        indexRestClient.addDocument(MockEntity.createEntities().get(1), 1, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(2), 2, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(3), 3, false);
        indexRestClient.addDocument(MockEntity.createEntities().get(4), 4, false);
    }

    @After
    public void deleteIndex() throws Exception {
        searchRestClient.deleteIndex();
    }

    @Test
    public void shouldCountDocuments() throws Exception {
        Thread.sleep(2000);
        String query = "{\n\"match_all\" : {}\n}";
        String result = searchRestClient.countDocuments(query);

        boolean condition = result.contains("\"count\" : 4");
        assertTrue("Count of documents has failed!", condition);
    }

    @Test
    public void shouldGetDocumentById() throws Exception {
        String result = searchRestClient.getDocument(1);

        boolean condition = result.contains("\"found\" : true");
        assertTrue("Get of document has failed!", condition);
    }

    @Test
    public void shouldGetDocumentByQuery() throws Exception {
        Thread.sleep(2000);
        String query = "{\n\"match_all\" : {}\n}";
        String result = searchRestClient.getDocument(query, null, null, null);

        boolean condition = result.contains("\"total\" : 4");
        assertTrue("Get of document has failed!", condition);
    }

    private static SearchRestClient initializeSearchRestClient() {
        SearchRestClient restClient = SearchRestClient.getInstance();
        restClient.setIndex(testIndexName);
        restClient.setType("testsearchclient");
        return restClient;
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndex(testIndexName);
        restClient.setType("testsearchclient");
        return restClient;
    }
}
