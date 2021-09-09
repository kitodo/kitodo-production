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
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


import org.awaitility.Awaitility;
import org.awaitility.Durations;
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
    private static final String NODE_NAME = "indexernode";
    private static final String TITLE = "title";
    private static final String TYPE = "type";
    private static final String AMOUNT = "amount";
    private static final String PROCESSES = "processes";
    private static final String IDENTIFIER = "id";
    private static final String TARGET = "target";

    public static Node prepareNode() throws Exception {
        Settings settings = MockEntity.prepareNodeSettings();
        removeOldDataDirectories("target/" + NODE_NAME);
        Supplier<String> nodeNameSupplier = () -> NODE_NAME;
        return new ExtendedNode(settings, Collections.singleton(Netty4Plugin.class), nodeNameSupplier);
    }

    private static void removeOldDataDirectories(String dataDirectory) throws Exception {
        File dataDir = new File(dataDirectory);
        if (dataDir.exists()) {
            FileSystemUtils.deleteSubDirectories(dataDir.toPath());
        }
    }

    private static Settings prepareNodeSettings() {
        String port = ConfigMain.getParameter("elasticsearch.port", "9205");
        return Settings.builder().put("node.name", NODE_NAME)
                .put("path.data", TARGET)
                .put("path.logs", TARGET)
                .put("path.home", TARGET)
                .put("http.type", "netty4")
                .put("http.port", port)
                .put("transport.tcp.port", HTTP_TRANSPORT_PORT)
                .put("transport.type", "netty4")
                .put("action.auto_create_index", "false").build();
    }

    public static void setUpAwaitility() {
        Awaitility.setDefaultPollInterval(10, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(Durations.TWO_SECONDS);
    }

    public static HashMap<Integer, Map<String, Object>> createEntities() {
        HashMap<Integer, Map<String, Object>> documents = new HashMap<>();

        Map<String, Object> firstBatchMap = new HashMap<>();
        firstBatchMap.put(TITLE, "Batch1");
        firstBatchMap.put(TYPE, "LOGISTIC");
        firstBatchMap.put(AMOUNT, 2);
        Map<String, Object> firstBatchProcesses = new HashMap<>();
        firstBatchProcesses.put(IDENTIFIER, Arrays.asList(1, 2));
        firstBatchMap.put(PROCESSES, firstBatchProcesses);
        documents.put(1, firstBatchMap);

        Map<String, Object> secondBatchMap = new HashMap<>();
        secondBatchMap.put(TITLE, "Sort");
        secondBatchMap.put(TYPE, "");
        secondBatchMap.put(AMOUNT, 4);
        Map<String, Object> secondBatchMapProcesses = new HashMap<>();
        secondBatchMapProcesses.put(IDENTIFIER, Collections.emptyList());
        secondBatchMap.put(PROCESSES, secondBatchMapProcesses);
        documents.put(2, secondBatchMap);

        Map<String, Object> thirdBatchMap = new HashMap<>();
        thirdBatchMap.put(TITLE, "Batch2");
        thirdBatchMap.put(TYPE, "");
        thirdBatchMap.put(AMOUNT, 0);
        Map<String, Object> thirdBatchMapProcesses = new HashMap<>();
        thirdBatchMapProcesses.put(IDENTIFIER, Collections.emptyList());
        thirdBatchMap.put(PROCESSES, thirdBatchMapProcesses);
        documents.put(3, thirdBatchMap);

        Map<String, Object> fourthBatchMap = new HashMap<>();
        fourthBatchMap.put(TITLE, "Order");
        fourthBatchMap.put(TYPE, "");
        fourthBatchMap.put(AMOUNT, 2);
        Map<String, Object> fourthBatchMapProcesses = new HashMap<>();
        fourthBatchMapProcesses.put(IDENTIFIER, Collections.emptyList());
        fourthBatchMap.put(PROCESSES, fourthBatchMapProcesses);
        documents.put(4, fourthBatchMap);

        return documents;
    }
}
