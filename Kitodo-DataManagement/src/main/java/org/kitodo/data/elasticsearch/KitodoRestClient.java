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

import java.io.IOException;
import java.util.Collections;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.api.RestClientInterface;

/**
 * Implementation of Elastic Search REST Client for Index Module.
 */
public class KitodoRestClient implements RestClientInterface {

    protected String index;
    protected String type;
    protected RestClient restClient;

    /**
     * Create REST client.
     *
     */
    public void initiateClient() {
        String host = ConfigMain.getParameter("elasticsearch.host", "localhost");
        int port = ConfigMain.getIntParameter("elasticsearch.port", 9200);
        String protocol = ConfigMain.getParameter("elasticsearch.protocol", "http");
        initiateClient(host, port, protocol);
    }

    /**
     * Get information about client server.
     *
     * @return information about the server
     */
    public String getServerInformation() throws IOException {
        Response response = restClient.performRequest("GET", "/", Collections.singletonMap("pretty", "true"));
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Close REST Client.
     *
     * @throws IOException
     *             add description
     */
    public void closeClient() throws IOException {
        restClient.close();
    }

    /**
     * Create REST client.
     *
     * @param host
     *            default host is localhost
     * @param port
     *            default port ist 9200
     * @param protocol
     *            default protocol is http
     */
    private void initiateClient(String host, Integer port, String protocol) {
        restClient = RestClient.builder(new HttpHost(host, port, protocol)).build();
    }

    /**
     * Getter for index.
     * 
     * @return index name
     */
    public String getIndex() {
        return index;
    }

    /**
     * Setter for index.
     * 
     * @param index
     *            - equal to the name of database, default kitodo
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Getter for type.
     * 
     * @return type name
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for type.
     *
     * @param type
     *            - equal to the name of table in database
     */
    public void setType(String type) {
        this.type = type;
    }
}
