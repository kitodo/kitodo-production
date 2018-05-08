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

package org.goobi.production.flow.jobs;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.GoobiHotfolder;
import org.goobi.production.importer.ImportObject;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.thread.TaskScriptThread;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

/**
 * HotfolderJob class.
 *
 * @author Robert Sehr
 */
@Deprecated
public class HotfolderJob extends AbstractGoobiJob {
    private static final Logger logger = LogManager.getLogger(HotfolderJob.class);

    private static final ServiceManager serviceManager = new ServiceManager();
    private static final FileService fileService = serviceManager.getFileService();

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.jobs.SimpleGoobiJob#initialize()
     */
    @Override
    public String getJobName() {
        return "HotfolderJob";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.jobs.SimpleGoobiJob#execute()
     */
    @Override
    public void execute() {
        if (ConfigCore.getBooleanParameter("runHotfolder", false)) {
            logger.trace("1");
            List<GoobiHotfolder> hotlist = GoobiHotfolder.getInstances();
            logger.trace("2");
            for (GoobiHotfolder hotfolder : hotlist) {
                logger.trace("3");
                List<URI> list = hotfolder.getCurrentFiles();
                logger.trace("4");
                long size = list.size();
                logger.trace("5");
                try {
                    if (size > 0) {
                        if (!hotfolder.isLocked()) {
                            logger.trace("6");
                            Thread.sleep(10000);
                            logger.trace("7");
                            list = hotfolder.getCurrentFiles();
                            logger.trace("8");
                            if (size == list.size()) {
                                hotfolder.lock();
                                logger.trace("9");
                                Map<String, Integer> failedData = collectFailedData(hotfolder);
                                if (!failedData.isEmpty()) {
                                    // // TODO Errorhandling
                                    renameFailedData(hotfolder, failedData);
                                }
                                hotfolder.unlock();
                            }
                        } else {
                            logger.trace("15");
                            return;
                        }
                        logger.trace("16");
                    }

                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    logger.trace("17");
                    Thread.currentThread().interrupt();
                } catch (IOException | DAOException | RuntimeException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }
    }

    private Map<String, Integer> collectFailedData(GoobiHotfolder hotFolder)
            throws DAOException, IOException {
        Map<String, Integer> failedData = new HashMap<>();
        Template template = serviceManager.getTemplateService().getById(hotFolder.getTemplate());
        List<URI> metsFiles = hotFolder.getFileNamesByFilter(GoobiHotfolder.filter);

        for (URI fileName : metsFiles) {
            logger.debug("found file: {}", fileName);
            logger.trace("10");

            int returnValue = generateProcess(fileName.getRawPath(), template, hotFolder.getFolderAsUri(),
                hotFolder.getCollection(), hotFolder.getUpdateStrategy());
            logger.trace("11");
            if (returnValue != 0) {
                logger.trace("12");
                failedData.put(fileName.getRawPath(), returnValue);
                logger.trace("13");
            } else {
                logger.debug("finished file: {}", fileName);
            }
        }
        return failedData;
    }

    private void renameFailedData(GoobiHotfolder hotFolder, Map<String, Integer> failedData) {
        logger.trace("14");
        for (Map.Entry<String, Integer> entry : failedData.entrySet()) {
            String key = entry.getKey();
            File oldFile = new File(hotFolder.getFolderAsFile(), key);
            if (oldFile.exists()) {
                File newFile = new File(oldFile.getAbsolutePath() + "_");
                oldFile.renameTo(newFile);
            }
            logger.error("error while importing file: " + key + " with error code " + entry.getValue());
        }
    }

    /**
     * Generate process.
     *
     * @param processTitle
     *            String
     * @param template
     *            Process object
     * @param dir
     *            File object
     * @param digitalCollection
     *            String
     * @param updateStrategy
     *            String
     * @return int
     */
    public static int generateProcess(String processTitle, Template template, URI dir, String digitalCollection,
            String updateStrategy) throws IOException {
        // wenn keine anchor Datei, dann Vorgang anlegen
        if (!processTitle.contains("anchor") && processTitle.endsWith("xml")) {
            if (!updateStrategy.equals("ignore")) {
                boolean test = testTitle(processTitle.substring(0, processTitle.length() - 4));
                if (!test && updateStrategy.equals("error")) {
                    URI images = fileService.createResource(dir,
                        processTitle.substring(0, processTitle.length() - 4) + File.separator);
                    List<URI> imageDir = new ArrayList<>();
                    if (fileService.isDirectory(images)) {
                        List<URI> files = fileService.getSubUris(images);
                        imageDir.addAll(files);
                        fileService.delete(images);
                    }
                    try {
                        fileService.delete(dir.resolve(File.separator + processTitle));
                    } catch (RuntimeException e) {
                        logger.error("Can not delete file " + processTitle, e);
                        return 30;
                    }
                    URI anchor = fileService.createResource(dir,
                        processTitle.substring(0, processTitle.length() - 4) + "_anchor.xml");
                    if (fileService.fileExist(anchor)) {
                        fileService.delete(anchor);
                    }
                    return 27;
                } else if (!test && updateStrategy.equals("update")) {
                    // TODO UPDATE mets data
                    URI images = fileService.createResource(dir,
                        processTitle.substring(0, processTitle.length() - 4) + File.separator);
                    List<URI> imageDir = new ArrayList<>();
                    if (fileService.isDirectory(images)) {
                        List<URI> files = fileService.getSubUris(images);
                        imageDir.addAll(files);
                        fileService.delete(images);
                    }
                    try {
                        fileService.delete(dir.resolve(File.separator + processTitle));
                    } catch (RuntimeException e) {
                        logger.error("Can not delete file " + processTitle, e);
                        return 30;
                    }
                    URI anchor = fileService.createResource(dir,
                        processTitle.substring(0, processTitle.length() - 4) + "_anchor.xml");
                    if (fileService.fileExist(anchor)) {
                        fileService.delete(anchor);
                    }
                    return 28;
                }
            }
            CopyProcess form = new CopyProcess();
            form.setTemplate(template);
            form.setMetadataFile(dir.resolve(File.separator + processTitle));
            form.prepare(template.getId());
            form.getProzessKopie().setTitle(processTitle.substring(0, processTitle.length() - 4));
            if (form.testTitle()) {
                if (digitalCollection == null) {
                    List<String> collections = new ArrayList<>();
                    form.setDigitalCollections(collections);
                } else {
                    List<String> col = new ArrayList<>();
                    col.add(digitalCollection);
                    form.setDigitalCollections(col);
                }
                form.evaluateOpac();

                try {
                    Process p = form.neuenProzessAnlegen();
                    if (Objects.nonNull(p) && Objects.nonNull(p.getId())) {
                        // copy image files to new directory
                        URI images = fileService.createResource(dir,
                            processTitle.substring(0, processTitle.length() - 4) + File.separator);
                        List<URI> imageDir = new ArrayList<>();
                        if (fileService.isDirectory(images)) {
                            List<URI> files = fileService.getSubUris(images);
                            imageDir.addAll(files);
                            for (URI file : imageDir) {
                                URI image = fileService.createResource(images, fileService.getFileName(file));
                                URI dest = fileService.createResource(
                                    serviceManager.getProcessService().getImagesOrigDirectory(false, p),
                                    fileService.getFileName(image));
                                fileService.moveFile(image, dest);
                            }
                            fileService.delete(images);
                        }

                        // copy fulltext files
                        URI textDirectory = fileService.createDirectory(dir,
                            processTitle.substring(0, processTitle.length() - 4) + "_txt");
                        fileService.moveDirectory(textDirectory, fileService.getTxtDirectory(p));

                        // copy source files
                        URI sourceDirectory = fileService.createDirectory(dir,
                            processTitle.substring(0, processTitle.length() - 4) + "_src");
                        fileService.moveDirectory(sourceDirectory, fileService.getImportDirectory(p));

                        try {
                            fileService.delete(dir.resolve(File.separator + processTitle));
                        } catch (IOException e) {
                            logger.error("Can not delete file " + processTitle + " after importing " + p.getTitle()
                                    + " into kitodo",
                                e);
                            return 30;
                        }
                        URI anchorUri = fileService.createResource(dir,
                            processTitle.substring(0, processTitle.length() - 4) + "_anchor.xml");
                        fileService.delete(anchorUri);
                        List<Task> tasks = serviceManager.getProcessService().getById(p.getId()).getTasks();
                        runThreads(tasks);
                    }
                } catch (ReadException e) {
                    logger.error(e.getMessage(), e);
                    return 20;
                } catch (PreferencesException e) {
                    logger.error(e.getMessage(), e);
                    return 21;
                } catch (DAOException e) {
                    logger.error(e.getMessage(), e);
                    return 22;
                } catch (WriteException e) {
                    logger.error(e.getMessage(), e);
                    return 23;
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    return 24;
                }
            }
            // TODO updateImagePath aufrufen

            return 0;
        } else {
            return 26;
        }
    }

    private static void runThreads(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getProcessingStatus() == 1 && task.isTypeAutomatic()) {
                TaskScriptThread thread = new TaskScriptThread(task);
                thread.start();
            }
        }
    }

    /**
     * Generate process.
     *
     * @param io
     *            ImportObject
     * @param template
     *            Process object
     * @return Process object
     */
    @SuppressWarnings("static-access")
    public static Process generateProcess(ImportObject io, Template template) throws IOException {
        String processTitle = io.getProcessTitle();
        logger.trace("processtitle is {}", processTitle);
        URI metsfilename = io.getMetsFilename();
        logger.trace("mets filename is {}", metsfilename);
        URI basepath = URI.create(metsfilename.toString().substring(0, metsfilename.toString().length() - 4));
        logger.trace("basepath is {}", basepath);
        Process p = null;
        if (!testTitle(processTitle)) {
            logger.trace("wrong title");
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
                fileService.delete(metsfilename);
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
                JobCreation.moveFiles(metsfilename, basepath, p);

            } catch (ReadException | PreferencesException | WriteException | IOException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.trace("title is invalid");
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
    public static boolean testTitle(String title) {
        if (title != null) {
            long amount;
            try {
                amount = serviceManager.getProcessService().findNumberOfProcessesWithTitle(title);
            } catch (DataException e) {
                return false;
            }
            if (amount > 0) {
                Helper.setFehlerMeldung("processTitleAllreadyInUse");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
