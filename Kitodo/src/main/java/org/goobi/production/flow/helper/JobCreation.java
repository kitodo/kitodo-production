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
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.io.SafeFile;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.importer.ImportObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.persistence.apache.ProcessManager;
import org.kitodo.data.database.persistence.apache.StepManager;
import org.kitodo.data.database.persistence.apache.StepObject;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.services.ServiceManager;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class JobCreation {
    private static final Logger logger = Logger.getLogger(JobCreation.class);
    private static final ServiceManager serviceManager = new ServiceManager();

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
            logger.error("cannot create process, process title \"" + processTitle + "\" is already in use");
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
                if (p != null && p.getId() != null) {
                    moveFiles(metsfile, basepath, p);
                    List<StepObject> steps = StepManager.getStepsForProcess(p.getId());
                    for (StepObject s : steps) {
                        if (s.getProcessingStatus() == 1 && s.isTypeAutomatic()) {
                            ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                            myThread.start();
                        }
                    }
                }
            } catch (ReadException e) {
                Helper.setFehlerMeldung("Cannot read file " + processTitle, e);
                logger.error(e);
            } catch (PreferencesException e) {
                Helper.setFehlerMeldung("Cannot read file " + processTitle, e);
                logger.error(e);
            } catch (SwapException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Cannot save process " + processTitle, e);
                logger.error(e);
            } catch (WriteException e) {
                Helper.setFehlerMeldung("Cannot write file " + processTitle, e);
                logger.error(e);
            } catch (IOException e) {
                Helper.setFehlerMeldung("Cannot write file " + processTitle, e);
                logger.error(e);
            } catch (InterruptedException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (ResponseException e) {
                Helper.setFehlerMeldung("ElasticSearch server response incorrect", e);
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
     * @param titel
     *            String
     * @return boolean
     */
    public static boolean testTitle(String titel) {
        if (titel != null) {
            int anzahl = 0;
            anzahl = ProcessManager.getNumberOfProcessesWithTitle(titel);
            if (anzahl > 0) {
                Helper.setFehlerMeldung("processTitleAllreadyInUse");
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
     *            SafeFile object
     * @param basepath
     *            String
     * @param p
     *            Process object
     */
    @SuppressWarnings("static-access")
    public static void moveFiles(SafeFile metsfile, String basepath, Process p)
            throws SwapException, DAOException, IOException, InterruptedException {
        if (ConfigCore.getBooleanParameter("importUseOldConfiguration", false)) {
            SafeFile imagesFolder = new SafeFile(basepath);
            if (!imagesFolder.exists()) {
                imagesFolder = new SafeFile(basepath + "_" + p.DIRECTORY_SUFFIX);
            }
            if (imagesFolder.isDirectory()) {
                List<String> imageDir = new ArrayList<String>();

                String[] files = imagesFolder.list();
                for (int i = 0; i < files.length; i++) {
                    imageDir.add(files[i]);
                }
                for (String file : imageDir) {
                    SafeFile image = new SafeFile(imagesFolder, file);
                    SafeFile dest = new SafeFile(
                            serviceManager.getProcessService().getImagesOrigDirectory(false, p) + image.getName());
                    image.moveFile(dest);
                }
                imagesFolder.deleteDirectory();
            }

            // copy pdf files
            SafeFile pdfs = new SafeFile(basepath + "_pdf" + File.separator);
            if (pdfs.isDirectory()) {
                pdfs.moveDirectory(serviceManager.getProcessService().getPdfDirectory(p));
            }

            // copy fulltext files

            SafeFile fulltext = new SafeFile(basepath + "_txt");

            if (fulltext.isDirectory()) {

                fulltext.moveDirectory(serviceManager.getProcessService().getTxtDirectory(p));
            }

            // copy source files

            SafeFile sourceDir = new SafeFile(basepath + "_src" + File.separator);
            if (sourceDir.isDirectory()) {
                sourceDir.moveDirectory(serviceManager.getProcessService().getImportDirectory(p));
            }

            try {
                metsfile.forceDelete();
            } catch (Exception e) {
                logger.error("Can not delete file " + metsfile.getName() + " after importing " + p.getTitle()
                        + " into goobi", e);

            }
            SafeFile anchor = new SafeFile(basepath + "_anchor.xml");
            if (anchor.exists()) {
                anchor.deleteQuietly();
            }
        } else {
            // new folder structure for process imports
            SafeFile importFolder = new SafeFile(basepath);
            if (importFolder.isDirectory()) {
                SafeFile[] folderList = importFolder.listFiles();
                for (SafeFile directory : folderList) {
                    if (directory.getName().contains("images")) {
                        SafeFile[] imageList = directory.listFiles();
                        for (SafeFile imagedir : imageList) {
                            if (imagedir.isDirectory()) {
                                for (SafeFile file : imagedir.listFiles()) {
                                    file.moveFile(new SafeFile(serviceManager.getProcessService().getImagesDirectory(p)
                                            + imagedir.getName(), file.getName()));
                                }
                            } else {
                                imagedir.moveFile(new SafeFile(serviceManager.getProcessService().getImagesDirectory(p),
                                        imagedir.getName()));
                            }
                        }
                    } else if (directory.getName().contains("ocr")) {
                        SafeFile ocr = new SafeFile(serviceManager.getProcessService().getOcrDirectory(p));
                        if (!ocr.exists()) {
                            ocr.mkdir();
                        }
                        SafeFile[] ocrList = directory.listFiles();
                        for (SafeFile ocrdir : ocrList) {
                            if (ocrdir.isDirectory()) {
                                ocrdir.moveDirectory(new SafeFile(ocr, ocrdir.getName()));
                            } else {
                                ocrdir.moveFile(new SafeFile(ocr, ocrdir.getName()));
                            }
                        }
                    } else {
                        SafeFile i = new SafeFile(serviceManager.getProcessService().getImportDirectory(p));
                        if (!i.exists()) {
                            i.mkdir();
                        }
                        SafeFile[] importList = directory.listFiles();
                        for (SafeFile importdir : importList) {
                            if (importdir.isDirectory()) {
                                importdir.moveDirectory(new SafeFile(i, importdir.getName()));
                            } else {
                                importdir.moveFile(new SafeFile(i, importdir.getName()));
                            }
                        }
                    }
                }
                importFolder.deleteDirectory();

                metsfile.deleteQuietly();
            }

        }
    }
}
