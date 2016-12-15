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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.io.SafeFile;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.importer.ImportObject;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.apache.ProcessManager;
import de.sub.goobi.persistence.apache.StepManager;
import de.sub.goobi.persistence.apache.StepObject;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class JobCreation {
    private static final Logger logger = Logger.getLogger(JobCreation.class);

    @SuppressWarnings("static-access")
    public static Prozess generateProcess(ImportObject io, Prozess vorlage) {
        String processTitle = io.getProcessTitle();
        if(logger.isTraceEnabled()){
        	logger.trace("processtitle is " + processTitle);
        }
        String metsfilename = io.getMetsFilename();
        if(logger.isTraceEnabled()){
        	logger.trace("mets filename is " + metsfilename);
        }
        String basepath = metsfilename.substring(0, metsfilename.length() - 4);
        if(logger.isTraceEnabled()){
        	logger.trace("basepath is " + basepath);
        }
        SafeFile metsfile = new SafeFile(metsfilename);
        Prozess p = null;
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
        cp.getProzessKopie().setTitel(processTitle);
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
                        if (s.getBearbeitungsstatus() == 1 && s.isTypAutomatisch()) {
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
            }
        } else {
            logger.error("title " + processTitle + "is invalid");
        }
        return p;
    }

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

    @SuppressWarnings("static-access")
    public static void moveFiles(SafeFile metsfile, String basepath, Prozess p) throws SwapException, DAOException, IOException, InterruptedException {
        if (ConfigMain.getBooleanParameter("importUseOldConfiguration", false)) {
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
                    SafeFile dest = new SafeFile(p.getImagesOrigDirectory(false) + image.getName());
                    image.moveFile(dest);
                }
                imagesFolder.deleteDirectory();
            }

            // copy pdf files
            SafeFile pdfs = new SafeFile(basepath + "_pdf" + File.separator);
            if (pdfs.isDirectory()) {
            	pdfs.moveDirectory(p.getPdfDirectory());
            }

            // copy fulltext files

            SafeFile fulltext = new SafeFile(basepath + "_txt");

            if (fulltext.isDirectory()) {

            	fulltext.moveDirectory(p.getTxtDirectory());
            }

            // copy source files

            SafeFile sourceDir = new SafeFile(basepath + "_src" + File.separator);
            if (sourceDir.isDirectory()) {
            	sourceDir.moveDirectory(p.getImportDirectory());
            }

            try {
            	metsfile.forceDelete();
            } catch (Exception e) {
                logger.error("Can not delete file " + metsfile.getName() + " after importing " + p.getTitel() + " into goobi", e);

            }
            SafeFile anchor = new SafeFile(basepath + "_anchor.xml");
            if (anchor.exists()) {
            	anchor.deleteQuietly();
            }
        }

        else {
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
                                	file.moveFile(new SafeFile(p.getImagesDirectory() + imagedir.getName(), file.getName()));
                                }
                            } else {
                            	imagedir.moveFile(new SafeFile(p.getImagesDirectory(), imagedir.getName()));
                            }
                        }
                    } else if (directory.getName().contains("ocr")) {
                        SafeFile ocr = new SafeFile(p.getOcrDirectory());
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
                        SafeFile i = new SafeFile(p.getImportDirectory());
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
