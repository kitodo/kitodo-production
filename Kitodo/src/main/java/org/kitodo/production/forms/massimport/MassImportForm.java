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

package org.kitodo.production.forms.massimport;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.ImportException;
import org.kitodo.exceptions.KitodoCsvImportException;
import org.kitodo.production.enums.SeparatorCharacter;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.forms.CsvRecord;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.XMLUtils;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.MassImportService;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

@Named("MassImportForm")
@ViewScoped
public class MassImportForm extends BaseForm {

    private static final Logger logger = LogManager.getLogger(MassImportForm.class);

    private int projectId;
    private int templateId;
    private String templateTitle;
    private Ruleset ruleset;
    private ImportConfiguration importConfiguration;
    private UploadedFile file;
    private SeparatorCharacter csvSeparator = SeparatorCharacter.COMMA;
    private SeparatorCharacter metadataGroupEntrySeparator = SeparatorCharacter.DOLLAR;
    private List<String> metadataKeys = new LinkedList<>();
    private List<CsvRecord> records = new LinkedList<>();
    private String importedCsvHeaderLine = "";
    private List<String> importedCsvLines = new LinkedList<>();
    private final MassImportService massImportService = ServiceManager.getMassImportService();
    private final AddMetadataDialog addMetadataDialog = new AddMetadataDialog(this);
    private LinkedList<HashMap<String, String>> importResults = new LinkedList<>();
    private Integer progress = 0;
    private Boolean rulesetConfigurationForOpacImportComplete = null;
    private String configurationError = null;
    private Boolean skipEmptyColumns = true;
    private static final String recordIdentifier = "recordIdentifier";
    private static final String errorMessage = "errorMessage";
    private static final String processTitle = "processTitle";

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
            Template template = ServiceManager.getTemplateService().getById(templateId);
            templateTitle = template.getTitle();
            ruleset = template.getRuleset();
            RulesetManagementInterface rulesetInterface = ServiceManager.getRulesetService().openRuleset(template.getRuleset());
            addMetadataDialog.setRulesetManagement(rulesetInterface);
            checkRecordIdentifierConfigured(rulesetInterface);
        } catch (DAOException | IOException e) {
            Helper.setErrorMessage(e);
        }
    }

    private void checkRecordIdentifierConfigured(RulesetManagementInterface ruleset) {
        if (Objects.isNull(rulesetConfigurationForOpacImportComplete)) {
            rulesetConfigurationForOpacImportComplete = ServiceManager.getImportService()
                    .isRecordIdentifierMetadataConfigured(ruleset);
        }
        if (!rulesetConfigurationForOpacImportComplete) {
            PrimeFaces.current().executeScript("PF('recordIdentifierMissingDialog').show();");
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
            resetValues();
            if (!csvLines.isEmpty()) {
                importedCsvHeaderLine = csvLines.get(0);
                csvSeparator = ServiceManager.getMassImportService().guessCsvSeparator(csvLines);
                updateMetadataKeys();
                if (csvLines.size() > 1) {
                    importedCsvLines = csvLines.subList(1, csvLines.size());
                    parseCsvLines();
                }
            }
        } catch (IOException | CsvException | KitodoCsvImportException e) {
            Helper.setErrorMessage(e);
            records = new LinkedList<>();
        }
    }

    /**
     * Parses the lines of a CSV file and processes the data for further use.
     *
     * <p>
     * This method updates the metadata keys, validates the integrity of the imported records,
     * and checks for consistency between the number of metadata keys and the data in the CSV.
     * It also handles scenarios such as missing separators, empty columns, and skips empty columns
     * if the configured option is enabled.
     *
     * <p>
     * When the parsing or validation fails due to mismatched separators, missing metadata keys,
     * or other issues, appropriate error or warning messages are set, and an empty record
     * structure is initialized.
     *
     * <p>
     *
     * @throws IOException if an error occurs while reading the CSV lines
     * @throws KitodoCsvImportException if an error specific to Kitodo CSV import occurs
     * @throws CsvException if an error occurs during parsing of the CSV file
     */
    public void parseCsvLines() throws IOException, KitodoCsvImportException, CsvException {
        try {
            updateMetadataKeys();
            records = massImportService.parseLines(importedCsvLines, csvSeparator.getSeparator());
            boolean success = true;
            if (!records.isEmpty() && !records.get(0).getCsvCells().isEmpty()
                    && records.get(0).getCsvCells().size() != metadataKeys.size()) {
                Helper.setErrorMessage(Helper.getTranslation("massImport.separatorCountMismatchHeader", csvSeparator.toString()));
                records = new LinkedList<>();
                success = false;
            }
            if (success && skipEmptyColumns) {
                List<Integer> skipIndices = ServiceManager.getMassImportService().getColumnSkipIndices(records, metadataKeys.size());
                if (!skipIndices.isEmpty()) {
                    metadataKeys = discardMetadataKeysOfEmptyColumns(metadataKeys, skipIndices);
                    records = discardEmptyColumns(records, skipIndices);
                }
            }
            if (missingFunctionalMetadataInFirstColumn()) {
                Helper.setErrorMessage("massImport.invalidConfigurationFirstColumn");
            }
        } catch (IOException | CsvException | KitodoCsvImportException e) {
            Helper.setErrorMessage(e);
            records = new LinkedList<>();
        }
    }

    private List<String> discardMetadataKeysOfEmptyColumns(List<String> metadataKeys, List<Integer> skipIndices) {
        return ServiceManager.getMassImportService().discardMetadataKeysOfEmptyColumns(metadataKeys, skipIndices);
    }

    private List<CsvRecord> discardEmptyColumns(List<CsvRecord> records, List<Integer> skipIndices) {
        return ServiceManager.getMassImportService().discardEmptyColumns(records, skipIndices);
    }

    private void resetValues() {
        metadataKeys = new LinkedList<>();
        records = new LinkedList<>();
        importResults = new LinkedList<>();
        importedCsvHeaderLine = "";
        importedCsvLines = new LinkedList<>();
    }

    private void updateMetadataKeys() {
        metadataKeys = Arrays.stream(importedCsvHeaderLine.split(Pattern.quote(String.valueOf(csvSeparator.getSeparator())), -1))
                .map(XMLUtils::removeBom)
                .map(String::trim)
                .collect(Collectors.toList());
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
        importResults = new LinkedList<>();
        PrimeFaces.current().ajax().update("massImportResultDialog");
        try {
            LinkedList<LinkedHashMap<String, List<String>>> presetMetadata = massImportService.prepareMetadata(metadataKeys, records);
            if (firstColumnContainsRecordsIdentifier()) {
                importRecords(presetMetadata);
            } else if (firstColumnContainsDoctype()) {
                createProcessesFromCsvData(presetMetadata);
            } else {
                Helper.setErrorMessage("massImport.invalidConfigurationFirstColumn");
            }
            PrimeFaces.current().executeScript("PF('massImportProgressBar').cancel();"
                    + "PF('massImportProgressDialog').hide();PF('massImportResultDialog').show();");
            PrimeFaces.current().ajax().update("massImportResultDialog");
        } catch (ImportException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        } catch (ConfigException e) {
            configurationError = e.getLocalizedMessage();
            PrimeFaces.current().executeScript("PF('massImportProgressBar').cancel();");
            PrimeFaces.current().executeScript("PF('configErrorDialog').show();");
            PrimeFaces.current().ajax().update("configErrorDialog");
        }
    }

    /**
     * Prepare massimport by resetting progress and import success map.
     */
    public void prepare() {
        progress = 0;
        importResults = new LinkedList<>();
        PrimeFaces.current().ajax().update("massImportProgressForm:massImportProgress");
    }

    /**
     * Import records by ID and add preset metadata.
     *
     * @param processMetadata List preset metadata lists, with first metadata containing record ID for catalog search
     */
    private void importRecords(LinkedList<LinkedHashMap<String, List<String>>> processMetadata) {
        ImportService importService = ServiceManager.getImportService();
        PrimeFaces.current().ajax().update("massImportProgressDialog");
        for (LinkedHashMap<String, List<String>> record : processMetadata) {
            HashMap<String, String> entryMap = new HashMap<>();
            try {
                try {
                    String id = importService.getRecordId(record, templateId, true);
                    entryMap.put(recordIdentifier, id);
                } catch (ConfigException | IOException | DAOException ee) {
                    logger.info(ee.getLocalizedMessage());
                    entryMap.put(recordIdentifier, null);
                }
                Process process = importService.importProcessForMassImport(projectId, templateId, importConfiguration, record);
                entryMap.put(processTitle, process.getTitle());
            } catch (ImportException e) {
                entryMap.put(errorMessage, e.getLocalizedMessage());
            }
            importResults.add(entryMap);
            PrimeFaces.current().ajax().update("massImportProgressDialog");
        }
    }

    /**
     * Create records purely with given preset metadata.
     *
     * @param processMetadata List preset metadata lists, with first metadata containing document type of process to be created
     */
    private void createProcessesFromCsvData(LinkedList<LinkedHashMap<String, List<String>>> processMetadata) {
        ImportService importService = ServiceManager.getImportService();
        PrimeFaces.current().ajax().update("massImportProgressDialog");
        for (LinkedHashMap<String, List<String>> entry : processMetadata) {
            HashMap<String, String> entryMap = new HashMap<>();
            try {
                try {
                    String id = importService.getRecordId(entry, templateId, false);
                    if (StringUtils.isNotBlank(id)) {
                        entryMap.put(recordIdentifier, id);
                    }
                } catch (DAOException | IOException ex) {
                    logger.error(ex.getLocalizedMessage());
                }
                Process process = importService.createProcessFromData(projectId, templateId, entry,
                        metadataGroupEntrySeparator.getSeparator());
                entryMap.put(processTitle, process.getTitle());
            } catch (Exception e) {
                entryMap.put(errorMessage, e.getLocalizedMessage());
            }
            importResults.add(entryMap);
            PrimeFaces.current().ajax().update("massImportProgressDialog");
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
            String metadataKey = metadataKeys.get(columnIndex);
            try {
                return ServiceManager.getRulesetService().getMetadataTranslation(addMetadataDialog.getRulesetManagement(), metadataKey,
                        metadataGroupEntrySeparator.getSeparator());
            } catch (IOException e) {
                Helper.setErrorMessage(e);
                return metadataKey;
            }
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
     * Get importConfiguration.
     *
     * @return value of importConfiguration
     */
    public ImportConfiguration getImportConfigurationId() {
        return importConfiguration;
    }

    /**
     * Set importConfiguration.
     *
     * @param importConfiguration
     *            as ImportConfiguration
     */
    public void setImportConfigurationId(ImportConfiguration importConfiguration) {
        this.importConfiguration = importConfiguration;
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
    public SeparatorCharacter getCsvSeparator() {
        return csvSeparator;
    }

    /**
     * Set csvSeparator.
     *
     * @param csvSeparator as org.kitodo.production.enums.SeparatorCharacter
     */
    public void setCsvSeparator(SeparatorCharacter csvSeparator) {
        this.csvSeparator = csvSeparator;
    }


    /**
     * Get metadataGroupEntrySeparator.
     *
     * @return value of metadataGroupEntrySeparator
     */
    public SeparatorCharacter getMetadataGroupEntrySeparator() {
        return metadataGroupEntrySeparator;
    }

    /**
     * Set metadataGroupEntrySeparator.
     *
     * @param metadataGroupEntrySeparator as org.kitodo.production.enums.SeparatorCharacter
     */
    public void setMetadataGroupEntrySeparator(SeparatorCharacter metadataGroupEntrySeparator) {
        this.metadataGroupEntrySeparator = metadataGroupEntrySeparator;
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
     * @param metadataKeys as List of String
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
     * @return value of csvSeparatorCharacters from MassImportService
     */
    public SeparatorCharacter[] getCsvSeparatorCharacters() {
        return ServiceManager.getMassImportService().getCsvSeparatorCharacters();
    }

    /**
     * Returns an array of {@link SeparatorCharacter} available as MetadataGroup separators. This list omits the
     * {@link SeparatorCharacter} currently selected as CSV column separator character.
     *
     * @return array of available MetadataGroup separators
     */
    public SeparatorCharacter[] getMetadataGroupEntrySeparators() {
        return Arrays.stream(ServiceManager.getMassImportService().getCsvSeparatorCharacters())
                .filter(separatorChar -> !csvSeparator.equals(separatorChar))
                .toArray(SeparatorCharacter[]::new);
    }

    /**
     * Get templateTitle.
     *
     * @return value of templateTitle
     */
    public String getTemplateTitle() {
        return templateTitle;
    }

    /**
     * Get rulesetTitle.
     *
     * @return value of rulesetTitle
     */
    public String getRulesetTitle() {
        return ruleset.getTitle();
    }

    /**
     * Gets addMetadataDialog.
     *
     * @return value of addMetadataDialog
     */
    public AddMetadataDialog getAddMetadataDialog() {
        return addMetadataDialog;
    }

    /**
     * Return a list of maps containing recordIdentifiers and process titles of successful imports.
     *
     * @return list of maps containing record identifier and process titles of successful imports
     */
    public LinkedList<HashMap<String, String>> getSuccessfulImports() {
        if (Objects.nonNull(importResults)) {
            return importResults.stream().filter(map -> Objects.isNull(map.get(errorMessage)))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return new LinkedList<>();
    }

    /**
     * Return a list of maps containing recordIdentifiers and error messages of failed imports.
     *
     * @return list of maps containing record identifier and error messages of failed imports
     */
    public LinkedList<HashMap<String, String>> getFailedImports() {
        if (Objects.nonNull(importResults)) {
            return importResults.stream().filter(map -> Objects.nonNull(map.get(errorMessage)))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return new LinkedList<>();
    }

    /**
     * Remove metadata key and CsvCells with given index from list of metadata keys and all current CsvRecords.
     *
     * @param index index of metadata key and CsvCells to remove
     */
    public void removeMetadata(int index) {
        if (index < metadataKeys.size()) {
            metadataKeys.remove(index);
            for (CsvRecord csvRecord : records) {
                csvRecord.getCsvCells().remove(index);
            }
        }
    }

    /**
     * Get mass import progress.
     *
     * @return mass import progress
     */
    public int getProgress() {
        if (records.isEmpty()) {
            progress = 0;
        } else {
            progress = (importResults.size() * 100) / records.size();
        }
        PrimeFaces.current().ajax().update("massImportProgressForm:massImportProgress");
        return progress;
    }

    /**
     * Get number of already imported records.
     *
     * @return number of imported records
     */
    public int getNumberOfProcessesRecords() {
        return importResults.size();
    }

    /**
     * Get ConfigurationError value.
     * @return configuration error value
     */
    public String getConfigurationError() {
        return configurationError;
    }

    /**
     * Check and return whether first column contains metadata that is _not_ configured as "recordIdentifier" or "docType"
     * in ruleset.
     * @return 'true' if first column contains metadata that is not configured as "recordIdentifier" or "docType" and
     *          'false' otherwise
     */
    public boolean missingFunctionalMetadataInFirstColumn() {
        return !(firstColumnContainsRecordsIdentifier() || firstColumnContainsDoctype());
    }

    /**
     * Check and return whether first column contains metadata configured as "recordIdentifier" in ruleset or not.
     * @return 'true' if first column contains functional metadata of type "recordIdentifier" and 'false' otherwise
     */
    public boolean firstColumnContainsRecordsIdentifier() {
        if (metadataKeys.isEmpty()) {
            return false;
        } else {
            return ServiceManager.getImportService().isRecordIdentifierMetadata(addMetadataDialog.getRulesetManagement(),
                    metadataKeys.iterator().next());
        }
    }

    /**
     * Check and return whether first column contains metadata configured as "docType" in ruleset or not.
     * @return 'true' if first column contains functional metadata of type "docType" and 'false' otherwise
     */
    public boolean firstColumnContainsDoctype() {
        if (metadataKeys.isEmpty()) {
            return false;
        } else {
            return ServiceManager.getImportService().isDocTypeMetadata(addMetadataDialog.getRulesetManagement(),
                    metadataKeys.iterator().next());
        }
    }

    /**
     * Retrieve and return CSS style class of functional metadata with given index metadata key list 'metadataKeys'.
     * @param index index of metadata in list 'metadataKeys' whose CSS class is returned
     * @return CSS style class of metadata as String
     */
    public String getFunctionalMetadataStyleClass(int index) {
        try {
            return ServiceManager.getImportService().getFunctionalMetadataStyleClass(addMetadataDialog.getRulesetManagement(),
                    metadataKeys, index, metadataGroupEntrySeparator);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
            return "";
        }
    }

    /**
     * Generate and return label for table containing records imported from CSV file or added manually.
     *
     * @return record table label
     */
    public String getDataRecordLabel() {
        if (Objects.isNull(this.file) || StringUtils.isBlank(this.file.getFileName())) {
            return records.size() + " " + Helper.getTranslation("records");
        } else {
            return records.size() + " " + Helper.getTranslation("records") + " (\"" + this.file.getFileName() + "\")";
        }
    }

    /**
     * Get value of 'skipEmptyColumns'.
     *
     * @return value of 'skipEmptyColumns'
     */
    public Boolean getSkipEmptyColumns() {
        return skipEmptyColumns;
    }

    /**
     * Set value of 'skipEmptyColumns'.
     *
     * @param skipEmptyColumns value of 'skipEmptyColumns'
     */
    public void setSkipEmptyColumns(Boolean skipEmptyColumns) {
        this.skipEmptyColumns = skipEmptyColumns;
    }

    /**
     * Check and return whether the button to initiate the mass import should be disabled or not.
     *
     * @return "true" if the list of records is empty, the first column does not contain the necessary functional metadata
     *                or the first column contains record identifier but no catalog has been selected as source for the metadata import
     *         "false" otherwise
     */
    public boolean isMassImportDisabled() {
        return records.isEmpty()
                || missingFunctionalMetadataInFirstColumn()
                || (firstColumnContainsRecordsIdentifier() && Objects.isNull(importConfiguration));
    }

    /**
     * Whether metadata keys contain recordIdentifier or not.
     *
     * @return whether metadata keys contain recordIdentifier or not
     */
    public boolean isMetadataKeysContainRecordIdentifier() {
        return MassImportService.metadataKeyListContainsRecordIdentifier(metadataKeys, addMetadataDialog.getRulesetManagement());
    }

    /**
     * Get label of functional metadata 'recordIdentifier', retrieved from current ruleset.
     *
     * @return label of functional metadata 'recordIdentifier', if such metadata is defined in ruleset; returns empty
     *         String otherwise
     */
    public String getRecordIdentifierLabel() {
        try {
            return MassImportService.getRecordIdentifierMetadataLabel(metadataKeys, addMetadataDialog.getRulesetManagement());
        } catch (IOException e) {
            logger.error(e);
            return "";
        }
    }
}
