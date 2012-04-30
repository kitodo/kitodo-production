package org.goobi.production.flow.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.goobi.production.Import.ImportObject;
import org.goobi.production.cli.helper.CopyProcess;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

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
			logger.trace("wrong title");
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
				moveFiles(metsfile, basepath, p);

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
	private static void moveFiles(File metsfile, String basepath, Prozess p) throws SwapException, DAOException, IOException, InterruptedException {

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
				File dest = new File(p.getImagesOrigDirectory() + image.getName());
				FileUtils.moveFile(image, dest);
			}
			deleteDirectory(imagesFolder);
		}

		// copy fulltext files

		File fulltext = new File(basepath + "_txt");

		if (fulltext.isDirectory()) {

			FileUtils.moveDirectory(fulltext, new File(p.getTxtDirectory()));
		}

		// copy source files

		File sourceDir = new File(basepath + "_src" + File.separator);
		if (sourceDir.isDirectory()) {
			FileUtils.moveDirectory(sourceDir, new File(p.getSourceDirectory()));
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
	
	private static void deleteDirectory(File directory) {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
