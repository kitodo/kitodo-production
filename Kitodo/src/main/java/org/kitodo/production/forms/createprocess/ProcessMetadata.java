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
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.data.ImportService;
import org.primefaces.model.TreeNode;

public class ProcessMetadata {
    private static final Logger logger = LogManager.getLogger(ProcessMetadata.class);

    private final CreateProcessForm createProcessForm;

    private ProcessFieldedMetadata processDetails = ProcessFieldedMetadata.EMPTY;

    private TreeNode selectedMetadataTreeNode;

    private String addMetadataKeySelectedItem = "";

    public ProcessMetadata(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * initialize process details table.
     * @param structure
     *          which its Metadata are wanted to be shown
     */
    public ProcessFieldedMetadata initializeProcessDetails(LogicalDivision structure) {
        processDetails = ImportService.initializeProcessDetails(structure, this.createProcessForm.getRulesetManagement(),
                this.createProcessForm.getAcquisitionStage(), this.createProcessForm.getPriorityList());
        return processDetails;
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
     * Get selectedMetadataTreeNode.
     *
     * @return value of selectedMetadataTreeNode
     */
    public TreeNode getSelectedMetadataTreeNode() {
        return selectedMetadataTreeNode;
    }

    /**
     * Set selectedMetadataTreeNode.
     *
     * @param selectedMetadataTreeNode as org.primefaces.model.TreeNode
     */
    public void setSelectedMetadataTreeNode(TreeNode selectedMetadataTreeNode) {
        this.selectedMetadataTreeNode = selectedMetadataTreeNode;
    }

    /**
     * Set addMetadataKeySelectedItem.
     *
     * @param addMetadataKeySelectedItem as java.lang.String
     */
    public void setAddMetadataKeySelectedItem(String addMetadataKeySelectedItem) {
        this.addMetadataKeySelectedItem = addMetadataKeySelectedItem;
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

    /**
     * Check and return whether any further metadata can be added to the currently selected structure element or not.
     *
     * @return whether any further metadata can be added to currently selected structure element.
     */
    public boolean metadataAddableToStructureElement() throws InvalidMetadataValueException {
        createProcessForm.getAddMetadataDialog().prepareAddableMetadataForStructure();
        return !(createProcessForm.getAddMetadataDialog().getAddableMetadata().isEmpty());
    }

    /**
     * Adds an empty table line with 'addMetadataKeySelectedItem' type.
     */
    public void addMetadataEntry() {
        try {
            if (Objects.nonNull(selectedMetadataTreeNode) && Objects.nonNull(selectedMetadataTreeNode.getData())) {
                ((ProcessFieldedMetadata) selectedMetadataTreeNode.getData()).getAdditionallySelectedFields().clear();
                ((ProcessFieldedMetadata) selectedMetadataTreeNode.getData()).addAdditionallySelectedField(addMetadataKeySelectedItem);
            } else {
                getProcessDetails().getAdditionallySelectedFields().clear();
                getProcessDetails().addAdditionallySelectedField(addMetadataKeySelectedItem);
            }
        } catch (NoSuchMetadataFieldException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
    }

    /**
     * Updates the logical division of the process details.
     */
    public void update() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        LogicalDivision logicalDivision = (LogicalDivision) processDetails.getDivision();
        if (Objects.nonNull(logicalDivision)) {
            processDetails.preserve();
            processDetails = initializeProcessDetails(logicalDivision);
        }
    }
}
