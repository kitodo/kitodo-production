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
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
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
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.index.converter.ProcessConverter;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ExportException;
import org.kitodo.exceptions.MetadataException;
import org.kitodo.production.enums.ProcessState;
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
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.workflow.WorkflowControllerService;

public class ExportDms extends ExportMets {
    private static final Logger logger = LogManager.getLogger(ExportDms.class);
    private static final String EXPORT_DIR_DELETE = "errorDirectoryDeleting";
    private static final String ERROR_EXPORT = "errorExport";

    private final FileService fileService = ServiceManager.getFileService();
    private final ProcessService processService = ServiceManager.getProcessService();

    private boolean exportWithImages = true;

    private Task workFlowTask;

    public ExportDms() {
    }

    public ExportDms(boolean exportImages) {
        this.exportWithImages = exportImages;
    }

    public ExportDms(Task workFlowTask) {
        this.workFlowTask = workFlowTask;
    }

    /**
     * Export to DMS.
     *
     * @param task
     *            Task object
     * @throws IOException
     *             if I/O fails while running a script for a script condition of
     *             a subsequent task, or the METS file cannot be read when
     *             evaluating an XPath condition
     */
    public void startExport(Task task) throws DataException, IOException, DAOException {
        if (startExport(task.getProcess())) {
            new WorkflowControllerService().close(task);
        }
    }

    /**
     * Export to DMS.
     *
     * @param process
     *            Process object
     */
    @Override
    public boolean startExport(Process process) throws DataException {
        if (!exportCompletedChildren(process.getChildren())) {
            return false;
        }

        boolean wasNotAlreadyExported = !process.isExported();
        if (wasNotAlreadyExported) {
            process.setExported(true);
            processService.save(process);
        }

        boolean exportSuccessful = startExport(process, (URI) null);
        if (Objects.nonNull(process.getParent())) {
            startExport(process.getParent());
        }
        if (wasNotAlreadyExported) {
            process.setExported(exportSuccessful);
            processService.save(process);
        }
        return exportSuccessful;
    }

