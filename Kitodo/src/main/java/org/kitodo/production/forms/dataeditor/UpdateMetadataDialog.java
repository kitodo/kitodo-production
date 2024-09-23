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

import java.util.LinkedList;
import java.util.List;

import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.MetadataComparison;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.PrimeFaces;

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
        try {
            metadataComparisons = DataEditorService.reimportCatalogMetadata(dataEditor.getProcess(),
                    dataEditor.getWorkpiece(), dataEditor.getMetadataPanel().getLogicalMetadataRows());
            if (metadataComparisons.isEmpty()) {
                PrimeFaces.current().executeScript("PF('metadataUnchangedDialog').show();");
            } else {
                PrimeFaces.current().ajax().update("updateMetadataDialog");
                PrimeFaces.current().executeScript("PF('updateMetadataDialog').show();");
            }
        } catch (Exception e) {
            Helper.setErrorMessage(e.getMessage());
        }
    }
}
