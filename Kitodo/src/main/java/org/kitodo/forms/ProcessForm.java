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

package org.kitodo.forms;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.export.ExportXmlLog;
import org.goobi.production.flow.helper.SearchResultGeneration;
import org.jdom.transform.XSLTransformException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.exceptions.ExportFileException;
import org.kitodo.exporter.dms.ExportDms;
import org.kitodo.exporter.download.ExportMets;
import org.kitodo.exporter.download.ExportPdf;
import org.kitodo.exporter.download.Multipage;
import org.kitodo.exporter.download.TiffHeader;
import org.kitodo.helper.GoobiScript;
import org.kitodo.helper.Helper;
import org.kitodo.helper.SelectItemList;
import org.kitodo.helper.WebDav;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.file.FileService;
import org.kitodo.services.workflow.WorkflowControllerService;

@Named("ProcessForm")
@SessionScoped
public class ProcessForm extends TemplateBaseForm {
    private static final long serialVersionUID = 2838270843176821134L;
    private static final Logger logger = LogManager.getLogger(ProcessForm.class);
    private Process process = new Process();
    private Task task = new Task();
    private List<ProcessCounterObject> processCounterObjects;
    private HashMap<String, Integer> counterSummary;
    private Property templateProperty;
    private Property workpieceProperty;
    private String kitodoScript;
    private Map<String, Boolean> anzeigeAnpassen;
    private String newProcessTitle;
    private String selectedXslt = "";
    private boolean showClosedProcesses = false;
    private boolean showInactiveProjects = false;
    private List<Property> properties;
    private List<Property> templates;
    private List<Property> workpieces;
    private Property property;
    private String addToWikiField = "";
    private List<ProcessDTO> processDTOS = new ArrayList<>();
    private transient FileService fileService = serviceManager.getFileService();
    private transient WorkflowControllerService workflowControllerService = serviceManager
            .getWorkflowControllerService();
    private String doneDirectoryName;
    private static final String EXPORT_FINISHED = "exportFinished";
    private static final String PROPERTIES_SAVED = "propertiesSaved";
    private static final String PROPERTY_SAVED = "propertySaved";
    private List<ProcessDTO> selectedProcesses = new ArrayList<>();
    String processListPath = MessageFormat.format(REDIRECT_PATH, "processes");
    private String processEditPath = MessageFormat.format(REDIRECT_PATH, "processEdit");

    private String processEditReferer = DEFAULT_LINK;
    private String taskEditReferer = DEFAULT_LINK;

    /**
     * Constructor.
     */
    public ProcessForm() {
        super();
        this.anzeigeAnpassen = new HashMap<>();
        this.anzeigeAnpassen.put("lockings", false);
        this.anzeigeAnpassen.put("swappedOut", false);
        this.anzeigeAnpassen.put("selectionBoxes", false);
        this.anzeigeAnpassen.put("processId", false);
        this.anzeigeAnpassen.put("batchId", false);
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
        doneDirectoryName = ConfigCore.getParameterOrDefaultValue(ParameterCore.DONE_DIRECTORY_NAME);
    }

