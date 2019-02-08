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

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.api.RestClientInterface;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;

/**
 * Implementation of Elastic Search REST Client for Index Module.
 */
public abstract class KitodoRestClient implements RestClientInterface {

    private static final Logger logger = LogManager.getLogger(KitodoRestClient.class);

    protected String index;
    protected String type;
    protected RestClient client;
    protected RestHighLevelClient highLevelClient;

    /**
     * Create REST client.
     *
     */
    protected void initiateClient() {
        String host = ConfigMain.getParameter("elasticsearch.host", "localhost");
        int port = ConfigMain.getIntParameter("elasticsearch.port", 9200);
        String protocol = ConfigMain.getParameter("elasticsearch.protocol", "http");
        initiateClient(host, port, protocol);
    }

    /**
     * Create REST client with other without basic authentication.
     *
     * @param host
     *            default host is localhost
     * @param port
     *            default port ist 9200
     * @param protocol
     *            default protocol is http
     */
    private void initiateClient(String host, Integer port, String protocol) {
        if (ConfigMain.getBooleanParameter("elasticsearch.useAuthentication")) {
            initiateClientWithAuth(host, port, protocol);
        } else {
            client = RestClient.builder(new HttpHost(host, port, protocol)).build();
            highLevelClient = new RestHighLevelClient(client);
        }
    }

    /**
     * Create REST client with basic authentication.
     * 
     * @param host
     *            default host is localhost
     * @param port
     *            default port ist 9200
     * @param protocol
     *            default protocol is http
     */
    private void initiateClientWithAuth(String host, Integer port, String protocol) {
        String user = ConfigMain.getParameter("elasticsearch.user", "elastic");
        String password = ConfigMain.getParameter("elasticsearch.password", "changeme");

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

        client = RestClient.builder(new HttpHost(host, port, protocol)).setHttpClientConfigCallback(
            httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)).build();
        highLevelClient = new RestHighLevelClient(client);
    }

    /**
     * Get information about client server.
     *
     * @return information about the server
     */
    public String getServerInformation() throws IOException {
        Response response = client.performRequest(HttpMethod.GET, "/", Collections.singletonMap("pretty", "true"));
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Get information about client server.
     *
     * @return information about the server
     */
    public String getServerInfo() throws IOException {
        MainResponse response = highLevelClient.info();
        return response.toString();
    }

    /**
     * Get mapping.
     *
     * @return mapping
     */
    public String getMapping() throws IOException {
        Response response = client.performRequest(HttpMethod.GET, "/" + index + "/_mapping",
            Collections.singletonMap("pretty", "true"));
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Create new index without mapping.
     */
    public boolean createIndex() throws IOException, CustomResponseException {
        return createIndex(null);
    }

    /**
     * Create new index with mapping.
     *
     * @param query
     *            contains mapping
     * @return true or false - can be used for displaying information to user if
     *         success
     */
    public boolean createIndex(String query) throws IOException, CustomResponseException {
        if (query == null) {
            query = "{\"settings\" : {\"index\" : {\"number_of_shards\" : 1,\"number_of_replicas\" : 0}}}";
        }
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Response indexResponse = client.performRequest(HttpMethod.PUT, "/" + index, Collections.emptyMap(), entity);
        int statusCode = processStatusCode(indexResponse.getStatusLine());
        return statusCode == 200 || statusCode == 201;
    }

    /**
     * Check if index already exists. Needed for frontend.
     *
     * @return false if doesn't exists, true if exists
     */
    public boolean indexExists() throws IOException, CustomResponseException {
        Response indexResponse = client.performRequest(HttpMethod.GET, "/" + index, Collections.emptyMap());
        int statusCode = processStatusCode(indexResponse.getStatusLine());
        return statusCode == 200 || statusCode == 201;
    }

    /**
     * Delete the whole index. Used for cleaning after tests!
     */
    public void deleteIndex() throws IOException {
        client.performRequest(HttpMethod.DELETE, "/" + index);
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
     *            - equal to the name of table in database, but not necessary
     */
    public void setType(String type) {
        this.type = type;
    }

    protected void handleResponseException(ResponseException e) throws CustomResponseException {
        if (e.getResponse().getStatusLine().getStatusCode() == 404) {
            logger.debug(e.getMessage(), e);
        } else {
            throw new CustomResponseException(e);
        }
    }

    protected int processStatusCode(StatusLine statusLine) throws CustomResponseException {
        int statusCode = statusLine.getStatusCode();
        processStatusCode(statusCode, statusLine);
        return statusCode;
    }

    protected int processStatusCode(RestStatus restStatus) throws CustomResponseException {
        int statusCode = restStatus.getStatus();
        processStatusCode(statusCode, restStatus);
        return statusCode;
    }

    private void processStatusCode(int statusCode, Object restStatus) throws CustomResponseException {
        if (statusCode >= 400 && statusCode <= 499) {
            throw new CustomResponseException("Client error: " + restStatus.toString());
        } else if (statusCode >= 500 && statusCode <= 599) {
            throw new CustomResponseException("Server error: " + restStatus.toString());
        }
    }
}
