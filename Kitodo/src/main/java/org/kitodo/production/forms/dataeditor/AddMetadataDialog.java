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

import java.util.List;

import javax.faces.model.SelectItem;

import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.model.TreeNode;

public class AddMetadataDialog {

    private final DataEditorForm dataEditor;
    private List<SelectItem> addableMetadata;
    private String selectedMetadata = "";

    AddMetadataDialog(DataEditorForm dataEditorForm) {
        this.dataEditor = dataEditorForm;
    }

    /**
     * Prepare addable metadata for metadata group.
     */
    public void prepareAddableMetadataForGroup(TreeNode treeNode) {
        dataEditor.getMetadataPanel().setSelectedMetadataTreeNode(treeNode);
        addableMetadata = DataEditorService.getAddableMetadataForGroup(dataEditor.getProcess().getRuleset(), treeNode);
    }

    /**
     * Prepare addable metadata for logical structure element.
     *
     * @param metadataNodes metadata already assigned to the logical structure element
     */
    public void prepareAddableMetadataForStructure(List<TreeNode> metadataNodes) {
        // parameter "structureType" not required because "currentItem" is always true!
        dataEditor.getMetadataPanel().setSelectedMetadataTreeNode(null);
        addableMetadata = DataEditorService.getAddableMetadataForStructureElement(this.dataEditor, true, metadataNodes, null, true);
    }

    /**
     * Prepare addable metadata for media unit.
     *
     * @param metadataNodes metadata already assigned to media unit
     */
    public void prepareAddableMetadataForMediaUnit(List<TreeNode> metadataNodes) {
        // parameter "structureType" not required because "currentItem" is always true!
        addableMetadata = DataEditorService.getAddableMetadataForStructureElement(this.dataEditor, true, metadataNodes, null, false);
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
        this.dataEditor.getMetadataPanel().setAddMetadataKeySelectedItem(selectedMetadata);
    }

    /**
     * Get addableMetadata.
     *
     * @return value of addableMetadata
     */
    public List<SelectItem> getAddableMetadata() {
        return addableMetadata;
    }
}
