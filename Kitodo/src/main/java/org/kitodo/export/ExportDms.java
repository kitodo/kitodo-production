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

package org.kitodo.export;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.MetadataFormat;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.helper.tasks.ExportDmsTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.helper.tasks.TaskSitter;
import org.kitodo.production.metadata.copier.CopierData;
import org.kitodo.production.metadata.copier.DataCopier;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class ExportDms extends ExportMets {
    private static final Logger logger = LogManager.getLogger(ExportDms.class);
    private String atsPpnBand;
    private boolean exportWithImages = true;
    private final FileService fileService = ServiceManager.getFileService();
    private static final String EXPORT_DIR_DELETE = "errorDirectoryDeleting";
    private static final String ERROR_EXPORT = "errorExport";

    /**
     * The field exportDmsTask holds an optional task instance. Its progress and
     * its errors will be passed to the task manager screen (if available) for
     * visualisation.
     */
    private EmptyTask exportDmsTask = null;

    public ExportDms() {
    }

    public ExportDms(boolean exportImages) {
        this.exportWithImages = exportImages;
    }

    /**
     * DMS-Export an eine gewünschte Stelle.
     *
     * @param process
     *            object
     * @param destination
     *            String
     */
    @Override
    public boolean startExport(Process process, URI destination) {
        if (process.getProject().isUseDmsImport()
                && ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.ASYNCHRONOUS_AUTOMATIC_EXPORT)) {
            TaskManager.addTask(new ExportDmsTask(this, process, destination));
            Helper.setMessage(TaskSitter.isAutoRunningThreads() ? "DMSExportByThread" : "DMSExportThreadCreated",
                process.getTitle());
            return true;
        } else {
            return startExport(process, destination, (ExportDmsTask) null);
        }
    }

    /**
     * The function startExport() performs a DMS export to a desired place. In
     * addition, it accepts an optional ExportDmsTask object. If that is passed
     * in, the progress in it will be updated during processing and occurring
     * errors will be passed to it to be visible in the task manager screen.
     *
     * @param process
     *            process to export
     * @param destination
     *            work directory of the user who triggered the export
     * @param exportDmsTask
     *            ExportDmsTask object to submit progress updates and errors
     * @return false if an error condition was caught, true otherwise
     */
    public boolean startExport(Process process, URI destination, ExportDmsTask exportDmsTask) {
        this.exportDmsTask = exportDmsTask;
        try {
            return startExport(process, destination,
                ServiceManager.getProcessService().readMetadataFile(process).getDigitalDocument());
        } catch (IOException | DAOException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
                logger.error(Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(process.getTitle())), e);
            } else {
                Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            }
            return false;
        }
    }

    /**
     * Start export.
     *
     * @param process
     *            object
     * @param destination
     *            String
     * @param newFile
     *            DigitalDocument
     * @return boolean
     */
    public boolean startExport(Process process, URI destination, LegacyMetsModsDigitalDocumentHelper newFile)
            throws IOException, DAOException {

        this.myPrefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        this.atsPpnBand = Helper.getNormalizedTitle(process.getTitle());

        LegacyMetsModsDigitalDocumentHelper gdzfile = readDocument(process, newFile);
        if (Objects.isNull(gdzfile)) {
            return false;
        }

        boolean dataCopierResult = executeDataCopierProcess(gdzfile, process);
        if (!dataCopierResult) {
            return false;
        }

        trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());

        // validate metadata
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.USE_META_DATA_VALIDATION)
                && !ServiceManager.getMetadataValidationService().validate(gdzfile, this.myPrefs, process)) {
            return false;
        }

        return prepareAndDownloadSaveLocation(process, destination, gdzfile);
    }

    private boolean prepareAndDownloadSaveLocation(Process process, URI destinationDirectory,
            LegacyMetsModsDigitalDocumentHelper gdzfile) throws IOException, DAOException {
        // TODO: why create again destinationDirectory if it is already given as
        // an
        // input??
        URI destination;
        URI userHome;
        if (process.getProject().isUseDmsImport()) {
            // TODO: I have got here value usr/local/kitodo/hotfolder
            destination = new File(process.getProject().getDmsImportRootPath()).toURI();
            userHome = destination;

            // if necessary, create process folder
            if (process.getProject().isDmsImportCreateProcessFolder()) {
                URI userHomeProcess = fileService.createResource(userHome,
                    File.separator + Helper.getNormalizedTitle(process.getTitle()));
                destination = userHomeProcess;
                boolean createProcessFolderResult = createProcessFolder(userHomeProcess, userHome, process.getProject(),
                    process.getTitle());
                if (!createProcessFolderResult) {
                    return false;
                }
            }
        } else {
            destination = URI.create(destinationDirectory + atsPpnBand + "/");
            // if the home exists, first delete and then create again
            userHome = destination;
            if (!fileService.delete(userHome)) {
                Helper.setErrorMessage(
                    Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(process.getTitle())),
                    Helper.getTranslation(EXPORT_DIR_DELETE, Collections.singletonList("Home")));
                return false;
            }
            prepareUserDirectory(destination);
        }
        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setProgress(1);
        }

        return exportImagesAndMetsToDestinationUri(process, gdzfile, destination, userHome);
    }

    private boolean exportImagesAndMetsToDestinationUri(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile,
            URI destination, URI userHome) throws IOException, DAOException {

        if (exportWithImages) {
            try {
                directoryDownload(process, destination);
            } catch (IOException | InterruptedException | RuntimeException e) {
                if (Objects.nonNull(exportDmsTask)) {
                    exportDmsTask.setException(e);
                } else {
                    Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
                }
                return false;
            }
        }

        /*
         * export the file to the desired location, either directly into the import
         * folder or into the user's home, then start the import thread
         */
        if (process.getProject().isUseDmsImport()) {
            asyncExportWithImport(process, gdzfile, userHome);
        } else {
            exportWithoutImport(process, gdzfile, userHome);
        }
        return true;
    }

    private boolean executeDataCopierProcess(LegacyMetsModsDigitalDocumentHelper gdzfile, Process process) {
        try {
            String rules = ConfigCore.getParameter(ParameterCore.COPY_DATA_ON_EXPORT);
            if (Objects.nonNull(rules) && !executeDataCopierProcess(gdzfile, process, rules)) {
                return false;
            }
        } catch (NoSuchElementException e) {
            logger.catching(Level.TRACE, e);
            // no configuration simply means here is nothing to do
        }
        return true;
    }

    private boolean executeDataCopierProcess(LegacyMetsModsDigitalDocumentHelper gdzfile, Process process,
            String rules) {
        try {
            new DataCopier(rules).process(new CopierData(gdzfile, process));
        } catch (ConfigurationException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
            } else {
                Helper.setErrorMessage("dataCopier.syntaxError", e.getMessage(), logger, e);
                return false;
            }
        }
        return true;
    }

    private boolean createProcessFolder(URI userHomeProcess, URI userHome, Project project, String processTitle)
            throws IOException {
        String normalizedTitle = Helper.getNormalizedTitle(processTitle);

        // delete old import folder
        if (!fileService.delete(userHomeProcess)) {
            Helper.setErrorMessage(Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(processTitle)),
                Helper.getTranslation(EXPORT_DIR_DELETE, Collections.singletonList("Import")));
            return false;
        }
       
        if (!fileService.fileExist(userHomeProcess)) {
            fileService.createDirectory(userHome, normalizedTitle);
        }

        return true;
    }

    private LegacyMetsModsDigitalDocumentHelper readDocument(Process process, LegacyMetsModsDigitalDocumentHelper newFile) {
        LegacyMetsModsDigitalDocumentHelper gdzfile;
        try {
            switch (MetadataFormat.findFileFormatsHelperByName(process.getProject().getFileFormatDmsExport())) {
                case METS:
                    gdzfile = new LegacyMetsModsDigitalDocumentHelper(this.myPrefs.getRuleset());
                    break;
                case METS_AND_RDF:
                default:
                    throw new UnsupportedOperationException("Dead code pending removal");
            }

            gdzfile.setDigitalDocument(newFile);
            return gdzfile;
        } catch (RuntimeException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
                logger.error(Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(process.getTitle())), e);
            } else {
                Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            }
            return null;
        }
    }

    private void asyncExportWithImport(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile, URI userHome)
            throws IOException, DAOException {
        String fileFormat = process.getProject().getFileFormatDmsExport();

        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setWorkDetail(atsPpnBand + ".xml");
        }
        if (MetadataFormat.findFileFormatsHelperByName(fileFormat) == MetadataFormat.METS) {
            // if METS, then write by writeMetsFile...
            writeMetsFile(process, fileService.createResource(userHome, File.separator + atsPpnBand + ".xml"), gdzfile);
        } else {
            // ...if not, just write a fileformat
            gdzfile.write(userHome + File.separator + atsPpnBand + ".xml");
        }

        // if necessary, METS and RDF should be written in the export
        if (MetadataFormat.findFileFormatsHelperByName(fileFormat) == MetadataFormat.METS_AND_RDF) {
            writeMetsFile(process, fileService.createResource(userHome, File.separator + atsPpnBand + ".mets.xml"),
                gdzfile);
        }

        Helper.setMessage(process.getTitle() + ": ", "DMS-Export started");
        if (!ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_WITHOUT_TIME_LIMIT)) {
            exportWithTimeLimit(process);
        }
        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setProgress(100);
        }
    }

    private void exportWithTimeLimit(Process process) throws IOException {
        DmsImportThread asyncThread = new DmsImportThread(process, atsPpnBand);
        asyncThread.start();
        String processTitle = process.getTitle();

        try {
            // wait 30 seconds for the thread, possibly kill
            asyncThread.join(process.getProject().getDmsImportTimeOut().longValue());
            if (asyncThread.isAlive()) {
                asyncThread.stopThread();
            }
        } catch (InterruptedException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
                logger.error(Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(processTitle)));
            } else {
                Thread.currentThread().interrupt();
                Helper.setErrorMessage(ERROR_EXPORT, new Object[] {processTitle }, logger, e);
            }
        }

        String result = asyncThread.getResult();
        if (!result.isEmpty()) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(new RuntimeException(processTitle + ": " + result));
            } else {
                Helper.setErrorMessage(processTitle + ": ", result);
            }
        } else {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setProgress(100);
            } else {
                Helper.setMessage(process.getTitle() + ": ", "exportFinished");
            }
        }
    }

    private void exportWithoutImport(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile, URI destinationDirectory)
            throws IOException, DAOException {
        if (MetadataFormat
                .findFileFormatsHelperByName(process.getProject().getFileFormatDmsExport()) == MetadataFormat.METS) {
            writeMetsFile(process, fileService.createResource(destinationDirectory, atsPpnBand + ".xml"), gdzfile);
        } else {
            gdzfile.write(destinationDirectory + atsPpnBand + ".xml");
        }

        Helper.setMessage(process.getTitle() + ": ", "exportFinished");
    }

    /**
     * Get exportDmsTask.
     *
     * @return value of exportDmsTask
     */
    public EmptyTask getExportDmsTask() {
        return exportDmsTask;
    }

    /**
     * Setter method to pass in a task thread to whom progress and error messages
     * shall be reported.
     *
     * @param task
     *            task implementation
     */
    public void setExportDmsTask(EmptyTask task) {
        this.exportDmsTask = task;
    }

    /**
     * Run through all metadata and children of given docstruct to trim the strings
     * calls itself recursively.
     */
    private void trimAllMetadata(LegacyDocStructHelperInterface inStruct) {
        // trim all metadata values
        if (Objects.nonNull(inStruct.getAllMetadata())) {
            for (LegacyMetadataHelper md : inStruct.getAllMetadata()) {
                if (Objects.nonNull(md.getValue())) {
                    md.setStringValue(md.getValue().trim());
                }
            }
        }

        // run through all children of docstruct
        if (Objects.nonNull(inStruct.getAllChildren())) {
            for (LegacyDocStructHelperInterface child : inStruct.getAllChildren()) {
                trimAllMetadata(child);
            }
        }
    }

    /**
     * Download image.
     *
     * @param process
     *            object
     * @param userHome
     *            File
     * @param atsPpnBand
     *            String
     * @param ordnerEndung
     *            String
     */
    public void imageDownload(Process process, URI userHome, String atsPpnBand, final String ordnerEndung)
            throws IOException {
        // determine the source folder
        URI tifOrdner = ServiceManager.getProcessService().getImagesTifDirectory(true, process.getId(),
            process.getTitle(), process.getProcessBaseUri());

        // copy the source folder to the destination folder
        if (fileService.fileExist(tifOrdner) && !fileService.getSubUris(tifOrdner).isEmpty()) {
            URI zielTif = userHome.resolve(atsPpnBand + ordnerEndung + "/");

            /* bei Agora-Import einfach den Ordner anlegen */
            if (process.getProject().isUseDmsImport()) {
                if (!fileService.fileExist(zielTif)) {
                    fileService.createDirectory(userHome, atsPpnBand + ordnerEndung);
                }
            } else {
                // if no async import, then create the folder with user
                // authorization again
                User user = ServiceManager.getUserService().getAuthenticatedUser();
                try {
                    fileService.createDirectoryForUser(zielTif, user.getLogin());
                } catch (IOException e) {
                    handleException(e, process.getTitle());
                    throw e;
                } catch (RuntimeException e) {
                    handleException(e, process.getTitle());
                }
            }

            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setWorkDetail(null);
            }
        }
    }

    private void handleException(Exception e, String processTitle) {
        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setException(e);
            logger.error("Could not create destination directory", e);
        } else {
            Helper.setErrorMessage(ERROR_EXPORT, new Object[] {processTitle }, logger, e);
        }
    }

    /**
     * Starts copying all directories configured as export folder.
     *
     * @param process
     *            object
     * @param destination
     *            the destination directory
     * @throws InterruptedException
     *             if the user clicked stop on the thread running the export DMS
     *             task
     *
     */
    private void directoryDownload(Process process, URI destination) throws IOException, InterruptedException {
        Collection<Subfolder> processDirs = process.getProject().getFolders().parallelStream()
                .filter(Folder::isCopyFolder).map(folder -> new Subfolder(process, folder))
                .collect(Collectors.toList());
        VariableReplacer variableReplacer = new VariableReplacer(null, null, process, null);

        for (Subfolder processDir : processDirs) {
            URI dstDir = destination.resolve(variableReplacer.replace(processDir.getFolder().getRelativePath()));
            fileService.createDirectories(dstDir);

            Collection<URI> srcs = processDir.listContents().values();
            int progress = 0;
            for (URI src : srcs) {
                if (Objects.nonNull(exportDmsTask)) {
                    exportDmsTask.setWorkDetail(fileService.getFileName(src));
                }
                fileService.copyFileToDirectory(src, dstDir);
                if (Objects.nonNull(exportDmsTask)) {
                    exportDmsTask.setProgress((int) ((progress++ + 1) * 98d / processDirs.size() / srcs.size() + 1));
                    if (exportDmsTask.isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
            }
        }
    }
}
