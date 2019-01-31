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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    public static HashMap<Integer, Map<String, Object>> createEntities() {
        HashMap<Integer, Map<String, Object>> documents = new HashMap<>();

        Map<String, Object> firstBatchMap = new HashMap<>();
        firstBatchMap.put("title", "Batch1");
        firstBatchMap.put("type", "LOGISTIC");
        firstBatchMap.put("amount", 2);
        Map<String, Object> firstBatchProcesses = new HashMap<>();
        firstBatchProcesses.put("id", Arrays.asList(1, 2));
        firstBatchMap.put("processes", firstBatchProcesses);
        documents.put(1, firstBatchMap);

        Map<String, Object> secondBatchMap = new HashMap<>();
        secondBatchMap.put("title", "Sort");
        secondBatchMap.put("type", "");
        secondBatchMap.put("amount", 4);
        Map<String, Object> secondBatchMapProcesses = new HashMap<>();
        secondBatchMapProcesses.put("id", Collections.emptyList());
        secondBatchMap.put("processes", secondBatchMapProcesses);
        documents.put(2, secondBatchMap);

        Map<String, Object> thirdBatchMap = new HashMap<>();
        thirdBatchMap.put("title", "Batch2");
        thirdBatchMap.put("type", "");
        thirdBatchMap.put("amount", 0);
        Map<String, Object> thirdBatchMapProcesses = new HashMap<>();
        thirdBatchMapProcesses.put("id", Collections.emptyList());
        thirdBatchMap.put("processes", thirdBatchMapProcesses);
        documents.put(3, thirdBatchMap);

        Map<String, Object> fourthBatchMap = new HashMap<>();
        fourthBatchMap.put("title", "Order");
        fourthBatchMap.put("type", "");
        fourthBatchMap.put("amount", 2);
        Map<String, Object> fourthBatchMapProcesses = new HashMap<>();
        fourthBatchMapProcesses.put("id", Collections.emptyList());
        fourthBatchMap.put("processes", fourthBatchMapProcesses);
        documents.put(4, fourthBatchMap);

        return documents;
    }
}
