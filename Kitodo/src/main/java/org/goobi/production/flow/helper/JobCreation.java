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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.importer.ImportObject;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.helper.Helper;
import org.kitodo.production.thread.TaskScriptThread;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class JobCreation {
    private static final Logger logger = LogManager.getLogger(JobCreation.class);
    private static final FileService fileService = ServiceManager.getFileService();

    /**
     * Private constructor to hide the implicit public one.
     */
    private JobCreation() {

    }

    /**
     * Generate process.
     *
     * @param io
     *            ImportObject
     * @param template
     *            Template object
     * @return Process object
     */
    public static Process generateProcess(ImportObject io, Template template) throws DataException, IOException {
        String processTitle = io.getProcessTitle();
        logger.trace("processtitle is {}", processTitle);
        // TODO: what is differecene between metsfilename and basepath and
        // metsfile
        URI metsfilename = io.getMetsFilename();
        logger.trace("mets filename is {}", metsfilename);
        URI basepath = metsfilename;
        logger.trace("basepath is {}", basepath);
        URI metsfile = metsfilename;
        Process p = null;
        if (!testTitle(processTitle)) {
            logger.error("cannot create process, process title \"" + processTitle + "\" is already in use");
            // removing all data
            removeImages(basepath);
            try {
                fileService.delete(metsfile);
            } catch (RuntimeException e) {
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
        cp.setTemplate(template);
        cp.setMetadataFile(metsfilename);
        cp.prepare(io);
        cp.getProzessKopie().setTitle(processTitle);
        logger.trace("testing title");
        if (cp.testTitle()) {
            logger.trace("title is valid");
            cp.evaluateOpac();
            try {
                p = cp.createProcess(io);
                startThreads(p, basepath, metsfile);
            } catch (ReadException | PreferencesException | IOException e) {
                Helper.setErrorMessage("Cannot read file " + processTitle, logger, e);
            } catch (DAOException e) {
                Helper.setErrorMessage("errorSaving",
                    new Object[] {Helper.getTranslation("process") + " " + processTitle }, logger, e);
            } catch (WriteException e) {
                Helper.setErrorMessage("Cannot write file " + processTitle, logger, e);
            }
        } else {
            logger.error("title " + processTitle + "is invalid");
        }
        return p;
    }

    private static void removeImages(URI imagesFolder) throws IOException {
        if (fileService.isDirectory(imagesFolder)) {
            fileService.delete(imagesFolder);
        } else {
            imagesFolder = fileService.createResource(imagesFolder, "_images");
            if (fileService.isDirectory(imagesFolder)) {
                fileService.delete(imagesFolder);
            }
        }
    }

    private static void startThreads(Process process, URI basepath, URI metsfile) throws DAOException, IOException {
        if (Objects.nonNull(process) && Objects.nonNull(process.getId())) {
            moveFiles(metsfile, basepath, process);
            List<Task> tasks = ServiceManager.getProcessService().getById(process.getId()).getTasks();
            for (Task task : tasks) {
                if (task.getProcessingStatus() == 1 && task.isTypeAutomatic()) {
                    Thread thread = new TaskScriptThread(task);
                    thread.start();
                }
            }
        }
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
            Long amount = ServiceManager.getProcessService().findNumberOfProcessesWithTitle(title);
            if (amount > 0) {
                Helper.setErrorMessage("processTitleAlreadyInUse");
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
    public static void moveFiles(URI metsfile, URI basepath, Process p) throws IOException {
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.IMPORT_USE_OLD_CONFIGURATION)) {
            URI imagesFolder = basepath;
            if (!fileService.fileExist(imagesFolder)) {
                imagesFolder = fileService.createResource(basepath, "_images");
            }
            if (fileService.isDirectory(imagesFolder)) {
                List<URI> imageDir = new ArrayList<>(fileService.getSubUris(imagesFolder));
                for (URI uri : imageDir) {
                    URI image = fileService.createResource(imagesFolder, uri.toString());
                    URI dest = fileService.createResource(
                        ServiceManager.getProcessService().getImagesOrigDirectory(false, p),
                        fileService.getFileName(image));
                    fileService.moveFile(image, dest);
                }
                fileService.delete(imagesFolder);
            }

            // copy pdf files
            moveDirectory(basepath.resolve("_pdf" + File.separator), fileService.getPdfDirectory(p));

            // copy fulltext files
            moveDirectory(basepath.resolve("_txt"), fileService.getTxtDirectory(p));

            // copy source files
            moveDirectory(basepath.resolve("_src" + File.separator), fileService.getImportDirectory(p));

            try {
                fileService.delete(metsfile);
            } catch (RuntimeException e) {
                logger.error("Can not delete file " + metsfile + " after importing " + p.getTitle() + " into kitodo",
                    e);

            }
            File anchor = new File(basepath + "_anchor.xml");
            if (anchor.exists()) {
                fileService.delete(anchor.toURI());
            }
        } else {
            createNewFolderForProcessImports(basepath, metsfile, p);
        }
    }

    private static void createNewFolderForProcessImports(URI importFolder, URI metsFile, Process p) throws IOException {
        if (fileService.isDirectory(importFolder)) {
            List<URI> folderList = fileService.getSubUris(importFolder);
            for (URI directory : folderList) {
                if (fileService.getFileName(directory).contains("images")) {
                    List<URI> imageList = fileService.getSubUris(directory);
                    for (URI imageDir : imageList) {
                        if (fileService.isDirectory(imageDir)) {
                            for (URI file : fileService.getSubUris(imageDir)) {
                                fileService.moveFile(file, fileService.createResource(fileService.getImagesDirectory(p),
                                    fileService.getFileName(imageDir) + fileService.getFileName(file)));
                            }
                        } else {
                            fileService.moveFile(imageDir, fileService.createResource(fileService.getImagesDirectory(p),
                                fileService.getFileName(imageDir)));
                        }
                    }
                } else if (fileService.getFileName(directory).contains("ocr")) {
                    URI ocr = fileService.getOcrDirectory(p);
                    if (!fileService.fileExist(ocr)) {
                        fileService.createDirectory(ocr, null);
                    }
                    List<URI> ocrList = fileService.getSubUris(directory);
                    for (URI ocrDir : ocrList) {
                        moveDirectory(ocrDir, fileService.createResource(ocr, fileService.getFileName(ocrDir)));
                    }
                } else {
                    URI i = fileService.getImportDirectory(p);
                    if (!fileService.fileExist(i)) {
                        fileService.createResource(i.getPath());
                    }
                    List<URI> importList = fileService.getSubUris(directory);
                    for (URI importDir : importList) {
                        moveDirectory(importDir, fileService.createResource(i, fileService.getFileName(importDir)));
                    }
                }
            }
            fileService.delete(importFolder);
            fileService.delete(metsFile);
        }
    }

    private static void moveDirectory(URI sourceDirectory, URI targetDirectory) throws IOException {
        if (fileService.isDirectory(sourceDirectory)) {
            fileService.moveDirectory(sourceDirectory, targetDirectory);
        }
    }
}
