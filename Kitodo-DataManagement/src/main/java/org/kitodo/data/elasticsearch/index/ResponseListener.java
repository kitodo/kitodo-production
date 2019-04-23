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

package org.kitodo.data.elasticsearch.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;

public class ResponseListener implements ActionListener<BulkResponse> {

    private static final Logger logger = LogManager.getLogger(ResponseListener.class);

    private String type;
    private int batchSize;
    private BulkResponse bulkResponse = null;

    /**
     * Constructor with information about type and size of batch.
     *
     * @param type
     *            as String
     * @param batchSize
     *            as int
     */
    ResponseListener(String type, int batchSize) {
        this.type = type;
        this.batchSize = batchSize;
    }

    @Override
    public void onResponse(BulkResponse bulkResponse) {
        this.bulkResponse = bulkResponse;
        if (bulkResponse.hasFailures()) {
            logger.error(bulkResponse.buildFailureMessage());
        }
    }

    @Override
    public void onFailure(Exception e) {
        // TODO: add error handling
        logger.error("I got failure for type '{}' with size {}!", this.type, this.batchSize);
        logger.error(e.getMessage(), e);
    }

    /**
     * Get bulkResponse.
     *
     * @return value of bulkResponse
     */
    BulkResponse getBulkResponse() {
        return bulkResponse;
    }
}
