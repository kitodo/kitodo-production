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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.config.OPACConfig;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ParameterNotFoundException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.omnifaces.util.Ajax;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FileUploadDialog extends MetadataImportDialog {

    private String selectedCatalog;
    private static final Logger logger = LogManager.getLogger(FileUploadDialog.class);
    private boolean additionalImport = false;

    public FileUploadDialog(CreateProcessForm createProcessForm) {
        super(createProcessForm);
    }

    /**
     * import from csv file.
     *
     * @param event
     *            the file upload event
     */
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploadedFile = event.getFile();
        ImportService importService = ServiceManager.getImportService();
        try {
            Document internalDocument = importService.convertDataRecordToInternal(
                createRecordFromXMLElement(IOUtils.toString(uploadedFile.getInputStream(), Charset.defaultCharset())),
                selectedCatalog, false);
            TempProcess tempProcess = importService.createTempProcessFromDocument(selectedCatalog, internalDocument,
                createProcessForm.getTemplate().getId(), createProcessForm.getProject().getId());

            LinkedList<TempProcess> processes = new LinkedList<>();
            processes.add(tempProcess);

            Collection<String> higherLevelIdentifier = this.createProcessForm.getRulesetManagement()
                    .getFunctionalKeys(FunctionalMetadata.HIGHERLEVEL_IDENTIFIER);

            if (!higherLevelIdentifier.isEmpty()) {
                String parentID = importService.getParentID(internalDocument,
                    higherLevelIdentifier.toArray()[0].toString());
                importService.checkForParent(parentID, createProcessForm.getTemplate().getRuleset().getId(),
                    createProcessForm.getProject().getId());
                if (Objects.isNull(importService.getParentTempProcess())) {
                    TempProcess parentTempProcess = extractParentRecordFromFile(uploadedFile, internalDocument);
                    if (Objects.nonNull(parentTempProcess)) {
                        processes.add(parentTempProcess);
                    }
                }
            }

            if (createProcessForm.getProcesses().size() > 0 && additionalImport) {
                extendsMetadataTableOfMetadataTab(processes);
                Ajax.update(FORM_CLIENTID);
            } else {
                this.createProcessForm.setProcesses(processes);
                this.createProcessForm.fillCreateProcessForm(processes.getFirst());
                showRecord();
            }
        } catch (IOException | ProcessGenerationException | URISyntaxException | ParserConfigurationException
                | UnsupportedFormatException | SAXException | ConfigException | XPathExpressionException
                | TransformerException | DAOException | InvalidMetadataValueException
                | NoSuchMetadataFieldException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    private TempProcess extractParentRecordFromFile(UploadedFile uploadedFile, Document internalDocument)
            throws XPathExpressionException, UnsupportedFormatException, URISyntaxException, IOException,
            ParserConfigurationException, SAXException, ProcessGenerationException, TransformerException,
            InvalidMetadataValueException, NoSuchMetadataFieldException {
        Collection<String> higherLevelIdentifier = this.createProcessForm.getRulesetManagement()
                .getFunctionalKeys(FunctionalMetadata.HIGHERLEVEL_IDENTIFIER);

        if (higherLevelIdentifier.size() > 0) {
            ImportService importService = ServiceManager.getImportService();
            String parentID = importService.getParentID(internalDocument,
                higherLevelIdentifier.toArray()[0].toString());
            if (Objects.nonNull(parentID) && OPACConfig.isParentInRecord(selectedCatalog)) {
                Document internalParentDocument = importService.convertDataRecordToInternal(
                    createRecordFromXMLElement(
                        IOUtils.toString(uploadedFile.getInputStream(), Charset.defaultCharset())),
                    selectedCatalog, true);
                return importService.createTempProcessFromDocument(selectedCatalog, internalParentDocument,
                    createProcessForm.getTemplate().getId(), createProcessForm.getProject().getId());
            }
        }
        return null;
    }

    private DataRecord createRecordFromXMLElement(String xmlContent) {
        DataRecord record = new DataRecord();
        try {
            record.setMetadataFormat(
                MetadataFormat.getMetadataFormat(OPACConfig.getConfigValue(selectedCatalog, "metadataFormat")));
            record.setFileFormat(FileFormat.getFileFormat(OPACConfig.getConfigValue(selectedCatalog, "returnFormat")));
        } catch (ParameterNotFoundException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        record.setOriginalData(xmlContent);
        return record;
    }

    @Override
    public List<String> getCatalogs() {
        List<String> catalogs = super.getCatalogs();
        List<String> catalogsWithFileUpload = new ArrayList<>();
        for (String catalog : catalogs) {
            boolean isFileUpload = OPACConfig.getFileUploadConfig(catalog);
            if (isFileUpload) {
                catalogsWithFileUpload.add(catalog);
            }
        }
        return catalogsWithFileUpload;
    }

    /**
     * Get selectedCatalog.
     * 
     * @return the selected catalog.
     */
    public String getSelectedCatalog() {
        return selectedCatalog;
    }

    /**
     * Set selected catalog.
     * 
     * @param selectedCatalog
     *            the selected catalog.
     */
    public void setSelectedCatalog(String selectedCatalog) {
        this.selectedCatalog = selectedCatalog;
    }

    /**
     * Checks the additional import.
     *
     * @return true if is additional import
     */
    public boolean isAdditionalImport() {
        return additionalImport;
    }

    /**
     * Set additional import.
     *
     * @param additionalImport
     *            the value if is additional import
     */
    public void setAdditionalImport(boolean additionalImport) {
        this.additionalImport = additionalImport;
    }
}
