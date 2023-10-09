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

package org.kitodo.production.services.ocr;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.production.services.ServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OcrdWorkflowService {

    private static volatile OcrdWorkflowService instance = null;

    /**
     * Return singleton variable of type OcrdWorkflowService.
     *
     * @return unique instance of OcrdWorkflowService
     */
    public static OcrdWorkflowService getInstance() {
        OcrdWorkflowService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (OcrdWorkflowService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new OcrdWorkflowService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Get OCR-D workflows - available means that ...
     *
     * @return list of OCR-D workflows objects
     */
    public List<Pair> getOcrdWorkflows() {
        List workflows = new ArrayList();
        workflows.add(new ImmutablePair<>("1", "One"));
        workflows.add(new ImmutablePair<>("2", "Two"));
        workflows.add(new ImmutablePair<>("3", "Tree"));
        return workflows;
    }

    public Pair getOcrdWorkflow(String ocrdWorkflowId) {
        if (StringUtils.isNotEmpty(ocrdWorkflowId)) {
            return getOcrdWorkflows().stream().filter(pair -> pair.getKey().equals(ocrdWorkflowId)).findFirst().get();
        }
        return null;
    }

}
