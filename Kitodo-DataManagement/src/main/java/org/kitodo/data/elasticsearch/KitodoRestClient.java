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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.rest.RestStatus;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.elasticsearch.api.RestClientInterface;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;

/**
 * Implementation of ElasticSearch REST Client for Index Module.
 */
public abstract class KitodoRestClient implements RestClientInterface {

    private static final Logger logger = LogManager.getLogger(KitodoRestClient.class);

    protected String indexBase;
    protected RestClient client;
    protected RestHighLevelClient highLevelClient;

    public static final List<String> MAPPING_TYPES = Arrays.asList("batch", "docket", "filter", "process", "project",
            "property", "ruleset", "task", "template", "workflow");

    /**
     * Create REST client.
     *
     */
    protected void initiateClient() {
        String host = ConfigMain.getParameter("elasticsearch.host", "localhost");
        int port = ConfigMain.getIntParameter("elasticsearch.port", 9200);
        String protocol = ConfigMain.getParameter("elasticsearch.protocol", "http");
        String path = ConfigMain.getParameter("elasticsearch.path", "/");
        initiateClient(host, port, protocol, path);
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
     * @param path
     *            default path is /
     */
    private void initiateClient(String host, Integer port, String protocol, String path) {
        if (ConfigMain.getBooleanParameter("elasticsearch.useAuthentication")) {
            initiateClientWithAuth(host, port, protocol, path);
        } else {
            RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, protocol));

            if (!path.isEmpty() && !path.equals("/")) {
                builder.setPathPrefix(path);
            }

            client = builder.build();
            highLevelClient = new RestHighLevelClient(builder);
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
     * @param path
     *           default path is /
     */
    private void initiateClientWithAuth(String host, Integer port, String protocol, String path) {
        String user = ConfigMain.getParameter("elasticsearch.user", "elastic");
        String password = ConfigMain.getParameter("elasticsearch.password", "changeme");

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, protocol)).setHttpClientConfigCallback(
            httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        if (!path.isEmpty() && !path.equals("/")) {
            builder.setPathPrefix(path);
        }
        client = builder.build();
        highLevelClient = new RestHighLevelClient(builder);
    }

    /**
     * Get information about client server.
     *
     * @return information about the server
     */
    public String getServerInformation() throws IOException {
        Request request = new Request(HttpMethod.GET, "/");
        request.addParameter("pretty", "true");
        Response response = performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Get information about client server.
     *
     * @return information about the server
     */
    public String getServerInfo() throws IOException {
        MainResponse response = highLevelClient.info(RequestOptions.DEFAULT);
        return response.toString();
    }

    /**
     * Get mapping.
     *
     * @param mappingType
     *           the name of table in database as String
     * @return mapping
     */
    public String getMapping(String mappingType) throws IOException {
        Request request = new Request(HttpMethod.GET, "/" + indexBase + "_" + mappingType + "/_mapping");
        request.addParameter("pretty", "true");
        Response response = performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Create new indexes without mappings.
     */
    public void createIndexes() throws IOException, CustomResponseException {
        for (String mappingType : MAPPING_TYPES) {
            createIndex(null, mappingType);
        }
    }

    /**
     * Create new index with mapping.
     *
     * @param query
     *            contains mapping
     * @param mappingType
     *            the name of table in database as String
     * @return true or false - can be used for displaying information to user if
     *         success
     */
    public boolean createIndex(String query, String mappingType) throws IOException, CustomResponseException {
        if (query == null) {
            query = "{\"settings\" : {\"index\" : {\"number_of_shards\" : 1,\"number_of_replicas\" : 0}}}";
        }
        HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
        Request request = new Request(HttpMethod.PUT, "/" + indexBase + "_" + mappingType);
        request.setEntity(entity);
        Response indexResponse = performRequest(request);
        int statusCode = processStatusCode(indexResponse.getStatusLine());
        return statusCode == 200 || statusCode == 201;
    }

    /**
     * Check if all indexes already exist. Needed for frontend.
     *
     * @return false if any index doesn't exist, true else
     */
    public boolean typeIndexesExist() throws IOException, CustomResponseException {
        for (String mappingType : MAPPING_TYPES) {
            Response indexResponse = performRequest(new Request(HttpMethod.GET, "/" + indexBase + "_" + mappingType));
            int statusCode = processStatusCode(indexResponse.getStatusLine());
            if (statusCode != 200 && statusCode != 201) {
                return false;
            }
        }
        return true;
    }

    /**
     * Delete index of given mappingType. Currently, only used in test cases.
     * @param mappingType mapping type
     */
    public void deleteIndex(String mappingType) throws IOException {
        performRequest(new Request(HttpMethod.DELETE, "/" + indexBase + "_" + mappingType));
    }

    /**
     * Delete all indexes. Used for cleaning after tests!
     */
    public void deleteAllIndexes() throws IOException {
        for (String mappingType : MAPPING_TYPES) {
            performRequest(new Request(HttpMethod.DELETE, "/" + indexBase + "_" + mappingType));
        }
    }

    /**
     * Getter for indexBase.
     * The indexBase is the prefix of all indexes - equal to the name of database, default kitodo.
     *
     * @return indexBase name
     */
    public String getIndexBase() {
        return indexBase;
    }

    /**
     * Update refresh interval of search indices to 1 ms.
     *
     * @throws IOException when index cannot be reached
     */
    public void setRefreshInterval() throws IOException {
        this.highLevelClient.indices().putSettings(Requests.updateSettingsRequest()
                .settings(Collections.singletonMap("refresh_interval", "1ms")), RequestOptions.DEFAULT);
    }

    /**
     * Setter for indexBase. The indexBase is the prefix of all indexes
     *
     * @param indexBase
     *            - equal to the name of database, default kitodo
     */
    public void setIndexBase(String indexBase) {
        this.indexBase = indexBase;
    }

    protected void handleResponseException(ResponseException e) throws CustomResponseException {
        if (e.getResponse().getStatusLine().getStatusCode() == 404) {
            if (logger.isTraceEnabled()) {
                logger.trace(e.getMessage(), e);
            } else {
                logger.debug(e.getMessage().replaceAll("\\p{Space}+", " "));
            }
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
    
    private Response performRequest(Request request) throws IOException {
        Response response = client.performRequest(request);
        if (logger.isDebugEnabled()) {
            logger.debug(logger.isTraceEnabled()
                    ? response.toString() + System.lineSeparator() + EntityUtils.toString(response.getEntity())
                    : response.toString());
        }
        return response;
    }
}
