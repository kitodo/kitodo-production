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

package org.kitodo.production.forms.dataeditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.helper.Helper;
import org.primefaces.model.TreeNode;

/**
 * Backing bean for the metadata panel of the metadata editor.
 */
public class MetadataPanel implements Serializable {

    private static final Logger logger = LogManager.getLogger(MetadataPanel.class);

    private String addMetadataKeySelectedItem = "";

    private Collection<Metadata> clipboard = new ArrayList<>();

    private final DataEditorForm dataEditorForm;

    private ProcessFieldedMetadata logicalMetadataTable = ProcessFieldedMetadata.EMPTY;
    private ProcessFieldedMetadata physicalMetadataTable = ProcessFieldedMetadata.EMPTY;
    private TreeNode selectedMetadataTreeNode;

    MetadataPanel(DataEditorForm dataEditor) {
        this.dataEditorForm = dataEditor;
    }

    /**
     * Adds an empty table line with the given type.
     */
    public void addMetadataEntry() {
        try {
            if (Objects.nonNull(selectedMetadataTreeNode) && Objects.nonNull(selectedMetadataTreeNode.getData())) {
                ((ProcessFieldedMetadata) selectedMetadataTreeNode.getData()).addAdditionallySelectedField(addMetadataKeySelectedItem);
            } else {
                logicalMetadataTable.addAdditionallySelectedField(addMetadataKeySelectedItem);
            }
        } catch (NoSuchMetadataFieldException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
    }

    /**
     * Empties the metadata panel.
     */
    public void clear() {
        logicalMetadataTable = ProcessFieldedMetadata.EMPTY;
        physicalMetadataTable = ProcessFieldedMetadata.EMPTY;
        clipboard.clear();
        addMetadataKeySelectedItem = "";
    }

    /**
     * Set addMetadataKeySelectedItem.
     *
     * @param addMetadataKeySelectedItem as java.lang.String
     */
    public void setAddMetadataKeySelectedItem(String addMetadataKeySelectedItem) {
        this.addMetadataKeySelectedItem = addMetadataKeySelectedItem;
    }

    Collection<Metadata> getClipboard() {
        return clipboard;
    }

    /**
     * Returns the rows of logical metadata that JSF has to display.
     *
     * @return the rows of logical metadata
     */
    public TreeNode getLogicalMetadataRows() {
        return logicalMetadataTable.getTreeNode();
    }

    /**
     * Returns the rows of physical metadata that JSF has to display.
     *
     * @return the rows of physical metadata
     */
    public TreeNode getPhysicalMetadataRows() {
        return physicalMetadataTable.getTreeNode();
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

    void showLogical(Optional<IncludedStructuralElement> optionalStructure) {
        if (optionalStructure.isPresent()) {
            StructuralElementViewInterface divisionView = dataEditorForm.getRulesetManagement().getStructuralElementView(
                    optionalStructure.get().getType(), dataEditorForm.getAcquisitionStage(), dataEditorForm.getPriorityList());
            logicalMetadataTable = new ProcessFieldedMetadata(optionalStructure.get(), divisionView);
            dataEditorForm.getAddDocStrucTypeDialog().prepareSelectAddableMetadataTypesItems(true,
                    getLogicalMetadataRows().getChildren());
        } else {
            logicalMetadataTable = ProcessFieldedMetadata.EMPTY;
        }

    }

    void showPageInLogical(MediaUnit mediaUnit) {
        if (Objects.nonNull(mediaUnit)) {
            StructuralElementViewInterface divisionView = dataEditorForm.getRulesetManagement().getStructuralElementView(
                    mediaUnit.getType(), dataEditorForm.getAcquisitionStage(), dataEditorForm.getPriorityList());
            logicalMetadataTable = new ProcessFieldedMetadata(mediaUnit, divisionView);
            dataEditorForm.getAddDocStrucTypeDialog().prepareSelectAddableMetadataTypesItems(true,
                    getPhysicalMetadataRows().getChildren());
        } else {
            logicalMetadataTable = ProcessFieldedMetadata.EMPTY;
        }

    }

    void showPhysical(Optional<MediaUnit> optionalMediaUnit) {
        if (optionalMediaUnit.isPresent() && Objects.nonNull(optionalMediaUnit.get().getType())) {
            StructuralElementViewInterface divisionView = dataEditorForm.getRulesetManagement().getStructuralElementView(
                    optionalMediaUnit.get().getType(), dataEditorForm.getAcquisitionStage(), dataEditorForm.getPriorityList());
            physicalMetadataTable = new ProcessFieldedMetadata(optionalMediaUnit.get(), divisionView);
            dataEditorForm.getAddDocStrucTypeDialog().prepareSelectAddableMetadataTypesItems(true);
        } else {
            physicalMetadataTable = ProcessFieldedMetadata.EMPTY;
        }

    }

    /**
     * Preserve metadata.
     */
    public void preserve() {
        try {
            this.preserveLogical();
            this.preservePhysical();
        } catch (InvalidMetadataValueException | NoSuchMetadataFieldException e) {
            logger.info(e.getMessage());
        }
    }

    void preserveLogical() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        logicalMetadataTable.preserve();
        this.dataEditorForm.checkForChanges();
    }

    void preservePhysical() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        physicalMetadataTable.preserve();
        this.dataEditorForm.checkForChanges();
    }
}
