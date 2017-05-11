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

package org.kitodo.services.data;

import com.sun.research.ws.wadl.HTTPMethods;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.goobi.io.BackupFileRotation;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.export.ExportDocket;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.json.simple.parser.ParseException;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.helper.enums.MetadataFormat;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.ProcessDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProcessType;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;
import org.kitodo.services.file.FileService;

import ugh.dl.DigitalDocument;
import ugh.dl.Fileformat;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;
import ugh.fileformats.mets.XStream;

public class ProcessService extends TitleSearchService<Process> {

    Helper help = new Helper();

    private Boolean selected = false;
    private ProcessDAO processDAO = new ProcessDAO();
    private ProcessType processType = new ProcessType();
    private Indexer<Process, ProcessType> indexer = new Indexer<>(Process.class);
    private final MetadatenSperrung msp = new MetadatenSperrung();
    private final ServiceManager serviceManager = new ServiceManager();
    private final FileService fileService = serviceManager.getFileService();
    private static final Logger logger = LogManager.getLogger(ProcessService.class);
    private static final String TEMPORARY_FILENAME_PREFIX = "temporary_";

    public static String DIRECTORY_PREFIX = "orig";
    public static String DIRECTORY_SUFFIX = "images";

    /**
     * Constructor with searcher's assigning.
     */
    public ProcessService() {
        super(new Searcher(Process.class));
    }

    public Process find(Integer id) throws DAOException {
        return processDAO.find(id);
    }

    public List<Process> findAll() throws DAOException {
        return processDAO.findAll();
    }

    /**
     * Method saves process object to database.
     *
     * @param process
     *            object
     */
    public void saveToDatabase(Process process) throws DAOException {
        processDAO.save(process, getProgress(process));
    }

