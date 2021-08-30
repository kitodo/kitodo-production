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

package org.kitodo.production.forms.createprocess;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.services.data.ImportService;
import org.primefaces.model.TreeNode;

public class ProcessMetadataTab {
    private static final Logger logger = LogManager.getLogger(ProcessMetadataTab.class);

    private final CreateProcessForm createProcessForm;

    private ProcessFieldedMetadata processDetails = ProcessFieldedMetadata.EMPTY;

    public ProcessMetadataTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * initialize process details table.
     * @param structure
     *          which its Metadata are wanted to be shown
     */
    public ProcessFieldedMetadata initializeProcessDetails(LogicalDivision structure) {
        return ImportService.initializeProcessDetails(structure, this.createProcessForm.getRulesetManagement(),
                this.createProcessForm.getAcquisitionStage(), this.createProcessForm.getPriorityList());
    }

    /**
     * Returns the logical metadata tree.
     *
     * @return the logical metadata tree
     */
    public TreeNode getLogicalMetadataTree() {
        return processDetails.getTreeNode();
    }

    /**
     * Get processDetails.
     *
     * @return value of processDetails
     */
    ProcessFieldedMetadata getProcessDetails() {
        return processDetails;
    }

    /**
     * Set processDetails.
     * @param processDetails
     *          as ProcessFieldedMetadata
     */
    void setProcessDetails(ProcessFieldedMetadata processDetails) {
        this.processDetails = processDetails;
    }


    /**
     * Get all details in the processDetails as a list.
     *
     * @return the list of details of the processDetails
     */
    public List<ProcessDetail> getProcessDetailsElements() {
        return processDetails.getRows();
    }

    /**
     * preserve all the metadata in the processDetails.
     */
    public void preserve() {
        try {
            processDetails.preserve();
        } catch (NoSuchMetadataFieldException | InvalidMetadataValueException e) {
            logger.error(e.getLocalizedMessage());
        }
    }
}
