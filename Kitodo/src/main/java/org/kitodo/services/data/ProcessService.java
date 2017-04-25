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
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.apache.log4j.Logger;
import org.goobi.io.BackupFileRotation;
import org.goobi.io.SafeFile;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.export.ExportDocket;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.ProcessProperty;
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
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.TitleSearchService;

import ugh.dl.DigitalDocument;
import ugh.dl.Fileformat;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;
import ugh.fileformats.mets.XStream;

public class ProcessService extends TitleSearchService {

    private static final Logger myLogger = Logger.getLogger(ProcessService.class);

    private Boolean selected = false;

    private final MetadatenSperrung msp = new MetadatenSperrung();

    Helper help = new Helper();

    public static String DIRECTORY_PREFIX = "orig";
    public static String DIRECTORY_SUFFIX = "images";

    private static final String TEMPORARY_FILENAME_PREFIX = "temporary_";

    private ProcessDAO processDao = new ProcessDAO();
    private ProcessType processType = new ProcessType();
    private Indexer<Process, ProcessType> indexer = new Indexer<>(Process.class);
    private UserService userService = new UserService();

    /**
     * Constructor with searcher's assigning.
     */
    public ProcessService() {
        super(new Searcher(Process.class));
    }

    public Process find(Integer id) throws DAOException {
        return processDao.find(id);
    }

    public List<Process> findAll() throws DAOException {
        return processDao.findAll();
    }

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param process
     *            object
     */
    public void save(Process process) throws CustomResponseException, DAOException, IOException {
        processDao.save(process, getProgress(process));
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(process, processType);
    }

