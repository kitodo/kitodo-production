package de.sub.goobi.export.dms;

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

import org.apache.log4j.Logger;

import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsModsImportExport;
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.tasks.ExportDmsTask;
import de.sub.goobi.helper.tasks.TaskManager;
import de.sub.goobi.helper.tasks.TaskSitter;
import de.sub.goobi.metadaten.MetadatenVerifizierung;

public class ExportDms extends ExportMets {
	private static final Logger myLogger = Logger.getLogger(ExportDms.class);
	ConfigProjects cp;
	private boolean exportWithImages = true;
	private boolean exportFulltext = true;
	private ExportDmsTask exportDmsTask = null;

	public final static String DIRECTORY_SUFFIX = "_tif";

	public ExportDms() {
	}

	public ExportDms(boolean exportImages) {
		this.exportWithImages = exportImages;
	}

	public void setExportFulltext(boolean exportFulltext) {
		this.exportFulltext = exportFulltext;
	}

	/**
	 * DMS-Export an eine gewünschte Stelle
	 * 
	 * @param myProzess
	 * @param zielVerzeichnis
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws WriteException
	 * @throws PreferencesException
	 * @throws DAOException
	 * @throws SwapException
	 * @throws TypeNotAllowedForParentException
	 */
	@Override
	public boolean startExport(Prozess myProzess, String inZielVerzeichnis)
			throws IOException, InterruptedException, WriteException,
 PreferencesException, SwapException, DAOException,
			TypeNotAllowedForParentException {

		if (ConfigMain.getBooleanParameter("asynchronousAutomaticExport", false)) {
			TaskManager.addTask(new ExportDmsTask(this, myProzess, inZielVerzeichnis));
			Helper.setMeldung(TaskSitter.isAutoRunningThreads() ? "DMSExportByThread" : "DMSExportThreadCreated",
					myProzess.getTitel());
			return true;
		} else {
			return startExport(myProzess, inZielVerzeichnis, null);
		}
	}

	/**
	 * The function startExport() performs a DMS export to a desired place. In
	 * addition, it accepts an optional ExportDmsTask object. If that is passed
	 * in, the progress in it will be updated during processing and occurring
	 * errors will be passed to it to be visible in the task manager screen.
	 * 
	 * @param myProzess
	 *            process to export
	 * @param inZielVerzeichnis
	 *            work directory of the user who triggered the export
	 * @param exportDmsTask
	 *            ExportDmsTask object to submit progress updates and errors
	 * @return false if an error condition was caught, true otherwise
	 * @throws IOException
	 *             if “goobi_projects.xml” could not be read
	 * @throws InterruptedException
	 *             if the thread running the script to create a directory is
	 *             interrupted by another thread while it is waiting
	 * @throws WriteException
	 *             if a FileNotFoundException occurs when opening the
	 *             FileOutputStream to write the METS/MODS object
	 * @throws PreferencesException
	 *             if the file format selected for DMS export in the project of
	 *             the process to export that implements
	 *             {@link ugh.dl.Fileformat#getDigitalDocument()} throws it
	 * @throws SwapException
	 *             if after swapping a process back in neither a file system
	 *             entry "images" nor "meta.xml" exists
	 * @throws DAOException
	 *             if saving the fact that a process has been swapped back in to
	 *             the database fails
	 * @throws TypeNotAllowedForParentException
	 *             declared in
	 *             {@link ugh.dl.DigitalDocument#createDocStruct(DocStructType)}
	 *             but never thrown, see
	 *             https://github.com/goobi/goobi-ugh/issues/2
	 */
	public boolean startExport(Prozess myProzess, String inZielVerzeichnis, ExportDmsTask exportDmsTask)
			throws IOException, InterruptedException, WriteException, PreferencesException,
			SwapException, DAOException, TypeNotAllowedForParentException {

		this.exportDmsTask = exportDmsTask;
		this.myPrefs = myProzess.getRegelsatz().getPreferences();
		this.cp = new ConfigProjects(myProzess.getProjekt().getTitel());
		String atsPpnBand = myProzess.getTitel();

		/*
		 * -------------------------------- Dokument einlesen
		 * --------------------------------
		 */
		Fileformat gdzfile;
		Fileformat newfile;
		try {
			gdzfile = myProzess.readMetadataFile();
			switch (MetadataFormat.findFileFormatsHelperByName(myProzess
					.getProjekt().getFileFormatDmsExport())) {
			case METS:
				newfile = new MetsModsImportExport(this.myPrefs);
				break;

			case METS_AND_RDF:
				newfile = new RDFFile(this.myPrefs);
				break;

			default:
				newfile = new RDFFile(this.myPrefs);
				break;
			}

			newfile.setDigitalDocument(gdzfile.getDigitalDocument());
			gdzfile = newfile;

		} catch (Exception e) {
			if (exportDmsTask != null) {
				exportDmsTask.setException(e);
			} else {
				Helper.setFehlerMeldung(Helper.getTranslation("exportError")
						+ myProzess.getTitel(), e);
			}
			myLogger.error("Export abgebrochen, xml-LeseFehler", e);
			return false;
		}

		trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());

