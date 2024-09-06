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
package org.kitodo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.kitodo.config.ConfigMain;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.node.Node;
import org.opensearch.transport.Netty4Plugin;

public class MockIndex {

    private static Node node;
    private static final String HTTP_TRANSPORT_PORT = "9305";
    private static final String TARGET = "target";

    public static void startNode() throws Exception {
        final String nodeName = "index";
        final String port = ConfigMain.getParameter("elasticsearch.port", "9205");
        Environment environment = prepareEnvironment(port, nodeName, Paths.get("target", "classes"));
        removeOldDataDirectories("target/" + nodeName);
        node = new ExtendedNode(environment, Collections.singleton(Netty4Plugin.class));
        node.start();
    }

    public static void stopNode() throws Exception {
        node.close();
        node = null;
    }

    private static void removeOldDataDirectories(String dataDirectory) throws Exception {
        File dataDir = new File(dataDirectory);
        if (dataDir.exists()) {
            FileUtils.deleteDirectory(dataDir);
        }
    }

    private static Environment prepareEnvironment(String httpPort, String nodeName, Path configPath) {
        Settings settings = Settings.builder().put("node.name", nodeName)
                .put("path.data", TARGET)
                .put("path.logs", TARGET)
                .put("path.home", TARGET)
                .put("http.type", "netty4")
                .put("http.port", httpPort)
                .put("transport.tcp.port", HTTP_TRANSPORT_PORT)
                .put("transport.type", "netty4")
                .put("action.auto_create_index", "false").build();
        return new Environment(settings, configPath);
    }
}