    /**
     * Method saves process document to the index of Elastic Search.
     *
     * @param process
     *            object
     */
    public void saveToIndex(Process process) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(process, processType);
    }

    /**
     * Method saves batches related to modified process.
     *
     * @param process
     *            object
     */
    protected void saveDependenciesToIndex(Process process) throws CustomResponseException, IOException {
        for (Batch batch : process.getBatches()) {
            serviceManager.getBatchService().saveToIndex(batch);
        }
        if (process.getProject() != null) {
            serviceManager.getProjectService().saveToIndex(process.getProject());
        }
    }

    public void saveList(List<Process> list) throws DAOException {
        processDAO.saveList(list);
    }

    /**
     * Method removes process object from database.
     *
     * @param process
     *            object
     */
    public void removeFromDatabase(Process process) throws DAOException {
        processDAO.remove(process);
    }

    /**
     * Method removes process object from database.
     *
     * @param id
     *            of process object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        processDAO.remove(id);
    }

    /**
     * Method removes process object from index of Elastic Search.
     *
     * @param process
     *            object
     */
    public void removeFromIndex(Process process) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(process, processType);
    }

    public List<Process> search(String query) throws DAOException {
        return processDAO.search(query);
    }

    public Long count(String query) throws DAOException {
        return processDAO.count(query);
    }

    public void refresh(Process process) {
        processDAO.refresh(process);
    }

    /**
     * Find processes by output name.
     * 
     * @param outputName
     *            as String
     * @return list of search results
     */
    public List<SearchResult> findByOutputName(String outputName)
            throws CustomResponseException, IOException, ParseException {
        QueryBuilder query = createSimpleQuery("outputName", outputName, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find processes for exact creation date.
     *
     * @param creationDate
     *            of the searched processes as Date
     * @param searchCondition
     *            as SearchCondition - bigger, smaller and so on
     * @return list of search results
     */
    public List<SearchResult> findByCreationDate(Date creationDate, SearchCondition searchCondition)
            throws CustomResponseException, IOException, ParseException {
        QueryBuilder query = createSimpleCompareDateQuery("creationDate", creationDate, searchCondition);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find processes by wiki field.
     *
     * @param wikiField
     *            as String
     * @return list of search results
     */
    public List<SearchResult> findByWikiField(String wikiField)
            throws CustomResponseException, IOException, ParseException {
        QueryBuilder query = createSimpleQuery("wikiField", wikiField, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find processes by id of project.
     *
     * @param id
     *            of project
     * @return list of search results with batches for specific process id
     */
    public List<SearchResult> findByProjectId(Integer id) throws CustomResponseException, IOException, ParseException {
        QueryBuilder query = createSimpleQuery("project", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find processes by title of project.
     *
     * @param title
     *            of process
     * @return list of search results with batches for specific process id
     */
    public List<SearchResult> findByProjectTitle(String title)
            throws CustomResponseException, IOException, ParseException {
        List<SearchResult> processes = new ArrayList<>();

        List<SearchResult> projects = serviceManager.getProjectService().findByTitle(title, true);
        for (SearchResult project : projects) {
            processes.addAll(findByProjectId(project.getId()));
        }
        return processes;
    }

    /**
     * Find processes by id of batch.
     *
     * @param id
     *            of process
     * @return list of search results with processes for specific batch id
     */
    public List<SearchResult> findByBatchId(Integer id) throws CustomResponseException, IOException, ParseException {
        QueryBuilder query = createSimpleQuery("batches.id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find processes by title of batch.
     *
     * @param title
     *            of batch
     * @return list of search results with processes for specific batch title
     */
    public List<SearchResult> findByBatchTitle(String title)
            throws CustomResponseException, IOException, ParseException {
        List<SearchResult> processes = new ArrayList<>();

        List<SearchResult> batches = serviceManager.getBatchService().findByTitle(title, true);
        for (SearchResult batch : batches) {
            processes.addAll(findByBatchId(batch.getId()));
        }
        return processes;
    }

    /**
     * Find processes by property.
     *
     * @param title
     *            of property
     * @param value
     *            of property
     * @return list of search results with processes for specific property
     */
    public List<SearchResult> findByProperty(String title, String value)
            throws CustomResponseException, IOException, ParseException {
        List<SearchResult> processes = new ArrayList<>();

        List<SearchResult> properties = serviceManager.getPropertyService().findByTitleAndValue(title, value);
        for (SearchResult property : properties) {
            processes.addAll(findByPropertyId(property.getId()));
        }
        return processes;
    }

    /**
     * Simulate relationship between property and process type.
     * 
     * @param id
     *            of property
     * @return list of search results with processes for specific property id
     */
    private List<SearchResult> findByPropertyId(Integer id)
            throws CustomResponseException, IOException, ParseException {
        QueryBuilder query = createSimpleQuery("properties.id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws CustomResponseException, DAOException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), processType);
    }

    /**
     * Returns the batches of the desired type for a process.
     *
     * @param type
     *            of batches to return
     * @return all batches of the desired type
     */
    public List<Batch> getBatchesByType(Process process, Type type) {
        List<Batch> batches = getBatchesInitialized(process);
        if (type != null) {
            List<Batch> result = new ArrayList<>(batches);
            Iterator<Batch> indicator = result.iterator();
            while (indicator.hasNext()) {
                if (!type.equals(indicator.next().getType())) {
                    indicator.remove();
                }
            }
            return result;
        }
        return batches;
    }

    /**
     * The function getBatchesInitialized() returns the batches for a process
     * and takes care that the object is initialized from Hibernate already and
     * will not be bothered if the Hibernate session ends. TODO: check if it is
     * necessary!!
     *
     * @return the batches field of the process which is loaded
     */
    public List<Batch> getBatchesInitialized(Process process) {
        if (process.getId() != null) {
            Hibernate.initialize(process.getBatches());
        }
        return process.getBatches();
    }

    /**
     * The function getHistoryInitialized() returns the history events for a
     * process and takes care that the object is initialized from Hibernate
     * already and will not be bothered if the Hibernate session ends. TODO:
     * check if it is necessary!!
     *
     * @return the history field of the process which is loaded
     */
    public List<History> getHistoryInitialized(Process process) {
        try {
            @SuppressWarnings("unused")
            Session s = Helper.getHibernateSession();
            Hibernate.initialize(process.getHistory());
        } catch (HibernateException e) {
            logger.debug("Hibernate exception: ", e);
        }
        if (process.getHistory() == null) {
            process.setHistory(new ArrayList<History>());
        }
        return process.getHistory();
    }

    /**
     * The function getPropertiesInitialized() returns the descriptive fields
     * (“properties”) for a process and takes care that the object is
     * initialized from Hibernate already and will not be bothered if the
     * Hibernate session ends. TODO: check if it is necessary!! <- e.g.
     * BeanHelper uses it
     *
     * @return the properties field of the process which is loaded
     */
    public List<Property> getPropertiesInitialized(Process process) {
        try {
            Hibernate.initialize(process.getProperties());
        } catch (HibernateException e) {
            logger.debug("Hibernate exception: ", e);
        }
        return process.getProperties();
    }

    /**
     * Get blocked user.
     *
     * @return blocked metadata (user)
     */
    public User getBlockedUsers(Process process) {
        User result = null;
        if (MetadatenSperrung.isLocked(process.getId())) {
            String userID = this.msp.getLockBenutzer(process.getId());
            try {
                result = serviceManager.getUserService().find(Integer.valueOf(userID));
            } catch (Exception e) {
                Helper.setFehlerMeldung(Helper.getTranslation("userNotFound"), e);
            }
        }
        return result;
    }

    public long getBlockedMinutes(Process process) {
        return this.msp.getLockSekunden(process.getId()) / 60;
    }

    public long getBlockedSeconds(Process process) {
        return this.msp.getLockSekunden(process.getId()) % 60;
    }

    /**
     * Get directory for tig images.
     *
     * @param useFallBack
     *            add description
     * @param process
     *            object
     * @return tif directory
     */
    public String getImagesTifDirectory(boolean useFallBack, Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        File dir = new File(getImagesDirectory(process));
        DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
        DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");
        /* nur die _tif-Ordner anzeigen, die nicht mir orig_ anfangen */
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + DIRECTORY_SUFFIX) && !name.startsWith(DIRECTORY_PREFIX + "_"));
            }
        };

        String tifOrdner = "";
        String[] verzeichnisse = fileService.list(filterVerz, dir);

        if (verzeichnisse != null) {
            for (int i = 0; i < verzeichnisse.length; i++) {
                tifOrdner = verzeichnisse[i];
            }
        }

        if (tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                String[] folderList = fileService.list(dir);
                for (String folder : folderList) {
                    if (folder.endsWith(suffix)) {
                        tifOrdner = folder;
                        break;
                    }
                }
            }
        }

        if (!tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                File tif = new File(tifOrdner);
                String[] files = fileService.list(tif);
                if (files == null || files.length == 0) {
                    String[] folderList = fileService.list(dir);
                    for (String folder : folderList) {
                        if (folder.endsWith(suffix) && !folder.startsWith(DIRECTORY_PREFIX)) {
                            tifOrdner = folder;
                            break;
                        }
                    }
                }
            }
        }

        if (tifOrdner.equals("")) {
            tifOrdner = process.getTitle() + "_" + DIRECTORY_SUFFIX;
        }

        String result = getImagesDirectory(process);

        if (!result.endsWith(File.separator)) {
            result += File.separator;
        }
        if (!ConfigCore.getBooleanParameter("useOrigFolder", true)
                && ConfigCore.getBooleanParameter("createOrigFolderIfNotExists", false)) {
            fileService.createDirectory(URI.create(result), tifOrdner);
        }
        return result;
    }

    /**
     * Check if Tif directory exists.
     * 
     * @return true if the Tif-Image-Directory exists, false if not
     */
    public Boolean checkIfTifDirectoryExists(Process process) {
        File testMe;
        try {
            testMe = new File(getImagesTifDirectory(true, process));
        } catch (DAOException | IOException | InterruptedException | SwapException e) {
            return false;
        }
        return testMe.list() != null && testMe.exists() && fileService.list(testMe).length > 0;

    }

    /**
     * Get images origin directory.
     *
     * @param useFallBack
     * @param process
     * @return path
     */
    public String getImagesOrigDirectory(boolean useFallBack, Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        if (ConfigCore.getBooleanParameter("useOrigFolder", true)) {
            File dir = new File(getImagesDirectory(process));
            DIRECTORY_SUFFIX = ConfigCore.getParameter("DIRECTORY_SUFFIX", "tif");
            DIRECTORY_PREFIX = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");
            /* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
            FilenameFilter filterVerz = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith("_" + DIRECTORY_SUFFIX) && name.startsWith(DIRECTORY_PREFIX + "_"));
                }
            };

            String origOrdner = "";
            String[] verzeichnisse = fileService.list(filterVerz, dir);
            for (int i = 0; i < verzeichnisse.length; i++) {
                origOrdner = verzeichnisse[i];
            }

            if (origOrdner.equals("") && useFallBack) {
                String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    String[] folderList = fileService.list(dir);
                    for (String folder : folderList) {
                        if (folder.endsWith(suffix)) {
                            origOrdner = folder;
                            break;
                        }
                    }
                }
            }

            if (!origOrdner.equals("") && useFallBack) {
                String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    File tif = new File(origOrdner);
                    String[] files = fileService.list(tif);
                    if (files == null || files.length == 0) {
                        String[] folderList = fileService.list(dir);
                        for (String folder : folderList) {
                            if (folder.endsWith(suffix)) {
                                origOrdner = folder;
                                break;
                            }
                        }
                    }
                }
            }

            if (origOrdner.equals("")) {
                origOrdner = DIRECTORY_PREFIX + "_" + process.getTitle() + "_" + DIRECTORY_SUFFIX;
            }
            String rueckgabe = getImagesDirectory(process);
            if (ConfigCore.getBooleanParameter("createOrigFolderIfNotExists", false)
                    && process.getSortHelperStatus().equals("100000000")) {
                fileService.createDirectory(URI.create(rueckgabe), origOrdner);
            }
            return rueckgabe;
        } else {
            return getImagesTifDirectory(useFallBack, process);
        }
    }

    /**
     * Get images directory.
     *
     * @param process
     *            object
     * @return path
     */
    public String getImagesDirectory(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        String pfad = getProcessDataDirectory(process);
        fileService.createDirectory(URI.create(pfad), "images");
        return pfad;
    }

    /**
     * Get source directory.
     *
     * @param process
     *            object
     * @return path
     */
    public String getSourceDirectory(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        File dir = new File(getImagesDirectory(process));
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + "source"));
            }
        };
        File sourceFolder = null;
        String[] verzeichnisse = fileService.list(filterVerz, dir);
        if (verzeichnisse == null || verzeichnisse.length == 0) {
            sourceFolder = new File(dir, process.getTitle() + "_source");
            if (ConfigCore.getBooleanParameter("createSourceFolder", false)) {
                sourceFolder.mkdir();
            }
        } else {
            sourceFolder = new File(dir, verzeichnisse[0]);
        }

        return sourceFolder.getAbsolutePath();
    }

    /**
     * Get process data directory.
     *
     * @param process
     *            object
     * @return path
     */
    public String getProcessDataDirectory(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        String path = getProcessDataDirectoryIgnoreSwapping(process);

        if (process.isSwappedOutGui()) {
            ProcessSwapInTask pst = new ProcessSwapInTask();
            pst.initialize(process);
            pst.setProgress(1);
            pst.setShowMessages(true);
            pst.run();
            if (pst.getException() != null) {
                if (!new File(path, "images").exists() && !new File(path, "meta.xml").exists()) {
                    throw new SwapException(pst.getException().getMessage());
                } else {
                    process.setSwappedOutGui(false);
                }
                new ProcessDAO().save(process, this.getProgress(process));
            }
        }
        return path;
    }

    public String getOcrDirectory(Process process)
            throws SwapException, DAOException, IOException, InterruptedException {
        return getProcessDataDirectory(process) + "ocr" + File.separator;
    }

    public String getTxtDirectory(Process process)
            throws SwapException, DAOException, IOException, InterruptedException {
        return getOcrDirectory(process) + process.getTitle() + "_txt" + File.separator;
    }

    public String getWordDirectory(Process process)
            throws SwapException, DAOException, IOException, InterruptedException {
        return getOcrDirectory(process) + process.getTitle() + "_wc" + File.separator;
    }

    public String getPdfDirectory(Process process)
            throws SwapException, DAOException, IOException, InterruptedException {
        return getOcrDirectory(process) + process.getTitle() + "_pdf" + File.separator;
    }

    public String getAltoDirectory(Process process)
            throws SwapException, DAOException, IOException, InterruptedException {
        return getOcrDirectory(process) + process.getTitle() + "_alto" + File.separator;
    }

    public String getImportDirectory(Process process)
            throws SwapException, DAOException, IOException, InterruptedException {
        return getProcessDataDirectory(process) + "import" + File.separator;
    }

    /**
     * Get process data directory ignoring swapping.
     *
     * @param process
     *            object
     * @return path
     */
    public String getProcessDataDirectoryIgnoreSwapping(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        String pfad = ConfigCore.getKitodoDataDirectory() + process.getId() + File.separator;
        pfad = pfad.replaceAll(" ", "__");
        String processId = process.getId().toString();
        processId = processId.replaceAll(" ", "__");
        fileService.createDirectory(URI.create(pfad), processId);
        return pfad;
    }

    /**
     * The function getBatchID returns the batches the process is associated
     * with as readable text as read-only property "batchID".
     *
     * @return the batches the process is in
     */
    public String getBatchID(Process process) {
        if (process.getBatches() == null || process.getBatches().size() == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        BatchService batchService = new BatchService();
        for (Batch batch : process.getBatches()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(batchService.getLabel(batch));
        }
        return result.toString();
    }

    /**
     * Get size of tasks' list.
     *
     * @param process
     *            object
     * @return size
     */
    public int getTasksSize(Process process) {
        if (process.getTasks() == null) {
            return 0;
        } else {
            return process.getTasks().size();
        }
    }

    /**
     * Get size of histories' list.
     *
     * @param process
     *            object
     * @return size
     */
    public int getHistorySize(Process process) {
        if (process.getHistory() == null) {
            return 0;
        } else {
            return process.getHistory().size();
        }
    }

    /**
     * Get size of properties' list.
     *
     * @param process
     *            object
     * @return size
     */
    public int getPropertiesSize(Process process) {
        if (process.getProperties() == null) {
            return 0;
        } else {
            return process.getProperties().size();
        }
    }

    /**
     * Get size of workpieces' list.
     *
     * @param process
     *            object
     * @return size
     */
    public int getWorkpiecesSize(Process process) {
        if (process.getWorkpieces() == null) {
            return 0;
        } else {
            return process.getWorkpieces().size();
        }
    }

    /**
     * Get size of templates' list.
     *
     * @param process
     *            object
     * @return size
     */
    public int getTemplatesSize(Process process) {
        if (process.getTemplates() == null) {
            return 0;
        } else {
            return process.getTemplates().size();
        }
    }

    /**
     * Get current task.
     *
     * @param process
     *            object
     * @return current task
     */
    public Task getCurrentTask(Process process) {
        for (Task task : process.getTasks()) {
            if (task.getProcessingStatusEnum() == TaskStatus.OPEN
                    || task.getProcessingStatusEnum() == TaskStatus.INWORK) {
                return task;
            }
        }
        return null;
    }

    public String getCreationDateAsString(Process process) {
        return Helper.getDateAsFormattedString(process.getCreationDate());
    }

    private HashMap<String, Integer> calculationForProgress(Process process) {
        HashMap<String, Integer> results = new HashMap<>();
        int open = 0;
        int inProcessing = 0;
        int closed = 0;
        Hibernate.initialize(process.getTasks());
        for (Task task : process.getTasks()) {
            if (task.getProcessingStatusEnum() == TaskStatus.DONE) {
                closed++;
            } else if (task.getProcessingStatusEnum() == TaskStatus.LOCKED) {
                open++;
            } else {
                inProcessing++;
            }
        }

        results.put("open", open);
        results.put("inProcessing", inProcessing);
        results.put("closed", closed);

        if ((open + inProcessing + closed) == 0) {
            results.put("open", 1);
        }

        return results;
    }

    /**
     * Old getFortschritt().
     *
     * @param process
     *            object
     * @return string
     */
    public String getProgress(Process process) {
        HashMap<String, Integer> steps = calculationForProgress(process);
        double open = 0;
        double inProcessing = 0;
        double closed = 0;

        open = (steps.get("open") * 100)
                / (double) (steps.get("open") + steps.get("inProcessing") + steps.get("closed"));
        inProcessing = (steps.get("inProcessing") * 100)
                / (double) (steps.get("open") + steps.get("inProcessing") + steps.get("closed"));
        closed = 100 - open - inProcessing;

        java.text.DecimalFormat df = new java.text.DecimalFormat("#000");

        return df.format(closed) + df.format(inProcessing) + df.format(closed);
    }

    /**
     * Old getProcess().
     *
     * @param process
     *            object
     * @return return progress for open steps
     */
    public int getProgressOpen(Process process) {
        HashMap<String, Integer> steps = calculationForProgress(process);
        return (steps.get("open") * 100) / (steps.get("open") + steps.get("inProcessing") + steps.get("closed"));
    }

    /**
     * Old getProgressTwo().
     *
     * @param process
     *            object
     * @return progress for processed steps
     */
    public int getProgressInProcessing(Process process) {
        HashMap<String, Integer> steps = calculationForProgress(process);

        return (steps.get("inProcessing") * 100)
                / (steps.get("open") + steps.get("inProcessing") + steps.get("closed"));
    }

    /**
     * Old getProgressThree().
     *
     * @param process
     *            object
     * @return progress for closed steps
     */
    public int getProgressClosed(Process process) {
        HashMap<String, Integer> steps = calculationForProgress(process);

        double open = 0;
        double inProcessing = 0;
        double closed = 0;

        open = ((steps.get("open") * 100)
                / (double) (steps.get("open") + steps.get("inProcessing") + steps.get("closed")));
        inProcessing = (steps.get("inProcessing") * 100)
                / (double) (steps.get("open") + steps.get("inProcessing") + steps.get("closed"));
        closed = 100 - open - inProcessing;
        return (int) closed;
    }

    public String getMetadataFilePath(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        return getProcessDataDirectory(process) + "meta.xml";
    }

    public String getTemplateFilePath(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        return getProcessDataDirectory(process) + "template.xml";
    }

    public String getFulltextFilePath(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        return getProcessDataDirectory(process) + "fulltext.xml";
    }

    /**
     * Read metadata file.
     *
     * @param process
     *            object
     * @return filer format
     */
    public Fileformat readMetadataFile(Process process)
            throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException {
        if (!checkForMetadataFile(process)) {
            throw new IOException(Helper.getTranslation("metadataFileNotFound") + " " + getMetadataFilePath(process));
        }
        Hibernate.initialize(process.getRuleset());
        /* prüfen, welches Format die Metadaten haben (Mets, xstream oder rdf */
        String type = MetadatenHelper.getMetaFileType(getMetadataFilePath(process));
        if (logger.isDebugEnabled()) {
            logger.debug("current meta.xml file type for id " + process.getId() + ": " + type);
        }

        Fileformat ff = determineFileFormat(type, process);

        try {
            ff.read(getMetadataFilePath(process));
        } catch (ReadException e) {
            if (e.getMessage().startsWith("Parse error at line -1")) {
                Helper.setFehlerMeldung("metadataCorrupt");
            } else {
                throw e;
            }
        }
        return ff;
    }

    private Fileformat determineFileFormat(String type, Process process) throws PreferencesException {
        Fileformat fileFormat = null;
        RulesetService rulesetService = new RulesetService();

        switch (type) {
            case "metsmods":
                fileFormat = new MetsModsImportExport(rulesetService.getPreferences(process.getRuleset()));
                break;
            case "mets":
                fileFormat = new MetsMods(rulesetService.getPreferences(process.getRuleset()));
                break;
            case "xstream":
                fileFormat = new XStream(rulesetService.getPreferences(process.getRuleset()));
                break;
            default:
                fileFormat = new RDFFile(rulesetService.getPreferences(process.getRuleset()));
                break;
        }
        return fileFormat;
    }

    // backup of meta.xml
    private void createBackupFile(Process process)
            throws IOException, InterruptedException, SwapException, DAOException {
        int numberOfBackups = 0;

        if (ConfigCore.getIntParameter("numberOfMetaBackups") != 0) {
            numberOfBackups = ConfigCore.getIntParameter("numberOfMetaBackups");
        }

        if (numberOfBackups != 0) {
            BackupFileRotation bfr = new BackupFileRotation();
            bfr.setNumberOfBackups(numberOfBackups);
            bfr.setFormat("meta.*\\.xml");
            bfr.setProcessDataDirectory(getProcessDataDirectory(process));
            bfr.performBackup();
        } else {
            logger.warn("No backup configured for meta data files.");
        }
    }

    private boolean checkForMetadataFile(Process process)
            throws IOException, InterruptedException, SwapException, DAOException, PreferencesException {
        boolean result = true;
        File f = new File(getMetadataFilePath(process));
        if (!f.exists()) {
            result = false;
        }

        return result;
    }

    private String getTemporaryMetadataFileName(String fileName) {

        File temporaryFile = new File(fileName);
        String directoryPath = temporaryFile.getParentFile().getPath();
        String temporaryFileName = TEMPORARY_FILENAME_PREFIX + temporaryFile.getName();

        return directoryPath + File.separator + temporaryFileName;
    }

    private void removePrefixFromRelatedMetsAnchorFilesFor(String temporaryMetadataFilename) throws IOException {
        File temporaryFile = new File(temporaryMetadataFilename);
        File directoryPath = new File(temporaryFile.getParentFile().getPath());
        for (File temporaryAnchorFile : fileService.listFiles(directoryPath)) {
            String temporaryAnchorFileName = temporaryAnchorFile.toString();
            if (temporaryAnchorFile.isFile()
                    && FilenameUtils.getBaseName(temporaryAnchorFileName).startsWith(TEMPORARY_FILENAME_PREFIX)) {
                String anchorFileName = FilenameUtils.concat(FilenameUtils.getFullPath(temporaryAnchorFileName),
                        temporaryAnchorFileName.replace(TEMPORARY_FILENAME_PREFIX, ""));
                temporaryAnchorFileName = FilenameUtils.concat(FilenameUtils.getFullPath(temporaryAnchorFileName),
                        temporaryAnchorFileName);
                fileService.renameFile(temporaryAnchorFileName, anchorFileName);
            }
        }
    }

    /**
     * Write metadata file.
     *
     * @param gdzfile
     *            file format
     * @param process
     *            object
     */
    public void writeMetadataFile(Fileformat gdzfile, Process process) throws IOException, InterruptedException,
            SwapException, DAOException, WriteException, PreferencesException {
        RulesetService rulesetService = new RulesetService();
        boolean backupCondition;
        boolean writeResult;
        File temporaryMetadataFile;
        Fileformat ff;
        String metadataFileName;
        String temporaryMetadataFileName;

        Hibernate.initialize(process.getRuleset());
        switch (MetadataFormat.findFileFormatsHelperByName(process.getProject().getFileFormatInternal())) {
            case METS:
                ff = new MetsMods(rulesetService.getPreferences(process.getRuleset()));
                break;
            case RDF:
                ff = new RDFFile(rulesetService.getPreferences(process.getRuleset()));
                break;
            default:
                ff = new XStream(rulesetService.getPreferences(process.getRuleset()));
                break;
        }
        // createBackupFile();
        metadataFileName = getMetadataFilePath(process);
        temporaryMetadataFileName = getTemporaryMetadataFileName(metadataFileName);

        ff.setDigitalDocument(gdzfile.getDigitalDocument());
        // ff.write(getMetadataFilePath());
        writeResult = ff.write(temporaryMetadataFileName);
        temporaryMetadataFile = new File(temporaryMetadataFileName);
        backupCondition = writeResult && temporaryMetadataFile.exists() && (temporaryMetadataFile.length() > 0);
        if (backupCondition) {
            createBackupFile(process);
            fileService.renameFile(temporaryMetadataFileName, metadataFileName);
            removePrefixFromRelatedMetsAnchorFilesFor(temporaryMetadataFileName);
        }
    }

    public void writeMetadataAsTemplateFile(Fileformat inFile, Process process) throws IOException,
            InterruptedException, SwapException, DAOException, WriteException, PreferencesException {
        inFile.write(getTemplateFilePath(process));
    }

    /**
     * Read metadata as template file.
     *
     * @param process
     *            object
     * @return file format
     */
    public Fileformat readMetadataAsTemplateFile(Process process)
            throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException {
        RulesetService rulesetService = new RulesetService();
        Hibernate.initialize(process.getRuleset());
        if (new File(getTemplateFilePath(process)).exists()) {
            Fileformat ff = null;
            String type = MetadatenHelper.getMetaFileType(getTemplateFilePath(process));
            if (logger.isDebugEnabled()) {
                logger.debug("current template.xml file type: " + type);
            }
            ff = determineFileFormat(type, process);
            /*
             * if (type.equals("mets")) { ff = new
             * MetsMods(rulesetService.getPreferences(process.getRuleset())); }
             * else if (type.equals("xstream")) { ff = new
             * XStream(rulesetService.getPreferences(process.getRuleset())); }
             * else { ff = new
             * RDFFile(rulesetService.getPreferences(process.getRuleset())); }
             */
            ff.read(getTemplateFilePath(process));
            return ff;
        } else {
            throw new IOException("File does not exist: " + getTemplateFilePath(process));
        }
    }

    /**
     * Check whether the operation contains steps that are not assigned to a
     * user or user group.
     */
    public boolean getContainsUnreachableSteps(Process process) {
        TaskService taskService = new TaskService();
        if (process.getTasks().size() == 0) {
            return true;
        }
        for (Task task : process.getTasks()) {
            if (taskService.getUserGroupsSize(task) == 0 && taskService.getUsersSize(task) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there is one task in edit mode, where the user has the rights to
     * write to image folder.
     */
    public boolean isImageFolderInUse(Process process) {
        for (Task task : process.getTasks()) {
            if (task.getProcessingStatusEnum() == TaskStatus.INWORK && task.isTypeImagesWrite()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get user of task in edit mode with rights to write to image folder.
     */
    public User getImageFolderInUseUser(Process process) {
        for (Task task : process.getTasks()) {
            if (task.getProcessingStatusEnum() == TaskStatus.INWORK && task.isTypeImagesWrite()) {
                return task.getProcessingUser();
            }
        }
        return null;
    }

    /**
     * Download docket.
     *
     * @param process
     *            object
     * @return empty string?
     */
    public String downloadDocket(Process process) {

        if (logger.isDebugEnabled()) {
            logger.debug("generate docket for process " + process.getId());
        }
        String rootPath = ConfigCore.getParameter("xsltFolder");
        File xsltFile = new File(rootPath, "docket.xsl");
        if (process.getDocket() != null) {
            xsltFile = new File(rootPath, process.getDocket().getFile());
            if (!xsltFile.exists()) {
                Helper.setFehlerMeldung("docketMissing");
                return "";
            }
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            String fileName = process.getTitle() + ".pdf";
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

            // write run note to servlet output stream
            try {
                ServletOutputStream out = response.getOutputStream();
                ExportDocket ern = new ExportDocket();
                ern.startExport(process, out, xsltFile.getAbsolutePath());
                out.flush();
                facesContext.responseComplete();
            } catch (Exception e) {
                Helper.setFehlerMeldung("Exception while exporting run note.", e.getMessage());
                response.reset();
            }

        }
        return "";
    }

    /**
     * Get first open task for the process.
     *
     * @param process
     *            object
     * @return first open task
     */
    public Task getFirstOpenStep(Process process) {

        for (Task step : process.getTasks()) {
            if (step.getProcessingStatusEnum().equals(TaskStatus.OPEN)
                    || step.getProcessingStatusEnum().equals(TaskStatus.INWORK)) {
                return step;
            }
        }
        return null;
    }

    /**
     * Get method from name.
     *
     * @param methodName
     *            string
     * @param process
     *            object
     * @return method from name
     */
    public String getMethodFromName(String methodName, Process process) {
        java.lang.reflect.Method method;
        try {
            method = this.getClass().getMethod(methodName);
            Object o = method.invoke(this);
            return (String) o;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            logger.debug("exception: " + e);
        }
        try {
            String folder = this.getImagesTifDirectory(false, process);
            folder = folder.substring(0, folder.lastIndexOf("_"));
            folder = folder + "_" + methodName;
            if (new File(folder).exists()) {
                return folder;
            }
        } catch (DAOException | InterruptedException | IOException | SwapException ex) {
            logger.debug("exception: " + ex);
        }
        return null;
    }

    /*
     * public List<String> getPossibleDigitalCollections(Process process) throws
     * JDOMException, IOException { return
     * DigitalCollections.possibleDigitalCollectionsForProcess(process); }
     */

    /**
     * The addMessageToWikiField() method is a helper method which composes the
     * new wiki field using a StringBuilder. The message is encoded using HTML
     * entities to prevent certain characters from playing merry havoc when the
     * message box shall be rendered in a browser later.
     *
     * @param message
     *            the message to append
     */
    public Process addToWikiField(String message, Process process) {
        StringBuilder composer = new StringBuilder();
        if (process.getWikiField() != null && process.getWikiField().length() > 0) {
            composer.append(process.getWikiField());
            composer.append("\r\n");
        }
        composer.append("<p>");
        composer.append(StringEscapeUtils.escapeHtml(message));
        composer.append("</p>");
        process.setWikiField(composer.toString());

        return process;
    }

    /**
     * The method addToWikiField() adds a message with a given level to the wiki
     * field of the process. Four level strings will be recognized and result in
     * different colors:
     *
     * <dl>
     * <dt><code>debug</code></dt>
     * <dd>gray</dd>
     * <dt><code>error</code></dt>
     * <dd>red</dd>
     * <dt><code>user</code></dt>
     * <dd>green</dd>
     * <dt><code>warn</code></dt>
     * <dd>orange</dd>
     * <dt><i>any other value</i></dt>
     * <dd>blue</dd>
     * <dt>
     *
     * @param level
     *            message colour, one of: "debug", "error", "info", "user" or
     *            "warn"; any other value defaults to "info"
     * @param message
     *            text
     */
    public String addToWikiField(String level, String message, Process process) {
        return WikiFieldHelper.getWikiMessage(process, process.getWikiField(), level, message);
    }

    /**
     * The method addToWikiField() adds a message signed by the given user to
     * the wiki field of the process.
     *
     * @param user
     *            to sign the message with
     * @param message
     *            to print
     */
    public void addToWikiField(User user, String message, Process process) {
        String text = message + " (" + user.getSurname() + ")";
        // addToWikiField("user", process, text);
    }

    /**
     * The method createProcessDirs() starts creation of directories configured
     * by parameter processDirs within kitodo_config.properties
     */
    public void createProcessDirs(Process process)
            throws SwapException, DAOException, IOException, InterruptedException {

        String[] processDirs = ConfigCore.getStringArrayParameter("processDirs");

        for (String processDir : processDirs) {
            fileService.createDirectory(URI.create(this.getProcessDataDirectory(process)),
                    processDir.replace("(processtitle)", process.getTitle()));
        }

    }

    /**
     * The function getDigitalDocument() returns the digital act of this
     * process.
     *
     * @return the digital act of this process
     * @throws PreferencesException
     *             if the no node corresponding to the file format is available
     *             in the rule set configured
     * @throws ReadException
     *             if the meta data file cannot be read
     * @throws SwapException
     *             if an error occurs while the process is swapped back in
     * @throws DAOException
     *             if an error occurs while saving the fact that the process has
     *             been swapped back in to the database
     * @throws IOException
     *             if creating the process directory or reading the meta data
     *             file fails
     * @throws InterruptedException
     *             if the current thread is interrupted by another thread while
     *             it is waiting for the shell script to create the directory to
     *             finish
     */
    public DigitalDocument getDigitalDocument(Process process)
            throws PreferencesException, ReadException, SwapException, DAOException, IOException, InterruptedException {
        return readMetadataFile(process).getDigitalDocument();
    }

    /**
     * Filter for correction / solution messages.
     *
     * @param lpe
     *            List of process properties
     * @return List of filtered correction / solution messages
     */
    protected List<Property> filterForCorrectionSolutionMessages(List<Property> lpe) {
        ArrayList<Property> filteredList = new ArrayList<>();
        List<String> listOfTranslations = new ArrayList<String>();
        String propertyTitle = "";

        listOfTranslations.add("Korrektur notwendig");
        listOfTranslations.add("Korrektur durchgefuehrt");
        listOfTranslations.add(Helper.getTranslation("Korrektur notwendig"));
        listOfTranslations.add(Helper.getTranslation("Korrektur durchgefuehrt"));

        if ((lpe == null) || (lpe.size() == 0)) {
            return filteredList;
        }

        // filtering for correction and solution messages
        for (Property property : lpe) {
            propertyTitle = property.getTitle();
            if (listOfTranslations.contains(propertyTitle)) {
                filteredList.add(property);
            }
        }
        return filteredList;
    }

    /**
     * Filter and sort after creation date list of process properties for
     * correction and solution messages.
     *
     * @return list of ProcessProperty objects
     */
    public List<Property> getSortedCorrectionSolutionMessages(Process process) {
        List<Property> filteredList;
        List<Property> lpe = process.getProperties();

        if (lpe.isEmpty()) {
            return new ArrayList<>();
        }

        filteredList = filterForCorrectionSolutionMessages(lpe);

        // sorting after creation date
        Collections.sort(filteredList, new Comparator<Property>() {
            @Override
            public int compare(Property o1, Property o2) {
                Date o1Date = o1.getCreationDate();
                Date o2Date = o2.getCreationDate();
                if (o1Date == null) {
                    o1Date = new Date();
                }
                if (o2Date == null) {
                    o2Date = new Date();
                }
                return o1Date.compareTo(o2Date);
            }
        });

        return new ArrayList<>(filteredList);
    }
}