    public void saveList(List<Process> list) throws DAOException {
        processDao.saveList(list);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param process
     *            object
     */
    public void remove(Process process) throws CustomResponseException, DAOException, IOException {
        processDao.remove(process);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(process, processType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws CustomResponseException, DAOException, IOException {
        processDao.remove(id);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(id);
    }

    public List<Process> search(String query) throws DAOException {
        return processDao.search(query);
    }

    public Long count(String query) throws DAOException {
        return processDao.count(query);
    }

    public void refresh(Process process) {
        processDao.refresh(process);
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
            myLogger.debug("Hibernate exception: ", e);
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
    public List<ProcessProperty> getPropertiesInitialized(Process process) {
        try {
            Hibernate.initialize(process.getProperties());
        } catch (HibernateException e) {
            myLogger.debug("Hibernate exception: ", e);
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
                result = userService.find(Integer.valueOf(userID));
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
        SafeFile dir = new SafeFile(getImagesDirectory(process));
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
        String[] verzeichnisse = dir.list(filterVerz);

        if (verzeichnisse != null) {
            for (int i = 0; i < verzeichnisse.length; i++) {
                tifOrdner = verzeichnisse[i];
            }
        }

        if (tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                String[] folderList = dir.list();
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
                SafeFile tif = new SafeFile(tifOrdner);
                String[] files = tif.list();
                if (files == null || files.length == 0) {
                    String[] folderList = dir.list();
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

        String result = getImagesDirectory(process) + tifOrdner;

        if (!result.endsWith(File.separator)) {
            result += File.separator;
        }
        if (!ConfigCore.getBooleanParameter("useOrigFolder", true)
                && ConfigCore.getBooleanParameter("createOrigFolderIfNotExists", false)) {
            FilesystemHelper.createDirectory(result);
        }
        return result;
    }

    /**
     * Check if Tif directory exists.
     * 
     * @return true if the Tif-Image-Directory exists, false if not
     */
    public Boolean checkIfTifDirectoryExists(Process process) {
        SafeFile testMe;
        try {
            testMe = new SafeFile(getImagesTifDirectory(true, process));
        } catch (DAOException | IOException | InterruptedException | SwapException e) {
            return false;
        }
        if (testMe.list() == null) {
            return false;
        }

        return testMe.exists() && testMe.list().length > 0;
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
            SafeFile dir = new SafeFile(getImagesDirectory(process));
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
            String[] verzeichnisse = dir.list(filterVerz);
            for (int i = 0; i < verzeichnisse.length; i++) {
                origOrdner = verzeichnisse[i];
            }

            if (origOrdner.equals("") && useFallBack) {
                String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    String[] folderList = dir.list();
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
                    SafeFile tif = new SafeFile(origOrdner);
                    String[] files = tif.list();
                    if (files == null || files.length == 0) {
                        String[] folderList = dir.list();
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
            String rueckgabe = getImagesDirectory(process) + origOrdner + File.separator;
            if (ConfigCore.getBooleanParameter("createOrigFolderIfNotExists", false)
                    && process.getSortHelperStatus().equals("100000000")) {
                FilesystemHelper.createDirectory(rueckgabe);
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
        String pfad = getProcessDataDirectory(process) + "images" + File.separator;
        FilesystemHelper.createDirectory(pfad);
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
        SafeFile dir = new SafeFile(getImagesDirectory(process));
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + "source"));
            }
        };
        SafeFile sourceFolder = null;
        String[] verzeichnisse = dir.list(filterVerz);
        if (verzeichnisse == null || verzeichnisse.length == 0) {
            sourceFolder = new SafeFile(dir, process.getTitle() + "_source");
            if (ConfigCore.getBooleanParameter("createSourceFolder", false)) {
                sourceFolder.mkdir();
            }
        } else {
            sourceFolder = new SafeFile(dir, verzeichnisse[0]);
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
                if (!new SafeFile(path, "images").exists() && !new SafeFile(path, "meta.xml").exists()) {
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
        String pfad = this.help.getKitodoDataDirectory() + process.getId() + File.separator;
        pfad = pfad.replaceAll(" ", "__");
        FilesystemHelper.createDirectory(pfad);
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
        if (myLogger.isDebugEnabled()) {
            myLogger.debug("current meta.xml file type for id " + process.getId() + ": " + type);
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
            myLogger.warn("No backup configured for meta data files.");
        }
    }

    private boolean checkForMetadataFile(Process process)
            throws IOException, InterruptedException, SwapException, DAOException, PreferencesException {
        boolean result = true;
        SafeFile f = new SafeFile(getMetadataFilePath(process));
        if (!f.exists()) {
            result = false;
        }

        return result;
    }

    private String getTemporaryMetadataFileName(String fileName) {

        SafeFile temporaryFile = new SafeFile(fileName);
        String directoryPath = temporaryFile.getParentFile().getPath();
        String temporaryFileName = TEMPORARY_FILENAME_PREFIX + temporaryFile.getName();

        return directoryPath + File.separator + temporaryFileName;
    }

    private void removePrefixFromRelatedMetsAnchorFilesFor(String temporaryMetadataFilename) throws IOException {
        SafeFile temporaryFile = new SafeFile(temporaryMetadataFilename);
        SafeFile directoryPath = new SafeFile(temporaryFile.getParentFile().getPath());
        for (SafeFile temporaryAnchorFile : directoryPath.listFiles()) {
            String temporaryAnchorFileName = temporaryAnchorFile.toString();
            if (temporaryAnchorFile.isFile()
                    && FilenameUtils.getBaseName(temporaryAnchorFileName).startsWith(TEMPORARY_FILENAME_PREFIX)) {
                String anchorFileName = FilenameUtils.concat(FilenameUtils.getFullPath(temporaryAnchorFileName),
                        temporaryAnchorFileName.replace(TEMPORARY_FILENAME_PREFIX, ""));
                temporaryAnchorFileName = FilenameUtils.concat(FilenameUtils.getFullPath(temporaryAnchorFileName),
                        temporaryAnchorFileName);
                FilesystemHelper.renameFile(temporaryAnchorFileName, anchorFileName);
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
        SafeFile temporaryMetadataFile;
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
        temporaryMetadataFile = new SafeFile(temporaryMetadataFileName);
        backupCondition = writeResult && temporaryMetadataFile.exists() && (temporaryMetadataFile.length() > 0);
        if (backupCondition) {
            createBackupFile(process);
            FilesystemHelper.renameFile(temporaryMetadataFileName, metadataFileName);
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
        if (new SafeFile(getTemplateFilePath(process)).exists()) {
            Fileformat ff = null;
            String type = MetadatenHelper.getMetaFileType(getTemplateFilePath(process));
            if (myLogger.isDebugEnabled()) {
                myLogger.debug("current template.xml file type: " + type);
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

        if (myLogger.isDebugEnabled()) {
            myLogger.debug("generate docket for process " + process.getId());
        }
        String rootPath = ConfigCore.getParameter("xsltFolder");
        SafeFile xsltFile = new SafeFile(rootPath, "docket.xsl");
        if (process.getDocket() != null) {
            xsltFile = new SafeFile(rootPath, process.getDocket().getFile());
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
            myLogger.debug("exception: " + e);
        }
        try {
            String folder = this.getImagesTifDirectory(false, process);
            folder = folder.substring(0, folder.lastIndexOf("_"));
            folder = folder + "_" + methodName;
            if (new SafeFile(folder).exists()) {
                return folder;
            }
        } catch (DAOException | InterruptedException | IOException | SwapException ex) {
            myLogger.debug("exception: " + ex);
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
            FilesystemHelper.createDirectory(FilenameUtils.concat(this.getProcessDataDirectory(process),
                    processDir.replace("(processtitle)", process.getTitle())));
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
    protected List<ProcessProperty> filterForCorrectionSolutionMessages(List<ProcessProperty> lpe) {
        ArrayList<ProcessProperty> filteredList = new ArrayList<ProcessProperty>();
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
        for (ProcessProperty pe : lpe) {
            propertyTitle = pe.getTitle();
            if (listOfTranslations.contains(propertyTitle)) {
                filteredList.add(pe);
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
    public List<ProcessProperty> getSortedCorrectionSolutionMessages(Process process) {
        List<ProcessProperty> filteredList;
        List<ProcessProperty> lpe = process.getProperties();

        if (lpe.isEmpty()) {
            return new ArrayList<>();
        }

        filteredList = filterForCorrectionSolutionMessages(lpe);

        // sorting after creation date
        Collections.sort(filteredList, new Comparator<ProcessProperty>() {
            @Override
            public int compare(ProcessProperty o1, ProcessProperty o2) {
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
