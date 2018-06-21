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

package org.kitodo.data.elasticsearch;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.Netty4Plugin;
import org.kitodo.config.ConfigMain;


/**
 * Mock entities for ElasticSearch classes.
 */
public class MockEntity {

    private static final String HTTP_TRANSPORT_PORT = "9305";
    private static final  String NODE_NAME = "indexernode";

    @SuppressWarnings("unchecked")
    public static Node prepareNode() throws Exception {
        Map settingsMap = MockEntity.prepareNodeSettings();

        removeOldDataDirectories("target/" + NODE_NAME);

        Settings settings = Settings.builder().put(settingsMap).build();
        return new ExtendedNode(settings, Collections.singleton(Netty4Plugin.class));
    }

    private static void removeOldDataDirectories(String dataDirectory) throws Exception {
        File dataDir = new File(dataDirectory);
        if (dataDir.exists()) {
            FileSystemUtils.deleteSubDirectories(dataDir.toPath());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map prepareNodeSettings() {
        String port = ConfigMain.getParameter("elasticsearch.port", "9205");

        Map settingsMap = new HashMap();
        settingsMap.put("node.name", NODE_NAME);
        // create all data directories under Maven build directory
        settingsMap.put("path.conf", "target");
        settingsMap.put("path.data", "target");
        settingsMap.put("path.logs", "target");
        settingsMap.put("path.home", "target");
        // set ports used by Elastic Search to something different than default
        settingsMap.put("http.type", "netty4");
        settingsMap.put("http.port", port);
        settingsMap.put("transport.tcp.port", HTTP_TRANSPORT_PORT);
        settingsMap.put("transport.type", "netty4");
        // disable automatic index creation
        settingsMap.put("action.auto_create_index", "false");
        return settingsMap;
    }

    public static void setUpAwaitility() {
        Awaitility.setDefaultPollInterval(10, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(Duration.TWO_SECONDS);
    }

    public static HashMap<Integer, HttpEntity> createEntities() {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();

        String jsonString = "{\"title\":\"Batch1\",\"type\":\"LOGISTIC\",\"amount\":2,\"processes\":[{\"id\":\"1\"},{\"id\":\"2\"}]}";
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(1, entity);

        jsonString = "{\"title\":\"Sort\",\"type\":\"null\",\"amount\":4,\"processes\":[]}";
        entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(2, entity);

        jsonString = "{\"title\":\"Batch2\",\"type\":\"null\",\"amount\":0,\"processes\":[]}";
        entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(3, entity);

        jsonString = "{\"title\":\"Order\",\"type\":\"null\",\"amount\":2,\"processes\":[]}";
        entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(4, entity);

        return documents;
    }
}
