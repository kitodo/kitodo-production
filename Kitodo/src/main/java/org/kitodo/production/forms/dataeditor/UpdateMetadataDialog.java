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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataformat.Division;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.MetadataComparison;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;

/**
 * Manages the dialog when a user clicks on the update metadata button.
 * 
 * <p>Re-imports the catalog record for a known record identifier and import configuration
 * and visualizes the differences between the current metadata and re-imported metadata of the
 * catalog.</p>
 */
@Named("UpdateMetadataDialog")
@ViewScoped
public class UpdateMetadataDialog implements Serializable {

    private static final Logger logger = LogManager.getLogger(UpdateMetadataDialog.class);

    @Inject
    private DataEditorForm dataEditor;

    private List<MetadataComparison> metadataComparisons = new LinkedList<>();

    private String recordIdentifier = "";

    /**
     * Get list of metadata comparisons displayed in 'UpdateMetadataDialog'.
     *
     * @return list of metadata comparisons
     */
    public List<MetadataComparison> getMetadataComparisons() {
        return metadataComparisons;
    }

    /**
     * Return catalog identifier of record whose metadata is imported.
     * 
     * <p>The record identifier is shown to the user in the metadata comparison dialog.</p>
     * 
     * @return the record identifier
     */
    public String getRecordIdentifier() {
        return this.recordIdentifier;
    }

    /**
     * Set record identifier of record whose metadata is imported.
     * 
     * <p>The record identifier is shown to the user in the metadata comparison dialog.</p>
     * 
     * @param recordIdentifier the record identifier
     */
    public void setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }

    /**
     * Perform metadata update for current process.
     */
    public void applyMetadataUpdate() {
        Division<?> division = dataEditor.getMetadataPanel().getLogicalMetadataTable().getDivision();
        if (Objects.nonNull(division) && division instanceof LogicalDivision) {
            DataEditorService.updateMetadataWithNewValues((LogicalDivision) division, getMetadataComparisons());
            dataEditor.getMetadataPanel().update();
        } else {
            Helper.setErrorMessage("cannot update metadata of non-logical division");
        }        
    }

    /**
     * Check and return whether conditions for metadata update are met or not.
     *
     * @return whether metadata of process can be updated
     */
    public boolean canUpdateMetadata() {
        try {
            return DataEditorService.canUpdateCatalogMetadata(
                dataEditor.getProcess(), dataEditor.getWorkpiece(), dataEditor.getStructurePanel().getSelectedLogicalNodeIfSingle()
            );
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return false;
        }
    }

    /**
     * Is called when a user clicks on the update metadata button of the logical metadata panel.
     */
    public void onUpdateCatalogMetadataClick() {
        if (canUpdateMetadata()) {
            Process process = dataEditor.getProcess();
            // update metadata from catalog using existing record identifier and import configuration
            updateCatalogMetadata(Objects.nonNull(process)
                    && Objects.nonNull(process.getImportConfiguration())
                    && process.getImportConfiguration().getValidateExternalData());
        }
    }

    /**
     * Trigger re-import of metadata of current process.
     *
     * @param validate whether to validate re-imported metadata against XML schemata
     */
    public void updateCatalogMetadata(boolean validate) {
        if (dataEditor.getSelectedStructure().isPresent()) {
            setRecordIdentifier(dataEditor.getProcessRecordIdentifier());
            try {
                HashSet<Metadata> existingMetadata = getMetadata(dataEditor.getMetadataPanel().getLogicalMetadataRows());
                metadataComparisons = DataEditorService.reimportCatalogMetadata(dataEditor.getProcess(), dataEditor.getWorkpiece(),
                        existingMetadata, dataEditor.getPriorityList(), dataEditor.getSelectedStructure().get().getType(), validate);
                if (metadataComparisons.isEmpty()) {
                    PrimeFaces.current().executeScript("PF('metadataUnchangedDialog').show();");
                } else {
                    PrimeFaces.current().ajax().update("updateMetadataDialog");
                    PrimeFaces.current().executeScript("PF('updateMetadataDialog').show();");
                }
            } catch (FileStructureValidationException e) {
                // in case of schema validation error show validation error dialog with details instead of simple error message)
                dataEditor.setValidationErrorTitle(Helper.getTranslation("validation.invalidExternalRecord"));
                dataEditor.showValidationExceptionDialog(e, null);
            } catch (Exception e) {
                Helper.setErrorMessage(e.getMessage());
            }
        } else {
            Helper.setErrorMessage("Unable to update metadata: no logical structure selected!");
        }
    }

    private HashSet<Metadata> getMetadata(TreeNode<Object> treeNode) throws InvalidMetadataValueException {
        HashSet<Metadata> processDetails = new HashSet<>();
        for (TreeNode<Object> child : treeNode.getChildren()) {
            processDetails.addAll(((ProcessDetail) child.getData()).getMetadata(false));
        }
        return processDetails;
    }
}