    /**
     * Save process and redirect to list view.
     *
     * @return url to list view
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
                return processListPath;
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                    logger, e);
            }
        } else {
            Helper.setErrorMessage("titleEmpty");
        }
        reload();
        return null;
    }

    /**
     * Delete process.
     */
    public void delete() {
        deleteMetadataDirectory();
        try {
            this.process.getProject().getProcesses().remove(this.process);
            this.process.setProject(null);
            this.process.getTemplate().getProcesses().remove(this.process);
            this.process.setTemplate(null);
            List<Batch> batches = new CopyOnWriteArrayList<>(process.getBatches());
            for (Batch batch : batches) {
                batch.getProcesses().remove(this.process);
                this.process.getBatches().remove(batch);
                serviceManager.getBatchService().save(batch);
            }
            serviceManager.getProcessService().remove(this.process);
        } catch (DataException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                e);
        }
    }

    /**
     * Get diagram image for current template.
     *
     * @return diagram image file
     */
    public InputStream getTasksDiagram() {
        Workflow workflow = this.process.getTemplate().getWorkflow();
        if (Objects.nonNull(workflow)) {
            return serviceManager.getTemplateService().getTasksDiagram(workflow.getFileName());
        }
        return serviceManager.getTemplateService().getTasksDiagram("");
    }

    /**
     * Remove content.
     *
     * @return String
     */
    public String deleteContent() {
        try {
            URI ocr = fileService.getOcrDirectory(this.process);
            if (fileService.fileExist(ocr)) {
                fileService.delete(ocr);
            }
            URI images = fileService.getImagesDirectory(this.process);
            if (fileService.fileExist(images)) {
                fileService.delete(images);
            }
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("errorDirectoryDeleting", new Object[] {Helper.getTranslation("metadata") }, logger,
                e);
        }

        Helper.setMessage("Content deleted");
        return null;
    }

    private boolean renameAfterProcessTitleChanged() {
        String validateRegEx = ConfigCore.getParameterOrDefaultValue(ParameterCore.VALIDATE_PROCESS_TITLE_REGEX);
        if (!this.newProcessTitle.matches(validateRegEx)) {
            Helper.setErrorMessage("processTitleInvalid");
            return false;
        } else {
            renamePropertiesValuesForProcessTitle(this.process.getProperties());
            renamePropertiesValuesForProcessTitle(this.process.getTemplates());
            removePropertiesWithEmptyTitle(this.process.getWorkpieces());

            try {
                renameImageDirectories();
                renameOcrDirectories();
                renameDefinedDirectories();
            } catch (IOException | RuntimeException e) {
                Helper.setErrorMessage("errorRenaming", new Object[] {Helper.getTranslation("directory") }, logger, e);
            }

            this.process.setTitle(this.newProcessTitle);

            // remove Tiffwriter file
            GoobiScript gs = new GoobiScript();
            List<Process> pro = new ArrayList<>();
            pro.add(this.process);
            gs.deleteTiffHeaderFile(pro);
            gs.updateImagePath(pro);
        }
        return true;
    }

    private void renamePropertiesValuesForProcessTitle(List<Property> properties) {
        for (Property property : properties) {
            if (Objects.nonNull(property.getValue()) && property.getValue().contains(this.process.getTitle())) {
                property.setValue(property.getValue().replaceAll(this.process.getTitle(), this.newProcessTitle));
            }
        }
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
            List<URI> subDirs = fileService.getSubUris(directory);
            for (URI imageDir : subDirs) {
                if (fileService.isDirectory(imageDir)) {
                    fileService.renameFile(imageDir,
                        fileService.getFileName(imageDir).replace(process.getTitle(), newProcessTitle));
                }
            }
        }
    }

    private void renameDefinedDirectories() {
        String[] processDirs = ConfigCore.getStringArrayParameter(ParameterCore.PROCESS_DIRS);
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
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("errorDirectoryDeleting", new Object[] {Helper.getTranslation("metadata") }, logger,
                e);
        }
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
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROPERTY.getTranslationPlural() }, logger,
                e);
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
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROPERTY.getTranslationPlural() }, logger,
                e);
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
            Helper.setMessage(PROPERTIES_SAVED);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROPERTY.getTranslationPlural() }, logger, e);
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
            Helper.setMessage(PROPERTIES_SAVED);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROPERTY.getTranslationPlural() }, logger, e);
        }
        loadWorkpieceProperties();
    }

    /**
     * Save task and redirect to processEdit view.
     *
     * @return url to processEdit view
     */
    public String saveTaskAndRedirect() {
        saveTask(this.task, this.process, ObjectType.PROCESS.getTranslationSingular(), serviceManager.getTaskService());
        return processEditPath + "&id=" + (Objects.isNull(this.process.getId()) ? 0 : this.process.getId());
    }

    /**
     * Remove task.
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
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
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
        deleteUser(this.task);
        return null;
    }

    /**
     * Remove UserGroup.
     *
     * @return empty String
     */
    public String deleteUserGroup() {
        deleteUserGroup(this.task);
        return null;
    }

    /**
     * Add UserGroup.
     *
     * @return empty String
     */
    public String addUserGroup() {
        addUserGroup(this.task);
        return null;
    }

    /**
     * Add User.
     *
     * @return empty String
     */
    public String addUser() {
        addUser(this.task);
        return null;
    }

    /**
     * Export METS.
     */
    public void exportMets() {
        ExportMets export = new ExportMets();
        try {
            export.startExport(this.process);
            Helper.setMessage(EXPORT_FINISHED);
        } catch (ReadException | ExportFileException | MetadataTypeNotAllowedException | WriteException
                | PreferencesException | IOException | RuntimeException | JAXBException e) {
            Helper.setErrorMessage("An error occurred while trying to export METS file for: " + this.process.getTitle(),
                logger, e);
        }
    }

    /**
     * Export PDF.
     */
    public void exportPdf() {
        ExportPdf export = new ExportPdf();
        try {
            export.startExport(this.process);
            Helper.setMessage(EXPORT_FINISHED);
        } catch (PreferencesException | WriteException | MetadataTypeNotAllowedException | ReadException | IOException
                | ExportFileException | RuntimeException | JAXBException e) {
            Helper.setErrorMessage("An error occurred while trying to export PDF file for: " + this.process.getTitle(),
                logger, e);
        }
    }

    /**
     * Export DMS.
     */
    public void exportDMS() {
        ExportDms export = new ExportDms();
        try {
            export.startExport(this.process);
            Helper.setMessage(EXPORT_FINISHED);
        } catch (PreferencesException | WriteException | MetadataTypeNotAllowedException | ReadException | IOException
                | ExportFileException | RuntimeException | JAXBException e) {
            Helper.setErrorMessage(ERROR_EXPORTING,
                new Object[] {ObjectType.PROCESS.getTranslationSingular(), this.process.getId() }, logger, e);
        }
    }

    /**
     * Export DMS for selected processes.
     */
    public void exportDMSForSelection() {
        exportDMSForProcesses(this.selectedProcesses);
    }

    /**
     * Export DMS processes on the page.
     */
    @SuppressWarnings("unchecked")
    public void exportDMSForPage() {
        exportDMSForProcesses(lazyDTOModel.getEntities());
    }

    /**
     * Export DMS for all found processes.
     */
    @SuppressWarnings("unchecked")
    public void exportDMSForAll() {
        exportDMSForProcesses(lazyDTOModel.getEntities());
    }

    private void exportDMSForProcesses(List<ProcessDTO> processes) {
        ExportDms export = new ExportDms();
        for (ProcessDTO process : processes) {
            try {
                Process processBean = serviceManager.getProcessService().getById(process.getId());
                export.startExport(processBean);
                Helper.setMessage(EXPORT_FINISHED);
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), process.getId() }, logger, e);
            } catch (PreferencesException | WriteException | MetadataTypeNotAllowedException | ReadException
                    | IOException | ExportFileException | RuntimeException | JAXBException e) {
                Helper.setErrorMessage(ERROR_EXPORTING,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), process.getId() }, logger, e);
            }
        }
    }

    /**
     * Upload all processes from home.
     */
    public void uploadFromHomeForAll() {
        WebDav myDav = new WebDav();
        List<URI> folder = myDav.uploadAllFromHome(doneDirectoryName);
        myDav.removeAllFromHome(folder, URI.create(doneDirectoryName));
        Helper.setMessage("directoryRemovedAll", doneDirectoryName);
    }

    /**
     * Upload from home for single process.
     */
    public void uploadFromHome() {
        WebDav myDav = new WebDav();
        myDav.uploadFromHome(this.process);
        Helper.setMessage("directoryRemoved", this.process.getTitle());
    }

    /**
     * Download to home for single process.
     */
    public void downloadToHome() {
        /*
         * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in
         * Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde,
         * ansonsten Download
         */
        if (!serviceManager.getProcessService().isImageFolderInUse(this.process)) {
            WebDav myDav = new WebDav();
            myDav.downloadToHome(this.process, false);
        } else {
            Helper.setMessage(
                Helper.getTranslation("directory ") + " " + this.process.getTitle() + " "
                        + Helper.getTranslation("isInUse"),
                serviceManager.getUserService()
                        .getFullName(serviceManager.getProcessService().getImageFolderInUseUser(this.process)));
            WebDav myDav = new WebDav();
            myDav.downloadToHome(this.process, true);
        }
    }

    /**
     * Download to home for selected processes.
     */
    public void downloadToHomeForSelection() {
        WebDav myDav = new WebDav();
        for (ProcessDTO processDTO : this.selectedProcesses) {
            download(myDav, processDTO);
        }
        // TODO: fix message
        Helper.setMessage("createdInUserHomeAll");
    }

    /**
     * Download to home for all process on the page.
     */
    @SuppressWarnings("unchecked")
    public void downloadToHomeForPage() {
        WebDav webDav = new WebDav();
        //TODO: lazyDTOModel.getEntities() - is not a page - how to get exactly this what is on the page?
        for (ProcessDTO process : (List<ProcessDTO>) lazyDTOModel.getEntities()) {
            download(webDav, process);
        }
        Helper.setMessage("createdInUserHome");
    }

    /**
     * Download to home for all found processes.
     */
    @SuppressWarnings("unchecked")
    public void downloadToHomeForAll() {
        WebDav webDav = new WebDav();
        for (ProcessDTO processDTO : (List<ProcessDTO>) lazyDTOModel.getEntities()) {
            download(webDav, processDTO);
        }
        Helper.setMessage("createdInUserHomeAll");
    }

    private void download(WebDav webDav, ProcessDTO processDTO) {
        try {
            Process process = serviceManager.getProcessService().convertDtoToBean(processDTO);
            if (!serviceManager.getProcessService().isImageFolderInUse(processDTO)) {
                webDav.downloadToHome(process, false);
            } else {
                Helper.setMessage(
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
     * Set up processing status page.
     */
    @SuppressWarnings("unchecked")
    public void setTaskStatusUpForPage() {
        setTaskStatusUpForProcesses(lazyDTOModel.getEntities());
    }

    /**
     * Set up processing status selection.
     */
    public void setTaskStatusUpForSelection() {
        setTaskStatusUpForProcesses(this.selectedProcesses);
    }

    /**
     * Set up processing status for all found processes.
     */
    @SuppressWarnings("unchecked")
    public void setTaskStatusUpForAll() {
        setTaskStatusUpForProcesses(lazyDTOModel.getEntities());
    }

    private void setTaskStatusUpForProcesses(List<ProcessDTO> processes) {
        for (ProcessDTO process : processes) {
            try {
                Process processBean = serviceManager.getProcessService().getById(process.getId());
                workflowControllerService.setTasksStatusUp(processBean);
                serviceManager.getProcessService().save(processBean);
            } catch (DAOException | DataException | IOException e) {
                Helper.setErrorMessage("errorChangeTaskStatus",
                    new Object[] {Helper.getTranslation("up"), process.getId() }, logger, e);
            }
        }
    }

    /**
     * Set down processing status page.
     */
    @SuppressWarnings("unchecked")
    public void setTaskStatusDownForPage() {
        setTaskStatusDownForProcesses(lazyDTOModel.getEntities());
    }

    /**
     * Set down processing status selection.
     */
    public void setTaskStatusDownForSelection() {
        setTaskStatusDownForProcesses(this.selectedProcesses);
    }

    /**
     * Set down processing status hits.
     */
    @SuppressWarnings("unchecked")
    public void setTaskStatusDownForAll() {
        setTaskStatusDownForProcesses(lazyDTOModel.getEntities());
    }

    private void setTaskStatusDownForProcesses(List<ProcessDTO> processes) {
        for (ProcessDTO process : processes) {
            try {
                Process processBean = serviceManager.getProcessService().getById(process.getId());
                workflowControllerService.setTasksStatusDown(processBean);
                serviceManager.getProcessService().save(processBean);
            } catch (DAOException | DataException e) {
                Helper.setErrorMessage("errorChangeTaskStatus",
                    new Object[] {Helper.getTranslation("down"), process.getId() }, logger, e);
            }
        }
    }

    /**
     * Task status up.
     */
    public void setTaskStatusUp() throws DataException, IOException {
        setTask(workflowControllerService.setTaskStatusUp(this.task));
        save();
        deleteSymlinksFromUserHomes();
    }

    /**
     * Task status down.
     */
    public void setTaskStatusDown() {
        setTask(workflowControllerService.setTaskStatusDown(this.task));
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
     * Set process by ID.
     *
     * @param processID
     *            ID of process to set.
     */
    public void setProcessByID(int processID) {
        try {
            setProcess(serviceManager.getProcessService().getById(processID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                new Object[] {ObjectType.PROCESS.getTranslationSingular(), processID }, logger, e);
        }
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
     * Reload task and process.
     */
    private void reload() {
        reload(this.task, ObjectType.TASK.getTranslationSingular(), serviceManager.getTaskService());
        reload(this.process, ObjectType.PROCESS.getTranslationSingular(), serviceManager.getProcessService());
    }

    /**
     * Calculate metadata and images pages.
     */
    @SuppressWarnings("unchecked")
    public void calculateMetadataAndImagesPage() {
        calculateMetadataAndImages(lazyDTOModel.getEntities());
    }

    /**
     * Calculate metadata and images selection.
     */
    public void calculateMetadataAndImagesSelection() {
        calculateMetadataAndImages(this.selectedProcesses);
    }

    /**
     * Calculate metadata and images hits.
     */
    @SuppressWarnings("unchecked")
    public void calculateMetadataAndImagesHits() {
        calculateMetadataAndImages(lazyDTOModel.getEntities());
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

    public Map<String, Integer> getCounterSummary() {
        return this.counterSummary;
    }

    public List<ProcessCounterObject> getProcessCounterObjects() {
        return this.processCounterObjects;
    }

    /**
     * Execute Kitodo script for hits list.
     */
    @SuppressWarnings("unchecked")
    public void executeKitodoScriptHits() {
        executeKitodoScriptForProcesses(lazyDTOModel.getEntities());
    }

    /**
     * Execute Kitodo script for processes displayed on the page.
     */
    @SuppressWarnings("unchecked")
    public void executeKitodoScriptPage() {
        executeKitodoScriptForProcesses(lazyDTOModel.getEntities());
    }

    /**
     * Execute Kitodo script for selected processes.
     */
    public void executeKitodoScriptSelection() {
        executeKitodoScriptForProcesses(this.selectedProcesses);
    }

    private void executeKitodoScriptForProcesses(List<ProcessDTO> processes) {
        GoobiScript gs = new GoobiScript();
        try {
            gs.execute(serviceManager.getProcessService().convertDtosToBeans(processes), this.kitodoScript);
        } catch (DAOException | DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
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

    public Map<String, Boolean> getAnzeigeAnpassen() {
        return this.anzeigeAnpassen;
    }

    public void setAnzeigeAnpassen(Map<String, Boolean> anzeigeAnpassen) {
        this.anzeigeAnpassen = anzeigeAnpassen;
    }

    public String getNewProcessTitle() {
        return this.newProcessTitle;
    }

    public void setNewProcessTitle(String newProcessTitle) {
        this.newProcessTitle = newProcessTitle;
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
     * Starts generation of xml logfile for current process.
     */
    public void createXML() {
        try {
            ExportXmlLog xmlExport = new ExportXmlLog();
            String directory = new File(serviceManager.getUserService().getHomeDirectory(getUser())).getPath();
            String destination = directory + "/" + this.process.getTitle() + "_log.xml";
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
            ExternalContext response = prepareHeaderInformation(facesContext, "export.xml");
            try (OutputStream out = response.getResponseOutputStream()) {
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
                List<URI> files = fileService.getSubUris(folder);

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
     * Downloads a docket for process.
     */
    public void downloadDocket() {
        try {
            serviceManager.getProcessService().downloadDocket(this.process);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Generate result as PDF.
     */
    public void generateResultAsPdf() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            ExternalContext response = prepareHeaderInformation(facesContext, "search.pdf");
            try (OutputStream out = response.getResponseOutputStream()) {
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
                // create formatter for cells with default locale
                DataFormatter formatter = new DataFormatter();
                Rectangle rectangle = new Rectangle(PageSize.A3.getHeight(), PageSize.A3.getWidth());
                PdfWriter.getInstance(document, out);
                document.setPageSize(rectangle);
                document.open();
                if (!rowList.isEmpty()) {
                    Paragraph p = new Paragraph(rowList.get(0).get(0).toString());
                    document.add(p);
                    PdfPTable table = new PdfPTable(9);
                    table.setSpacingBefore(20);
                    for (List<HSSFCell> row : rowList) {
                        for (HSSFCell hssfCell : row) {
                            String stringCellValue = formatter.formatCellValue(hssfCell);
                            table.addCell(stringCellValue);
                        }
                    }
                    document.add(table);
                }

                document.close();
                out.flush();
                facesContext.responseComplete();
            } catch (IOException | DocumentException | RuntimeException e) {
                Helper.setErrorMessage(ERROR_CREATING, new Object[] {Helper.getTranslation("resultPDF") }, logger, e);
            }
        }
    }

    /**
     * Generate result set.
     */
    public void generateResult() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            ExternalContext response = prepareHeaderInformation(facesContext, "search.xls");
            try (OutputStream out = response.getResponseOutputStream()) {
                SearchResultGeneration sr = new SearchResultGeneration(this.filter, this.showClosedProcesses,
                        this.showInactiveProjects);
                HSSFWorkbook wb = sr.getResult();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();
            } catch (IOException e) {
                Helper.setErrorMessage(ERROR_CREATING, new Object[] {Helper.getTranslation("resultSet") }, logger, e);
            }
        }
    }

    private ExternalContext prepareHeaderInformation(FacesContext facesContext, String outputFileName) {
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.responseReset();

        String contentType = externalContext.getMimeType(outputFileName);
        externalContext.setResponseContentType(contentType);
        externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + outputFileName + "\"");

        return externalContext;
    }

    /**
     * Return whether closed processes should be displayed or not.
     *
     * @return parameter controlling whether closed processes should be displayed
     *         or not
     */
    public boolean isShowClosedProcesses() {
        return this.showClosedProcesses;
    }

    /**
     * Set whether closed processes should be displayed or not.
     *
     * @param showClosedProcesses
     *            boolean flag signaling whether closed processes should be
     *            displayed or not
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        this.showClosedProcesses = showClosedProcesses;
        serviceManager.getProcessService().setShowClosedProcesses(showClosedProcesses);
    }

    /**
     * Set whether inactive projects should be displayed or not.
     *
     * @param showInactiveProjects
     *            boolean flag signaling whether inactive projects should be
     *            displayed or not
     */
    @Override
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
        serviceManager.getProcessService().setShowInactiveProjects(showInactiveProjects);
    }

    /**
     * Return whether inactive projects should be displayed or not.
     *
     * @return parameter controlling whether inactive projects should be displayed
     *         or not
     */
    @Override
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
                Helper.setErrorMessage(ERROR_RELOADING, new Object[] {Helper.getTranslation("wikiField") }, logger, e);
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
            Helper.setMessage(PROPERTY_SAVED);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROPERTY.getTranslationSingular() }, logger,
                e);
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
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROPERTY.getTranslationSingular() }, logger,
                e);
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
            Helper.setMessage(PROPERTY_SAVED);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROPERTY.getTranslationSingular() }, logger,
                e);
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
                    Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROPERTY.getTranslationSingular() },
                        logger, e);
                }
            }
        }
    }

    /**
     * Get dockets for select list.
     *
     * @return list of dockets as SelectItem objects
     */
    public List<SelectItem> getDockets() {
        return SelectItemList.getDockets();
    }

    /**
     * Get list of projects.
     *
     * @return list of projects as SelectItem objects
     */
    public List<SelectItem> getProjects() {
        return SelectItemList.getProjects();
    }

    /**
     * Get rulesets for select list.
     *
     * @return list of rulesets as SelectItem objects
     */
    public List<SelectItem> getRulesets() {
        return SelectItemList.getRulesets();
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
     * Method being used as viewAction for process edit form. If the given
     * parameter 'id' is '0', the form for creating a new process will be
     * displayed.
     *
     * @param id
     *            ID of the process to load
     */
    public void load(int id) {
        try {
            if (id != 0) {
                setProcess(this.serviceManager.getProcessService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), id },
                logger, e);
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
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
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
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.USER.getTranslationPlural() }, logger,
                e);
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
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.USER_GROUP.getTranslationPlural() },
                logger, e);
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

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the task edit page.
     *
     * @param referer
     *            the referring view
     */
    public void setTaskEditReferer(String referer) {
        if (referer.equals("processEdit?id=" + this.task.getProcess().getId())) {
            this.taskEditReferer = referer;
        } else {
            this.taskEditReferer = DEFAULT_LINK;
        }
    }

    /**
     * Get task edit page referring view.
     *
     * @return task eit page referring view
     */
    public String getTaskEditReferer() {
        return this.taskEditReferer;
    }

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the process edit page.
     *
     * @param referer
     *            the referring view
     */
    public void setProcessEditReferer(String referer) {
        if (!referer.isEmpty()) {
            if (referer.equals("processes")) {
                this.processEditReferer = referer;
            } else if (!referer.contains("taskEdit") || this.processEditReferer.isEmpty()) {
                this.processEditReferer = DEFAULT_LINK;
            }
        }
    }

    /**
     * Get process edit page referring view.
     *
     * @return process edit page referring view
     */
    public String getProcessEditReferer() {
        return this.processEditReferer;
    }
}
