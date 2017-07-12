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

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;

/**
 * Mock entities for ElasticSearch classes.
 */
public class MockEntity {

    @SuppressWarnings("unchecked")
    public static Map prepareNodeSettings(String httpPort, String httpTransportPort, String nodeName) {
        Map settingsMap = new HashMap();
        settingsMap.put("node.name", nodeName);
        // create all data directories under Maven build directory
        settingsMap.put("path.conf", "target");
        settingsMap.put("path.data", "target");
        settingsMap.put("path.logs", "target");
        settingsMap.put("path.home", "target");
        // set ports used by Elastic Search to something different than default
        settingsMap.put("http.type", "netty4");
        settingsMap.put("http.port", httpPort);
        settingsMap.put("transport.tcp.port", httpTransportPort);
        settingsMap.put("transport.type", "netty4");
        // disable automatic index creation
        settingsMap.put("action.auto_create_index", "false");
        return settingsMap;
    }

    public static HashMap<Integer, HttpEntity> createEntities() {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();

        String jsonString = "{\"title\":\"Batch1\",\"type\":\"LOGISTIC\",\"processes\":[{\"id\":\"1\"},{\"id\":\"2\"}]}";
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(1, entity);

        jsonString = "{\"title\":\"Batch2\",\"type\":\"null\",\"processes\":[]}";
        entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        documents.put(2, entity);

        return documents;
    }
}
