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

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.constants.StringConstants;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ConfigException;
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

@Named("ImportMetadataDialogSequence")
@ViewScoped
public class ImportMetadataDialogSequence implements Serializable {

    private static final Logger logger = LogManager.getLogger(ImportMetadataDialogSequence.class);

    @Inject
    private CreateProcessForm createProcessForm;

    @Inject
    private DataEditorForm dataEditorForm;

    /**
     * Open catalog search dialog when the user requests to import metadata from a catalog record.
     */
    public void importMetadata() {
        int templateId = dataEditorForm.getProcess().getTemplate().getId();
        int projectId = dataEditorForm.getProcess().getProject().getId();

        createProcessForm.prepareProcess(templateId, projectId, null, null);

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

        try {
            LinkedList<TempProcess> processes = ServiceManager.getImportService().importProcessHierarchy(
                recordId,
                createProcessForm.getCurrentImportConfiguration(), 
                createProcessForm.getProject().getId(), 
                createProcessForm.getTemplate().getId(), 
                1,
                createProcessForm.getRulesetManagement().getFunctionalKeys(FunctionalMetadata.HIGHERLEVEL_IDENTIFIER)
            );
            createProcessForm.setProcesses(processes);
            showMetadataComparisonDialog();
        } catch (IOException | ProcessGenerationException | XPathExpressionException | URISyntaxException
                     | ParserConfigurationException | UnsupportedFormatException | SAXException | DAOException
                     | ConfigException | TransformerException | NoRecordFoundException | InvalidMetadataValueException
                     | NoSuchMetadataFieldException e) {
            logger.error("error when selecting record in hitlist", e);
        }
    }

    private void showMetadataComparisonDialog() {
        try {
            if (!createProcessForm.getProcesses().isEmpty()) {
                Process process = dataEditorForm.getProcess();
                TempProcess tempProcess = createProcessForm.getProcesses().get(0);
                
                List<MetadataComparison> metadataComparisons = dataEditorForm.getUpdateMetadataDialog().getMetadataComparisons();
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

            };
        } catch (InvalidMetadataValueException | IOException | NoSuchMetadataFieldException | ProcessGenerationException e) {
            logger.error("error trying to compile metadata for the metadata comparison dialog", e);
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
