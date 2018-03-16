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

package de.sub.goobi.forms;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.export.download.ExportPdf;
import de.sub.goobi.export.download.Multipage;
import de.sub.goobi.export.download.TiffHeader;
import de.sub.goobi.helper.GoobiScript;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.WebDav;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.export.ExportXmlLog;
import org.goobi.production.flow.helper.SearchResultGeneration;
import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.StatisticsRenderingElement;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.jdom.transform.XSLTransformException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.enums.ObjectMode;
import org.kitodo.enums.ObjectType;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.kitodo.services.workflow.WorkflowService;

@Named("ProzessverwaltungForm")
@SessionScoped
public class ProzessverwaltungForm extends BasisForm {
    private static final long serialVersionUID = 2838270843176821134L;
    private static final Logger logger = LogManager.getLogger(ProzessverwaltungForm.class);
    private Process process = new Process();
    private Task task = new Task();
    private StatisticsManager statisticsManager;
    private List<ProcessCounterObject> processCounterObjects;
    private HashMap<String, Integer> counterSummary;
    private Property templateProperty;
    private Property workpieceProperty;
    private ObjectMode displayMode = ObjectMode.PROCESS;
    private ObjectMode editMode = ObjectMode.NONE;
    private String kitodoScript;
    private HashMap<String, Boolean> anzeigeAnpassen;
    private String newProcessTitle;
    private String selectedXslt = "";
    private StatisticsRenderingElement myCurrentTable;
    private boolean showClosedProcesses = false;
    private boolean showInactiveProjects = false;
    private List<Property> properties;
    private List<Property> templates;
    private List<Property> workpieces;
    private Property property;
    private String addToWikiField = "";
    private List<ProcessDTO> processDTOS = new ArrayList<>();
    private boolean showStatistics = false;
    private transient ServiceManager serviceManager = new ServiceManager();
    private transient FileService fileService = serviceManager.getFileService();
    private transient WorkflowService workflowService = serviceManager.getWorkflowService();
    private static String DONEDIRECTORYNAME = "fertig/";
    private List<ProcessDTO> selectedProcesses = new ArrayList<>();

    private static final String PROCESS_LIST_PATH = TEMPLATE_ROOT + "processes";
    private static final String PROCESS_LIST_PATH_OLD = TEMPLATE_ROOT + "ProzessverwaltungAlle";
    private static final String PROCESS_EDIT_PATH = TEMPLATE_ROOT + "processEdit";
    private static final String PROCESS_EDIT_PATH_OLD = TEMPLATE_ROOT + "ProzessverwaltungBearbeiten";
    private static final String TASK_EDIT_PATH = TEMPLATE_ROOT + "taskEdit";
    private static final String TASK_EDIT_PATH_OLD = TEMPLATE_ROOT + "inc_Prozessverwaltung/schritt";

