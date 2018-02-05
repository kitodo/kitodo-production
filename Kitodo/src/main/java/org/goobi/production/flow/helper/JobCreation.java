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

package org.goobi.production.flow.helper;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.importer.ImportObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.thread.TaskScriptThread;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class JobCreation {
    private static final Logger logger = LogManager.getLogger(JobCreation.class);
    private static final ServiceManager serviceManager = new ServiceManager();

    private static final FileService fileService = serviceManager.getFileService();

    /**
     * Generate process.
     *
     * @param io
     *            ImportObject
     * @param vorlage
     *            Process object
     * @return Process object
     */
    @SuppressWarnings("static-access")
    public static Process generateProcess(ImportObject io, Process vorlage) throws DataException, IOException {
        String processTitle = io.getProcessTitle();
        if (logger.isTraceEnabled()) {
            logger.trace("processtitle is " + processTitle);
        }
        // TODO: what is differecene between metsfilename and basepath and
        // metsfile
        URI metsfilename = io.getMetsFilename();
        if (logger.isTraceEnabled()) {
            logger.trace("mets filename is " + metsfilename);
        }
        URI basepath = metsfilename;
        if (logger.isTraceEnabled()) {
            logger.trace("basepath is " + basepath);
        }
        URI metsfile = metsfilename;
        Process p = null;
        if (!testTitle(processTitle)) {
            logger.error("cannot create process, process title \"" + processTitle + "\" is already in use");
            // removing all data
            URI imagesFolder = basepath;
            if (fileService.isDirectory(imagesFolder)) {
                fileService.delete(imagesFolder);
            } else {
                imagesFolder = fileService.createResource(basepath, "_images");
                if (fileService.isDirectory(imagesFolder)) {
                    fileService.delete(imagesFolder);
                }
            }
            try {
                fileService.delete(metsfile);
            } catch (Exception e) {
                logger.error("Can not delete file " + processTitle, e);
                return null;
            }
            File anchor = new File(basepath + "_anchor.xml");
            if (anchor.exists()) {
                fileService.delete(anchor.toURI());
            }
            return null;
        }

        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(vorlage);
        cp.setMetadataFile(metsfilename);
        cp.prepare(io);
        cp.getProzessKopie().setTitle(processTitle);
        logger.trace("testing title");
        if (cp.testTitle()) {
            logger.trace("title is valid");
            cp.evaluateOpac();
            try {
                p = cp.createProcess(io);
                if (p != null && p.getId() != null) {
                    moveFiles(metsfile, basepath, p);
                    List<Task> tasks = serviceManager.getProcessService().getById(p.getId()).getTasks();
                    for (Task t : tasks) {
                        if (t.getProcessingStatus() == 1 && t.isTypeAutomatic()) {
                            Thread myThread = new TaskScriptThread(t);
                            myThread.start();
                        }
                    }
                }
            } catch (ReadException | PreferencesException | IOException e) {
                Helper.setFehlerMeldung("Cannot read file " + processTitle, e);
                logger.error(e);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Cannot save process " + processTitle, e);
                logger.error(e);
            } catch (WriteException e) {
                Helper.setFehlerMeldung("Cannot write file " + processTitle, e);
                logger.error(e);
            }
        } else {
            logger.error("title " + processTitle + "is invalid");
        }
        return p;
    }

    /**
     * Test title.
     *
     * @param title
     *            String
     * @return boolean
     */
    private static boolean testTitle(String title) throws DataException {
        if (title != null) {
            Long amount = serviceManager.getProcessService().findNumberOfProcessesWithTitle(title);
            if (amount > 0) {
                Helper.setFehlerMeldung("processTitleAlreadyInUse");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Move files.
     *
     * @param metsfile
     *            File object
     * @param basepath
     *            String
     * @param p
     *            Process object
     */
    @SuppressWarnings("static-access")
    public static void moveFiles(URI metsfile, URI basepath, Process p) throws IOException {
        if (ConfigCore.getBooleanParameter("importUseOldConfiguration", false)) {
            URI imagesFolder = basepath;
            if (!fileService.fileExist(imagesFolder)) {
                imagesFolder = fileService.createResource(basepath, "_images");
            }
            if (fileService.isDirectory(imagesFolder)) {
                List<URI> imageDir = new ArrayList<>(fileService.getSubUris(imagesFolder));
                for (URI uri : imageDir) {
                    URI image = fileService.createResource(imagesFolder, uri.toString());
                    URI dest = fileService.createResource(
                            serviceManager.getProcessService().getImagesOrigDirectory(false, p),
                            fileService.getFileName(image));
                    fileService.moveFile(image, dest);
                }
                fileService.delete(imagesFolder);
            }

            // copy pdf files
            URI pdfs = (basepath.resolve("_pdf" + File.separator));
            if (fileService.isDirectory(pdfs)) {
                fileService.moveDirectory(pdfs, serviceManager.getFileService().getPdfDirectory(p));
            }

            // copy fulltext files

            URI fulltext = basepath.resolve("_txt");

            if (fileService.isDirectory(fulltext)) {

                fileService.moveDirectory(fulltext, serviceManager.getFileService().getTxtDirectory(p));
            }

            // copy source files

            URI sourceDir = basepath.resolve("_src" + File.separator);
            if (fileService.isDirectory(sourceDir)) {
                fileService.moveDirectory(sourceDir, (serviceManager.getFileService().getImportDirectory(p)));
            }

            try {
                fileService.delete(metsfile);
            } catch (Exception e) {
                logger.error("Can not delete file " + metsfile + " after importing " + p.getTitle() + " into kitodo",
                        e);

            }
            File anchor = new File(basepath + "_anchor.xml");
            if (anchor.exists()) {
                fileService.delete(anchor.toURI());
            }
        } else {
            // new folder structure for process imports
            URI importFolder = basepath;
            if (fileService.isDirectory(importFolder)) {
                ArrayList<URI> folderList = fileService.getSubUris(importFolder);
                for (URI directory : folderList) {
                    if (fileService.getFileName(directory).contains("images")) {
                        ArrayList<URI> imageList = fileService.getSubUris(directory);
                        for (URI imagedir : imageList) {
                            if (fileService.isDirectory(imagedir)) {
                                for (URI file : fileService.getSubUris(imagedir)) {
                                    fileService.moveFile(file,
                                            fileService.createResource(
                                                    serviceManager.getFileService().getImagesDirectory(p),
                                                    fileService.getFileName(imagedir) + fileService.getFileName(file)));
                                }
                            } else {
                                fileService.moveFile(imagedir,
                                        fileService.createResource(
                                                serviceManager.getFileService().getImagesDirectory(p),
                                                fileService.getFileName(imagedir)));
                            }
                        }
                    } else if (fileService.getFileName(directory).contains("ocr")) {
                        URI ocr = serviceManager.getFileService().getOcrDirectory(p);
                        if (!fileService.fileExist(ocr)) {
                            fileService.createDirectory(ocr, null);
                        }
                        ArrayList<URI> ocrList = fileService.getSubUris(directory);
                        for (URI ocrdir : ocrList) {
                            if (fileService.isDirectory(ocrdir)) {
                                fileService.moveDirectory(ocrdir,
                                        fileService.createResource(ocr, fileService.getFileName(ocrdir)));
                            } else {
                                fileService.moveDirectory(ocrdir,
                                        fileService.createResource(ocr, fileService.getFileName(ocrdir)));
                            }
                        }
                    } else {
                        URI i = serviceManager.getFileService().getImportDirectory(p);
                        if (!fileService.fileExist(i)) {
                            fileService.createResource(i.getPath());
                        }
                        ArrayList<URI> importList = fileService.getSubUris(directory);
                        for (URI importdir : importList) {
                            if (fileService.isDirectory(importdir)) {
                                fileService.moveDirectory(importdir,
                                        fileService.createResource(i, fileService.getFileName(importdir)));
                            } else {
                                fileService.moveDirectory(importdir,
                                        fileService.createResource(i, fileService.getFileName(importdir)));
                            }
                        }
                    }
                }
                fileService.delete(importFolder);
                fileService.delete(metsfile);
            }
        }
    }
}
