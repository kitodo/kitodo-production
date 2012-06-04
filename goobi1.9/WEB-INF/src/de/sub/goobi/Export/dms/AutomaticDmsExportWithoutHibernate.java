package de.sub.goobi.Export.dms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
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
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsModsImportExport;
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Export.download.ExportMetsWithoutHibernate;
import de.sub.goobi.Metadaten.MetadatenVerifizierungWithoutHibernate;
import de.sub.goobi.Persistence.apache.FolderInformation;
import de.sub.goobi.Persistence.apache.ProcessManager;
import de.sub.goobi.Persistence.apache.ProcessObject;
import de.sub.goobi.Persistence.apache.ProjectManager;
import de.sub.goobi.Persistence.apache.ProjectObject;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class AutomaticDmsExportWithoutHibernate extends ExportMetsWithoutHibernate {
	private static final Logger myLogger = Logger.getLogger(AutomaticDmsExportWithoutHibernate.class);
	ConfigProjects cp;
	private boolean exportWithImages = true;
	private boolean exportFulltext = true;
	private FolderInformation fi;
	private ProjectObject project;
	
	public final static String DIRECTORY_SUFFIX = "_tif";

	public AutomaticDmsExportWithoutHibernate() {
	}

	public AutomaticDmsExportWithoutHibernate(boolean exportImages) {
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
	 * @throws UghHelperException
	 * @throws ExportFileException
	 * @throws MetadataTypeNotAllowedException
	 * @throws DocStructHasNoTypeException
	 * @throws DAOException
	 * @throws SwapException
	 * @throws TypeNotAllowedForParentException
	 */
	
	@Override
	public void startExport(ProcessObject process) throws DAOException, IOException, PreferencesException, WriteException, SwapException, TypeNotAllowedForParentException, InterruptedException {
		this.myPrefs = ProcessManager.getRuleset(process.getRulesetId()).getPreferences();
	
		this.project =ProjectManager.getProjectById(process.getProjekteID());
		
		
		this.cp = new ConfigProjects(this.project.getTitel());
		String atsPpnBand = process.getTitle();

		/*
		 * -------------------------------- Dokument einlesen
		 * --------------------------------
		 */
		Fileformat gdzfile;
		Fileformat newfile;
		try {
			 this.fi = new FolderInformation(process.getId(), process.getTitle());
			String metadataPath = this.fi.getMetadataFilePath();
			gdzfile = process.readMetadataFile(metadataPath, this.myPrefs);
			switch (MetadataFormat.findFileFormatsHelperByName(this.project.getFileFormatDmsExport())) {
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
			Helper.setFehlerMeldung(Helper.getTranslation("exportError") + process.getTitle(), e);
			myLogger.error("Export abgebrochen, xml-LeseFehler", e);
			return;
		}

		trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());

		/*
		 * -------------------------------- Metadaten validieren
		 * --------------------------------
		 */

		if (ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
			MetadatenVerifizierungWithoutHibernate mv = new MetadatenVerifizierungWithoutHibernate();
			if (!mv.validate(gdzfile, this.myPrefs, process.getId(), process.getTitle())) {
				throw new InterruptedException("invalid data");

			}
		}

		/*
		 * -------------------------------- Speicherort vorbereiten und
		 * downloaden --------------------------------
		 */
		String zielVerzeichnis;
		File benutzerHome;

		zielVerzeichnis = this.project.getDmsImportImagesPath();
		benutzerHome = new File(zielVerzeichnis);

		/* ggf. noch einen Vorgangsordner anlegen */
		if (this.project.isDmsImportCreateProcessFolder()) {
			benutzerHome = new File(benutzerHome + File.separator + process.getTitle());
			zielVerzeichnis = benutzerHome.getAbsolutePath();
			/* alte Import-Ordner löschen */
			if (!Helper.deleteDir(benutzerHome)) {
				Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), "Import folder could not be cleared");
				return;
			}
			/* alte Success-Ordner löschen */
			File successFile = new File(this.project.getDmsImportSuccessPath() + File.separator + process.getTitle());
			if (!Helper.deleteDir(successFile)) {
				Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), "Success folder could not be cleared");
				return;
			}
			/* alte Error-Ordner löschen */
			File errorfile = new File(this.project.getDmsImportErrorPath() + File.separator + process.getTitle());
			if (!Helper.deleteDir(errorfile)) {
				Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), "Error folder could not be cleared");
				return;
			}

			if (!benutzerHome.exists()) {
				benutzerHome.mkdir();
			}
		}

		/*
		 * -------------------------------- der eigentliche Download der Images
		 * --------------------------------
		 */
		try {
			if (this.exportWithImages) {
				imageDownload(process, benutzerHome, atsPpnBand, DIRECTORY_SUFFIX);
				fulltextDownload(process, benutzerHome, atsPpnBand, DIRECTORY_SUFFIX);
			} else if (this.exportFulltext) {
				fulltextDownload(process, benutzerHome, atsPpnBand, DIRECTORY_SUFFIX);
			}
		} catch (Exception e) {
			Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), e);
			return;
		}

		/*
		 * -------------------------------- zum Schluss Datei an gewünschten Ort
		 * exportieren entweder direkt in den Import-Ordner oder ins
		 * Benutzerhome anschliessend den Import-Thread starten
		 * --------------------------------
		 */
		if (this.project.isUseDmsImport()) {
			if (MetadataFormat.findFileFormatsHelperByName(this.project.getFileFormatDmsExport()) == MetadataFormat.METS) {
				/* Wenn METS, dann per writeMetsFile schreiben... */
				writeMetsFile(process, benutzerHome + File.separator + atsPpnBand + ".xml", gdzfile, false);
			} else {
				/* ...wenn nicht, nur ein Fileformat schreiben. */
				gdzfile.write(benutzerHome + File.separator + atsPpnBand + ".xml");
			}

			/* ggf. sollen im Export mets und rdf geschrieben werden */
			if (MetadataFormat.findFileFormatsHelperByName(this.project.getFileFormatDmsExport()) == MetadataFormat.METS_AND_RDF) {
				writeMetsFile(process, benutzerHome + File.separator + atsPpnBand + ".mets.xml", gdzfile, false);
			}

			Helper.setMeldung(null, process.getTitle() + ": ", "DMS-Export started");

		
			if (!ConfigMain.getBooleanParameter("exportWithoutTimeLimit")) {
				
				/* Success-Ordner wieder löschen */
				if (this.project.isDmsImportCreateProcessFolder()) {
					File successFile = new File(this.project.getDmsImportSuccessPath() + File.separator + process.getTitle());
					Helper.deleteDir(successFile);
				}
			}
			// return ;
		}
	
		return;
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

	public void fulltextDownload(ProcessObject myProzess, File benutzerHome, String atsPpnBand, final String ordnerEndung) throws IOException,
			InterruptedException, SwapException, DAOException {

		// Helper help = new Helper();
		// File tifOrdner = new File(myProzess.getImagesTifDirectory());

		// download sources
//		File sources = new File(fi.getSourceDirectory());
//		if (sources.exists()) {
//			File destination = new File(benutzerHome + File.separator
//					+ atsPpnBand + "_src");
//			if (!destination.exists()) {
//				destination.mkdir();
//			}
//			// TODO all data??
//			File[] dateien = sources.listFiles();
//			for (int i = 0; i < dateien.length; i++) {
//				File meinZiel = new File(destination + File.separator
//						+ dateien[i].getName());
//				Helper.copyFile(dateien[i], meinZiel);
//			}
//		}
		
		
		File txtFolder = new File(this.fi.getTxtDirectory());
		if (txtFolder.exists()) {
			File destination = new File(benutzerHome + File.separator + atsPpnBand + "_txt");
			if (!destination.exists()) {
				destination.mkdir();
			}
			File[] dateien = txtFolder.listFiles();
			for (int i = 0; i < dateien.length; i++) {
				File meinZiel = new File(destination + File.separator + dateien[i].getName());
				Helper.copyFile(dateien[i], meinZiel);
			}
		}

		File wordFolder = new File(this.fi.getWordDirectory());
		if (wordFolder.exists()) {
			File destination = new File(benutzerHome + File.separator + atsPpnBand + "_wc");
			if (!destination.exists()) {
				destination.mkdir();
			}
			File[] dateien = wordFolder.listFiles();
			for (int i = 0; i < dateien.length; i++) {
				File meinZiel = new File(destination + File.separator + dateien[i].getName());
				Helper.copyFile(dateien[i], meinZiel);
			}
		}

		File pdfFolder = new File(this.fi.getPdfDirectory());
		if (pdfFolder.exists()) {
			File destination = new File(benutzerHome + File.separator + atsPpnBand + "_pdf");
			if (!destination.exists()) {
				destination.mkdir();
			}
			File[] dateien = pdfFolder.listFiles();
			for (int i = 0; i < dateien.length; i++) {
				File meinZiel = new File(destination + File.separator + dateien[i].getName());
				Helper.copyFile(dateien[i], meinZiel);
			}
		}
	}

	public void imageDownload(ProcessObject myProzess, File benutzerHome, String atsPpnBand, final String ordnerEndung) throws IOException,
			InterruptedException, SwapException, DAOException {
		/*
		 * -------------------------------- erstmal alle Filter
		 * --------------------------------
		 */
		// FilenameFilter filterTifDateien = new FilenameFilter() {
		// public boolean accept(File dir, String name) {
		// return name.endsWith(".tif");
		// }
		// };

		/*
		 * -------------------------------- dann den Ausgangspfad ermitteln
		 * --------------------------------
		 */
		Helper help = new Helper();
		File tifOrdner = new File(this.fi.getImagesTifDirectory());

		/*
		 * -------------------------------- jetzt die Ausgangsordner in die
		 * Zielordner kopieren --------------------------------
		 */
		if (tifOrdner.exists()) {
			File zielTif = new File(benutzerHome + File.separator + atsPpnBand + ordnerEndung);

			/* bei Agora-Import einfach den Ordner anlegen */
			if (this.project.isUseDmsImport()) {
				if (!zielTif.exists()) {
					zielTif.mkdir();
				}
			} else {
				/*
				 * wenn kein Agora-Import, dann den Ordner mit
				 * Benutzerberechtigung neu anlegen
				 */
				Benutzer myBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
				try {
					help.createUserDirectory(zielTif.getAbsolutePath(), myBenutzer.getLogin());
				} catch (Exception e) {
					Helper.setFehlerMeldung("Export canceled, error", "could not create destination directory");
					myLogger.error("could not create destination directory", e);
				}
			}

			/* jetzt den eigentlichen Kopiervorgang */

			File[] dateien = tifOrdner.listFiles(Helper.dataFilter);
			for (int i = 0; i < dateien.length; i++) {
				File meinZiel = new File(zielTif + File.separator + dateien[i].getName());
				Helper.copyFile(dateien[i], meinZiel);
			}
		}

	}
}