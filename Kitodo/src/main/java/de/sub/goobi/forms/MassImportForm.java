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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.unigoettingen.sub.search.opac.ConfigOpac;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.goobi.production.constants.FileNames;
import org.goobi.production.enums.ImportFormat;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.properties.ImportProperty;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

@Named("MassImportForm")
@SessionScoped
public class MassImportForm implements Serializable {
    private static final Logger logger = LogManager.getLogger(MassImportForm.class);
    private static final long serialVersionUID = -4225927414279404442L;
    private Template template;
    private List<Process> processes;
    private List<String> digitalCollections;
    private List<String> possibleDigitalCollections;
    private String opacCatalogue;
    private List<String> ids = new ArrayList<>();
    private ImportFormat format = null;
    private String idList = "";
    private String records = "";
    private List<String> usablePluginsForRecords;
    private List<String> usablePluginsForIDs;
    private List<String> usablePluginsForFiles;
    private List<String> usablePluginsForFolder;
    private String currentPlugin = "";
    private IImportPlugin plugin;
    private File importFile = null;
    private transient ServiceManager serviceManager = new ServiceManager();
    private UploadedFile uploadedFile = null;
    private List<Process> processList;
    private static final String GET_CURRENT_DOC_STRUCTS = "getCurrentDocStructs";
    private static final String OPAC_CONFIG = "configurationOPAC";

    /**
     * Constructor.
     */
    public MassImportForm() {
        usablePluginsForRecords = PluginLoader.getImportPluginsForType(ImportType.RECORD);
        usablePluginsForIDs = PluginLoader.getImportPluginsForType(ImportType.ID);
        usablePluginsForFiles = PluginLoader.getImportPluginsForType(ImportType.FILE);
        usablePluginsForFolder = PluginLoader.getImportPluginsForType(ImportType.FOLDER);
    }

    /**
     * Prepare.
     *
     * @return String
     */
    public String prepare(int id) {
        try {
            this.template = serviceManager.getTemplateService().getById(id);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return null;
        }
        if (serviceManager.getTemplateService().containsBeanUnreachableSteps(this.template.getTasks())) {
            if (this.template.getTasks().isEmpty()) {
                Helper.setErrorMessage("noStepsInWorkflow");
            }
            for (Task task : this.template.getTasks()) {
                if (serviceManager.getTaskService().getUserGroupsSize(task) == 0
                        && serviceManager.getTaskService().getUsersSize(task) == 0) {
                    Helper.setErrorMessage("noUserInStep", new Object[] {task.getTitle()});
                }
            }
            return null;
        }
        initializePossibleDigitalCollections();
        return "/pages/MassImport";
    }

    /**
     * Generate a list with all possible collections for given project.
     */
    @SuppressWarnings("unchecked")
    private void initializePossibleDigitalCollections() {
        final String defaultString = "default";
        this.possibleDigitalCollections = new ArrayList<>();
        ArrayList<String> defaultCollections = new ArrayList<>();
        String filename = ConfigCore.getKitodoConfigDirectory() + FileNames.DIGITAL_COLLECTIONS_FILE;
        if (!(new File(filename).exists())) {
            Helper.setErrorMessage("File not found: ", filename);
            return;
        }
        this.digitalCollections = new ArrayList<>();
        try {
            // read data and determine the root
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new File(filename));
            Element root = doc.getRootElement();
            // run through all projects
            List<Element> projects = root.getChildren();
            for (Element project : projects) {
                // collect default collections
                if (project.getName().equals(defaultString)) {
                    List<Element> myCols = project.getChildren("DigitalCollection");
                    for (Element digitalCollection : myCols) {
                        if (digitalCollection.getAttribute(defaultString) != null
                                && digitalCollection.getAttributeValue(defaultString).equalsIgnoreCase("true")) {
                            digitalCollections.add(digitalCollection.getText());
                        }
                        defaultCollections.add(digitalCollection.getText());
                    }
                } else {
                    runThroughProject(project);
                }
            }
        } catch (JDOMException | IOException e1) {
            Helper.setErrorMessage("Error while parsing digital collections", logger, e1);
        }

