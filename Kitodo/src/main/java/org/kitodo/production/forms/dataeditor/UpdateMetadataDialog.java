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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.kitodo.api.Metadata;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.MetadataComparison;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;

public class UpdateMetadataDialog {

    private final DataEditorForm dataEditor;

    private List<MetadataComparison> metadataComparisons = new LinkedList<>();

    UpdateMetadataDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Get list of metadata comparisons displayed in 'UpdateMetadataDialog'.
     *
     * @return list of metadata comparisons
     */
    public List<MetadataComparison> getMetadataComparisons() {
        return metadataComparisons;
    }

    /**
     * Trigger re-import of metadata of current process.
     */
    public void updateCatalogMetadata() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            try {
                HashSet<Metadata> existingMetadata = getMetadata(dataEditor.getMetadataPanel().getLogicalMetadataRows());
                metadataComparisons = DataEditorService.reimportCatalogMetadata(dataEditor.getProcess(),
                        dataEditor.getWorkpiece(), existingMetadata, dataEditor.getPriorityList(),
                        dataEditor.getSelectedStructure().get().getType());
                if (metadataComparisons.isEmpty()) {
                    PrimeFaces.current().executeScript("PF('metadataUnchangedDialog').show();");
                } else {
                    PrimeFaces.current().ajax().update("updateMetadataDialog");
                    PrimeFaces.current().executeScript("PF('updateMetadataDialog').show();");
                }
            } catch (Exception e) {
                Helper.setErrorMessage(e.getMessage());
            }
        } else {
            Helper.setErrorMessage("Unable to update metadata: no logical structure selected!");
        }
    }

    private HashSet<Metadata> getMetadata(TreeNode treeNode) throws InvalidMetadataValueException {
        HashSet<Metadata> processDetails = new HashSet<>();
        for (TreeNode child : treeNode.getChildren()) {
            processDetails.addAll(((ProcessDetail) child.getData()).getMetadata(false));
        }
        return processDetails;
    }
}
