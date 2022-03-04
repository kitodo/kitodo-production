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

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ImportException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
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
    private Template template;
    private String importMode = "file";
    private String docType;
    private String selectedCatalog;
    private UploadedFile file;
    private InputStream fileInputStream;
    private String ppnString;
    private final MassImportService massImportService = ServiceManager.getMassImportService();
    private final List<Locale.LanguageRange> priorityList = ServiceManager.getUserService()
            .getCurrentMetadataLanguage();
    private static final String PROCESS_LIST_PATH = "/pages/processes.jsf?faces-redirect=true";
    private List<SelectItem> allDocTypes;
    private Workpiece workpiece;
    RulesetManagementInterface rulesetManagement;

    private final AdditionalMetadata additionalMetadata = new AdditionalMetadata(this);
    private final AddMetadataDialog addMetadataDialog = new AddMetadataDialog(this);

    public Workpiece getWorkpiece() {
        return workpiece;
    }

    public RulesetManagementInterface getRulesetManagement() {
        return rulesetManagement;
    }

    public List<Locale.LanguageRange> getPriorityList() {
        return priorityList;
    }

    public AdditionalMetadata getAdditionalMetadata() {
        return additionalMetadata;
    }

    public AddMetadataDialog getAddMetadataDialog() {
        return addMetadataDialog;
    }

    /**
     * prepare MassImportForm.
     * @param templateId template id
     * @param projectId project id
     */
    public void prepareMassImport(int templateId, int projectId) throws DAOException, IOException {
        this.projectId = projectId;
        this.templateId = templateId;
        workpiece = new Workpiece();
        template = ServiceManager.getTemplateService().getById(templateId);
        rulesetManagement = ServiceManager.getRulesetService().openRuleset(template.getRuleset());
        setAllDocTypes(getAllRulesetDivisions());
    }

    /**
     * import from csv file.
     *
     * @param event the file upload event
     */
    public void handleFileUpload(FileUploadEvent event) {
        file = event.getFile();
        try {
            fileInputStream = file.getInputStream();
        } catch (IOException e) {
            Helper.setErrorMessage(Helper.getTranslation("errorReading", file.getFileName()));
        }

    }

    /**
     * Import processes from CSV file.
     */
    public String importFromCSV() {
        try {
            massImportService.importFromCSV(selectedCatalog, fileInputStream, projectId, templateId,
                    workpiece.getLogicalStructure().getMetadata());
            return PROCESS_LIST_PATH;
        } catch (IOException e) {
            Helper.setErrorMessage(Helper.getTranslation("errorReading", file.getFileName()));
        } catch (ImportException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return stayOnCurrentPage;
    }

    /**
     * Import processes from textField.
     */
    public String importFromText() {
        try {
            massImportService.importFromText(selectedCatalog, ppnString, projectId, templateId,
                    workpiece.getLogicalStructure().getMetadata());
            return PROCESS_LIST_PATH;
        } catch (ImportException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return stayOnCurrentPage;
    }

    /**
     * Import processes from CSV file or from textField.
     * @return the referring view, to return there
     */
    public String massImport() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        additionalMetadata.getProcessDetails().preserve();
        if (importMode.equals("file")) {
            return importFromCSV();
        } else if (importMode.equals("text")) {
            return importFromText();
        } else {
            return stayOnCurrentPage;
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
     * @param projectId as int
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
     * @param templateId as int
     */
    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    /**
     * Gets template.
     *
     * @return value of template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Get list of catalogs.
     *
     * @return list of catalogs
     */
    public List<String> getCatalogs() {
        try {
            return ServiceManager.getImportService().getAvailableCatalogs();
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Gets IdentifierParameter Label of chosen catalog.
     * @return IdentifierParameter Label
     */
    public String getIdentifierParameterLabel() {
        String label = ServiceManager.getImportService().getIdentifierParameterLabel(getSelectedCatalog());
        return Objects.isNull(label) || label.isEmpty() ? "Identifiers" : label + 's';
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
     * @param selectedCatalog as java.lang.String
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
     * @param file as org.primefaces.model.UploadedFile
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
        return StringUtils.isBlank(ppnString) ? null : ppnString;
    }

    /**
     * Set ppnString.
     *
     * @param ppnString as java.lang.String
     */
    public void setPpnString(String ppnString) {
        this.ppnString = ppnString;
    }

    /**
     * Get docType.
     * @return docType as String
     */
    public String getDocType() {
        return docType;
    }

    /**
     * set docType.
     * @param docType as String
     */
    public void setDocType(String docType) {
        if (Objects.isNull(this.docType) || this.docType.isEmpty() || !this.docType.equals(docType)) {
            if (Objects.isNull(allDocTypes) || allDocTypes.isEmpty()) {
                this.docType = "";
                Helper.setErrorMessage("errorLoadingDocTypes");
            } else {
                this.docType = docType;
            }
            workpiece.getLogicalStructure().setType(docType);
            if (this.docType.isEmpty()) {
                additionalMetadata.setProcessDetails(ProcessFieldedMetadata.EMPTY);
            } else {
                additionalMetadata.initializeProcessDetails();
            }
        }
    }

    /**
     * Gets importMode.
     *
     * @return value of importMode
     */
    public String getImportMode() {
        return importMode;
    }

    /**
     * Sets importMode.
     *
     * @param importMode value of importMode
     */
    public void setImportMode(String importMode) {
        this.importMode = importMode;
    }

    private List<SelectItem> getAllRulesetDivisions() throws IOException {
        List<SelectItem> allDocTypes = rulesetManagement
                .getStructuralElements(priorityList).entrySet()
                .stream().map(entry -> new SelectItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        if (allDocTypes.isEmpty()) {
            Helper.setErrorMessage("errorLoadingDocTypes");
        }
        return allDocTypes;
    }

    /**
     * Get all document types.
     *
     * @return list of all ruleset divisions
     */
    public List<SelectItem> getAllDoctypes() {
        return allDocTypes;
    }

    /**
     * Set allDocTypes.
     *
     * @param allDocTypes as java.util.List
     */
    void setAllDocTypes(List<SelectItem> allDocTypes) {
        this.allDocTypes = allDocTypes;
    }
}
