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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ImportException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.MassImportService;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

@Named("MassImportForm")
@ViewScoped
public class MassImportForm extends BaseForm {

    private static final Logger logger = LogManager.getLogger(MassImportForm.class);

    private int projectId;
    private int templateId;
    private String templateTitle;
    private String selectedCatalog;
    private UploadedFile file;
    private String csvSeparator = ";";
    private String previousCsvSeparator = null;
    private List<String> metadataKeys = Collections.singletonList("ID");
    private List<CsvRecord> records = new LinkedList<>();
    private final List<Character> csvSeparatorCharacters = Arrays.asList(',', ';');
    private final MassImportService massImportService = ServiceManager.getMassImportService();
    private static final String PROCESS_LIST_PATH = "/pages/processes.jsf?faces-redirect=true";

    /**
     * Prepare mass import.
     *
     * @param templateId ID of template used to create processes during mass import
     * @param projectId ID of project for which processes are created
     */
    public void prepareMassImport(int templateId, int projectId) {
        this.projectId = projectId;
        this.templateId = templateId;
        try {
            this.templateTitle = ServiceManager.getTemplateService().getById(templateId).getTitle();
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
        }
    }

    /**
     * Handle file upload.
     *
     * @param event FileUploadEvent to handle
     */
    public void handleFileUpload(FileUploadEvent event) {
        file = event.getFile();
        try {
            List<String> csvLines = massImportService.getLines(file);
            metadataKeys = new LinkedList<>();
            records = new LinkedList<>();
            if (!csvLines.isEmpty()) {
                metadataKeys = Arrays.asList(csvLines.get(0).split(csvSeparator, -1));
                if (csvLines.size() > 1) {
                    records = massImportService.parseLines(csvLines.subList(1, csvLines.size()), csvSeparator);
                }
            }
        } catch (IOException e) {
            Helper.setErrorMessage(e);
        }
    }

    /**
     * Event listender function called when user switches CSV separator character used to split text lines into cells.
     */
    public void changeSeparator() {
        metadataKeys = List.of(String.join(previousCsvSeparator, metadataKeys).split(csvSeparator));
        records = massImportService.updateSeparator(records, previousCsvSeparator, csvSeparator);
    }

    /**
     * Add new CSV lines.
     */
    public void addRecord() {
        records.add(new CsvRecord(metadataKeys.size()));
    }

    /**
     * Remove CSV record.
     *
     * @param csvRecord CSV record to remove
     */
    public void removeLine(CsvRecord csvRecord) {
        records.remove(csvRecord);
    }

    /**
     * Import all records from list.
     */
    public void startMassImport() {
        try {
            massImportService.importRows(selectedCatalog, metadataKeys, records, projectId, templateId);
            FacesContext context = FacesContext.getCurrentInstance();
            String path = context.getExternalContext().getRequestContextPath() + PROCESS_LIST_PATH;
            context.getExternalContext().redirect(path);
        } catch (IOException e) {
            Helper.setErrorMessage(Helper.getTranslation("errorReading", file.getFileName()));
        } catch (ImportException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Get column header for column with index "columnIndex".
     *
     * @param columnIndex index of column for which column header is returned
     * @return column header
     */
    public String getColumnHeader(Integer columnIndex) {
        if (columnIndex < metadataKeys.size()) {
            return metadataKeys.get(columnIndex);
        }
        return "";
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
        return StringUtils.isBlank(selectedCatalog) ? null : selectedCatalog;
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
     * Get csvSeparator.
     *
     * @return value of csvSeparator
     */
    public String getCsvSeparator() {
        return csvSeparator;
    }

    /**
     * Set csvSeparator.
     *
     * @param csvSeparator as java.lang.String
     */
    public void setCsvSeparator(String csvSeparator) {
        this.previousCsvSeparator = this.csvSeparator;
        this.csvSeparator = csvSeparator;
    }

    /**
     * Get metadataKeys.
     *
     * @return value of metadataKeys
     */
    public List<String> getMetadataKeys() {
        return metadataKeys;
    }

    /**
     * Set metadataKeys.
     *
     * @param metadataKeys as java.util.List<java.lang.String>
     */
    public void setMetadataKeys(List<String> metadataKeys) {
        this.metadataKeys = metadataKeys;
    }

    /**
     * Get records.
     *
     * @return value of records
     */
    public List<CsvRecord> getRecords() {
        return records;
    }

    /**
     * Set records.
     *
     * @param records as List of CsvRecord
     */
    public void setRecords(List<CsvRecord> records) {
        this.records = records;
    }

    /**
     * Get csvSeparatorCharacters.
     *
     * @return value of csvSeparatorCharacters
     */
    public List<Character> getCsvSeparatorCharacters() {
        return csvSeparatorCharacters;
    }

    /**
     * Get templateTitle.
     *
     * @return value of templateTitle
     */
    public String getTemplateTitle() {
        return templateTitle;
    }
}
