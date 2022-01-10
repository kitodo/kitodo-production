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

import java.util.List;

import javax.faces.model.SelectItem;

import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.model.TreeNode;

public class AddMetadataDialog {

    private final MassImportForm massImportForm;
    private List<SelectItem> addableMetadata;
    private String selectedMetadata = "";

    public AddMetadataDialog(MassImportForm massImportForm) {
        this.massImportForm = massImportForm;
    }

    /**
     * Gets addableMetadata.
     *
     * @return value of addableMetadata
     */
    public List<SelectItem> getAddableMetadata() {
        return addableMetadata;
    }

    /**
     * Sets addableMetadata.
     *
     * @param addableMetadata value of addableMetadata
     */
    public void setAddableMetadata(List<SelectItem> addableMetadata) {
        this.addableMetadata = addableMetadata;
    }

    /**
     * Gets selectedMetadata.
     *
     * @return value of selectedMetadata
     */
    public String getSelectedMetadata() {
        return selectedMetadata;
    }

    /**
     * Sets selectedMetadata.
     *
     * @param selectedMetadata value of selectedMetadata
     */
    public void setSelectedMetadata(String selectedMetadata) {
        this.selectedMetadata = selectedMetadata;
    }

    /**
     * Prepare addable metadata for logical structure element.
     */
    public void prepareAddableMetadataForStructure() throws InvalidMetadataValueException {
        addableMetadata = DataEditorService.getAddableMetadataForStructureElement(
                massImportForm.getRulesetManagement().getStructuralElementView(
                        massImportForm.getDocType(),
                        massImportForm.getAdditionalMetadata().getAcquisitionStage(),
                        massImportForm.getPriorityList()),
                DataEditorService.getExistingMetadataRows(massImportForm.getAdditionalMetadata().getLogicalMetadataTree().getChildren()),
                massImportForm.getAdditionalMetadata().getProcessDetails().getAdditionallySelectedFields(),
                massImportForm.getTemplate().getRuleset());
    }

    /**
     * Prepare addable metadata for metadata group.
     */
    public void prepareAddableMetadataForGroup(TreeNode treeNode) {
        massImportForm.getAdditionalMetadata().setSelectedMetadataTreeNode(treeNode);
        addableMetadata = DataEditorService.getAddableMetadataForGroup(
                massImportForm.getTemplate().getRuleset(), treeNode);
    }
}
