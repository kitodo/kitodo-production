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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.helper.XMLUtils;
import org.kitodo.production.services.ServiceManager;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.xml.sax.SAXException;

public class FileUploadDialog extends MetadataImportDialog {

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
        try {
            String xmlString = IOUtils.toString(uploadedFile.getInputStream(), Charset.defaultCharset());
            createProcessForm.setXmlString(XMLUtils.removeBom(xmlString));
            createProcessForm.setFilename(uploadedFile.getFileName());
            if (MetadataFormat.EAD.name().equals(createProcessForm.getCurrentImportConfiguration().getMetadataFormat())
                    && createProcessForm.limitExceeded(createProcessForm.getXmlString())) {
                createProcessForm.calculateNumberOfEadElements();
                Ajax.update("maxNumberOfRecordsExceededDialog");
                PrimeFaces.current().executeScript("PF('maxNumberOfRecordsExceededDialog').show();");
            } else {
                processXmlString();
            }
        } catch (IOException | ProcessGenerationException | URISyntaxException | ParserConfigurationException
                 | UnsupportedFormatException | SAXException | ConfigException | XPathExpressionException
                 | TransformerException | DAOException | InvalidMetadataValueException | NoSuchMetadataFieldException
                 | XMLStreamException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    private void processXmlString() throws UnsupportedFormatException, XPathExpressionException,
            ProcessGenerationException, URISyntaxException, IOException, ParserConfigurationException, SAXException,
            InvalidMetadataValueException, TransformerException, NoSuchMetadataFieldException, DAOException {
        LinkedList<TempProcess> processes = ServiceManager.getImportService().processUploadedFile(createProcessForm);
        if (!createProcessForm.getProcesses().isEmpty() && additionalImport) {
            extendsMetadataTableOfMetadataTab(processes);
        } else {
            createProcessForm.setProcesses(processes);
            TempProcess currentTempProcess = processes.getFirst();
            createProcessForm.fillCreateProcessForm(currentTempProcess);
            attachToExistingParentAndGenerateAtstslIfNotExist(currentTempProcess);
            Ajax.update(FORM_CLIENTID);
        }
    }

    @Override
    public List<ImportConfiguration> getImportConfigurations() {
        if (Objects.isNull(importConfigurations)) {
            try {
                importConfigurations = ServiceManager.getImportConfigurationService().getAllFileUploadConfigurations();
            } catch (IllegalArgumentException | DAOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                importConfigurations = new LinkedList<>();
            }
        }
        return importConfigurations;
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
