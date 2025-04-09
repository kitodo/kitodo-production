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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.InvalidPropertiesFormatException;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;

/**
 * Checks the connection to the search service. This is an asynchronous function
 * to {@link IndexingService}. It uses its variables for inter-thread
 * communication.
 */
class ServerConnectionChecker implements Runnable {
    private static final Logger logger = LogManager.getLogger(ServerConnectionChecker.class);
    private static final Pattern PATTERN_SERVER = Pattern.compile("cluster_name\\W+([^\"]*).*?number\\W+([^\"]*)",
        Pattern.DOTALL);
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
    private static final String downloadServerInformation() {
        try (InputStream greetStream = ConfigCore.getSearchServerUrl().openStream();
                Scanner scanner = new Scanner(greetStream, StandardCharsets.US_ASCII)) {
            scanner.useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";
            Matcher serverMatcher = PATTERN_SERVER.matcher(body);
            if (serverMatcher.find()) {
                String serverInformation = serverMatcher.group(1) + ' ' + serverMatcher.group(2);
                logger.info("Search server found: {}", serverInformation);
                return serverInformation;
            } else {
                throw new InvalidPropertiesFormatException(body);
            }
        } catch (IOException | RuntimeException e) {
            logger.error("elasticSearchNotRunning", e);
        }
        return "";
    }
}
