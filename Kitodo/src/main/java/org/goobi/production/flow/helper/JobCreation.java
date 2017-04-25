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
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.importer.ImportObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.persistence.apache.ProcessManager;
import org.kitodo.data.database.persistence.apache.StepManager;
import org.kitodo.data.database.persistence.apache.StepObject;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class JobCreation {
    private static final Logger logger = Logger.getLogger(JobCreation.class);
    private static final ServiceManager serviceManager = new ServiceManager();

    private static final FileService fileService = new FileService();

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
    public static Process generateProcess(ImportObject io, Process vorlage) throws IOException {
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
        File metsfile = new File(metsfilename);
        Process p = null;
        if (!testTitle(processTitle)) {
            logger.error("cannot create process, process title \"" + processTitle + "\" is already in use");
            // removing all data
            File imagesFolder = new File(basepath);
            if (imagesFolder.isDirectory()) {
                fileService.delete(imagesFolder.toURI());
            } else {
                imagesFolder = new File(basepath + "_" + vorlage.DIRECTORY_SUFFIX);
                if (imagesFolder.isDirectory()) {
                    fileService.delete(imagesFolder.toURI());
                }
            }
            try {
                fileService.delete(metsfile.toURI());
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
        cp.metadataFile = metsfilename;
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
            } catch (CustomResponseException e) {
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
     *            File object
     * @param basepath
     *            String
     * @param p
     *            Process object
     */
    @SuppressWarnings("static-access")
    public static void moveFiles(File metsfile, String basepath, Process p)
            throws SwapException, DAOException, IOException, InterruptedException {
        if (ConfigCore.getBooleanParameter("importUseOldConfiguration", false)) {
            File imagesFolder = new File(basepath);
            if (!imagesFolder.exists()) {
                imagesFolder = new File(basepath + "_" + p.DIRECTORY_SUFFIX);
            }
            if (imagesFolder.isDirectory()) {
                List<String> imageDir = new ArrayList<String>();

                String[] files = imagesFolder.list();
                for (int i = 0; i < files.length; i++) {
                    imageDir.add(files[i]);
                }
                for (String file : imageDir) {
                    File image = new File(imagesFolder, file);
                    File dest = new File(
                            serviceManager.getProcessService().getImagesOrigDirectory(false, p) + image.getName());
                    fileService.moveFile(image, dest);
                }
                fileService.delete(imagesFolder.toURI());
            }

            // copy pdf files
            File pdfs = new File(basepath + "_pdf" + File.separator);
            if (pdfs.isDirectory()) {
                fileService.moveDirectory(pdfs, new File(serviceManager.getProcessService().getPdfDirectory(p)));
            }

            // copy fulltext files

            File fulltext = new File(basepath + "_txt");

            if (fulltext.isDirectory()) {

                fileService.moveDirectory(fulltext, new File(serviceManager.getProcessService().getTxtDirectory(p)));
            }

            // copy source files

            File sourceDir = new File(basepath + "_src" + File.separator);
            if (sourceDir.isDirectory()) {
                fileService.moveDirectory(sourceDir,
                        new File((serviceManager.getProcessService().getImportDirectory(p))));
            }

            try {
                fileService.delete(metsfile.toURI());
            } catch (Exception e) {
                logger.error("Can not delete file " + metsfile.getName() + " after importing " + p.getTitle()
                        + " into kitodo", e);

            }
            File anchor = new File(basepath + "_anchor.xml");
            if (anchor.exists()) {
                fileService.delete(anchor.toURI());
            }
        } else {
            // new folder structure for process imports
            File importFolder = new File(basepath);
            if (importFolder.isDirectory()) {
                File[] folderList = importFolder.listFiles();
                for (File directory : folderList) {
                    if (directory.getName().contains("images")) {
                        File[] imageList = directory.listFiles();
                        for (File imagedir : imageList) {
                            if (imagedir.isDirectory()) {
                                for (File file : imagedir.listFiles()) {
                                    fileService.moveFile(file,
                                            new File(serviceManager.getProcessService().getImagesDirectory(p)
                                                    + imagedir.getName(), file.getName()));
                                }
                            } else {
                                fileService.moveFile(imagedir, new File(
                                        serviceManager.getProcessService().getImagesDirectory(p), imagedir.getName()));
                            }
                        }
                    } else if (directory.getName().contains("ocr")) {
                        File ocr = new File(serviceManager.getProcessService().getOcrDirectory(p));
                        if (!ocr.exists()) {
                            ocr.mkdir();
                        }
                        File[] ocrList = directory.listFiles();
                        for (File ocrdir : ocrList) {
                            if (ocrdir.isDirectory()) {
                                fileService.moveDirectory(ocrdir, new File(ocr, ocrdir.getName()));
                            } else {
                                fileService.moveDirectory(ocrdir, new File(ocr, ocrdir.getName()));
                            }
                        }
                    } else {
                        File i = new File(serviceManager.getProcessService().getImportDirectory(p));
                        if (!i.exists()) {
                            i.mkdir();
                        }
                        File[] importList = directory.listFiles();
                        for (File importdir : importList) {
                            if (importdir.isDirectory()) {
                                fileService.moveDirectory(importdir, new File(i, importdir.getName()));
                            } else {
                                fileService.moveDirectory(importdir, new File(i, importdir.getName()));
                            }
                        }
                    }
                }
                fileService.delete(importFolder.toURI());

                fileService.delete(metsfile.toURI());
            }

        }
    }
}
