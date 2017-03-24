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

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.io.SafeFile;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.GoobiHotfolder;
import org.goobi.production.importer.ImportObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.persistence.apache.StepManager;
import org.kitodo.data.database.persistence.apache.StepObject;
import org.kitodo.services.ServiceManager;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

/**
 * HotfolderJob class.
 *
 * @author Robert Sehr
 */
@Deprecated
public class HotfolderJob extends AbstractGoobiJob {
    private static final Logger logger = Logger.getLogger(HotfolderJob.class);

    private static final ServiceManager serviceManager = new ServiceManager();

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
        // logger.error("TEST123");
        if (ConfigMain.getBooleanParameter("runHotfolder", false)) {
            logger.trace("1");
            List<GoobiHotfolder> hotlist = GoobiHotfolder.getInstances();
            logger.trace("2");
            for (GoobiHotfolder hotfolder : hotlist) {
                logger.trace("3");
                List<SafeFile> list = SafeFile.createAll(hotfolder.getCurrentFiles());
                logger.trace("4");
                long size = getSize(list);
                logger.trace("5");
                try {
                    if (size > 0) {
                        if (!hotfolder.isLocked()) {

                            logger.trace("6");
                            Thread.sleep(10000);
                            logger.trace("7");
                            list = SafeFile.createAll(hotfolder.getCurrentFiles());
                            logger.trace("8");
                            if (size == getSize(list)) {
                                hotfolder.lock();
                                logger.trace("9");
                                Process template = serviceManager.getProcessService().find(hotfolder.getTemplate());
                                serviceManager.getProcessService().refresh(template);
                                logger.trace("10");
                                List<String> metsfiles = hotfolder.getFileNamesByFilter(GoobiHotfolder.filter);
                                logger.trace("11");
                                HashMap<String, Integer> failedData = new HashMap<String, Integer>();
                                logger.trace("12");

                                for (String filename : metsfiles) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("found file: " + filename);
                                    }
                                    logger.trace("13");

                                    int returnValue = generateProcess(filename, template,
                                            new SafeFile(hotfolder.getFolderAsFile()), hotfolder.getCollection(),
                                            hotfolder.getUpdateStrategy());
                                    logger.trace("14");
                                    if (returnValue != 0) {
                                        logger.trace("15");
                                        failedData.put(filename, returnValue);
                                        logger.trace("16");
                                    } else {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("finished file: " + filename);
                                        }
                                    }
                                }
                                if (!failedData.isEmpty()) {
                                    // // TODO Errorhandling
                                    logger.trace("17");
                                    for (String filename : failedData.keySet()) {
                                        SafeFile oldFile = new SafeFile(hotfolder.getFolderAsFile(), filename);
                                        if (oldFile.exists()) {
                                            SafeFile newFile = new SafeFile(oldFile.getAbsolutePath() + "_");
                                            oldFile.renameTo(newFile);
                                        }
                                        logger.error("error while importing file: " + filename + " with error code "
                                                + failedData.get(filename));
                                    }
                                }
                                hotfolder.unlock();
                            }
                        } else {
                            logger.trace("18");
                            return;
                        }
                        logger.trace("19");
                    }

                } catch (InterruptedException e) {
                    logger.error(e);
                    logger.trace("20");
                } catch (DAOException e) {
                    logger.error(e);
                    logger.trace("21");
                } catch (Exception e) {
                    logger.error(e);
                }
            }

        }
    }

    private long getSize(List<SafeFile> list) {
        long size = 0;
        for (SafeFile f : list) {
            if (f.isDirectory()) {
                SafeFile[] subdir = f.listFiles();
                for (SafeFile sub : subdir) {
                    size += sub.length();
                }
            } else {
                size += f.length();
            }
        }
        return size;
    }

    /**
     * Generate process.
     *
     * @param processTitle
     *            String
     * @param vorlage
     *            Process object
     * @param dir
     *            SafeFile object
     * @param digitalCollection
     *            String
     * @param updateStrategy
     *            String
     * @return int
     */
    public static int generateProcess(String processTitle, Process vorlage, SafeFile dir, String digitalCollection,
            String updateStrategy) {
        // wenn keine anchor Datei, dann Vorgang anlegen
        if (!processTitle.contains("anchor") && processTitle.endsWith("xml")) {
            if (!updateStrategy.equals("ignore")) {
                boolean test = testTitle(processTitle.substring(0, processTitle.length() - 4));
                if (!test && updateStrategy.equals("error")) {
                    SafeFile images = new SafeFile(dir.getAbsoluteFile() + File.separator
                            + processTitle.substring(0, processTitle.length() - 4) + File.separator);
                    List<String> imageDir = new ArrayList<String>();
                    if (images.isDirectory()) {
                        String[] files = images.list();
                        for (int i = 0; i < files.length; i++) {
                            imageDir.add(files[i]);
                        }
                        images.deleteQuietly();
                    }
                    try {
                        new SafeFile(dir.getAbsolutePath() + File.separator + processTitle).forceDelete();
                    } catch (Exception e) {
                        logger.error("Can not delete file " + processTitle, e);
                        return 30;
                    }
                    SafeFile anchor = new SafeFile(dir.getAbsolutePath() + File.separator
                            + processTitle.substring(0, processTitle.length() - 4) + "_anchor.xml");
                    if (anchor.exists()) {
                        anchor.deleteQuietly();
                    }
                    return 27;
                } else if (!test && updateStrategy.equals("update")) {
                    // TODO UPDATE mets data
                    SafeFile images = new SafeFile(dir.getAbsoluteFile() + File.separator
                            + processTitle.substring(0, processTitle.length() - 4) + File.separator);
                    List<String> imageDir = new ArrayList<String>();
                    if (images.isDirectory()) {
                        String[] files = images.list();
                        for (int i = 0; i < files.length; i++) {
                            imageDir.add(files[i]);
                        }
                        images.deleteQuietly();
                    }
                    try {
                        new SafeFile(dir.getAbsolutePath() + File.separator + processTitle).forceDelete();
                    } catch (Exception e) {
                        logger.error("Can not delete file " + processTitle, e);
                        return 30;
                    }
                    SafeFile anchor = new SafeFile(dir.getAbsolutePath() + File.separator
                            + processTitle.substring(0, processTitle.length() - 4) + "_anchor.xml");
                    if (anchor.exists()) {
                        anchor.deleteQuietly();
                    }
                    return 28;
                }
            }
            CopyProcess form = new CopyProcess();
            form.setProzessVorlage(vorlage);
            form.metadataFile = dir.getAbsolutePath() + File.separator + processTitle;
            form.prepare();
            form.getProzessKopie().setTitle(processTitle.substring(0, processTitle.length() - 4));
            if (form.testTitle()) {
                if (digitalCollection == null) {
                    List<String> collections = new ArrayList<String>();
                    form.setDigitalCollections(collections);
                } else {
                    List<String> col = new ArrayList<String>();
                    col.add(digitalCollection);
                    form.setDigitalCollections(col);
                }
                form.OpacAuswerten();

                try {
                    Process p = form.NeuenProzessAnlegen2();
                    if (p.getId() != null) {

                        // copy image files to new directory
                        SafeFile images = new SafeFile(dir.getAbsoluteFile() + File.separator
                                + processTitle.substring(0, processTitle.length() - 4) + File.separator);
                        List<String> imageDir = new ArrayList<String>();
                        if (images.isDirectory()) {
                            String[] files = images.list();
                            for (int i = 0; i < files.length; i++) {
                                imageDir.add(files[i]);
                            }
                            for (String file : imageDir) {
                                SafeFile image = new SafeFile(images, file);
                                SafeFile dest = new SafeFile(
                                        serviceManager.getProcessService().getImagesOrigDirectory(false, p)
                                                + image.getName());
                                image.moveFile(dest);
                            }
                            images.deleteDirectory();
                        }

                        // copy fulltext files

                        SafeFile fulltext = new SafeFile(dir.getAbsoluteFile() + File.separator
                                + processTitle.substring(0, processTitle.length() - 4) + "_txt" + File.separator);
                        if (fulltext.isDirectory()) {

                            fulltext.moveDirectory(serviceManager.getProcessService().getTxtDirectory(p));
                        }

                        // copy source files

                        SafeFile sourceDir = new SafeFile(dir.getAbsoluteFile() + File.separator
                                + processTitle.substring(0, processTitle.length() - 4) + "_src" + File.separator);
                        if (sourceDir.isDirectory()) {
                            sourceDir.moveDirectory(serviceManager.getProcessService().getImportDirectory(p));
                        }

                        try {
                            new SafeFile(dir.getAbsolutePath() + File.separator + processTitle).forceDelete();
                        } catch (Exception e) {
                            logger.error("Can not delete file " + processTitle + " after importing " + p.getTitle()
                                    + " into goobi", e);
                            return 30;
                        }
                        SafeFile anchor = new SafeFile(dir.getAbsolutePath() + File.separator
                                + processTitle.substring(0, processTitle.length() - 4) + "_anchor.xml");
                        if (anchor.exists()) {
                            anchor.deleteQuietly();
                        }
                        List<StepObject> steps = StepManager.getStepsForProcess(p.getId());
                        for (StepObject s : steps) {
                            if (s.getProcessingStatus() == 1 && s.isTypeAutomatic()) {
                                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                                myThread.start();
                            }
                        }
                    }
                } catch (ReadException e) {
                    logger.error(e);
                    return 20;
                } catch (PreferencesException e) {
                    logger.error(e);
                    return 21;
                } catch (SwapException e) {
                    logger.error(e);
                    return 22;
                } catch (DAOException e) {
                    logger.error(e);
                    return 22;
                } catch (WriteException e) {
                    logger.error(e);
                    return 23;
                } catch (IOException e) {
                    logger.error(e);
                    return 24;
                } catch (InterruptedException e) {
                    logger.error(e);
                    return 25;
                }
            }
            // TODO updateImagePath aufrufen

            return 0;
        } else {
            return 26;
        }
    }

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
    public static Process generateProcess(ImportObject io, Process vorlage) {
        String processTitle = io.getProcessTitle();
        if (logger.isTraceEnabled()) {
            logger.trace("processtitle is " + processTitle);
        }
        String metsfilename = io.getMetsFilename();
        if (logger.isTraceEnabled()) {
            logger.trace("mets filename is " + metsfilename);
        }
        String basepath = metsfilename.substring(0, metsfilename.length() - 4);
        if (logger.isTraceEnabled()) {
            logger.trace("basepath is " + basepath);
        }
        SafeFile metsfile = new SafeFile(metsfilename);
        Process p = null;
        if (!testTitle(processTitle)) {
            logger.trace("wrong title");
            // removing all data
            SafeFile imagesFolder = new SafeFile(basepath);
            if (imagesFolder.isDirectory()) {
                imagesFolder.deleteQuietly();
            } else {
                imagesFolder = new SafeFile(basepath + "_" + vorlage.DIRECTORY_SUFFIX);
                if (imagesFolder.isDirectory()) {
                    imagesFolder.deleteQuietly();
                }
            }
            try {
                metsfile.forceDelete();
            } catch (Exception e) {
                logger.error("Can not delete file " + processTitle, e);
                return null;
            }
            SafeFile anchor = new SafeFile(basepath + "_anchor.xml");
            if (anchor.exists()) {
                anchor.deleteQuietly();
            }
            return null;
        }

        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(vorlage);
        cp.metadataFile = metsfilename;
        cp.prepare(io);
        cp.getProzessKopie().setTitle(processTitle);
        logger.trace("testing title");
        if (cp.testTitle()) {
            logger.trace("title is valid");
            cp.OpacAuswerten();
            try {
                p = cp.createProcess(io);
                JobCreation.moveFiles(metsfile, basepath, p);

            } catch (ReadException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (PreferencesException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (SwapException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (DAOException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (WriteException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (IOException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (InterruptedException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            }
        } else {
            logger.trace("title is invalid");
        }
        return p;

    }

    /**
     * Test title.
     *
     * @param titel
     *            String
     * @return boolean
     */
    public static boolean testTitle(String titel) {
        if (titel != null) {
            long anzahl = 0;
            try {
                anzahl = serviceManager.getProcessService().count("from Process where title='" + titel + "'");
            } catch (DAOException e) {
                return false;
            }
            if (anzahl > 0) {
                Helper.setFehlerMeldung("processTitleAllreadyInUse");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