		/*
		 * -------------------------------- Metadaten validieren
		 * --------------------------------
		 */

		if (ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
			MetadatenVerifizierung mv = new MetadatenVerifizierung();
			if (!mv.validate(gdzfile, this.myPrefs, myProzess)) {
				return false;
			}
		}

		/*
		 * -------------------------------- Speicherort vorbereiten und
		 * downloaden --------------------------------
		 */
		String zielVerzeichnis;
		File benutzerHome;
		if (myProzess.getProjekt().isUseDmsImport()) {
			zielVerzeichnis = myProzess.getProjekt().getDmsImportImagesPath();
			benutzerHome = new File(zielVerzeichnis);

			/* ggf. noch einen Vorgangsordner anlegen */
			if (myProzess.getProjekt().isDmsImportCreateProcessFolder()) {
				benutzerHome = new File(benutzerHome + File.separator
						+ myProzess.getTitel());
				zielVerzeichnis = benutzerHome.getAbsolutePath();
				/* alte Import-Ordner löschen */
				if (!Helper.deleteDir(benutzerHome)) {
					Helper.setFehlerMeldung("Export canceled, Process: "
							+ myProzess.getTitel(),
							"Import folder could not be cleared");
					return false;
				}
				/* alte Success-Ordner löschen */
				File successFile = new File(myProzess.getProjekt()
						.getDmsImportSuccessPath()
						+ File.separator
						+ myProzess.getTitel());
				if (!Helper.deleteDir(successFile)) {
					Helper.setFehlerMeldung("Export canceled, Process: "
							+ myProzess.getTitel(),
							"Success folder could not be cleared");
					return false;
				}
				/* alte Error-Ordner löschen */
				File errorfile = new File(myProzess.getProjekt()
						.getDmsImportErrorPath()
						+ File.separator
						+ myProzess.getTitel());
				if (!Helper.deleteDir(errorfile)) {
					Helper.setFehlerMeldung("Export canceled, Process: "
							+ myProzess.getTitel(),
							"Error folder could not be cleared");
					return false;
				}

				if (!benutzerHome.exists()) {
					benutzerHome.mkdir();
				}
			}

		} else {
			zielVerzeichnis = inZielVerzeichnis + atsPpnBand + File.separator;
			// wenn das Home existiert, erst löschen und dann neu anlegen
			benutzerHome = new File(zielVerzeichnis);
			if (!Helper.deleteDir(benutzerHome)) {
				Helper.setFehlerMeldung(
						"Export canceled: " + myProzess.getTitel(),
						"could not delete home directory");
				return false;
			}
			prepareUserDirectory(zielVerzeichnis);
		}
		if (exportDmsTask != null) {
			exportDmsTask.setProgress(1);
		}

		/*
		 * -------------------------------- der eigentliche Download der Images
		 * --------------------------------
		 */
		try {
			if (this.exportWithImages) {
				imageDownload(myProzess, benutzerHome, atsPpnBand,
						DIRECTORY_SUFFIX);
				fulltextDownload(myProzess, benutzerHome, atsPpnBand,
						DIRECTORY_SUFFIX);
			}else if (this.exportFulltext){
				fulltextDownload(myProzess, benutzerHome, atsPpnBand,
						DIRECTORY_SUFFIX);
			}
		} catch (Exception e) {
			if (exportDmsTask != null) {
				exportDmsTask.setException(e);
			} else {
				Helper.setFehlerMeldung(
						"Export canceled, Process: " + myProzess.getTitel(), e);
			}
			return false;
		}