    /**
     * Export to the DMS.
     *
     * @param process
     *            process to export
     * @param unused
     *            user home directory
     */
    @Override
    public boolean startExport(Process process, URI unused) {
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.ASYNCHRONOUS_AUTOMATIC_EXPORT)) {
            TaskManager.addTask(new ExportDmsTask(this, process));
            Helper.setMessage(TaskSitter.isAutoRunningThreads() ? "DMSExportByThread" : "DMSExportThreadCreated",
                process.getTitle());
            return false;
        } else {
            return startExport(process, (ExportDmsTask) null);
        }
    }

    /**
     * Performs a DMS export to a desired place. In addition, it accepts an
     * optional ExportDmsTask object. If that is passed in, the progress in it
     * will be updated during processing and occurring errors will be passed to
     * it to be visible in the task manager screen.
     *
     * @param process
     *            process to export
     * @param exportDmsTask
     *            ExportDmsTask object to submit progress updates and errors
     * @return false if an error condition was caught, true otherwise
     */
    public boolean startExport(Process process, ExportDmsTask exportDmsTask) {
        this.exportDmsTask = exportDmsTask;
        try {
            return startExport(process,
                processService.readMetadataFile(process).getDigitalDocument());
        } catch (IOException | DAOException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
                logger.error(Helper.getTranslation(ERROR_EXPORT, process.getTitle()), e);
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
     * @param newFile
     *            DigitalDocument
     * @return boolean
     */
    private boolean startExport(Process process, LegacyMetsModsDigitalDocumentHelper newFile)
            throws IOException, DAOException {

        this.myPrefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());

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
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(new MetadataException("metadata validation failed", null));
            }
            return false;
        }

        return prepareExportLocation(process, gdzfile);
    }

    private boolean exportCompletedChildren(List<Process> children) throws DataException {
        for (Process child:children) {
            if (ProcessConverter.getCombinedProgressAsString(child, false).equals(ProcessState.COMPLETED.getValue())
                    && !child.isExported()) {
                if (!startExport(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean prepareExportLocation(Process process,
            LegacyMetsModsDigitalDocumentHelper gdzfile) throws IOException, DAOException {

        URI hotfolder = new File(process.getProject().getDmsImportRootPath()).toURI();
        String processTitle = Helper.getNormalizedTitle(process.getTitle());
        URI exportFolder = new File(hotfolder.getPath(), processTitle).toURI();

        // delete old export folder
        if (!fileService.delete(exportFolder)) {
            String message = Helper.getTranslation(ERROR_EXPORT, processTitle);
            String description = Helper.getTranslation(EXPORT_DIR_DELETE, exportFolder.getPath());
            Helper.setErrorMessage(message, description);
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(new ExportException(message + ": " + description));
            }
            return false;
        }

        fileService.createDirectory(hotfolder, processTitle);

        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setProgress(1);
        }

        return exportImagesAndMetsToDestinationUri(process, gdzfile, exportFolder);
    }

    private boolean exportImagesAndMetsToDestinationUri(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile,
            URI destination) throws IOException, DAOException {

        if (exportWithImages) {
            try {
                directoryDownload(process, destination);
            } catch (IOException | InterruptedException | RuntimeException | URISyntaxException e) {
                if (Objects.nonNull(exportDmsTask)) {
                    exportDmsTask.setException(e);
                } else {
                    Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
                }
                return false;
            }
        }

        // export the file to the import folder
        return asyncExportWithImport(process, gdzfile, destination);
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

    private LegacyMetsModsDigitalDocumentHelper readDocument(Process process, LegacyMetsModsDigitalDocumentHelper newFile) {
        LegacyMetsModsDigitalDocumentHelper gdzfile;
        try {
            gdzfile = new LegacyMetsModsDigitalDocumentHelper(this.myPrefs.getRuleset());
            gdzfile.setDigitalDocument(newFile);
            return gdzfile;
        } catch (RuntimeException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
                logger.error(Helper.getTranslation(ERROR_EXPORT, process.getTitle()), e);
            } else {
                Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            }
            return null;
        }
    }

    private boolean asyncExportWithImport(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile, URI userHome)
            throws IOException, DAOException {

        String atsPpnBand = Helper.getNormalizedTitle(process.getTitle());
        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setWorkDetail(atsPpnBand + ".xml");
        }
        boolean metsFileWrittenSuccesful = writeMetsFile(process, fileService.createResource(userHome,
                File.separator + atsPpnBand + ".xml"), gdzfile);

        if (Objects.nonNull(exportDmsTask)) {
            exportDmsTask.setProgress(100);
        }
        return metsFileWrittenSuccesful;
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
     * Get workflowTask.
     *
     * @return value of workFlowTask
     */
    public Task getWorkflowTask() {
        return workFlowTask;
    }

    /**
     * Set workflowTask.
     *
     * @param workFlowTask
     *              the workflow stask associated with the export
     */
    public void setWorkflowTask(Task workFlowTask) {
        this.workFlowTask = workFlowTask;
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
        for (LegacyMetadataHelper md : inStruct.getAllMetadata()) {
            if (Objects.nonNull(md.getValue())) {
                md.setStringValue(md.getValue().trim());
            }
        }

        // run through all children of docstruct
        for (LegacyDocStructHelperInterface child : inStruct.getAllChildren()) {
            trimAllMetadata(child);
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
        URI tifOrdner = processService.getImagesTifDirectory(true, process.getId(),
            process.getTitle(), process.getProcessBaseUri());

        // copy the source folder to the destination folder
        if (fileService.fileExist(tifOrdner) && !fileService.getSubUris(tifOrdner).isEmpty()) {
            URI zielTif = userHome.resolve(atsPpnBand + ordnerEndung + "/");

            // with Agora import simply create the folder
            if (!fileService.fileExist(zielTif)) {
                fileService.createDirectory(userHome, atsPpnBand + ordnerEndung);
            }

            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setWorkDetail(null);
            }
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
    private void directoryDownload(Process process, URI destination) throws IOException, InterruptedException, URISyntaxException {
        Collection<Subfolder> processDirs = process.getProject().getFolders().parallelStream()
                .filter(Folder::isCopyFolder).map(folder -> new Subfolder(process, folder))
                .collect(Collectors.toList());
        VariableReplacer variableReplacer = new VariableReplacer(null, process, null);

        String uriToDestination = destination.toString();
        if (!uriToDestination.endsWith("/")) {
            uriToDestination = uriToDestination.concat("/");
        }
        for (Subfolder processDir : processDirs) {
            URI dstDir = new URI(uriToDestination
                    + variableReplacer.replace(processDir.getFolder().getRelativePath()));
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
