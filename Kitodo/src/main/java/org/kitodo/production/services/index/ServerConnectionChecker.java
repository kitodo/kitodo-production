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

package org.kitodo.production.services.index;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.hibernate.search.backend.elasticsearch.ElasticsearchBackend;
import org.hibernate.search.mapper.orm.Search;
import org.kitodo.data.database.persistence.HibernateUtil;

/**
 * Checks the connection to the search service. This is an asynchronous function
 * to {@link IndexingService}. It uses its variables for inter-thread
 * communication.
 */
class ServerConnectionChecker implements Runnable {
    private static final Logger logger = LogManager.getLogger(ServerConnectionChecker.class);
    private static final int WAIT_BETWEEN_CHECKS_SECS = 3;

    private final IndexingService indexingService;

    public ServerConnectionChecker(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    public void run() {
        if (Objects.nonNull(indexingService.serverInformation) && SECONDS.convert(System.nanoTime()
                - indexingService.serverLastCheck, NANOSECONDS) < WAIT_BETWEEN_CHECKS_SECS) {
            return;
        }

        boolean clearId = false;
        try {
            if (indexingService.serverCheckThreadId == 0) {
                indexingService.serverCheckThreadId = currentThread().getId();
                if (indexingService.serverCheckThreadId == currentThread().getId()) {
                    clearId = true;
                    indexingService.serverInformation = downloadServerInformation();
                    indexingService.serverLastCheck = System.nanoTime();
                }
            }
        } catch (RuntimeException e) {
            logger.error(e);
            indexingService.serverInformation = "";
            indexingService.serverLastCheck = System.nanoTime();
        } finally {
            if (clearId) {
                indexingService.serverCheckThreadId = 0;
            }
        }
    }

    /**
     * Get search server information.
     */
    private static String downloadServerInformation() {
        try {
            ElasticsearchBackend elasticsearchBackend = Search.mapping(HibernateUtil.getSession().getSessionFactory())
                    .backend()
                    .unwrap(ElasticsearchBackend.class);
            // do not call close() on restClient as this will terminate the connection to search index
            RestClient restClient = elasticsearchBackend.client(RestClient.class);
            Request request = new Request("GET", "/");
            Response response = restClient.performRequest(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                String serverInformation = String.format("Connection established to %s", response.getHost().toURI());
                logger.info("Search server found: {}", serverInformation);
                return serverInformation;
            } else {
                String message = String.format("Error connecting to Elasticsearch server: %s",
                        response.getStatusLine().getReasonPhrase());
                logger.error(message);
                return "";
            }
        } catch (IOException e) {
            logger.error("searchServerNotRunning", e);
        }
        return "";
    }
}
