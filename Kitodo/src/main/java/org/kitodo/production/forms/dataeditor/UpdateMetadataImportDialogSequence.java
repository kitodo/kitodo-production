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
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.constants.StringConstants;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.forms.createprocess.CreateProcessForm;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.MetadataComparison;
import org.kitodo.production.helper.ProcessHelper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;
import org.xml.sax.SAXException;

/**
 * Manages the dialog sequence when a user requests to update the metadata of a logical structure element.
 * 
 * <p>First, the catalog search dialog is shown, where the user can select an import configuration and 
 * search parameters. If the catalog search matches multiple records, the hit list dialog is shown. 
 * Finally, the metadata comparison dialog allows selecting the specific metadata information
 * imported for a logical structure element.</p>
 */
@Named("UpdateMetadataImportDialogSequence")
@ViewScoped
public class UpdateMetadataImportDialogSequence implements Serializable {

    private static final Logger logger = LogManager.getLogger(UpdateMetadataImportDialogSequence.class);

    @Inject
    private CreateProcessForm createProcessForm;

    @Inject
    private DataEditorForm dataEditorForm;

    @Inject
    private UpdateMetadataDialog updateMetadataDialog;

    /** 
     * Is called when a user clicks on the import metadata button of the logical metadata panel.
     */
    public void onImportMetadataClick() {
        if (canImportMetadata()) {
            importMetadata();
        }
    }

    /**
     * Return true if any logical division is selected such that catalog metadata can be imported.
     * 
     * @return true if a logical division is selected
     */
    public boolean canImportMetadata() {
        TreeNode selectedNode = dataEditorForm.getStructurePanel().getSelectedLogicalNodeIfSingle();
        return Objects.nonNull(selectedNode)
                && selectedNode.getData() instanceof StructureTreeNode
                && ((StructureTreeNode) selectedNode.getData()).getDataObject() instanceof LogicalDivision;
    }

    /**
     * Open catalog search dialog when the user requests to import metadata from a catalog record.
     */
    public void importMetadata() {
        int templateId = dataEditorForm.getProcess().getTemplate().getId();
        int projectId = dataEditorForm.getProcess().getProject().getId();

        String previouslySelectedField = createProcessForm.getCatalogImportDialog().getSelectedField();
        ImportConfiguration previouslySelectedImportConfiguration = createProcessForm.getCurrentImportConfiguration();

        createProcessForm.prepareProcess(templateId, projectId, null, null, false);

        // recover previously selected import configuration and search field
        if (Objects.nonNull(previouslySelectedImportConfiguration)) {
            createProcessForm.setCurrentImportConfiguration(previouslySelectedImportConfiguration);
        }
        if (Objects.nonNull(previouslySelectedField) && !previouslySelectedField.isEmpty()) {
            createProcessForm.getCatalogImportDialog().setSelectedField(previouslySelectedField);
        }

        // always reset search term to empty value
        createProcessForm.getCatalogImportDialog().setSearchTerm("");

        PrimeFaces.current().executeScript("PF('catalogSearchDialog').show();");
    }

    /**
     * Perform a catalog search and open either the histlist dialog (if there are multiple search results) 
     * or the metadata comparison dialog, if there is only a single search result.
     */
    public void catalogSearch() {
        createProcessForm.getProcesses().clear();
        createProcessForm.getCatalogImportDialog().search();
        showMetadataComparisonDialog();
    }

    /**
     * Open the metadata comparison dialog after a user selects a record from the hitlist dialog.
     */
    public void selectRecord() {
        String recordId = Helper.getRequestParameter("ID");
        updateMetadataDialog.setRecordIdentifier(recordId);

        try {
            LinkedList<TempProcess> processes = ServiceManager.getImportService().importProcessHierarchy(
                recordId,
                createProcessForm.getCurrentImportConfiguration(), 
                createProcessForm.getProject().getId(), 
                createProcessForm.getTemplate().getId(), 
                1,
                createProcessForm.getRulesetManagement().getFunctionalKeys(FunctionalMetadata.HIGHERLEVEL_IDENTIFIER),
                    false
            );
            createProcessForm.setProcesses(processes);
            showMetadataComparisonDialog();
        } catch (IOException | ProcessGenerationException | XPathExpressionException | URISyntaxException
                 | ParserConfigurationException | UnsupportedFormatException | SAXException | DAOException
                 | ConfigException | TransformerException | NoRecordFoundException | InvalidMetadataValueException
                 | NoSuchMetadataFieldException | FileStructureValidationException e) {
            logger.error("error when selecting record in hitlist", e);
        }
    }

    /**
     * Extract metadata comparison information from create process form (after catalog search was performed by the user) 
     * and show comparison dialog.
     */
    private void showMetadataComparisonDialog() {
        try {
            if (!createProcessForm.getProcesses().isEmpty()) {
                Process process = dataEditorForm.getProcess();
                TempProcess tempProcess = createProcessForm.getProcesses().getFirst();
                
                List<MetadataComparison> metadataComparisons = updateMetadataDialog.getMetadataComparisons();
                metadataComparisons.clear();
            
                HashSet<Metadata> existingMetadata = getMetadata(dataEditorForm.getMetadataPanel().getLogicalMetadataRows());

                RulesetManagementInterface ruleset = ServiceManager.getRulesetService().openRuleset(process.getRuleset());
                for (Metadata metadata : tempProcess.getWorkpiece().getLogicalStructure().getMetadata()) {
                    ProcessHelper.setMetadataDomain(metadata, ruleset);
                }

                ProcessHelper.generateAtstslFields(tempProcess, Collections.emptyList(), StringConstants.EDIT, false);

                metadataComparisons.addAll(
                    DataEditorService.initializeMetadataComparisons(
                        process, 
                        existingMetadata, 
                        tempProcess.getWorkpiece().getLogicalStructure().getMetadata(), 
                        dataEditorForm.getPriorityList(),
                        dataEditorForm.getSelectedStructure().get().getType()
                    )
                );

                if (metadataComparisons.isEmpty()) {
                    PrimeFaces.current().executeScript("PF('metadataUnchangedDialog').show();");
                } else {
                    PrimeFaces.current().ajax().update("updateMetadataDialog");
                    PrimeFaces.current().executeScript("PF('updateMetadataDialog').show();");
                }

            }
        } catch (InvalidMetadataValueException | IOException | NoSuchMetadataFieldException | ProcessGenerationException e) {
            logger.error("error trying to compile metadata for the metadata comparison dialog", e);
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
