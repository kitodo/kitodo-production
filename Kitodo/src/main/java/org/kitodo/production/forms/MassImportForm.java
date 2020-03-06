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

package org.kitodo.production.forms;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.RulesetNotFoundException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.MassImportService;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.xml.sax.SAXException;

@Named("MassImportForm")
@SessionScoped
public class MassImportForm extends BaseForm {

    private int projectId;
    private int templateId;
    private String selectedCatalog;
    private UploadedFile file;
    private String ppnString;
    private MassImportService massImportService = ServiceManager.getMassImportService();

    public void prepareMassImport(int templateId, int projectId) {
        this.projectId = projectId;
        this.templateId = templateId;
    }

    /**
     * import from csv file.
     * 
     * @param event
     *            the file upload event
     */
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        try {
            massImportService.importFromCSV(selectedCatalog, file, projectId, templateId);
        } catch (IOException | NoRecordFoundException | ParserConfigurationException | UnsupportedFormatException
                | XPathExpressionException | URISyntaxException | SAXException | ProcessGenerationException
                | RulesetNotFoundException | InvalidMetadataValueException | DataException
                | NoSuchMetadataFieldException | DAOException e) {
            Helper.setErrorMessage(Helper.getTranslation("errorReading", file.getFileName()));
        }
    }

    /**
     * Import processes from textField.
     */
    public void importFromText() {
        try {
            massImportService.importFromText(selectedCatalog, ppnString, projectId, templateId);
        } catch (NoRecordFoundException | ParserConfigurationException | UnsupportedFormatException
                | XPathExpressionException | URISyntaxException | SAXException | ProcessGenerationException
                | IOException | RulesetNotFoundException | InvalidMetadataValueException | DataException
                | NoSuchMetadataFieldException | DAOException e) {
            Helper.setErrorMessage(Helper.getTranslation("errorReading", file.getFileName()));
        }
    }

    /**
     * Get projectId.
     *
     * @return value of projectId
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * Set projectId.
     *
     * @param projectId
     *            as int
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * Get templateId.
     *
     * @return value of templateId
     */
    public int getTemplateId() {
        return templateId;
    }

    /**
     * Set templateId.
     *
     * @param templateId
     *            as int
     */
    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    /**
     * Get selectedCatalog.
     *
     * @return value of selectedCatalog
     */
    public String getSelectedCatalog() {
        return selectedCatalog;
    }

    /**
     * Set selectedCatalog.
     *
     * @param selectedCatalog
     *            as java.lang.String
     */
    public void setSelectedCatalog(String selectedCatalog) {
        this.selectedCatalog = selectedCatalog;
    }

    /**
     * Get file.
     *
     * @return value of file
     */
    public UploadedFile getFile() {
        return file;
    }

    /**
     * Set file.
     *
     * @param file
     *            as org.primefaces.model.UploadedFile
     */
    public void setFile(UploadedFile file) {
        this.file = file;
    }

    /**
     * Get ppnString.
     *
     * @return value of ppnString
     */
    public String getPpnString() {
        return ppnString;
    }

    /**
     * Set ppnString.
     *
     * @param ppnString
     *            as java.lang.String
     */
    public void setPpnString(String ppnString) {
        this.ppnString = ppnString;
    }
}