        if (this.possibleDigitalCollections.isEmpty()) {
            this.possibleDigitalCollections = defaultCollections;
        }
    }

    @SuppressWarnings("unchecked")
    private void runThroughProject(Element project) {
        final String defaultString = "default";

        List<Element> projectNames = project.getChildren("name");
        for (Element projectName : projectNames) {
            // all all collections to list
            if (projectName.getText().equalsIgnoreCase(this.template.getProject().getTitle())) {
                List<Element> myCols = project.getChildren("DigitalCollection");
                for (Element digitalCollection : myCols) {
                    if (digitalCollection.getAttribute(defaultString) != null
                            && digitalCollection.getAttributeValue(defaultString).equalsIgnoreCase("true")) {
                        digitalCollections.add(digitalCollection.getText());
                    }
                    this.possibleDigitalCollections.add(digitalCollection.getText());
                }
            }
        }
    }

    private List<String> allFilenames = new ArrayList<>();
    private List<String> selectedFilenames = new ArrayList<>();

    public List<String> getAllFilenames() {

        return this.allFilenames;
    }

    public void setAllFilenames(List<String> allFilenames) {
        this.allFilenames = allFilenames;
    }

    public List<String> getSelectedFilenames() {
        return this.selectedFilenames;
    }

    public void setSelectedFilenames(List<String> selectedFilenames) {
        this.selectedFilenames = selectedFilenames;
    }

    /**
     * Convert data.
     *
     * @return String
     */
    public String convertData() throws IOException, DataException {
        this.processList = new ArrayList<>();
        if (StringUtils.isEmpty(currentPlugin)) {
            Helper.setErrorMessage("missingPlugin");
            return null;
        }
        if (testForData()) {
            List<ImportObject> answer = new ArrayList<>();

            // found list with ids
            PrefsInterface prefs = serviceManager.getRulesetService().getPreferences(this.template.getRuleset());
            String tempFolder = ConfigCore.getParameter("tempfolder");
            this.plugin.setImportFolder(tempFolder);
            this.plugin.setPrefs(prefs);
            this.plugin.setOpacCatalogue(this.getOpacCatalogue());
            this.plugin.setKitodoConfigDirectory(ConfigCore.getKitodoConfigDirectory());

            if (StringUtils.isNotEmpty(this.idList)) {
                answer = this.plugin.generateFiles(generateRecordList());
            } else if (this.importFile != null) {
                this.plugin.setFile(this.importFile);
                answer = getAnswer(this.plugin.generateRecordsFromFile());
            } else if (StringUtils.isNotEmpty(this.records)) {
                answer = getAnswer(this.plugin.splitRecords(this.records));
            } else if (!this.selectedFilenames.isEmpty()) {
                answer = getAnswer(this.plugin.generateRecordsFromFilenames(this.selectedFilenames));
            }

            iterateOverAnswer(answer);

            if (answer.size() != this.processList.size()) {
                // some error on process generation, don't go to next page
                return null;
            }
        } else {
            Helper.setErrorMessage("missingData");
            return null;
        }

        removeFiles();

        this.idList = null;
        this.records = "";
        return "/pages/MassImportFormPage3";
    }

    private void iterateOverAnswer(List<ImportObject> answer) throws DataException, IOException {
        Batch batch = null;
        if (answer.size() > 1) {
            batch = getBatch();
        }

        for (ImportObject io : answer) {
            if (batch != null) {
                io.getBatches().add(batch);
            }

            if (io.getImportReturnValue().equals(ImportReturnValue.EXPORT_FINISHED)) {
                addProcessToList(io);
            } else {
                removeImportFileNameFromSelectedFileNames(io);
            }
        }
    }

    private void removeFiles() throws IOException {
        if (this.importFile != null) {
            Files.delete(this.importFile.toPath());
            this.importFile = null;
        }
        if (selectedFilenames != null && !selectedFilenames.isEmpty()) {
            this.plugin.deleteFiles(this.selectedFilenames);
        }
    }

    /**
     * File upload with binary copying.
     */
    public void uploadFile() throws IOException {

        if (this.uploadedFile == null) {
            Helper.setErrorMessage("noFileSelected");
            return;
        }

        String basename = this.uploadedFile.getName();
        if (basename.startsWith(".")) {
            basename = basename.substring(1);
        }
        if (basename.contains("/")) {
            basename = basename.substring(basename.lastIndexOf('/') + 1);
        }
        if (basename.contains("\\")) {
            basename = basename.substring(basename.lastIndexOf('\\') + 1);
        }
        URI temporalFile = serviceManager.getFileService()
                .createResource(ConfigCore.getParameter("tempfolder", "/usr/local/kitodo/temp/") + basename);

        serviceManager.getFileService().copyFile(URI.create(this.uploadedFile.getName()), temporalFile);
    }

    public UploadedFile getUploadedFile() {
        return this.uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    /**
     * tests input fields for correct data.
     *
     * @return true if data is valid or false otherwise
     */

    private boolean testForData() {
        return !(StringUtils.isEmpty(this.idList) && StringUtils.isEmpty(this.records) && (this.importFile == null)
                && this.selectedFilenames.isEmpty());
    }

    private List<Record> generateRecordList() {
        List<String> pluginIds = this.plugin.splitIds(this.idList);
        List<Record> recordList = new ArrayList<>();
        for (String id : pluginIds) {
            Record r = new Record();
            r.setData(id);
            r.setId(id);
            r.setCollections(this.digitalCollections);
            recordList.add(r);
        }
        return recordList;
    }

    private List<ImportObject> getAnswer(List<Record> recordList) {
        for (Record record : recordList) {
            record.setCollections(this.digitalCollections);
        }
        return this.plugin.generateFiles(recordList);
    }

    private Batch getBatch() {
        if (importFile != null) {
            List<String> arguments = new ArrayList<>();
            arguments.add(FilenameUtils.getBaseName(importFile.getAbsolutePath()));
            arguments.add(DateTimeFormat.shortDateTime().print(new DateTime()));
            return new Batch(Helper.getTranslation("importedBatchLabel", arguments), Type.LOGISTIC);
        } else {
            return new Batch();
        }
    }

    private void addProcessToList(ImportObject io) throws DataException, IOException {
        URI importFileName = io.getImportFileName();
        Process process = JobCreation.generateProcess(io, this.template);
        if (process == null) {
            if (Objects.nonNull(importFileName) && !serviceManager.getFileService().getFileName(importFileName).isEmpty()
                    && selectedFilenames != null && !selectedFilenames.isEmpty()
                    && selectedFilenames.contains(importFileName.getRawPath())) {
                selectedFilenames.remove(importFileName.getRawPath());
            }
            Helper.setErrorMessage(
                    "import failed for " + io.getProcessTitle() + ", process generation failed");

        } else {
            Helper.setMessage(ImportReturnValue.EXPORT_FINISHED.getValue() + " for " + io.getProcessTitle());
            this.processList.add(process);
        }
    }

    private void removeImportFileNameFromSelectedFileNames(ImportObject io) {
        URI importFileName = io.getImportFileName();
        Helper.setErrorMessage("importFailedError", new Object[] {io.getProcessTitle(), io.getErrorMessage()});
        if (Objects.nonNull(importFileName) && !serviceManager.getFileService().getFileName(importFileName).isEmpty()
                && selectedFilenames != null && !selectedFilenames.isEmpty()
                && selectedFilenames.contains(importFileName.getRawPath())) {
            selectedFilenames.remove(importFileName.getRawPath());
        }
    }

    /**
     * Set id list.
     *
     * @param idList
     *            the idList to set
     */
    public void setIdList(String idList) {
        this.idList = idList;
    }

    /**
     * Get id list.
     *
     * @return the idList
     */
    public String getIdList() {
        return this.idList;
    }

    /**
     * Set records.
     *
     * @param records
     *            the records to set
     */
    public void setRecords(String records) {
        this.records = records;
    }

    /**
     * Get recrods.
     *
     * @return the records
     */
    public String getRecords() {
        return this.records;
    }

    /**
     * Set processes.
     *
     * @param processes
     *            the process list to set
     */
    public void setProcess(List<Process> processes) {
        this.processes = processes;
    }

    /**
     * Get processes.
     *
     * @return the process
     */
    public List<Process> getProcess() {
        return this.processes;
    }

    /**
     * Set template.
     *
     * @param template
     *            the template to set
     */
    public void setTemplate(Template template) {
        this.template = template;

    }

    /**
     * Get template.
     *
     * @return the template
     */
    public Template getTemplate() {
        return this.template;
    }

    /**
     * Get all OPAC catalogues.
     *
     * @return the opac catalogues
     */
    public List<String> getAllOpacCatalogues() {
        List<String> allOpacCatalogues = new ArrayList<>();
        try {
            allOpacCatalogues = ConfigOpac.getAllCatalogueTitles();
        } catch (RuntimeException e) {
            Helper.setErrorMessage("errorReading", new Object[]{Helper.getTranslation(OPAC_CONFIG)}, logger, e);
        }
        return allOpacCatalogues;
    }

    /**
     * Set OPAC catalogue.
     *
     * @param opacCatalogue
     *            the opacCatalogue to set
     */

    public void setOpacCatalogue(String opacCatalogue) {
        this.opacCatalogue = opacCatalogue;
    }

    /**
     * Get OPAC catalogue.
     *
     * @return the opac catalogues
     */

    public String getOpacCatalogue() {
        return this.opacCatalogue;
    }

    /**
     * Set digital collections.
     *
     * @param digitalCollections
     *            the digitalCollections to set
     */
    public void setDigitalCollections(List<String> digitalCollections) {
        this.digitalCollections = digitalCollections;
    }

    /**
     * Get digital collections.
     *
     * @return the digitalCollections
     */
    public List<String> getDigitalCollections() {
        return this.digitalCollections;
    }

    /**
     * Set possible digital collection.
     *
     * @param possibleDigitalCollection
     *            the possibleDigitalCollection to set
     */
    public void setPossibleDigitalCollection(List<String> possibleDigitalCollection) {
        this.possibleDigitalCollections = possibleDigitalCollection;
    }

    /**
     * Get possible digital collection.
     *
     * @return the possibleDigitalCollection
     */
    public List<String> getPossibleDigitalCollection() {
        return this.possibleDigitalCollections;
    }

    public void setPossibleDigitalCollections(List<String> possibleDigitalCollections) {
        this.possibleDigitalCollections = possibleDigitalCollections;
    }

    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    /**
     * Set ids.
     *
     * @param ids
     *            the ids to set
     */
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    /**
     * Get ids.
     *
     * @return the ids
     */
    public List<String> getIds() {
        return this.ids;
    }

    /**
     * Set format.
     *
     * @param format
     *            the format to set
     */
    public void setFormat(String format) {
        this.format = ImportFormat.getTypeFromTitle(format);
    }

    /**
     * Get format.
     *
     * @return the format
     */
    public String getFormat() {
        if (this.format == null) {
            return null;
        }
        return this.format.getTitle();
    }

    /**
     * Set current plugin.
     *
     * @param currentPlugin
     *            the currentPlugin to set
     */
    public void setCurrentPlugin(String currentPlugin) {
        this.currentPlugin = currentPlugin;
        if (this.currentPlugin != null && this.currentPlugin.length() > 0) {
            this.plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.IMPORT, this.currentPlugin);

            if (Objects.nonNull(this.plugin)) {
                if (this.plugin.getImportTypes().contains(ImportType.FOLDER)) {
                    this.allFilenames = this.plugin.getAllFilenames();
                }
                this.plugin.setPrefs(serviceManager.getRulesetService().getPreferences(template.getRuleset()));
            }
        }
    }

    /**
     * Get current plugin.
     *
     * @return the currentPlugin
     */
    public String getCurrentPlugin() {
        return this.currentPlugin;
    }

    public IImportPlugin getPlugin() {
        return plugin;
    }

    /**
     * Set usable plugins for records.
     *
     * @param usablePluginsForRecords
     *            the usablePluginsForRecords to set
     */
    public void setUsablePluginsForRecords(List<String> usablePluginsForRecords) {
        this.usablePluginsForRecords = usablePluginsForRecords;
    }

    /**
     * Get usable plugins for records.
     *
     * @return the usablePluginsForRecords
     */
    public List<String> getUsablePluginsForRecords() {
        return this.usablePluginsForRecords;
    }

    /**
     * Set usable plugins for ids.
     *
     * @param usablePluginsForIDs
     *            the usablePluginsForIDs to set
     */
    public void setUsablePluginsForIDs(List<String> usablePluginsForIDs) {
        this.usablePluginsForIDs = usablePluginsForIDs;
    }

    /**
     * Get usable plugins for ids.
     *
     * @return the usablePluginsForIDs
     */
    public List<String> getUsablePluginsForIDs() {
        return this.usablePluginsForIDs;
    }

    /**
     * Set usable plugins for files.
     *
     * @param usablePluginsForFiles
     *            the usablePluginsForFiles to set
     */
    public void setUsablePluginsForFiles(List<String> usablePluginsForFiles) {
        this.usablePluginsForFiles = usablePluginsForFiles;
    }

    /**
     * get usable plugins for files.
     *
     * @return the usablePluginsForFiles
     */
    public List<String> getUsablePluginsForFiles() {
        return this.usablePluginsForFiles;
    }

    /**
     * Get has next page.
     *
     * @return boolean
     */
    public boolean getHasNextPage() {
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod(GET_CURRENT_DOC_STRUCTS);
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (list != null) {
                return true;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RuntimeException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        try {
            method = this.plugin.getClass().getMethod("getProperties");
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<ImportProperty> list = (List<ImportProperty>) o;
            return !list.isEmpty();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RuntimeException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return false;
    }

    /**
     * Get next page.
     *
     * @return next page
     */
    public String nextPage() {
        if (!testForData()) {
            Helper.setErrorMessage("missingData");
            return null;
        }
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod(GET_CURRENT_DOC_STRUCTS);
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (list != null) {
                return "/pages/MultiMassImportPage2";
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RuntimeException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return "/pages/MassImportFormPage2";
    }

    /**
     * Get properties.
     *
     * @return list of ImportProperty objects
     */
    public List<ImportProperty> getProperties() {

        if (this.plugin != null) {
            return this.plugin.getProperties();
        }
        return new ArrayList<>();
    }

    public List<Process> getProcessList() {
        return this.processList;
    }

    public void setProcessList(List<Process> processList) {
        this.processList = processList;
    }

    public List<String> getUsablePluginsForFolder() {
        return this.usablePluginsForFolder;
    }

    public void setUsablePluginsForFolder(List<String> usablePluginsForFolder) {
        this.usablePluginsForFolder = usablePluginsForFolder;
    }

    /**
     * Download docket.
     *
     * @return String
     */
    public String downloadDocket() throws IOException {
        serviceManager.getProcessService().downloadDocket(this.processList);
        return "";
    }

    /**
     * Get document structure.
     *
     * @return list of DocstructElement objects
     */
    public List<? extends DocstructElement> getDocstructs() {
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod(GET_CURRENT_DOC_STRUCTS);
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (list != null) {
                return list;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RuntimeException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new ArrayList<>();
    }

    public int getDocstructssize() {
        return getDocstructs().size();
    }

    public String getInclude() {
        return "plugins/" + plugin.getTitle() + ".jsp";
    }
}
