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

import javax.faces.model.SelectItem;

import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.model.TreeNode;

public class AddMetadataDialog {

    private final CreateProcessForm createProcessForm;
    private String selectedMetadata = "";
    private List<SelectItem> addableMetadata;

    AddMetadataDialog(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * Get selectedMetadata.
     *
     * @return value of selectedMetadata
     */
    public String getSelectedMetadata() {
        return selectedMetadata;
    }

    /**
     * Set selectedMetadata.
     *
     * @param selectedMetadata as java.lang.String
     */
    public void setSelectedMetadata(String selectedMetadata) {
        this.selectedMetadata = selectedMetadata;
        this.createProcessForm.getProcessMetadata().setAddMetadataKeySelectedItem(selectedMetadata);
    }

    /**
     * Get addableMetadata.
     *
     * @return value of addableMetadata
     */
    public List<SelectItem> getAddableMetadata() {
        return addableMetadata;
    }

    /**
     * Check and return whether any further metadata can be added to the currently selected structure element or not.
     *
     * @return whether any further metadata can be added to currently selected structure element.
     */
    public boolean metadataAddableToStructureElement() throws InvalidMetadataValueException {
        prepareAddableMetadataForStructure();
        return !getAddableMetadata().isEmpty();
    }

    /**
     * Prepare addable metadata for logical structure element.
     * @throws InvalidMetadataValueException invalidMetadataValueException
     */
    public void prepareAddableMetadataForStructure() throws InvalidMetadataValueException {
        createProcessForm.getProcessMetadata().setSelectedMetadataTreeNode(null);
        addableMetadata = DataEditorService.getAddableMetadataForStructureElement(
                createProcessForm.getRulesetManagement().getStructuralElementView(
                        createProcessForm.getProcessDataTab().getDocType(),
                        createProcessForm.getAcquisitionStage(),
                        createProcessForm.getPriorityList()),
                DataEditorService.getExistingMetadataRows(createProcessForm.getProcessMetadata().getLogicalMetadataTree().getChildren()),
                createProcessForm.getProcessMetadata().getProcessDetails().getAdditionallySelectedFields(),
                createProcessForm.getMainProcess().getRuleset());
    }

    /**
     * Prepare addable metadata for metadata group.
     */
    public void prepareAddableMetadataForGroup(Ruleset ruleset, TreeNode treeNode) {
        createProcessForm.getProcessMetadata().setSelectedMetadataTreeNode(treeNode);
        addableMetadata = DataEditorService.getAddableMetadataForGroup(ruleset, treeNode);
    }
}