    /**
     * Constructor.
     */
    public ProzessverwaltungForm() {
        super();
        this.anzeigeAnpassen = new HashMap<>();
        this.anzeigeAnpassen.put("lockings", false);
        this.anzeigeAnpassen.put("swappedOut", false);
        this.anzeigeAnpassen.put("selectionBoxes", false);
        this.anzeigeAnpassen.put("processId", false);
        this.anzeigeAnpassen.put("batchId", false);
        this.sortierung = "titelAsc";
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getProcessService()));
        /*
         * Vorgangsdatum generell anzeigen?
         */
        User user = getUser();
        if (user != null) {
            this.anzeigeAnpassen.put("processDate", user.isConfigProductionDateShow());
        } else {
            this.anzeigeAnpassen.put("processDate", false);
        }
        DONEDIRECTORYNAME = ConfigCore.getParameter("doneDirectoryName", "fertig/");
    }

    /**
     * needed for ExtendedSearch.
     *
     * @return always true
     */
    public boolean getInitialize() {
        return true;
    }

    /**
     * Create new process.
     */
    public String newProcess() {
        this.process = new Process();
        this.newProcessTitle = "";
        this.editMode = ObjectMode.PROCESS;
        setProperties(new ArrayList<>());
        return redirectToEdit();
    }

    /**
     * Create new template.
     *
     * @return page
     */
    public String newTemplate() {
        this.process = new Process();
        this.newProcessTitle = "";
        this.process.setTemplate(true);
        this.editMode = ObjectMode.PROCESS;
        return PROCESS_EDIT_PATH_OLD;
    }

    /**
     * Edit process.
     *
     * @return page
     */
    public String editProcess() {
        reload();
        return redirectToEdit();
    }

    /**
     * Save process.
     *
     * @return null
     */
    public String save() {
        /*
         * wenn der Vorgangstitel geändert wurde, wird dieser geprüft und bei
         * erfolgreicher Prüfung an allen relevanten Stellen mitgeändert
         */
        if (this.process != null && this.process.getTitle() != null) {
            if (!this.process.getTitle().equals(this.newProcessTitle) && this.newProcessTitle != null
                    && !renameAfterProcessTitleChanged()) {
                return null;
            }

            try {
                serviceManager.getProcessService().save(this.process);
            } catch (DataException e) {
                Helper.setErrorMessage("fehlerNichtSpeicherbar", logger, e);
            }
        } else {
            Helper.setFehlerMeldung("titleEmpty");
        }
        return reload();
    }

    /**
     * Save process and redirect to list view.
     *
     * @return url to list view
     */
    public String saveAndRedirect() {
        if (this.process != null && this.process.getTitle() != null) {
            if (!this.process.getTitle().equals(this.newProcessTitle) && this.newProcessTitle != null
                    && !renameAfterProcessTitleChanged()) {
                return null;
            }

            try {
                serviceManager.getProcessService().save(this.process);
            } catch (DataException e) {
                Helper.setErrorMessage("fehlerNichtSpeicherbar", logger, e);
            }
        } else {
            Helper.setFehlerMeldung("titleEmpty");
        }
        return redirectToList();
    }

    /**
     * Delete process.
     *
     * @return page or empty String
     */
    public String delete() {
        deleteMetadataDirectory();
        this.process.getProject().getProcesses().remove(this.process);
        try {
            serviceManager.getProcessService().remove(this.process);
        } catch (DataException e) {
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation("prozess") }, logger, e);
            return null;
        }
        if (this.displayMode.equals(ObjectMode.TEMPLATE)) {
            return filterTemplates();
        } else {
            return filterAll();
        }
    }

    /**
     * Remove content.
     *
     * @return String
     */
    public String deleteContent() {
        // deleteMetadataDirectory();
        try {
            URI ocr = fileService.getOcrDirectory(this.process);
            if (fileService.fileExist(ocr)) {
                fileService.delete(ocr);
            }
            URI images = fileService.getImagesDirectory(this.process);
            if (fileService.fileExist(images)) {
                fileService.delete(images);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }

        Helper.setMeldung("Content deleted");
        return null;
    }

    private boolean renameAfterProcessTitleChanged() {
        String validateRegEx = ConfigCore.getParameter("validateProzessTitelRegex", "[\\w-]*");
        if (!this.newProcessTitle.matches(validateRegEx)) {
            this.editMode = ObjectMode.PROCESS;
            Helper.setFehlerMeldung(Helper.getTranslation("UngueltigerTitelFuerVorgang"));
            return false;
        } else {
            // process properties
            for (Property processProperty : this.process.getProperties()) {
                if (processProperty != null && processProperty.getValue() != null
                        && (processProperty.getValue().contains(this.process.getTitle()))) {
                    processProperty.setValue(processProperty.getValue()
                            .replaceAll(this.process.getTitle(), this.newProcessTitle));
                }
            }
            // template properties
            for (Property templateProperty : this.process.getTemplates()) {
                if (templateProperty.getValue().contains(this.process.getTitle())) {
                    templateProperty.setValue(templateProperty.getValue()
                            .replaceAll(this.process.getTitle(), this.newProcessTitle));
                }
            }
            // workpiece properties
            for (Property workpieceProperty : this.process.getWorkpieces()) {
                if (workpieceProperty.getValue().contains(this.process.getTitle())) {
                    workpieceProperty.setValue(workpieceProperty.getValue()
                            .replaceAll(this.process.getTitle(), this.newProcessTitle));
                }
            }

            try {
                renameImageDirectories();
                renameOcrDirectories();
                renameDefinedDirectories();
            } catch (Exception e) {
                Helper.setErrorMessage("errorRenaming", new Object[] {Helper.getTranslation("directory") }, logger, e);
            }

            this.process.setTitle(this.newProcessTitle);

            if (!this.process.isTemplate()) {
                // remove Tiffwriter file
                GoobiScript gs = new GoobiScript();
                ArrayList<Process> pro = new ArrayList<>();
                pro.add(this.process);
                gs.deleteTiffHeaderFile(pro);
                gs.updateImagePath(pro);
            }
        }
        return true;
    }

    private void renameImageDirectories() throws IOException {
        URI imageDirectory = fileService.getImagesDirectory(process);
        renameDirectories(imageDirectory);
    }

    private void renameOcrDirectories() throws IOException {
        URI ocrDirectory = fileService.getOcrDirectory(process);
        renameDirectories(ocrDirectory);
    }

    private void renameDirectories(URI directory) throws IOException {
        if (fileService.isDirectory(directory)) {
            ArrayList<URI> subDirs = fileService.getSubUris(directory);
            for (URI imageDir : subDirs) {
                if (fileService.isDirectory(imageDir)) {
                    fileService.renameFile(imageDir, fileService.getFileName(imageDir)
                            .replace(process.getTitle(), newProcessTitle));
                }
            }
        }
    }

    private void renameDefinedDirectories() {
        String[] processDirs = ConfigCore.getStringArrayParameter("processDirs");
        for (String processDir : processDirs) {
            // TODO: check it out
            URI processDirAbsolute = serviceManager.getProcessService().getProcessDataDirectory(process)
                    .resolve(processDir.replace("(processtitle)", process.getTitle()));

            File dir = new File(processDirAbsolute);
            if (dir.isDirectory()) {
                dir.renameTo(new File(dir.getAbsolutePath().replace(process.getTitle(), newProcessTitle)));
            }
        }
    }

    private void deleteMetadataDirectory() {
        for (Task task : this.process.getTasks()) {
            this.task = task;
            deleteSymlinksFromUserHomes();
        }
        try {
            fileService.delete(serviceManager.getProcessService().getProcessDataDirectory(this.process));
            URI ocrDirectory = fileService.getOcrDirectory(this.process);
            if (fileService.fileExist(ocrDirectory)) {
                fileService.delete(ocrDirectory);
            }
        } catch (Exception e) {
            Helper.setErrorMessage("Can not delete metadata directory", logger, e);
        }
    }

    /**
     * Filter current processes.
     *
     * @return page
     */
    public String filterCurrentProcesses() {
        this.statisticsManager = null;
        this.processCounterObjects = null;

        try {
            if (this.filter.equals("")) {
                filterProcessesWithoutFilter();
            } else {
                filterProcessesWithFilter();
            }

            this.page = new Page<>(0, this.processDTOS);
        } catch (DataException e) {
            Helper.setErrorMessage("ProzessverwaltungForm.filterCurrentProcesses", logger, e);
            return null;
        }
        this.displayMode = ObjectMode.PROCESS;
        return redirectToList();
    }

    /**
     * Filter templates.
     *
     * @return page
     */
    public String filterTemplates() {
        this.statisticsManager = null;
        this.processCounterObjects = null;
        try {
            if (this.filter.equals("")) {
                filterTemplatesWithoutFilter();
            } else {
                filterTemplatesWithFilter();
            }
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }

        this.page = new Page<>(0, this.processDTOS);
        this.displayMode = ObjectMode.TEMPLATE;
        return redirectToList();
    }

    /**
     * This method initializes the process list without any filter whenever the bean
     * is constructed.
     */
    @PostConstruct
    public void initializeProcessList() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String originLink = params.get("linkId");
        if (Objects.equals(originLink, "newProcess")) {
            createNewProcess();
        } else if (Objects.equals(originLink, "templates")) {
            filterTemplates();
        } else if (Objects.equals(originLink, "allProcesses")) {
            filterCurrentProcesses();
        }
        setFilter("");
    }

    /**
     * New process insert.
     *
     * @return page
     */
    public String createNewProcess() {
        filterTemplates();
        if (this.page.getTotalResults() == 1) {
            ProcessDTO single = (ProcessDTO) this.page.getListReload().get(0);
            ProzesskopieForm pkf = (ProzesskopieForm) Helper.getManagedBeanValue("#{ProzesskopieForm}");
            try {
                Process process = serviceManager.getProcessService().convertDtoToBean(single);
                if (pkf != null) {
                    pkf.setProzessVorlage(process);
                    return pkf.prepare(process.getId());
                }
                return "";
            } catch (DAOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
        return PROCESS_LIST_PATH_OLD;
    }

    /**
     * Anzeige der Sammelbände filtern.
     */
    public String filterAll() {
        this.statisticsManager = null;
        this.processCounterObjects = null;
        /*
         * Filter für die Auflistung anwenden
         */
        try {
            if (this.filter.equals("")) {
                if (this.displayMode.equals(ObjectMode.TEMPLATE)) {
                    filterTemplatesWithoutFilter();
                } else {
                    filterProcessesWithoutFilter();
                }
            } else {
                if (this.displayMode.equals(ObjectMode.TEMPLATE)) {
                    filterTemplatesWithFilter();
                } else {
                    filterProcessesWithFilter();
                }
            }
            this.page = new Page<>(0, processDTOS);
        } catch (DataException e) {
            Helper.setErrorMessage("fehlerBeimEinlesen", logger, e);
            return null;
        } catch (NumberFormatException ne) {
            Helper.setErrorMessage("Falsche Suchparameter angegeben", logger, ne);
            return null;
        } catch (UnsupportedOperationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return redirectToList();
    }

    private void filterProcessesWithFilter() throws DataException {
        BoolQueryBuilder query = serviceManager.getFilterService().queryBuilder(this.filter, ObjectType.PROCESS, false,
                false, false);
        if (!this.showClosedProcesses) {
            query.must(serviceManager.getProcessService().getQuerySortHelperStatus(false));
        }
        if (!this.showInactiveProjects) {
            query.must(serviceManager.getProcessService().getQueryProjectActive(true));
        }
        processDTOS = serviceManager.getProcessService().findByQuery(query, sortList(), false);
    }

    private void filterProcessesWithoutFilter() throws DataException {
        if (!this.showClosedProcesses) {
            if (!this.showInactiveProjects) {
                processDTOS = serviceManager.getProcessService()
                        .findOpenAndActiveProcessesWithoutTemplates(sortList());
            } else {
                processDTOS = serviceManager.getProcessService().findNotClosedProcessesWithoutTemplates(sortList());
            }
        } else {
            if (!this.showInactiveProjects) {
                processDTOS = serviceManager.getProcessService().findAllActiveWithoutTemplates(sortList());
            } else {
                processDTOS = serviceManager.getProcessService().findAllWithoutTemplates(sortList());
            }
        }
    }

    private void filterTemplatesWithFilter() throws DataException {
        BoolQueryBuilder query = serviceManager.getFilterService().queryBuilder(this.filter, ObjectType.PROCESS, true,
                false, false);
        if (!this.showClosedProcesses) {
            query.must(serviceManager.getProcessService().getQuerySortHelperStatus(false));
        }
        if (!this.showInactiveProjects) {
            query.must(serviceManager.getProcessService().getQueryProjectActive(true));
        }
        processDTOS = serviceManager.getProcessService().findByQuery(query, sortList(), false);
    }

    private void filterTemplatesWithoutFilter() throws DataException {
        if (!this.showClosedProcesses) {
            if (!this.showInactiveProjects) {
                processDTOS = serviceManager.getProcessService().findAllOpenAndActiveTemplates(sortList());
            } else {
                processDTOS = serviceManager.getProcessService().findAllNotClosedTemplates(sortList());
            }
        } else {
            if (!this.showInactiveProjects) {
                processDTOS = serviceManager.getProcessService().findTemplatesOfActiveProjects(sortList());
            } else {
                processDTOS = serviceManager.getTemplateService().findAllTemplates(sortList());
            }
        }
    }

    private String sortList() {
        String sort = SortBuilders.fieldSort("title").order(SortOrder.ASC).toString();
        if (this.sortierung.equals("titelAsc")) {
            sort += "," + SortBuilders.fieldSort("title").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("titelDesc")) {
            sort += "," + SortBuilders.fieldSort("title").order(SortOrder.DESC).toString();
        }
        if (this.sortierung.equals("batchAsc")) {
            sort += ", " + SortBuilders.fieldSort("batches.id").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("batchDesc")) {
            sort += ", " + SortBuilders.fieldSort("batches.id").order(SortOrder.DESC).toString();
        }
        if (this.sortierung.equals("projektAsc")) {
            sort += ", " + SortBuilders.fieldSort("project").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("projektDesc")) {
            sort += ", " + SortBuilders.fieldSort("project").order(SortOrder.DESC).toString();
        }
        if (this.sortierung.equals("vorgangsdatumAsc")) {
            sort += "," + SortBuilders.fieldSort("creationDate").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("vorgangsdatumDesc")) {
            sort += "," + SortBuilders.fieldSort("creationDate").order(SortOrder.DESC).toString();
        }
        if (this.sortierung.equals("fortschrittAsc")) {
            sort += "," + SortBuilders.fieldSort("sortHelperStatus").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("fortschrittDesc")) {
            sort += "," + SortBuilders.fieldSort("sortHelperStatus").order(SortOrder.DESC).toString();
        }
        return sort;
    }

    /**
     * Remove template properties.
     */
    public void deleteTemplateProperty() {
        try {
            this.templateProperty.getProcesses().clear();
            this.process.getTemplates().remove(this.templateProperty);
            serviceManager.getProcessService().save(this.process);
            serviceManager.getPropertyService().remove(this.templateProperty);
        } catch (DataException e) {
            Helper.setErrorMessage("propertiesNotDeleted", logger, e);
        }
        loadTemplateProperties();
    }

    /**
     * Remove workpiece properties.
     */
    public void deleteWorkpieceProperty() {
        try {
            this.workpieceProperty.getProcesses().clear();
            this.process.getWorkpieces().remove(this.workpieceProperty);
            serviceManager.getProcessService().save(this.process);
            serviceManager.getPropertyService().remove(this.workpieceProperty);
        } catch (DataException e) {
            Helper.setErrorMessage("propertiesNotDeleted", logger, e);
        }
        loadWorkpieceProperties();
    }

    /**
     * Create new template property.
     */
    public void createTemplateProperty() {
        if (this.templates == null) {
            this.templates = new ArrayList<>();
        }
        Property property = new Property();
        property.setType(PropertyType.STRING);
        this.templates.add(property);
        this.templateProperty = property;
    }

    /**
     * Create new workpiece property.
     */
    public void createWorkpieceProperty() {
        if (this.workpieces == null) {
            this.workpieces = new ArrayList<>();
        }
        Property property = new Property();
        property.setType(PropertyType.STRING);
        this.workpieces.add(property);
        this.workpieceProperty = property;
    }

    /**
     * Save template property.
     */
    public void saveTemplateProperty() {
        try {
            serviceManager.getPropertyService().save(this.templateProperty);
            if (!this.process.getTemplates().contains(this.templateProperty)) {
                this.process.getTemplates().add(this.templateProperty);
            }
            serviceManager.getProcessService().save(this.process);
            Helper.setMeldung("propertiesSaved");
        } catch (DataException e) {
            Helper.setErrorMessage("propertiesNotSaved", logger, e);
        }
        loadTemplateProperties();
    }

    /**
     * Save workpiece property.
     */
    public void saveWorkpieceProperty() {
        try {
            serviceManager.getPropertyService().save(this.workpieceProperty);
            if (!this.process.getWorkpieces().contains(this.workpieceProperty)) {
                this.process.getWorkpieces().add(this.workpieceProperty);
            }
            serviceManager.getProcessService().save(this.process);
            Helper.setMeldung("propertiesSaved");
        } catch (DataException e) {
            Helper.setErrorMessage("propertiesNotSaved", logger, e);
        }
        loadWorkpieceProperties();
    }

    /**
     * New task.
     */
    public String newTask() {
        this.task = new Task();
        this.task.setProcess(this.process);
        this.process.getTasks().add(this.task);
        this.editMode = ObjectMode.TASK;
        return redirectToEditTask();
    }

    /**
     * Take task.
     */
    public void saveTask() {
        this.task.setEditTypeEnum(TaskEditType.ADMIN);
        task.setProcessingTime(new Date());
        User user = getUser();
        if (user != null) {
            task.setProcessingUser(user);
        }

        try {
            serviceManager.getTaskService().save(this.task);
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("arbeitsschritt") }, logger, e);
        }
    }

    /**
     * Save task and redirect to processEdit view.
     *
     * @return url to processEdit view
     */
    public String saveTaskAndRedirect() {
        saveTask();
        return redirectToEdit();
    }

    /**
     * Remove task.
     *
     * @return page
     */
    public String deleteTask() {
        try {
            this.process.getTasks().remove(this.task);
            List<User> users = this.task.getUsers();
            for (User user : users) {
                user.getTasks().remove(this.task);
            }

            List<UserGroup> userGroups = this.task.getUserGroups();
            for (UserGroup userGroup : userGroups) {
                userGroup.getTasks().remove(this.task);
            }
            deleteSymlinksFromUserHomes();
            serviceManager.getTaskService().remove(this.task);
            return redirectToEdit();
        } catch (DataException e) {
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation("arbeitsschritt") }, logger, e);
            return null;
        }

    }

    // TODO deleteTask() and removeTask() can be merged, when old templates have been removed
    /**
     * Remove task.
     *
     */
    public void removeTask() {
        try {
            this.process.getTasks().remove(this.task);
            List<User> users = this.task.getUsers();
            for (User user : users) {
                user.getTasks().remove(this.task);
            }

            List<UserGroup> userGroups = this.task.getUserGroups();
            for (UserGroup userGroup : userGroups) {
                userGroup.getTasks().remove(this.task);
            }
            deleteSymlinksFromUserHomes();
            serviceManager.getTaskService().remove(this.task);
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    private void deleteSymlinksFromUserHomes() {
        WebDav webDav = new WebDav();
        /* alle Benutzer */
        for (User user : this.task.getUsers()) {
            try {
                webDav.uploadFromHome(user, this.task.getProcess());
            } catch (RuntimeException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
        /* alle Benutzergruppen mit ihren Benutzern */
        for (UserGroup userGroup : this.task.getUserGroups()) {
            for (User user : userGroup.getUsers()) {
                try {
                    webDav.uploadFromHome(user, this.task.getProcess());
                } catch (RuntimeException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                }
            }
        }
    }

    /**
     * Remove User.
     *
     * @return empty String
     */
    public String deleteUser() {
        Integer userId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            User user = serviceManager.getUserService().getById(userId);
            this.task.getUsers().remove(user);
            return null;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
            return null;
        }
    }

    /**
     * Remove UserGroup.
     *
     * @return empty String
     */
    public String deleteUserGroup() {
        Integer userGroupId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            UserGroup userGroup = serviceManager.getUserGroupService().getById(userGroupId);
            this.task.getUserGroups().remove(userGroup);
            return null;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
            return null;
        }
    }

    /**
     * Add UserGroup.
     *
     * @return empty String
     */
    public String addUserGroup() {
        Integer userGroupId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            UserGroup userGroup = serviceManager.getUserGroupService().getById(userGroupId);
            for (UserGroup taskUserGroup : this.task.getUserGroups()) {
                if (taskUserGroup.equals(userGroup)) {
                    return null;
                }
            }
            this.task.getUserGroups().add(userGroup);
            return null;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
            return null;
        }
    }

    /**
     * Add User.
     *
     * @return empty String
     */
    public String addUser() {
        Integer userId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            User user = serviceManager.getUserService().getById(userId);
            for (User taskUser : this.task.getUsers()) {
                if (taskUser.equals(user)) {
                    return null;
                }
            }
            this.task.getUsers().add(user);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
            return null;
        }
        return null;
    }

    /**
     * Export METS.
     */
    public void exportMets(int id) {
        ExportMets export = new ExportMets();
        try {
            this.process = serviceManager.getProcessService().getById(id);
            export.startExport(this.process);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("prozess"), id }, logger, e);
        } catch (Exception e) {
            Helper.setErrorMessage("An error occurred while trying to export METS file for: " + this.process.getTitle(),
                logger, e);
        }
    }

    /**
     * Export PDF.
     */
    public void exportPdf(int id) {
        ExportPdf export = new ExportPdf();
        try {
            this.process = serviceManager.getProcessService().getById(id);
            export.startExport(this.process);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("prozess"), id }, logger, e);
        } catch (Exception e) {
            Helper.setErrorMessage("An error occurred while trying to export PDF file for: " + this.process.getTitle(),
                logger, e);
        }
    }

    /**
     * Export DMS.
     */
    public void exportDMS(int id) {
        ExportDms export = new ExportDms();
        try {
            this.process = serviceManager.getProcessService().getById(id);
            export.startExport(this.process);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("prozess"), id }, logger, e);
        } catch (Exception e) {
            Helper.setErrorMessage("An error occurred while trying to export to DMS for: " + this.process.getTitle(),
                logger, e);
        }
    }

    /**
     * Export DMS page.
     */
    @SuppressWarnings("unchecked")
    public void exportDMSPage() {
        ExportDms export = new ExportDms();
        Boolean flagError = false;
        for (ProcessDTO processDTO : (List<ProcessDTO>) this.page.getListReload()) {
            try {
                Process process = serviceManager.getProcessService().convertDtoToBean(processDTO);
                export.startExport(process);
            } catch (Exception e) {
                Helper.setErrorMessage("errorExporting",
                    new Object[] {Helper.getTranslation("prozess"), processDTO.getId() }, logger, e);
                flagError = true;
            }
        }
        if (flagError) {
            Helper.setFehlerMeldung("ExportFinishedWithErrors");
        } else {
            Helper.setMeldung(null, "ExportFinished", "");
        }
    }

    /**
     * Export DMS selection.
     */
    @SuppressWarnings("unchecked")
    public void exportDMSSelection() {
        ExportDms export = new ExportDms();
        for (ProcessDTO processDTO : this.getSelectedProcesses()) {
            try {
                export.startExport(serviceManager.getProcessService().convertDtoToBean(processDTO));
            } catch (Exception e) {
                Helper.setErrorMessage("ExportError", logger, e);
            }
        }
        Helper.setMeldung(null, "ExportFinished", "");
    }

    /**
     * Export DMS hits.
     */
    @SuppressWarnings("unchecked")
    public void exportDMSHits() {
        ExportDms export = new ExportDms();
        for (Process proz : (List<Process>) this.page.getCompleteList()) {
            try {
                export.startExport(proz);
            } catch (Exception e) {
                Helper.setErrorMessage("ExportError", logger, e);
            }
        }
        logger.info(Helper.getTranslation("ExportFinished"));
        Helper.setMeldung(null, "ExportFinished", "");
    }

    /**
     * Upload all from home.
     *
     * @return empty String
     */
    public String uploadFromHomeAll() {
        WebDav myDav = new WebDav();
        List<URI> folder = myDav.uploadAllFromHome(DONEDIRECTORYNAME);
        myDav.removeAllFromHome(folder, URI.create(DONEDIRECTORYNAME));
        Helper.setMeldung(null, "directoryRemovedAll", DONEDIRECTORYNAME);
        return null;
    }

    /**
     * Upload from home.
     *
     * @return empty String
     */
    public String uploadFromHome() {
        WebDav myDav = new WebDav();
        myDav.uploadFromHome(this.process);
        Helper.setMeldung(null, "directoryRemoved", this.process.getTitle());
        return null;
    }

    /**
     * Download to home.
     */
    public void downloadToHome() {
        /*
         * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in
         * Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde, ansonsten
         * Download
         */
        if (!serviceManager.getProcessService().isImageFolderInUse(this.process)) {
            WebDav myDav = new WebDav();
            myDav.downloadToHome(this.process, false);
        } else {
            Helper.setMeldung(null,
                    Helper.getTranslation("directory ") + " " + this.process.getTitle() + " "
                            + Helper.getTranslation("isInUse"),
                    serviceManager.getUserService()
                            .getFullName(serviceManager.getProcessService().getImageFolderInUseUser(this.process)));
            WebDav myDav = new WebDav();
            myDav.downloadToHome(this.process, true);
        }
    }

    /**
     * Download to home page.
     */
    @SuppressWarnings("unchecked")
    public void downloadToHomePage() {
        WebDav webDav = new WebDav();
        for (ProcessDTO process : (List<ProcessDTO>) this.page.getListReload()) {
            download(webDav, process);
        }
        Helper.setMeldung(null, "createdInUserHome", "");
    }

    /**
     * Download to home selection.
     */
    @SuppressWarnings("unchecked")
    public void downloadToHomeSelection() {
        WebDav myDav = new WebDav();
        for (ProcessDTO processDTO : this.getSelectedProcesses()) {
            download(myDav, processDTO);
        }
        Helper.setMeldung(null, "createdInUserHomeAll", "");
    }

    private void download(WebDav webDav, ProcessDTO processDTO) {
        try {
            Process process = serviceManager.getProcessService().convertDtoToBean(processDTO);
            if (!serviceManager.getProcessService().isImageFolderInUse(processDTO)) {
                webDav.downloadToHome(process, false);
            } else {
                Helper.setMeldung(null,
                        Helper.getTranslation("directory ") + " " + processDTO.getTitle() + " "
                                + Helper.getTranslation("isInUse"),
                        serviceManager.getUserService()
                                .getFullName(serviceManager.getProcessService().getImageFolderInUseUser(process)));
                webDav.downloadToHome(process, true);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Download to home hits.
     */
    @SuppressWarnings("unchecked")
    public void downloadToHomeHits() {
        WebDav webDav = new WebDav();
        for (Process process : (List<Process>) this.page.getCompleteList()) {
            if (!serviceManager.getProcessService().isImageFolderInUse(process)) {
                webDav.downloadToHome(process, false);
            } else {
                Helper.setMeldung(null,
                        Helper.getTranslation("directory ") + " " + process.getTitle() + " "
                                + Helper.getTranslation("isInUse"),
                        serviceManager.getUserService()
                                .getFullName(serviceManager.getProcessService().getImageFolderInUseUser(process)));
                webDav.downloadToHome(process, true);
            }
        }
        Helper.setMeldung(null, "createdInUserHomeAll", "");
    }

    /**
     * Set up processing status page.
     */
    @SuppressWarnings("unchecked")
    public void setTaskStatusUpForPage() throws DAOException, DataException, IOException {
        List<ProcessDTO> processes = this.page.getListReload();
        for (ProcessDTO process : processes) {
            workflowService.setTasksStatusUp(serviceManager.getProcessService().getById(process.getId()));
        }
    }

    /**
     * Set up processing status selection.
     */
    public void setTaskStatusUpForSelection() throws DAOException, DataException, IOException {
        List<ProcessDTO> processDTOS = this.getSelectedProcesses();
        for (ProcessDTO processDTO : processDTOS) {
            workflowService.setTasksStatusUp(serviceManager.getProcessService().getById(processDTO.getId()));
        }
    }

    /**
     * Set up processing status hits.
     */
    @SuppressWarnings("unchecked")
    public void setTaskStatusUpForHits() throws DAOException, DataException, IOException {
        List<ProcessDTO> processes = this.page.getCompleteList();
        for (ProcessDTO process : processes) {
            workflowService.setTasksStatusUp(serviceManager.getProcessService().getById(process.getId()));
        }
    }

    /**
     * Set down processing status page.
     */
    @SuppressWarnings("unchecked")
    public void setTaskStatusDownForPage() throws DAOException, DataException {
        List<ProcessDTO> processes = this.page.getListReload();
        for (ProcessDTO process : processes) {
            workflowService.setTasksStatusDown(serviceManager.getProcessService().getById(process.getId()));
        }
    }

    /**
     * Set down processing status selection.
     */
    public void setTaskStatusDownForSelection() throws DAOException, DataException {
        List<ProcessDTO> processDTOS = this.getSelectedProcesses();
        for (ProcessDTO processDTO : processDTOS) {
            workflowService.setTasksStatusDown(serviceManager.getProcessService().getById(processDTO.getId()));
        }
    }

    /**
     * Set down processing status hits.
     */
    @SuppressWarnings("unchecked")
    public void setTaskStatusDownForHits() throws DAOException, DataException {
        List<ProcessDTO> processes = this.page.getCompleteList();
        for (ProcessDTO process : processes) {
            workflowService.setTasksStatusDown(serviceManager.getProcessService().getById(process.getId()));
        }
    }

    /**
     * Task status up.
     */
    public void setTaskStatusUp() throws DataException, IOException {
        setTask(workflowService.setTaskStatusUp(this.task));
        save();
        deleteSymlinksFromUserHomes();
    }

    /**
     * Task status down.
     */
    public void setTaskStatusDown() {
        setTask(workflowService.setTaskStatusDown(this.task));
        save();
        deleteSymlinksFromUserHomes();
    }

    /**
     * Get process object.
     *
     * @return process object
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * Set process.
     *
     * @param process
     *            Process object
     */
    public void setProcess(Process process) {
        this.process = process;
        this.newProcessTitle = process.getTitle();
        loadProcessProperties();
        loadTemplateProperties();
        loadWorkpieceProperties();
    }

    /**
     * Get task object.
     *
     * @return Task object
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * Set task.
     *
     * @param task
     *            Task object
     */
    public void setTask(Task task) {
        this.task = task;
        this.task.setLocalizedTitle(serviceManager.getTaskService().getLocalizedTitle(task.getTitle()));
    }

    // TODO: why second setter for task
    public void setTaskReload(Task task) {
        this.task = task;
    }

    public Property getTemplateProperty() {
        return this.templateProperty;
    }

    public void setTemplateProperty(Property templateProperty) {
        this.templateProperty = templateProperty;
    }

    public Property getWorkpieceProperty() {
        return this.workpieceProperty;
    }

    public void setWorkpieceProperty(Property workpieceProperty) {
        this.workpieceProperty = workpieceProperty;
    }

    /**
     * Get mode for display.
     *
     * @return mode for edition as ObjectMode objects
     */
    public ObjectMode getDisplayMode() {
        return this.displayMode;
    }

    /**
     * Set mode for display and set up default sort to title ASC.
     *
     * @param displayMode
     *            mode for edition as ObjectMode objects
     */
    public void setDisplayMode(ObjectMode displayMode) {
        this.sortierung = "titelAsc";
        this.displayMode = displayMode;
    }

    /**
     * Get mode for edition.
     *
     * @return mode for edition as ObjectMode objects
     */
    public ObjectMode getEditMode() {
        return editMode;
    }

    /**
     * Set mode for edition.
     *
     * @param editMode
     *            mode for edition as ObjectMode objects
     */
    public void setEditMode(ObjectMode editMode) {
        this.editMode = editMode;
    }

    /**
     * Set ordering up.
     *
     * @return String
     */
    public String setOrderingUp() {
        List<Task> tasks = this.process.getTasks();
        Integer ordering = this.task.getOrdering() - 1;
        for (Task task : tasks) {
            if (task.getOrdering().equals(ordering)) {
                task.setOrdering(ordering + 1);
            }
        }
        this.task.setOrdering(ordering);
        return save();
    }

    /**
     * Set ordering down.
     *
     * @return String
     */
    public String setOrderingDown() {
        List<Task> tasks = this.process.getTasks();
        Integer ordering = this.task.getOrdering() + 1;
        for (Task task : tasks) {
            if (task.getOrdering().equals(ordering)) {
                task.setOrdering(ordering - 1);
            }
        }
        this.task.setOrdering(ordering);
        return save();
    }

    /**
     * Reload.
     *
     * @return String
     */
    public String reload() {
        if (this.task != null && this.task.getId() != null) {
            try {
                Helper.getHibernateSession().refresh(this.task);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    Helper.setErrorMessage("errorReloading", new Object[] {Helper.getTranslation("arbeitsschritt") },
                        logger, e);
                }
            }
        }
        if (this.process != null && this.process.getId() != null) {
            try {
                Helper.getHibernateSession().refresh(this.process);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    Helper.setErrorMessage("errorReloading", new Object[] {Helper.getTranslation("prozess") }, logger,
                        e);
                }
            }
        }
        return null;
    }

    /**
     * Get choice of project.
     *
     * @return Integer
     */
    public Integer getProjektAuswahl() {
        if (this.process.getProject() != null) {
            return this.process.getProject().getId();
        } else {
            return 0;
        }
    }

    /**
     * Set choice of project.
     *
     * @param inProjektAuswahl
     *            Integer
     */
    public void setProjektAuswahl(Integer inProjektAuswahl) {
        if (inProjektAuswahl != 0) {
            try {
                this.process.setProject(serviceManager.getProjectService().getById(inProjektAuswahl));
            } catch (DAOException e) {
                Helper.setErrorMessage("Error assigning project", logger, e);
            }
        }
    }

    /**
     * Get list of projects.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getProjektAuswahlListe() {
        List<SelectItem> projects = new ArrayList<>();
        List<Project> temp = serviceManager.getProjectService().getByQuery("from Project ORDER BY title");
        for (Project project : temp) {
            projects.add(new SelectItem(project.getId(), project.getTitle(), null));
        }
        return projects;
    }

    /**
     * Calculate metadata and images pages.
     */
    @SuppressWarnings("unchecked")
    public void calculateMetadataAndImagesPage() {
        calculateMetadataAndImages(this.page.getListReload());
    }

    /**
     * Calculate metadata and images selection.
     */
    public void calculateMetadataAndImagesSelection() {
        calculateMetadataAndImages(this.getSelectedProcesses());
    }

    /**
     * Calculate metadata and images hits.
     */
    @SuppressWarnings("unchecked")
    public void calculateMetadataAndImagesHits() {
        calculateMetadataAndImages(this.page.getCompleteList());
    }

    private void calculateMetadataAndImages(List<ProcessDTO> processes) {

        this.processCounterObjects = new ArrayList<>();
        int allMetadata = 0;
        int allDocstructs = 0;
        int allImages = 0;

        int maxImages = 1;
        int maxDocstructs = 1;
        int maxMetadata = 1;

        int countOfProcessesWithImages = 0;
        int countOfProcessesWithMetadata = 0;
        int countOfProcessesWithDocstructs = 0;

        int averageImages = 0;
        int averageMetadata = 0;
        int averageDocstructs = 0;

        for (ProcessDTO proz : processes) {
            int tempImg = proz.getSortHelperImages();
            int tempMetadata = proz.getSortHelperMetadata();
            int tempDocstructs = proz.getSortHelperDocstructs();

            ProcessCounterObject pco = new ProcessCounterObject(proz.getTitle(), tempMetadata, tempDocstructs, tempImg);
            this.processCounterObjects.add(pco);

            if (tempImg > maxImages) {
                maxImages = tempImg;
            }
            if (tempMetadata > maxMetadata) {
                maxMetadata = tempMetadata;
            }
            if (tempDocstructs > maxDocstructs) {
                maxDocstructs = tempDocstructs;
            }
            if (tempImg > 0) {
                countOfProcessesWithImages++;
            }
            if (tempMetadata > 0) {
                countOfProcessesWithMetadata++;
            }
            if (tempDocstructs > 0) {
                countOfProcessesWithDocstructs++;
            }

            /* Werte für die Gesamt- und Durchschnittsberechnung festhalten */
            allImages += tempImg;
            allMetadata += tempMetadata;
            allDocstructs += tempDocstructs;
        }

        /* die prozentualen Werte anhand der Maximumwerte ergänzen */
        for (ProcessCounterObject pco : this.processCounterObjects) {
            pco.setRelImages(pco.getImages() * 100 / maxImages);
            pco.setRelMetadata(pco.getMetadata() * 100 / maxMetadata);
            pco.setRelDocstructs(pco.getDocstructs() * 100 / maxDocstructs);
        }

        if (countOfProcessesWithImages > 0) {
            averageImages = allImages / countOfProcessesWithImages;
        }

        if (countOfProcessesWithMetadata > 0) {
            averageMetadata = allMetadata / countOfProcessesWithMetadata;
        }

        if (countOfProcessesWithDocstructs > 0) {
            averageDocstructs = allDocstructs / countOfProcessesWithDocstructs;
        }

        this.counterSummary = new HashMap<>();
        this.counterSummary.put("sumProcesses", this.processCounterObjects.size());
        this.counterSummary.put("sumMetadata", allMetadata);
        this.counterSummary.put("sumDocstructs", allDocstructs);
        this.counterSummary.put("sumImages", allImages);
        this.counterSummary.put("averageImages", averageImages);
        this.counterSummary.put("averageMetadata", averageMetadata);
        this.counterSummary.put("averageDocstructs", averageDocstructs);
    }

    public HashMap<String, Integer> getCounterSummary() {
        return this.counterSummary;
    }

    public List<ProcessCounterObject> getProcessCounterObjects() {
        return this.processCounterObjects;
    }

    /**
     * Starte GoobiScript über alle Treffer.
     */
    @SuppressWarnings("unchecked")
    public void kitodoScriptHits() {
        GoobiScript gs = new GoobiScript();
        try {
            gs.execute(serviceManager.getProcessService().convertDtosToBeans(this.page.getCompleteList()),
                    this.kitodoScript);
        } catch (DAOException | DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Starte GoobiScript über alle Treffer der Seite.
     */
    @SuppressWarnings("unchecked")
    public void kitodoScriptPage() {
        GoobiScript gs = new GoobiScript();
        try {
            gs.execute(serviceManager.getProcessService().convertDtosToBeans(this.page.getListReload()), this.kitodoScript);
        } catch (DAOException | DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Starte GoobiScript über alle selectierten Treffer.
     */
    @SuppressWarnings("unchecked")
    public void kitodoScriptSelection() {

        GoobiScript gs = new GoobiScript();
        try {
            gs.execute(serviceManager.getProcessService().convertDtosToBeans(this.selectedProcesses), this.kitodoScript);
        } catch (DAOException | DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Statistische Auswertung.
     */
    public void setStatisticsStatusVolumes() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.STATUS_VOLUMES, this.processDTOS,
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        this.statisticsManager.calculate();
    }

    /**
     * Statistic UserGroups.
     */
    public void setStatisticsUserGroups() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.USERGROUPS, this.processDTOS,
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        this.statisticsManager.calculate();
    }

    /**
     * Statistic runtime Tasks.
     */
    public void setStatisticsRuntimeSteps() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.SIMPLE_RUNTIME_STEPS, this.processDTOS,
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void setStatisticsProduction() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.PRODUCTION, this.processDTOS,
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void setStatisticsStorage() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.STORAGE, this.processDTOS,
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void setStatisticsCorrection() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.CORRECTIONS, this.processDTOS,
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    /**
     * Project's statistics.
     */
    public void setStatisticsProject() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.PROJECTS, this.processDTOS,
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        this.statisticsManager.calculate();
    }

    /**
     * is called via jsf at the end of building a chart in include file
     * Prozesse_Liste_Statistik.xhtml and resets the statistics so that with the
     * next reload a chart is not shown anymore.
     */
    public String getResetStatistic() {
        this.showStatistics = false;
        return null;
    }

    public String getMyDatasetHoehe() {
        int bla = this.page.getCompleteList().size() * 20;
        return String.valueOf(bla);
    }

    public int getMyDatasetHoeheInt() {
        return this.page.getCompleteList().size() * 20;
    }

    /*
     * Downloads
     */
    public void downloadTiffHeader() throws IOException {
        TiffHeader tiff = new TiffHeader(this.process);
        tiff.exportStart();
    }

    public void downloadMultiTiff() throws IOException {
        Multipage mp = new Multipage();
        mp.startExport(this.process);
    }

    public String getKitodoScript() {
        return this.kitodoScript;
    }

    /**
     * Setter for kitodoScript.
     *
     * @param kitodoScript
     *            the kitodoScript
     */
    public void setKitodoScript(String kitodoScript) {
        this.kitodoScript = kitodoScript;
    }

    public HashMap<String, Boolean> getAnzeigeAnpassen() {
        return this.anzeigeAnpassen;
    }

    public void setAnzeigeAnpassen(HashMap<String, Boolean> anzeigeAnpassen) {
        this.anzeigeAnpassen = anzeigeAnpassen;
    }

    public String getNewProcessTitle() {
        return this.newProcessTitle;
    }

    public void setNewProcessTitle(String newProcessTitle) {
        this.newProcessTitle = newProcessTitle;
    }

    public StatisticsManager getStatisticsManager() {
        return this.statisticsManager;
    }

    /**
     * Getter for showStatistics.
     *
     * @return the showStatistics
     */
    public boolean isShowStatistics() {
        return this.showStatistics;
    }

    /**
     * Setter for showStatistics.
     *
     * @param showStatistics
     *            the showStatistics to set
     */
    public void setShowStatistics(boolean showStatistics) {
        this.showStatistics = showStatistics;
    }

    public static class ProcessCounterObject {
        private String title;
        private int metadata;
        private int docstructs;
        private int images;
        private int relImages;
        private int relDocstructs;
        private int relMetadata;

        /**
         * Constructor.
         *
         * @param title
         *            String
         * @param metadata
         *            int
         * @param docstructs
         *            int
         * @param images
         *            int
         */
        public ProcessCounterObject(String title, int metadata, int docstructs, int images) {
            super();
            this.title = title;
            this.metadata = metadata;
            this.docstructs = docstructs;
            this.images = images;
        }

        public int getImages() {
            return this.images;
        }

        public int getMetadata() {
            return this.metadata;
        }

        public String getTitle() {
            return this.title;
        }

        public int getDocstructs() {
            return this.docstructs;
        }

        public int getRelDocstructs() {
            return this.relDocstructs;
        }

        public int getRelImages() {
            return this.relImages;
        }

        public int getRelMetadata() {
            return this.relMetadata;
        }

        public void setRelDocstructs(int relDocstructs) {
            this.relDocstructs = relDocstructs;
        }

        public void setRelImages(int relImages) {
            this.relImages = relImages;
        }

        public void setRelMetadata(int relMetadata) {
            this.relMetadata = relMetadata;
        }
    }

    /**
     * starts generation of xml logfile for current process.
     */

    public void createXML() {
        try {
            ExportXmlLog xmlExport = new ExportXmlLog();

            String directory = new File(serviceManager.getUserService().getHomeDirectory(getUser()))
                    .getPath();
            String destination = directory + this.process.getTitle() + "_log.xml";
            xmlExport.startExport(this.process, destination);
        } catch (IOException e) {
            Helper.setErrorMessage("Error creating log file in home directory", logger, e);
        }
    }

    /**
     * transforms xml logfile with given xslt and provides download. //TODO: why
     * this whole stuff is not used?
     */
    public void transformXml() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            String outputFileName = "export.xml";
            /*
             * Vorbereiten der Header-Informationen
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(outputFileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + outputFileName + "\"");

            response.setContentType("text/xml");

            try {
                ServletOutputStream out = response.getOutputStream();
                ExportXmlLog export = new ExportXmlLog();
                export.startTransformation(out, this.process, this.selectedXslt);
                out.flush();
            } catch (IOException | XSLTransformException e) {
                Helper.setErrorMessage("Error transforming XML", logger, e);
            }
            facesContext.responseComplete();
        }
    }

    /**
     * Get XSLT list.
     *
     * @return list of Strings
     */
    public List<URI> getXsltList() {
        List<URI> answer = new ArrayList<>();
        try {
            URI folder = fileService.createDirectory(null, "xsltFolder");
            if (fileService.isDirectory(folder) && fileService.fileExist(folder)) {
                ArrayList<URI> files = fileService.getSubUris(folder);

                for (URI uri : files) {
                    if (uri.toString().endsWith(".xslt") || uri.toString().endsWith(".xsl")) {
                        answer.add(uri);
                    }
                }
            }
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return answer;
    }

    public void setSelectedXslt(String select) {
        this.selectedXslt = select;
    }

    public String getSelectedXslt() {
        return this.selectedXslt;
    }

    /**
     * Downloads a docket for myProcess.
     *
     * @return The navigation string
     */
    public String downloadDocket() throws IOException {
        serviceManager.getProcessService().downloadDocket(this.process);
        return "";
    }

    public void setMyCurrentTable(StatisticsRenderingElement myCurrentTable) {
        this.myCurrentTable = myCurrentTable;
    }

    public StatisticsRenderingElement getMyCurrentTable() {
        return this.myCurrentTable;
    }

    /**
     * Create excel.
     */
    public void createExcel() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {

            /*
             * Vorbereiten der Header-Informationen
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("export.xls");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"export.xls\"");
                ServletOutputStream out = response.getOutputStream();
                HSSFWorkbook wb = (HSSFWorkbook) this.myCurrentTable.getExcelRenderer().getRendering();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
    }

    /**
     * Generate result as PDF.
     */
    public void generateResultAsPdf() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {

            /*
             * Vorbereiten der Header-Informationen
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("search.pdf");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"search.pdf\"");
                ServletOutputStream out = response.getOutputStream();

                SearchResultGeneration sr = new SearchResultGeneration(this.filter, this.showClosedProcesses,
                        this.showInactiveProjects);
                HSSFWorkbook wb = sr.getResult();
                List<List<HSSFCell>> rowList = new ArrayList<>();
                HSSFSheet mySheet = wb.getSheetAt(0);
                Iterator<Row> rowIter = mySheet.rowIterator();
                while (rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    List<HSSFCell> row = new ArrayList<>();
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        row.add(myCell);
                    }
                    rowList.add(row);
                }
                Document document = new Document();
                Rectangle rectangle = new Rectangle(PageSize.A3.getHeight(), PageSize.A3.getWidth());
                PdfWriter.getInstance(document, out);
                document.setPageSize(rectangle);
                document.open();
                if (rowList.size() > 0) {
                    Paragraph p = new Paragraph(rowList.get(0).get(0).toString());
                    document.add(p);
                    PdfPTable table = new PdfPTable(9);
                    table.setSpacingBefore(20);
                    for (List<HSSFCell> row : rowList) {
                        for (HSSFCell hssfCell : row) {
                            // TODO aufhübschen und nicht toString() nutzen
                            String stringCellValue = hssfCell.toString();
                            table.addCell(stringCellValue);
                        }
                    }
                    document.add(table);
                }

                document.close();
                out.flush();
                facesContext.responseComplete();
            } catch (Exception e) {
                Helper.setErrorMessage("errorCreating", new Object[] {Helper.getTranslation("resultPDF") }, logger, e);
            }
        }
    }

    /**
     * Generate result set.
     */
    public void generateResult() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {

            /*
             * Vorbereiten der Header-Informationen
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("search.xls");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"search.xls\"");
                ServletOutputStream out = response.getOutputStream();
                SearchResultGeneration sr = new SearchResultGeneration(this.filter, this.showClosedProcesses,
                        this.showInactiveProjects);
                HSSFWorkbook wb = sr.getResult();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();
            } catch (IOException e) {
                Helper.setErrorMessage("errorCreating", new Object[] {Helper.getTranslation("resultSet") }, logger, e);
            }
        }
    }

    public boolean isShowClosedProcesses() {
        return this.showClosedProcesses;
    }

    public void setShowClosedProcesses(boolean showClosedProcesses) {
        this.showClosedProcesses = showClosedProcesses;
    }

    /**
     * Set whether inactive projects should be displayed or not.
     *
     * @param showInactiveProjects
     *            boolean flag signaling whether inactive projects should be
     *            displayed or not
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
    }

    /**
     * Return whether inactive projects should be displayed or not.
     *
     * @return parameter controlling whether inactive projects should be displayed
     *         or not
     */
    public boolean isShowInactiveProjects() {
        return this.showInactiveProjects;
    }

    /**
     * Get wiki field.
     *
     * @return values for wiki field
     */
    public String getWikiField() {
        return this.process.getWikiField();
    }

    /**
     * sets new value for wiki field.
     *
     * @param inString
     *            String
     */
    public void setWikiField(String inString) {
        this.process.setWikiField(inString);
    }

    public String getAddToWikiField() {
        return this.addToWikiField;
    }

    public void setAddToWikiField(String addToWikiField) {
        this.addToWikiField = addToWikiField;
    }

    /**
     * Add to wiki field.
     */
    public void addToWikiField() {
        if (addToWikiField != null && addToWikiField.length() > 0) {
            String message = this.addToWikiField + " (" + serviceManager.getUserService().getFullName(getUser()) + ")";
            this.process.setWikiField(
                    WikiFieldHelper.getWikiMessage(this.process, this.process.getWikiField(), "user", message));
            this.addToWikiField = "";
            try {
                serviceManager.getProcessService().save(process);
            } catch (DataException e) {
                Helper.setErrorMessage("errorReloading", new Object[] {Helper.getTranslation("wikiField") }, logger, e);
            }
        }
    }

    /**
     * Get property for process.
     *
     * @return property for process
     */
    public Property getProperty() {
        return this.property;
    }

    /**
     * Set property for process.
     *
     * @param property
     *            for process as Property object
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * Get list of properties for process.
     *
     * @return list of process properties
     */
    public List<Property> getProperties() {
        return this.properties;
    }

    /**
     * Set list of properties for process.
     *
     * @param properties
     *            for process as Property objects
     */
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Get list of templates for process.
     *
     * @return list of templates for process
     */
    public List<Property> getTemplates() {
        return this.templates;
    }

    /**
     * Set list of templates for process.
     *
     * @param templates
     *            for process as Property objects
     */
    public void setTemplates(List<Property> templates) {
        this.templates = templates;
    }

    /**
     * Get list of workpieces for process.
     *
     * @return list of workpieces for process
     */
    public List<Property> getWorkpieces() {
        return this.workpieces;
    }

    /**
     * Set list of workpieces for process.
     *
     * @param workpieces
     *            for process as Property objects
     */
    public void setWorkpieces(List<Property> workpieces) {
        this.workpieces = workpieces;
    }

    private void loadProcessProperties() {
        serviceManager.getProcessService().refresh(this.process);
        this.properties = this.process.getProperties();
    }

    private void loadTemplateProperties() {
        serviceManager.getProcessService().refresh(this.process);
        this.templates = this.process.getTemplates();
    }

    private void loadWorkpieceProperties() {
        serviceManager.getProcessService().refresh(this.process);
        this.workpieces = this.process.getWorkpieces();
    }

    /**
     * Create new property.
     */
    public void createNewProperty() {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        Property property = new Property();
        property.setType(PropertyType.STRING);
        this.properties.add(property);
        this.property = property;
    }

    /**
     * Save current property.
     */
    public void saveCurrentProperty() {
        try {
            serviceManager.getPropertyService().save(this.property);
            if (!this.process.getProperties().contains(this.property)) {
                this.process.getProperties().add(this.property);
            }
            serviceManager.getProcessService().save(this.process);
            Helper.setMeldung("propertiesSaved");
        } catch (DataException e) {
            Helper.setErrorMessage("propertiesNotSaved", logger, e);
        }
        loadProcessProperties();
    }

    /**
     * Delete property.
     */
    public void deleteProperty() {
        try {
            this.property.getProcesses().clear();
            this.process.getProperties().remove(this.property);
            serviceManager.getProcessService().save(this.process);
            serviceManager.getPropertyService().remove(this.property);
        } catch (DataException e) {
            Helper.setErrorMessage("propertiesNotDeleted", logger, e);
        }

        List<Property> properties = this.process.getProperties();
        removePropertiesWithEmptyTitle(properties);
        loadProcessProperties();
    }

    /**
     * Duplicate property.
     */
    public void duplicateProperty() {
        Property newProperty = serviceManager.getPropertyService().transfer(this.property);
        try {
            newProperty.getProcesses().add(this.process);
            this.process.getProperties().add(newProperty);
            serviceManager.getPropertyService().save(newProperty);
            Helper.setMeldung("propertySaved");
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("eigenschaft") }, logger, e);
        }
        loadProcessProperties();
    }

    // TODO: is it really a case that title is empty?
    private void removePropertiesWithEmptyTitle(List<Property> properties) {
        for (Property processProperty : properties) {
            if (processProperty.getTitle() == null) {
                try {
                    processProperty.getProcesses().clear();
                    this.process.getProperties().remove(processProperty);
                    serviceManager.getProcessService().save(this.process);
                    serviceManager.getPropertyService().remove(processProperty);
                } catch (DataException e) {
                    Helper.setErrorMessage("Property couldn't be removed: " + e.getMessage(), logger, e);
                }
            }
        }
    }

    /**
     * Get list od DTO processes.
     *
     * @return list of ProcessDTO objects
     */
    public List<ProcessDTO> getProcessDTOS() {
        return processDTOS;
    }

    /**
     * Method being used as viewAction for process edit form. If the given parameter
     * 'id' is '0', the form for creating a new process will be displayed.
     *
     * @param id
     *            ID of the process to load
     */
    public void loadProcess(int id) {
        try {
            if (id != 0) {
                setProcess(this.serviceManager.getProcessService().getById(id));
            } else {
                newProcess();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("prozess"), id }, logger, e);
        }
    }

    /**
     * Method being used as viewAction for task form.
     */
    public void loadTask(int id) {
        try {
            if (id != 0) {
                setTask(this.serviceManager.getTaskService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("arbeitsschritt"), id },
                logger, e);
        }
    }

    /**
     * Return list of users.
     *
     * @return list of user groups
     */
    public List<UserDTO> getActiveUsers() {
        try {
            return serviceManager.getUserService().findAllActiveUsers();
        } catch (DataException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("users") }, logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Return list of user groups.
     *
     * @return list of user groups
     */
    public List<UserGroupDTO> getUserGroups() {
        try {
            return serviceManager.getUserGroupService().findAll();
        } catch (DataException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("benutzergruppen") }, logger,
                e);
            return new LinkedList<>();
        }
    }

    /**
     * Returns selected processDTO.
     *
     * @return The list of processDTO.
     */
    public List<ProcessDTO> getSelectedProcesses() {
        return selectedProcesses;
    }

    /**
     * Sets selected processDTOs.
     *
     * @param selectedProcesses
     *            The list of ProcessDTOs.
     */
    public void setSelectedProcesses(List<ProcessDTO> selectedProcesses) {
        this.selectedProcesses = selectedProcesses;
    }

    // TODO:
    // replace calls to this function with "/pages/processEdit" once we have
    // completely switched to the new frontend pages
    private String redirectToEdit() {
        String urlParameters = "?" + REDIRECT_PARAMETER + "&id="
                + (Objects.isNull(this.process.getId()) ? 0 : this.process.getId());
        try {
            String referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                    .get("referer");
            String callerViewId = referrer.substring(referrer.lastIndexOf('/') + 1);
            if (!callerViewId.isEmpty()
                    && (callerViewId.contains("processes.jsf") || callerViewId.contains("taskEdit.jsf")
                            || callerViewId.contains("processEdit.jsf"))) {
                return PROCESS_EDIT_PATH + urlParameters;
            } else {
                return PROCESS_EDIT_PATH_OLD + urlParameters;
            }
        } catch (NullPointerException e) {
            // This NPE gets thrown - and therefore must be caught - when "ProzessverwaltungForm" is
            // used from it's integration test
            // class "ProzessverwaltungFormIT", where no "FacesContext" is available!
            return PROCESS_EDIT_PATH_OLD + urlParameters;
        }
    }

    // TODO:
    // replace calls to this function with "/pages/taskEdit" once we have
    // completely switched to the new frontend pages
    private String redirectToEditTask() {
        String urlParameters = "?" + REDIRECT_PARAMETER + "&id="
                + (Objects.isNull(this.task.getId()) ? 0 : this.task.getId());
        try {
            String referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                    .get("referer");
            String callerViewId = referrer.substring(referrer.lastIndexOf('/') + 1);
            if (!callerViewId.isEmpty() && callerViewId.contains("processEdit.jsf")) {
                return TASK_EDIT_PATH + urlParameters;
            } else {
                return TASK_EDIT_PATH_OLD + urlParameters;
            }
        } catch (NullPointerException e) {
            // This NPE gets thrown - and therefore must be caught - when
            // "ProzessverwaltungForm" is
            // used from it's integration test
            // class "ProzessverwaltungFormIT", where no "FacesContext" is available!
            return TASK_EDIT_PATH_OLD + urlParameters;
        }
    }

    // TODO:
    // replace calls to this function with "/pages/processes" once we have completely
    // switched to the new frontend pages
    private String redirectToList() {
        try {
            String referer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                    .get("referer");
            String callerViewId = referer.substring(referer.lastIndexOf("/") + 1);
            if (!callerViewId.isEmpty()
                    && (callerViewId.contains("searchProcess.jsf") || callerViewId.contains("processEdit.jsf"))) {
                return PROCESS_LIST_PATH + "?" + REDIRECT_PARAMETER;
            } else {
                return PROCESS_LIST_PATH_OLD + "?" + REDIRECT_PARAMETER;
            }
        } catch (NullPointerException e) {
            // This NPE gets thrown - and therefore must be caught - when "ProzessverwaltungForm" is
            // used from it's integration test
            // class "ProzessverwaltungFormIT", where no "FacesContext" is available!
            return PROCESS_LIST_PATH_OLD + "?" + REDIRECT_PARAMETER;
        }
    }
}
