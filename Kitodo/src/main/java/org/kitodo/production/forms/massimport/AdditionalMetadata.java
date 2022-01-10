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

package org.kitodo.production.forms.massimport;

import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.interfaces.MetadataTreeTableInterface;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.model.TreeNode;

public class AdditionalMetadata implements MetadataTreeTableInterface {

    private static final Logger logger = LogManager.getLogger(AdditionalMetadata.class);

    private final MassImportForm massImportForm;
    private final String acquisitionStage = "create";
    private ProcessFieldedMetadata processDetails = ProcessFieldedMetadata.EMPTY;
    private TreeNode selectedMetadataTreeNode = null;


    public AdditionalMetadata(MassImportForm massImportForm) {
        this.massImportForm = massImportForm;
    }

    /**
     * initialize ProcessDetails.
     */
    public void initializeProcessDetails() {
        processDetails = ImportService.initializeProcessDetails(
                massImportForm.getWorkpiece().getLogicalStructure(), massImportForm.getRulesetManagement(),
                acquisitionStage, massImportForm.getPriorityList());
    }

    /**
     * Gets processDetails.
     *
     * @return value of processDetails
     */
    public ProcessFieldedMetadata getProcessDetails() {
        return processDetails;
    }

    /**
     * Sets processDetails.
     *
     * @param processDetails value of processDetails
     */
    public void setProcessDetails(ProcessFieldedMetadata processDetails) {
        this.processDetails = processDetails;
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
     * Gets acquisitionStage.
     *
     * @return value of acquisitionStage
     */
    public String getAcquisitionStage() {
        return acquisitionStage;
    }

    /**
     * Gets selectedMetadataTreeNode.
     *
     * @return value of selectedMetadataTreeNode
     */
    public TreeNode getSelectedMetadataTreeNode() {
        return selectedMetadataTreeNode;
    }

    /**
     * Sets selectedMetadataTreeNode.
     *
     * @param selectedMetadataTreeNode value of selectedMetadataTreeNode
     */
    public void setSelectedMetadataTreeNode(TreeNode selectedMetadataTreeNode) {
        this.selectedMetadataTreeNode = selectedMetadataTreeNode;
    }

    @Override
    public boolean canBeDeleted(ProcessDetail processDetail) {
        return processDetail.getOccurrences() > 1 && processDetail.getOccurrences() > processDetail.getMinOcc()
                || (!processDetail.isRequired()
                && !massImportForm.getRulesetManagement().isAlwaysShowingForKey(processDetail.getMetadataID()));
    }

    @Override
    public boolean canBeAdded(TreeNode treeNode) throws InvalidMetadataValueException {
        if (Objects.isNull(treeNode.getParent().getParent())) {
            if (Objects.nonNull(selectedMetadataTreeNode) || Objects.isNull(massImportForm.getAddMetadataDialog().getAddableMetadata())) {
                massImportForm.getAddMetadataDialog().prepareAddableMetadataForStructure();
            }
        } else if (!Objects.equals(selectedMetadataTreeNode, treeNode.getParent())
                || Objects.isNull(massImportForm.getAddMetadataDialog().getAddableMetadata())) {
            prepareAddableMetadataForGroup(treeNode.getParent());
        }
        if (Objects.nonNull(massImportForm.getAddMetadataDialog().getAddableMetadata())) {
            return massImportForm.getAddMetadataDialog().getAddableMetadata().stream()
                    .map(SelectItem::getValue).collect(Collectors.toList()).contains(((ProcessDetail) treeNode.getData()).getMetadataID());
        }
        return false;
    }

    @Override
    public boolean metadataAddableToGroup(TreeNode metadataNode) {
        if (metadataNode.getData() instanceof ProcessFieldedMetadata) {
            return !(DataEditorService.getAddableMetadataForGroup(massImportForm.getTemplate().getRuleset(), metadataNode).isEmpty());
        }
        return false;
    }

    @Override
    public void prepareAddableMetadataForGroup(TreeNode treeNode) {
        massImportForm.getAddMetadataDialog().prepareAddableMetadataForGroup(treeNode);
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
        massImportForm.getAddMetadataDialog().prepareAddableMetadataForStructure();
        return !(massImportForm.getAddMetadataDialog().getAddableMetadata().isEmpty());
    }

    /**
     * Adds an empty table line with the given type.
     */
    public void addMetadataEntry() {
        try {
            if (Objects.nonNull(selectedMetadataTreeNode) && Objects.nonNull(selectedMetadataTreeNode.getData())) {
                ((ProcessFieldedMetadata) selectedMetadataTreeNode.getData()).getAdditionallySelectedFields().clear();
                ((ProcessFieldedMetadata) selectedMetadataTreeNode.getData()).addAdditionallySelectedField(
                        massImportForm.getAddMetadataDialog().getSelectedMetadata());
            } else {
                getProcessDetails().getAdditionallySelectedFields().clear();
                getProcessDetails().addAdditionallySelectedField(massImportForm.getAddMetadataDialog().getSelectedMetadata());
            }
        } catch (NoSuchMetadataFieldException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
    }
}
