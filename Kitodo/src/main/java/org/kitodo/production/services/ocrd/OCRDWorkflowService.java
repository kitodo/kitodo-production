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

package org.kitodo.production.services.ocrd;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OCRDWorkflowService {

    private static final Logger logger = LogManager.getLogger(OCRDWorkflowService.class);
    private static volatile OCRDWorkflowService instance = null;

    /**
     * Return singleton variable of type OCRDWorkflowService.
     *
     * @return unique instance of OCRDWorkflowService
     */
    public static OCRDWorkflowService getInstance() {
        OCRDWorkflowService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (OCRDWorkflowService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new OCRDWorkflowService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }


}
