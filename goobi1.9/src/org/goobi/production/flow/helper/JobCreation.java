package org.goobi.production.flow.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.importer.ImportObject;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.persistence.apache.StepManager;
import de.sub.goobi.persistence.apache.StepObject;

public class JobCreation {
	private static final Logger logger = Logger.getLogger(JobCreation.class);

	@SuppressWarnings("static-access")
	public static Prozess generateProcess(ImportObject io, Prozess vorlage) {
		String processTitle = io.getProcessTitle();
		logger.trace("processtitle is " + processTitle);
		String metsfilename = io.getMetsFilename();
		logger.trace("mets filename is " + metsfilename);
		String basepath = metsfilename.substring(0, metsfilename.length() - 4);
		logger.trace("basepath is " + basepath);
		File metsfile = new File(metsfilename);
		Prozess p = null;
		if (!testTitle(processTitle)) {
			logger.error("cannot create process, process title " + processTitle + "is allready in use");
			// removing all data
			File imagesFolder = new File(basepath);
			if (imagesFolder.exists() && imagesFolder.isDirectory()) {
				deleteDirectory(imagesFolder);
			} else {
				imagesFolder = new File(basepath + "_" + vorlage.DIRECTORY_SUFFIX);
				if (imagesFolder.exists() && imagesFolder.isDirectory()) {
					deleteDirectory(imagesFolder);
				}
			}
			try {
				FileUtils.forceDelete(metsfile);
			} catch (Exception e) {
				logger.error("Can not delete file " + processTitle, e);
				return null;
			}
			File anchor = new File(basepath + "_anchor.xml");
			if (anchor.exists()) {
				FileUtils.deleteQuietly(anchor);
			}
			return null;
		}

		CopyProcess cp = new CopyProcess();
		cp.setProzessVorlage(vorlage);
		cp.metadataFile = metsfilename;
		cp.Prepare(io);
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
			long anzahl = 0;
			try {
				anzahl = new ProzessDAO().count("from Prozess where titel='" + titel + "'");
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

	@SuppressWarnings("static-access")
	public static void moveFiles(File metsfile, String basepath, Prozess p) throws SwapException, DAOException, IOException, InterruptedException {
		if (ConfigMain.getBooleanParameter("importUseOldConfiguration", false)) {
			File imagesFolder = new File(basepath);
			if (!imagesFolder.exists()) {
				imagesFolder = new File(basepath + "_" + p.DIRECTORY_SUFFIX);
			}
			if (imagesFolder.exists() && imagesFolder.isDirectory()) {
				List<String> imageDir = new ArrayList<String>();

				String[] files = imagesFolder.list();
				for (int i = 0; i < files.length; i++) {
					imageDir.add(files[i]);
				}
				for (String file : imageDir) {
					File image = new File(imagesFolder, file);
					File dest = new File(p.getImagesOrigDirectory(false) + image.getName());
					FileUtils.moveFile(image, dest);
				}
				deleteDirectory(imagesFolder);
			}

			// copy pdf files
			File pdfs = new File(basepath + "_pdf" + File.separator);
			if (pdfs.isDirectory()) {
				FileUtils.moveDirectory(pdfs, new File(p.getPdfDirectory()));
			}

			// copy fulltext files

			File fulltext = new File(basepath + "_txt");

			if (fulltext.isDirectory()) {

				FileUtils.moveDirectory(fulltext, new File(p.getTxtDirectory()));
			}

			// copy source files

			File sourceDir = new File(basepath + "_src" + File.separator);
			if (sourceDir.isDirectory()) {
				FileUtils.moveDirectory(sourceDir, new File(p.getImportDirectory()));
			}

			try {
				FileUtils.forceDelete(metsfile);
			} catch (Exception e) {
				logger.error("Can not delete file " + metsfile.getName() + " after importing " + p.getTitel() + " into goobi", e);

			}
			File anchor = new File(basepath + "_anchor.xml");
			if (anchor.exists()) {
				FileUtils.deleteQuietly(anchor);
			}
		}

		else {
			// new folder structure for process imports
			File importFolder = new File(basepath);
			if (importFolder.exists() && importFolder.isDirectory()) {
				File[] folderList = importFolder.listFiles();
				for (File directory : folderList) {
					if (directory.getName().contains("images")) {
						File[] imageList = directory.listFiles();
						for (File imagedir : imageList) {
							if (imagedir.isDirectory()) {
								for (File file : imagedir.listFiles()) {
									FileUtils.moveFile(file, new File(p.getImagesDirectory() + imagedir.getName(), file.getName()));
								}
							} else {
								FileUtils.moveFile(imagedir, new File(p.getImagesDirectory(), imagedir.getName()));
							}
						}
					} else if (directory.getName().contains("ocr")) {
						File ocr = new File(p.getOcrDirectory());
						if (!ocr.exists()) {
							ocr.mkdir();
						}
						File[] ocrList = directory.listFiles();
						for (File ocrdir : ocrList) {
							if (ocrdir.isDirectory()) {
								FileUtils.moveDirectory(ocrdir, new File(ocr, ocrdir.getName()));
							} else {
								FileUtils.moveFile(ocrdir, new File(ocr, ocrdir.getName()));
							}
						}
					} else {
						File i = new File(p.getImportDirectory());
						if (!i.exists()) {
							i.mkdir();
						}
						File[] importList = directory.listFiles();
						for (File importdir : importList) {
							if (importdir.isDirectory()) {
								FileUtils.moveDirectory(importdir, new File(i, importdir.getName()));
							} else {
								FileUtils.moveFile(importdir, new File(i, importdir.getName()));
							}
						}
					}
				}
			}
		}
	}

	private static void deleteDirectory(File directory) {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
