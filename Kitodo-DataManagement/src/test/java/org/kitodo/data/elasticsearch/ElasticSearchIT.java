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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.Arrays.asList;

public class ElasticSearchIT {
    private Client client;
    private static String testIndexName = "indexname";
    private static final String HTTP_BASE_URL = "http://localhost";
    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";
    private static final String elasticSearchBaseUrl = HTTP_BASE_URL + ":" + HTTP_PORT;
    private static Node node;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void startElasticSearch() throws Exception {
        final String nodeName = "testnode";

        Map settingsMap = MockEntity.prepareNodeSettings(HTTP_PORT, HTTP_TRANSPORT_PORT, nodeName);

        removeOldDataDirectories("target/" + nodeName);

        Settings settings = Settings.builder().put(settingsMap).build();
        node = new ExtendedNode(settings, asList(Netty4Plugin.class));
        node.start();
    }

    private static class ExtendedNode extends Node {
        public ExtendedNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
        }
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
    public void initialize() {
        ClientConfig clientConfig = new DefaultClientConfig();
        client = Client.create(clientConfig);
        client.addFilter(new LoggingFilter(System.out));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateIndex() {
        WebResource service = client.resource(elasticSearchBaseUrl);
        // check first that does the index already exist
        ClientResponse clientResponse = service.path(testIndexName).head();
        if (clientResponse.getClientResponseStatus().equals(ClientResponse.Status.OK))
        {
            Assert.fail("Index exists already");
        }

        String indexJson =
                "{\"settings\" : {\"index\" : {\"number_of_shards\" : 1,\"number_of_replicas\" : 0}}}";
        try {
            String response =
                    service.path(testIndexName).
                            queryParam("timeout","5m").
                            accept(MediaType.APPLICATION_JSON).
                            put(String.class, indexJson);
            if (!response.contains("true")) {
                Assert.fail("Creating index failed. IndexName: " + testIndexName);
            }
        } catch (UniformInterfaceException e) {
            // failed due to Client side problem
            throw e;
        }

        // wait for Elastic Search to be ready for further processing
        HashMap statusParameters = new HashMap();
        final String timeout = "30s";
        statusParameters.put("timeout", timeout);
        statusParameters.put("wait_for_status", "green");
        String statusResponse = status(statusParameters);
        if (statusResponse.contains("red")) {
            Assert.fail("Failed to create index");
        }


    }

    private String status(final Map optionalParameters) {
        WebResource service = client.resource(elasticSearchBaseUrl);
        WebResource webResource = service.path("_cluster").path("/health");
        for (Object entry : optionalParameters.entrySet()) {
            if(entry instanceof Entry) {
                Entry temp = (Entry) entry;
                webResource = webResource.queryParam((String)temp.getKey(),(String)temp.getValue());
            }
        }
        return webResource.get(String.class);
    }

}
