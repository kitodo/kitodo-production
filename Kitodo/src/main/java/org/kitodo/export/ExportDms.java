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
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.helper.enums.MetadataFormat;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.ImageHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.helper.tasks.ExportDmsTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.helper.tasks.TaskSitter;
import org.kitodo.production.metadata.copier.CopierData;
import org.kitodo.production.metadata.copier.DataCopier;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class ExportDms extends ExportMets {
    private static final Logger logger = LogManager.getLogger(ExportDms.class);
    private String atsPpnBand;
    private boolean exportWithImages = true;
    private boolean exportFullText = true;
    private final FileService fileService = ServiceManager.getFileService();
    private static final String DIRECTORY_SUFFIX = "_tif";
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

    public void setExportFullText(boolean exportFullText) {
        this.exportFullText = exportFullText;
    }

    /**
     * DMS-Export an eine gewünschte Stelle.
     *
     * @param process
     *            object
     * @param inZielVerzeichnis
     *            String
     */
    @Override
    public boolean startExport(Process process, URI inZielVerzeichnis) {
        if (process.getProject().isUseDmsImport()
                && ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.ASYNCHRONOUS_AUTOMATIC_EXPORT)) {
            TaskManager.addTask(new ExportDmsTask(this, process, inZielVerzeichnis));
            Helper.setMessage(TaskSitter.isAutoRunningThreads() ? "DMSExportByThread" : "DMSExportThreadCreated",
                process.getTitle());
            return true;
        } else {
            return startExport(process, inZielVerzeichnis, (ExportDmsTask) null);
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
     * @param inZielVerzeichnis
     *            work directory of the user who triggered the export
     * @param exportDmsTask
     *            ExportDmsTask object to submit progress updates and errors
     * @return false if an error condition was caught, true otherwise
     */
    public boolean startExport(Process process, URI inZielVerzeichnis, ExportDmsTask exportDmsTask) {
        this.exportDmsTask = exportDmsTask;
        try {
            return startExport(process, inZielVerzeichnis,
                ServiceManager.getProcessService().readMetadataFile(process).getDigitalDocument());
        } catch (IOException | RuntimeException e) {
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
     * @param inZielVerzeichnis
     *            String
     * @param newFile
     *            DigitalDocument
     * @return boolean
     */
    public boolean startExport(Process process, URI inZielVerzeichnis, LegacyMetsModsDigitalDocumentHelper newFile)
            throws IOException {

        this.myPrefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        this.atsPpnBand = ServiceManager.getProcessService().getNormalizedTitle(process.getTitle());

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

        return prepareAndDownloadSaveLocation(process, inZielVerzeichnis, gdzfile);
    }

    private boolean prepareAndDownloadSaveLocation(Process process, URI inZielVerzeichnis,
            LegacyMetsModsDigitalDocumentHelper gdzfile) throws IOException {
        // TODO: why create again zielVerzeichnis if it is already given as an
        // input??
        URI zielVerzeichnis;
        URI userHome;
        if (process.getProject().isUseDmsImport()) {
            // TODO: I have got here value usr/local/kitodo/hotfolder
            zielVerzeichnis = new File(process.getProject().getDmsImportImagesPath()).toURI();
            userHome = zielVerzeichnis;

            // if necessary, create process folder
            if (process.getProject().isDmsImportCreateProcessFolder()) {
                URI userHomeProcess = fileService.createResource(userHome,
                    File.separator + ServiceManager.getProcessService().getNormalizedTitle(process.getTitle()));
                zielVerzeichnis = userHomeProcess;
                boolean createProcessFolderResult = createProcessFolder(userHomeProcess, userHome, process.getProject(),
                    process.getTitle());
                if (!createProcessFolderResult) {
                    return false;
                }
            }
        } else {
            zielVerzeichnis = URI.create(inZielVerzeichnis + atsPpnBand + "/");
            // if the home exists, first delete and then create again
            userHome = zielVerzeichnis;
            if (!fileService.delete(userHome)) {
                Helper.setErrorMessage(
                    Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(process.getTitle())),
                    Helper.getTranslation(EXPORT_DIR_DELETE, Collections.singletonList("Home")));
                return false;
            }
            prepareUserDirectory(zielVerzeichnis);
        }
        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setProgress(1);
        }

        return exportImagesAndMetsToDestinationUri(process, gdzfile, zielVerzeichnis, userHome);
    }

    private boolean exportImagesAndMetsToDestinationUri(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile,
            URI zielVerzeichnis, URI userHome) throws IOException {
        boolean downloadImages = downloadImages(process, userHome, zielVerzeichnis);
        if (!downloadImages) {
            return false;
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
            if (Objects.nonNull(rules)) {
                try {
                    new DataCopier(rules).process(new CopierData(gdzfile, process));
                } catch (ConfigurationException e) {
                    if (Objects.nonNull(exportDmsTask)) {
                        exportDmsTask.setException(e);
                    } else {
                        Helper.setErrorMessage("dataCopier.syntaxError", e.getMessage(), logger, e);
                    }
                    return false;
                }
            }
        } catch (NoSuchElementException e) {
            logger.catching(Level.TRACE, e);
            // no configuration simply means here is nothing to do
        }
        return true;
    }

    private boolean createProcessFolder(URI userHomeProcess, URI userHome, Project project, String processTitle)
            throws IOException {
        String normalizedTitle = ServiceManager.getProcessService().getNormalizedTitle(processTitle);

        // delete old import folder
        if (!fileService.delete(userHomeProcess)) {
            Helper.setErrorMessage(Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(processTitle)),
                Helper.getTranslation(EXPORT_DIR_DELETE, Collections.singletonList("Import")));
            return false;
        }
        // delete old success folder
        URI successFolder = URI.create(project.getDmsImportSuccessPath() + "/" + normalizedTitle);
        if (!fileService.delete(successFolder)) {
            Helper.setErrorMessage(Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(processTitle)),
                Helper.getTranslation(EXPORT_DIR_DELETE, Collections.singletonList("Success")));
            return false;
        }
        // delete old error folder
        URI errorFolder = URI.create(project.getDmsImportErrorPath() + "/" + normalizedTitle);
        if (!fileService.delete(errorFolder)) {
            Helper.setErrorMessage(Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(processTitle)),
                Helper.getTranslation(EXPORT_DIR_DELETE, Collections.singletonList("Error")));
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

    private boolean downloadImages(Process process, URI userHome, URI destinationDirectory) {
        try {
            if (this.exportWithImages) {
                imageDownload(process, userHome, atsPpnBand, DIRECTORY_SUFFIX);
                fulltextDownload(process, userHome, atsPpnBand, DIRECTORY_SUFFIX);
            } else if (this.exportFullText) {
                fulltextDownload(process, userHome, atsPpnBand, DIRECTORY_SUFFIX);
            }
            directoryDownload(process, destinationDirectory);
            return true;
        } catch (IOException | InterruptedException | RuntimeException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
            } else {
                Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            }
            return false;
        }
    }

    private void asyncExportWithImport(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile, URI userHome)
            throws IOException {
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
            // delete success folder again
            if (process.getProject().isDmsImportCreateProcessFolder()) {
                URI successFolder = URI.create(process.getProject().getDmsImportSuccessPath() + "/"
                        + ServiceManager.getProcessService().getNormalizedTitle(processTitle));
                fileService.delete(successFolder);
            }
        }
    }

    private void exportWithoutImport(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile, URI destinationDirectory)
            throws IOException {
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
     * Download full text.
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
    public void fulltextDownload(Process process, URI userHome, String atsPpnBand, final String ordnerEndung)
            throws IOException {

        downloadSources(process, userHome, atsPpnBand);
        downloadOCR(process, userHome, atsPpnBand);

        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setWorkDetail(null);
        }
    }

    private void downloadSources(Process process, URI userHome, String atsPpnBand) throws IOException {
        URI sources = fileService.getSourceDirectory(process);
        if (fileService.fileExist(sources) && !fileService.getSubUris(sources).isEmpty()) {
            URI destination = userHome.resolve(atsPpnBand + "_src");
            if (!fileService.fileExist(destination)) {
                fileService.createDirectory(userHome, atsPpnBand + "_src");
            }
            List<URI> files = fileService.getSubUris(sources);
            copyFiles(files, destination);
        }
    }

    private void downloadOCR(Process process, URI userHome, String atsPpnBand) throws IOException {
        URI ocr = fileService.getOcrDirectory(process);
        if (fileService.fileExist(ocr)) {
            List<URI> folder = fileService.getSubUris(ocr);
            for (URI dir : folder) {
                if (fileService.isDirectory(dir) && !fileService.getSubUris(dir).isEmpty()
                        && fileService.getFileName(dir).contains("_")) {
                    String suffix = fileService.getFileName(dir)
                            .substring(fileService.getFileName(dir).lastIndexOf('_'));
                    URI destination = userHome.resolve(File.separator + atsPpnBand + suffix);
                    if (!fileService.fileExist(destination)) {
                        fileService.createDirectory(userHome, atsPpnBand + suffix);
                    }
                    List<URI> files = fileService.getSubUris(dir);
                    copyFiles(files, destination);
                }
            }
        }
    }

    private void copyFiles(List<URI> files, URI destination) throws IOException {
        for (URI file : files) {
            if (fileService.isFile(file)) {
                if (Objects.nonNull(exportDmsTask)) {
                    exportDmsTask.setWorkDetail(fileService.getFileName(file));
                }
                URI target = destination.resolve(fileService.getFileName(file));
                fileService.copyFile(file, target);
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
            throws IOException, InterruptedException {
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

            copyTifFilesForProcess(tifOrdner, zielTif);

            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setWorkDetail(null);
            }
        }
    }

    private void copyTifFilesForProcess(URI tifSourceDirectory, URI tifDestinationDirectory)
            throws IOException, InterruptedException {
        List<URI> files = fileService.getSubUris(ImageHelper.dataFilter, tifSourceDirectory);
        for (int i = 0; i < files.size(); i++) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setWorkDetail(fileService.getFileName(files.get(i)));
            }

            fileService.copyFile(files.get(i), tifDestinationDirectory);
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setProgress((int) ((i + 1) * 98d / files.size() + 1));
                if (exportDmsTask.isInterrupted()) {
                    throw new InterruptedException();
                }
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
     * Starts copying all directories configured in kitodo_config.properties
     * parameter "processDirs" to export folder.
     *
     * @param process
     *            object
     * @param zielVerzeichnis
     *            the destination directory
     *
     */
    private void directoryDownload(Process process, URI zielVerzeichnis) throws IOException {
        String[] processDirs = ConfigCore.getStringArrayParameter(ParameterCore.PROCESS_DIRS);
        String normalizedTitle = ServiceManager.getProcessService().getNormalizedTitle(process.getTitle());

        for (String processDir : processDirs) {
            URI srcDir = ServiceManager.getProcessService().getProcessDataDirectory(process)
                    .resolve(processDir.replace("(processtitle)", normalizedTitle));
            URI dstDir = zielVerzeichnis.resolve(processDir.replace("(processtitle)", normalizedTitle));

            if (fileService.isDirectory(srcDir)) {
                fileService.copyDirectory(srcDir, dstDir);
            }
        }
    }
}