		/*
		 * -------------------------------- zum Schluss Datei an gewünschten Ort
		 * exportieren entweder direkt in den Import-Ordner oder ins
		 * Benutzerhome anschliessend den Import-Thread starten
		 * --------------------------------
		 */
		if (myProzess.getProjekt().isUseDmsImport()) {
			if (exportDmsTask != null) {
				exportDmsTask.setWorkDetail(atsPpnBand + ".xml");
			}
			if (MetadataFormat.findFileFormatsHelperByName(myProzess
					.getProjekt().getFileFormatDmsExport()) == MetadataFormat.METS) {
				/* Wenn METS, dann per writeMetsFile schreiben... */
				writeMetsFile(myProzess, benutzerHome + File.separator
						+ atsPpnBand + ".xml", gdzfile, false);
			} else {
				/* ...wenn nicht, nur ein Fileformat schreiben. */
				gdzfile.write(benutzerHome + File.separator + atsPpnBand
						+ ".xml");
			}

			/* ggf. sollen im Export mets und rdf geschrieben werden */
			if (MetadataFormat.findFileFormatsHelperByName(myProzess
					.getProjekt().getFileFormatDmsExport()) == MetadataFormat.METS_AND_RDF) {
				writeMetsFile(myProzess, benutzerHome + File.separator
						+ atsPpnBand + ".mets.xml", gdzfile, false);
			}

			Helper.setMeldung(null, myProzess.getTitel() + ": ",
					"DMS-Export started");
			DmsImportThread agoraThread = new DmsImportThread(myProzess,
					atsPpnBand);
			agoraThread.start();
			if (!ConfigMain.getBooleanParameter("exportWithoutTimeLimit")) {
				try {
					/* 30 Sekunden auf den Thread warten, evtl. killen */
					agoraThread.join(myProzess.getProjekt()
							.getDmsImportTimeOut().longValue());
					if (agoraThread.isAlive()) {
						agoraThread.stopThread();
					}
				} catch (InterruptedException e) {
					if (exportDmsTask != null) {
						exportDmsTask.setException(e);
					} else {
						Helper.setFehlerMeldung(myProzess.getTitel()
								+ ": error on export - ", e.getMessage());
					}
					myLogger.error(myProzess.getTitel() + ": error on export",
							e);
				}
				if (agoraThread.rueckgabe.length() > 0) {
					if (exportDmsTask != null) {
						exportDmsTask.setException(new RuntimeException(myProzess.getTitel() + ": "
								+ agoraThread.rueckgabe));
					} else {
						Helper.setFehlerMeldung(myProzess.getTitel() + ": ",
								agoraThread.rueckgabe);
					}
				} else {
					if (exportDmsTask != null) {
						exportDmsTask.setProgress(100);
					} else {
						Helper.setMeldung(null, myProzess.getTitel() + ": ",
								"ExportFinished");
					}
					/* Success-Ordner wieder löschen */
					if (myProzess.getProjekt().isDmsImportCreateProcessFolder()) {
						File successFile = new File(myProzess.getProjekt()
								.getDmsImportSuccessPath()
								+ File.separator
								+ myProzess.getTitel());
						Helper.deleteDir(successFile);
					}
				}
			}
			if (exportDmsTask != null) {
				exportDmsTask.setProgress(100);
			}
		} else {
			/* ohne Agora-Import die xml-Datei direkt ins Home schreiben */
			if (MetadataFormat.findFileFormatsHelperByName(myProzess
					.getProjekt().getFileFormatDmsExport()) == MetadataFormat.METS) {
				writeMetsFile(myProzess, zielVerzeichnis + atsPpnBand + ".xml",
						gdzfile, false);
			} else {
				gdzfile.write(zielVerzeichnis + atsPpnBand + ".xml");
			}

			Helper.setMeldung(null, myProzess.getTitel() + ": ",
					"ExportFinished");
		}
		return true;
	}

	/**
	 * run through all metadata and children of given docstruct to trim the
	 * strings calls itself recursively
	 */
	private void trimAllMetadata(DocStruct inStruct) {
		/* trimm all metadata values */
		if (inStruct.getAllMetadata() != null) {
			for (Metadata md : inStruct.getAllMetadata()) {
				if (md.getValue() != null) {
					md.setValue(md.getValue().trim());
				}
			}
		}

		/* run through all children of docstruct */
		if (inStruct.getAllChildren() != null) {
			for (DocStruct child : inStruct.getAllChildren()) {
				trimAllMetadata(child);
			}
		}
	}

	public void fulltextDownload(Prozess myProzess, File benutzerHome,
			String atsPpnBand, final String ordnerEndung) throws IOException,
			InterruptedException, SwapException, DAOException {
		
		// download sources
		File sources = new File(myProzess.getSourceDirectory());
		if (sources.exists() && sources.list().length > 0) {
			File destination = new File(benutzerHome + File.separator
					+ atsPpnBand + "_src");
			if (!destination.exists()) {
				destination.mkdir();
			}
			File[] dateien = sources.listFiles();
			for (int i = 0; i < dateien.length; i++) {
				File meinZiel = new File(destination + File.separator
						+ dateien[i].getName());
				Helper.copyFile(dateien[i], meinZiel);
			}
		}
		
		File ocr = new File(myProzess.getOcrDirectory());
		if (ocr.exists()) {
			File[] folder = ocr.listFiles();
			for (File dir : folder) {
				if (dir.isDirectory() && dir.list().length > 0) {
					String suffix = dir.getName().substring(dir.getName().lastIndexOf("_"));
					File destination = new File(benutzerHome + File.separator + atsPpnBand + suffix);
					if (!destination.exists()) {
						destination.mkdir();
					}
					File[] files = dir.listFiles();
					for (int i = 0; i < files.length; i++) {
						File target = new File(destination + File.separator + files[i].getName());
						Helper.copyFile(files[i], target);
					}
				}
			}
		}
	}

	public void imageDownload(Prozess myProzess, File benutzerHome,
			String atsPpnBand, final String ordnerEndung) throws IOException,
			InterruptedException, SwapException, DAOException {

		/*
		 * -------------------------------- dann den Ausgangspfad ermitteln
		 * --------------------------------
		 */
		File tifOrdner = new File(myProzess.getImagesTifDirectory(true));

		/*
		 * -------------------------------- jetzt die Ausgangsordner in die
		 * Zielordner kopieren --------------------------------
		 */
		if (tifOrdner.exists() && tifOrdner.list().length > 0) {
			File zielTif = new File(benutzerHome + File.separator + atsPpnBand
					+ ordnerEndung);

			/* bei Agora-Import einfach den Ordner anlegen */
			if (myProzess.getProjekt().isUseDmsImport()) {
				if (!zielTif.exists()) {
					zielTif.mkdir();
				}
			} else {
				/*
				 * wenn kein Agora-Import, dann den Ordner mit
				 * Benutzerberechtigung neu anlegen
				 */
				Benutzer myBenutzer = (Benutzer) Helper
						.getManagedBeanValue("#{LoginForm.myBenutzer}");
				try {
                    	FilesystemHelper.createDirectoryForUser(zielTif.getAbsolutePath(), myBenutzer.getLogin());
                    } catch (Exception e) {
					if (exportDmsTask != null) {
							exportDmsTask.setException(e);
						} else {
							Helper.setFehlerMeldung("Export canceled, error",
									"could not create destination directory");
						}
					myLogger.error("could not create destination directory", e);
				}
			}

			/* jetzt den eigentlichen Kopiervorgang */

			File[] dateien = tifOrdner.listFiles(Helper.dataFilter);
			for (int i = 0; i < dateien.length; i++) {
				if (exportDmsTask != null) {
					exportDmsTask.setWorkDetail(dateien[i].getName());
				}
				File meinZiel = new File(zielTif + File.separator
						+ dateien[i].getName());
				Helper.copyFile(dateien[i], meinZiel);
				if (exportDmsTask != null) {
					exportDmsTask.setProgress((int) ((i + 1) * 98d / dateien.length + 1));
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
}
