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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.ExtendedNode;
import org.kitodo.data.elasticsearch.MockEntity;

/**
 * Test class for IndexRestClient.
 */
public class IndexRestClientIT {

    private static IndexRestClient restClient;
    private static Node node;
    private static String testIndexName;
    private static String port;
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
        IndexRestClient restClient = initializeRestClient();
        assertTrue("Add of document has failed!", restClient.addDocument(MockEntity.createEntities().get(1), 1));
    }

    @Test
    public void shouldAddType() throws Exception {
        String result = restClient.addType(MockEntity.createEntities());

        boolean created = result.contains("HTTP/1.1 201 Created");
        assertTrue("Add of type has failed - document id 1!", created);

        created = result.contains("HTTP/1.1 201 Created");
        assertTrue("Add of type has failed - document id 2!", created);

        result = restClient.addType(MockEntity.createEntities());

        boolean ok = result.contains("HTTP/1.1 200 OK");
        assertTrue("Update of type has failed - document id 1!", ok);

        ok = result.contains("HTTP/1.1 200 OK");
        assertTrue("Update of type has failed - document id 2!", ok);
    }

    @Test
    public void shouldDeleteDocument() throws Exception {
        restClient.addType(MockEntity.createEntities());
        assertTrue("Delete of document has failed!", restClient.deleteDocument(1));

        assertTrue("Delete of document has failed!", restClient.deleteDocument(100));
    }

    private static IndexRestClient initializeRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndex(testIndexName);
        restClient.setType("indexer");
        return restClient;
    }
}
