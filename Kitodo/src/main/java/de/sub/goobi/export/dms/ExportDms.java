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

package de.sub.goobi.export.dms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.tasks.EmptyTask;
import de.sub.goobi.helper.tasks.ExportDmsTask;
import de.sub.goobi.helper.tasks.TaskManager;
import de.sub.goobi.helper.tasks.TaskSitter;
import de.sub.goobi.metadaten.copier.CopierData;
import de.sub.goobi.metadaten.copier.DataCopier;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.helper.enums.MetadataFormat;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class ExportDms extends ExportMets {
    private static final Logger logger = LogManager.getLogger(ExportDms.class);
    ConfigProjects cp;
    private boolean exportWithImages = true;
    private boolean exportFullText = true;
    private final ServiceManager serviceManager = new ServiceManager();
    private final FileService fileService = serviceManager.getFileService();

    /**
     * The field exportDmsTask holds an optional task instance. Its progress and
     * its errors will be passed to the task manager screen (if available) for
     * visualisation.
     */
    public EmptyTask exportDmsTask = null;

    public static final String DIRECTORY_SUFFIX = "_tif";

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
                && ConfigCore.getBooleanParameter("asynchronousAutomaticExport", false)) {
            TaskManager.addTask(new ExportDmsTask(this, process, inZielVerzeichnis));
            Helper.setMeldung(TaskSitter.isAutoRunningThreads() ? "DMSExportByThread" : "DMSExportThreadCreated",
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
                serviceManager.getProcessService().readMetadataFile(process).getDigitalDocument());
        } catch (Exception e) {
            if (exportDmsTask != null) {
                exportDmsTask.setException(e);
            } else {
                Helper.setFehlerMeldung(Helper.getTranslation("exportError") + process.getTitle(), e);
            }
            logger.error("Export abgebrochen, xml-LeseFehler", e);
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
    public boolean startExport(Process process, URI inZielVerzeichnis, DigitalDocumentInterface newFile)
            throws IOException, WriteException, PreferencesException, TypeNotAllowedForParentException {

        this.myPrefs = serviceManager.getRulesetService().getPreferences(process.getRuleset());
        this.cp = new ConfigProjects(process.getProject().getTitle());
        String atsPpnBand = process.getTitle();

        /*
         * Dokument einlesen
         */
        FileformatInterface gdzfile;
        try {
            switch (MetadataFormat.findFileFormatsHelperByName(process.getProject().getFileFormatDmsExport())) {
                case METS:
                    gdzfile = UghImplementation.INSTANCE.createMetsModsImportExport(this.myPrefs);
                    break;

                case METS_AND_RDF:
                default:
                    gdzfile = UghImplementation.INSTANCE.createRDFFile(this.myPrefs);
                    break;
            }

            gdzfile.setDigitalDocument(newFile);

        } catch (Exception e) {
            if (exportDmsTask != null) {
                exportDmsTask.setException(e);
            } else {
                Helper.setFehlerMeldung(Helper.getTranslation("exportError") + process.getTitle(), e);
            }
            logger.error("Export abgebrochen, xml-LeseFehler", e);
            return false;
        }

        String rules = ConfigCore.getParameter("copyData.onExport");
        if (rules != null && !rules.equals("- keine Konfiguration gefunden -")) {
            try {
                new DataCopier(rules).process(new CopierData(gdzfile, process));
            } catch (ConfigurationException e) {
                if (exportDmsTask != null) {
                    exportDmsTask.setException(e);
                } else {
                    Helper.setFehlerMeldung("dataCopier.syntaxError", e.getMessage());
                }
                return false;
            } catch (RuntimeException e) {
                if (exportDmsTask != null) {
                    exportDmsTask.setException(e);
                } else {
                    Helper.setFehlerMeldung("dataCopier.runtimeException", e.getMessage());
                }
                return false;
            }
        }

        trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());

        // validate metadata
        if (ConfigCore.getBooleanParameter("useMetadatenvalidierung")
                && !serviceManager.getMetadataValidationService().validate(gdzfile, this.myPrefs, process)) {
            return false;
        }

        /*
         * Speicherort vorbereiten und downloaden
         */
        //TODO: why create again zielVerzeichnis if it is already given as an input??
        URI zielVerzeichnis;
        URI userHome;
        if (process.getProject().isUseDmsImport()) {
            //TODO: I have got here value usr/local/kitodo/hotfolder
            zielVerzeichnis = URI.create(process.getProject().getDmsImportImagesPath());
            userHome = zielVerzeichnis;

            /* ggf. noch einen Vorgangsordner anlegen */
            if (process.getProject().isDmsImportCreateProcessFolder()) {
                URI userHomeProcess = fileService.createResource(userHome, File.separator + process.getTitle());
                zielVerzeichnis = userHomeProcess;
                /* alte Import-Ordner löschen */
                if (!fileService.delete(userHomeProcess)) {
                    Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(),
                        "Import folder could not be cleared");
                    return false;
                }
                /* alte Success-Ordner löschen */
                File successFile = new File(
                        process.getProject().getDmsImportSuccessPath() + File.separator + process.getTitle());
                if (!fileService.delete(successFile.toURI())) {
                    Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(),
                        "Success folder could not be cleared");
                    return false;
                }
                /* alte Error-Ordner löschen */
                File errorfile = new File(
                        process.getProject().getDmsImportErrorPath() + File.separator + process.getTitle());
                if (!fileService.delete(errorfile.toURI())) {
                    Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(),
                        "Error folder could not be cleared");
                    return false;
                }

                if (!fileService.fileExist(userHomeProcess)) {
                    fileService.createDirectory(userHome, process.getTitle());
                }
            }

        } else {
            zielVerzeichnis = URI.create(inZielVerzeichnis + atsPpnBand + "/");
            // wenn das Home existiert, erst löschen und dann neu anlegen
            userHome = zielVerzeichnis;
            if (!fileService.delete(userHome)) {
                Helper.setFehlerMeldung("Export canceled: " + process.getTitle(), "could not delete home directory");
                return false;
            }
            prepareUserDirectory(zielVerzeichnis);
        }
        if (exportDmsTask != null) {
            exportDmsTask.setProgress(1);
        }

        /*
         * der eigentliche Download der Images
         */
        try {
            if (this.exportWithImages) {
                imageDownload(process, userHome, atsPpnBand, DIRECTORY_SUFFIX);
                fulltextDownload(process, userHome, atsPpnBand, DIRECTORY_SUFFIX);
            } else if (this.exportFullText) {
                fulltextDownload(process, userHome, atsPpnBand, DIRECTORY_SUFFIX);
            }
            directoryDownload(process, zielVerzeichnis);
        } catch (Exception e) {
            if (exportDmsTask != null) {
                exportDmsTask.setException(e);
            } else {
                Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), e);
            }
            return false;
        }

        /*
         * zum Schluss Datei an gewünschten Ort exportieren entweder direkt in
         * den Import-Ordner oder ins Benutzerhome anschliessend den
         * Import-Thread starten
         */
        if (process.getProject().isUseDmsImport()) {
            if (exportDmsTask != null) {
                exportDmsTask.setWorkDetail(atsPpnBand + ".xml");
            }
            if (MetadataFormat.findFileFormatsHelperByName(
                process.getProject().getFileFormatDmsExport()) == MetadataFormat.METS) {
                /* Wenn METS, dann per writeMetsFile schreiben... */
                writeMetsFile(process, fileService.createResource(userHome, File.separator + atsPpnBand + ".xml"),
                    gdzfile, false);
            } else {
                /* ...wenn nicht, nur ein Fileformat schreiben. */
                gdzfile.write(userHome + File.separator + atsPpnBand + ".xml");
            }

            /* ggf. sollen im Export mets und rdf geschrieben werden */
            if (MetadataFormat.findFileFormatsHelperByName(
                process.getProject().getFileFormatDmsExport()) == MetadataFormat.METS_AND_RDF) {
                writeMetsFile(process, fileService.createResource(userHome, File.separator + atsPpnBand + ".mets.xml"),
                    gdzfile, false);
            }

            Helper.setMeldung(null, process.getTitle() + ": ", "DMS-Export started");
            if (!ConfigCore.getBooleanParameter("exportWithoutTimeLimit")) {
                DmsImportThread agoraThread = new DmsImportThread(process, atsPpnBand);
                agoraThread.start();
                try {
                    /* 30 Sekunden auf den Thread warten, evtl. killen */
                    agoraThread.join(process.getProject().getDmsImportTimeOut().longValue());
                    if (agoraThread.isAlive()) {
                        agoraThread.stopThread();
                    }
                } catch (InterruptedException e) {
                    if (exportDmsTask != null) {
                        exportDmsTask.setException(e);
                    } else {
                        Helper.setFehlerMeldung(process.getTitle() + ": error on export - ", e.getMessage());
                    }
                    logger.error(process.getTitle() + ": error on export", e);
                }
                if (agoraThread.result.length() > 0) {
                    if (exportDmsTask != null) {
                        exportDmsTask
                                .setException(new RuntimeException(process.getTitle() + ": " + agoraThread.result));
                    } else {
                        Helper.setFehlerMeldung(process.getTitle() + ": ", agoraThread.result);
                    }
                } else {
                    if (exportDmsTask != null) {
                        exportDmsTask.setProgress(100);
                    } else {
                        Helper.setMeldung(null, process.getTitle() + ": ", "ExportFinished");
                    }
                    /* Success-Ordner wieder löschen */
                    if (process.getProject().isDmsImportCreateProcessFolder()) {
                        File successFile = new File(
                                process.getProject().getDmsImportSuccessPath() + File.separator + process.getTitle());
                        fileService.delete(successFile.toURI());
                    }
                }
            }
            if (exportDmsTask != null) {
                exportDmsTask.setProgress(100);
            }
        } else {
            /* ohne Agora-Import die xml-Datei direkt ins Home schreiben */
            if (MetadataFormat.findFileFormatsHelperByName(
                process.getProject().getFileFormatDmsExport()) == MetadataFormat.METS) {
                writeMetsFile(process, fileService.createResource(zielVerzeichnis, atsPpnBand + ".xml"), gdzfile,
                    false);
            } else {
                gdzfile.write(zielVerzeichnis + atsPpnBand + ".xml");
            }

            Helper.setMeldung(null, process.getTitle() + ": ", "ExportFinished");
        }
        return true;
    }

    /**
     * Setter method to pass in a task thread to whom progress and error
     * messages shall be reported.
     *
     * @param task
     *            task implementation
     */
    public void setExportDmsTask(EmptyTask task) {
        this.exportDmsTask = task;
    }

    /**
     * Run through all metadata and children of given docstruct to trim the
     * strings calls itself recursively.
     */
    private void trimAllMetadata(DocStructInterface inStruct) {
        /* trimm all metadata values */
        if (inStruct.getAllMetadata() != null) {
            for (MetadataInterface md : inStruct.getAllMetadata()) {
                if (md.getValue() != null) {
                    md.setStringValue(md.getValue().trim());
                }
            }
        }

        /* run through all children of docstruct */
        if (inStruct.getAllChildren() != null) {
            for (DocStructInterface child : inStruct.getAllChildren()) {
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

        // download sources
        URI sources = serviceManager.getFileService().getSourceDirectory(process);
        if (fileService.fileExist(sources) && fileService.getSubUris(sources).size() > 0) {
            URI destination = userHome.resolve(File.separator + atsPpnBand + "_src");
            if (!fileService.fileExist(destination)) {
                fileService.createDirectory(userHome, atsPpnBand + "_src");
            }
            ArrayList<URI> files = fileService.getSubUris(sources);
            copyFiles(files, destination);
        }

        URI ocr = serviceManager.getFileService().getOcrDirectory(process);
        if (fileService.fileExist(ocr)) {
            ArrayList<URI> folder = fileService.getSubUris(ocr);
            for (URI dir : folder) {
                if (fileService.isDirectory(dir) && fileService.getSubUris(dir).size() > 0
                        && fileService.getFileName(dir).contains("_")) {
                    String suffix = fileService.getFileName(dir)
                            .substring(fileService.getFileName(dir).lastIndexOf('_'));
                    URI destination = userHome.resolve(File.separator + atsPpnBand + suffix);
                    if (!fileService.fileExist(destination)) {
                        fileService.createDirectory(userHome, atsPpnBand + suffix);
                    }
                    ArrayList<URI> files = fileService.getSubUris(dir);
                    copyFiles(files, destination);
                }
            }
        }
        if (exportDmsTask != null) {
            exportDmsTask.setWorkDetail(null);
        }
    }

    private void copyFiles(ArrayList<URI> files, URI destination) throws IOException {
        for (URI file : files) {
            if (fileService.isFile(file)) {
                if (exportDmsTask != null) {
                    exportDmsTask.setWorkDetail(fileService.getFileName(file));
                }
                URI target = destination.resolve(File.separator + fileService.getFileName(file));
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

        /*
         * dann den Ausgangspfad ermitteln
         */
        URI tifOrdner = serviceManager.getProcessService().getImagesTifDirectory(true, process);

        /*
         * jetzt die Ausgangsordner in die Zielordner kopieren
         */
        if (fileService.fileExist(tifOrdner) && fileService.getSubUris(tifOrdner).size() > 0) {
            URI zielTif = userHome.resolve(atsPpnBand + ordnerEndung + "/");

            /* bei Agora-Import einfach den Ordner anlegen */
            if (process.getProject().isUseDmsImport()) {
                if (!fileService.fileExist(zielTif)) {
                    fileService.createDirectory(userHome, atsPpnBand + ordnerEndung);
                }
            } else {
                /*
                 * wenn kein Agora-Import, dann den Ordner mit
                 * Benutzerberechtigung neu anlegen
                 */
                User user = Helper.getCurrentUser();
                try {
                    if (user != null) {
                        fileService.createDirectoryForUser(zielTif, user.getLogin());
                    } else {
                        throw new IOException("No logged user!");
                    }
                } catch (IOException e) {
                    handleException(e);
                    throw e;
                } catch (Exception e) {
                    handleException(e);
                    logger.error("could not create destination directory", e);
                }
            }

            /* jetzt den eigentlichen Kopiervorgang */
            ArrayList<URI> dateien = fileService.getSubUris(Helper.dataFilter, tifOrdner);
            for (int i = 0; i < dateien.size(); i++) {
                if (exportDmsTask != null) {
                    exportDmsTask.setWorkDetail(fileService.getFileName(dateien.get(i)));
                }

                fileService.copyFile(dateien.get(i), zielTif);
                if (exportDmsTask != null) {
                    exportDmsTask.setProgress((int) ((i + 1) * 98d / dateien.size() + 1));
                    if (exportDmsTask.isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
            }
            if (exportDmsTask != null) {
                exportDmsTask.setWorkDetail(null);
            }
        }
    }

    private void handleException(Exception e) {
        if (exportDmsTask != null) {
            exportDmsTask.setException(e);
        } else {
            Helper.setFehlerMeldung("Export canceled, error", "could not create destination directory");
        }
        logger.error("could not create destination directory", e);
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

        String[] processDirs = ConfigCore.getStringArrayParameter("processDirs");

        for (String processDir : processDirs) {
            URI srcDir = serviceManager.getProcessService().getProcessDataDirectory(process)
                    .resolve(processDir.replace("(processtitle)", process.getTitle()));
            URI dstDir = zielVerzeichnis.resolve(processDir.replace("(processtitle)", process.getTitle()));

            if (fileService.isDirectory(srcDir)) {
                fileService.copyDirectory(srcDir, dstDir);
            }
        }
    }
}
