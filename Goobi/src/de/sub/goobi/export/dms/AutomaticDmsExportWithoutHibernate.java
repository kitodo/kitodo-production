package de.sub.goobi.export.dms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import org.goobi.io.SafeFile;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
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
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.export.download.ExportMetsWithoutHibernate;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.EmptyTask;
import de.sub.goobi.metadaten.MetadatenVerifizierungWithoutHibernate;
import de.sub.goobi.metadaten.copier.CopierData;
import de.sub.goobi.metadaten.copier.DataCopier;
import de.sub.goobi.persistence.apache.FolderInformation;
import de.sub.goobi.persistence.apache.ProcessManager;
import de.sub.goobi.persistence.apache.ProcessObject;
import de.sub.goobi.persistence.apache.ProjectManager;
import de.sub.goobi.persistence.apache.ProjectObject;

public class AutomaticDmsExportWithoutHibernate extends ExportMetsWithoutHibernate {
	private static final Logger myLogger = Logger.getLogger(AutomaticDmsExportWithoutHibernate.class);
	ConfigProjects cp;
	private boolean exportWithImages = true;
	private boolean exportFulltext = true;
	private FolderInformation fi;
	private ProjectObject project;

	/**
	 * The field task holds an optional task instance whose progress will be
	 * updated and whom errors will be passed to to be visible in the task
	 * manager screen if it’s available.
	 */
	private EmptyTask task;
	
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
	 * @param process
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
	public boolean startExport(ProcessObject process) throws DAOException, IOException, PreferencesException, WriteException, SwapException, TypeNotAllowedForParentException, InterruptedException {
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
			if (task != null) {
				task.setException(e);
			}
			Helper.setFehlerMeldung(Helper.getTranslation("exportError") + process.getTitle(), e);
			myLogger.error("Export abgebrochen, xml-LeseFehler", e);
			return false;
		}

		String rules = ConfigMain.getParameter("copyData.onExport");
		if (rules != null && !rules.equals("- keine Konfiguration gefunden -")) {
			try {
				new DataCopier(rules).process(new CopierData(newfile, process));
			} catch (ConfigurationException e) {
				if (task != null) {
					task.setException(e);
				}
				Helper.setFehlerMeldung("dataCopier.syntaxError", e.getMessage());
				return false;
			} catch (RuntimeException e) {
				if (task != null) {
					task.setException(e);
				}
				Helper.setFehlerMeldung("dataCopier.runtimeException", e.getMessage());
				return false;
			}
		}

		trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());

		/*
		 * -------------------------------- Metadaten validieren
		 * --------------------------------
		 */

		if (ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
			MetadatenVerifizierungWithoutHibernate mv = new MetadatenVerifizierungWithoutHibernate();
			if (!mv.validate(gdzfile, this.myPrefs, process.getId(), process.getTitle())) {
				return false;

			}
		}

		/*
		 * -------------------------------- Speicherort vorbereiten und
		 * downloaden --------------------------------
		 */
		String zielVerzeichnis;
		SafeFile benutzerHome;

		zielVerzeichnis = this.project.getDmsImportImagesPath();
		benutzerHome = new SafeFile(zielVerzeichnis);

		/* ggf. noch einen Vorgangsordner anlegen */
		if (this.project.isDmsImportCreateProcessFolder()) {
			benutzerHome = new SafeFile(benutzerHome + File.separator + process.getTitle());
			zielVerzeichnis = benutzerHome.getAbsolutePath();
			/* alte Import-Ordner löschen */
			if (!benutzerHome.deleteDir()) {
				Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), "Import folder could not be cleared");
				return false;
			}
			/* alte Success-Ordner löschen */
			SafeFile successFile = new SafeFile(this.project.getDmsImportSuccessPath() + File.separator + process.getTitle());
			if (!successFile.deleteDir()) {
				Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), "Success folder could not be cleared");
				return false;
			}
			/* alte Error-Ordner löschen */
			SafeFile errorfile = new SafeFile(this.project.getDmsImportErrorPath() + File.separator + process.getTitle());
			if (!errorfile.deleteDir()) {
				Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), "Error folder could not be cleared");
				return false;
			}

			if (!benutzerHome.exists()) {
				benutzerHome.mkdir();
			}
			if (task != null) {
				task.setProgress(1);
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
			
			directoryDownload(process, zielVerzeichnis);
			
		} catch (Exception e) {
			if (task != null) {
				task.setException(e);
			}
			Helper.setFehlerMeldung("Export canceled, Process: " + process.getTitle(), e);
			return false;
		}

		/*
		 * -------------------------------- zum Schluss Datei an gewünschten Ort
		 * exportieren entweder direkt in den Import-Ordner oder ins
		 * Benutzerhome anschliessend den Import-Thread starten
		 * --------------------------------
		 */
		if (this.project.isUseDmsImport()) {
			if (task != null) {
				task.setWorkDetail(atsPpnBand + ".xml");
			}
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
					SafeFile successFile = new SafeFile(this.project.getDmsImportSuccessPath() + File.separator + process.getTitle());
					successFile.deleteDir();
				}
			}
		}
		if (task != null) {
			task.setProgress(100);
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

	public void fulltextDownload(ProcessObject myProzess, SafeFile benutzerHome, String atsPpnBand, final String ordnerEndung) throws IOException,
			InterruptedException, SwapException, DAOException {

		// download sources
		SafeFile sources = new SafeFile(fi.getSourceDirectory());
		if (sources.exists() && sources.list().length > 0) {
			SafeFile destination = new SafeFile(benutzerHome + File.separator
					+ atsPpnBand + "_src");
			if (!destination.exists()) {
				destination.mkdir();
			}
			SafeFile[] dateien = sources.listFiles();
			for (int i = 0; i < dateien.length; i++) {
				if(dateien[i].isFile()) {
					SafeFile meinZiel = new SafeFile(destination + File.separator
							+ dateien[i].getName());
					dateien[i].copyFile(meinZiel, false);
				}
			}
		}
		
		SafeFile ocr = new SafeFile(fi.getOcrDirectory());
		if (ocr.exists()) {
			SafeFile[] folder = ocr.listFiles();
			for (SafeFile dir : folder) {
				if (dir.isDirectory() && dir.list().length > 0 && dir.getName().contains("_")) {
					String suffix = dir.getName().substring(dir.getName().lastIndexOf("_"));
					SafeFile destination = new SafeFile(benutzerHome + File.separator + atsPpnBand + suffix);
					if (!destination.exists()) {
						destination.mkdir();
					}
					SafeFile[] files = dir.listFiles();
					for (int i = 0; i < files.length; i++) {
						if(files[i].isFile()) {
							SafeFile target = new SafeFile(destination + File.separator + files[i].getName());
							files[i].copyFile(target, false);
						}
					}
				}
			}
		}
	}

	public void imageDownload(ProcessObject myProzess, SafeFile benutzerHome, String atsPpnBand, final String ordnerEndung) throws IOException,
			InterruptedException, SwapException, DAOException {
		/*
		 * -------------------------------- den Ausgangspfad ermitteln
		 * --------------------------------
		 */
		SafeFile tifOrdner = new SafeFile(this.fi.getImagesTifDirectory(true));

		/*
		 * -------------------------------- jetzt die Ausgangsordner in die
		 * Zielordner kopieren --------------------------------
		 */
		if (tifOrdner.exists() && tifOrdner.list().length > 0) {
			SafeFile zielTif = new SafeFile(benutzerHome + File.separator + atsPpnBand + ordnerEndung);

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
					FilesystemHelper.createDirectoryForUser(zielTif.getAbsolutePath(), myBenutzer.getLogin());
				} catch (Exception e) {
					if (task != null) {
						task.setException(e);
					}
					Helper.setFehlerMeldung("Export canceled, error", "could not create destination directory");
					myLogger.error("could not create destination directory", e);
				}
			}

			/* jetzt den eigentlichen Kopiervorgang */

			SafeFile[] dateien = tifOrdner.listFiles(Helper.dataFilter);
			for (int i = 0; i < dateien.length; i++) {
				if (task != null) {
					task.setWorkDetail(dateien[i].getName());
				}
				if(dateien[i].isFile()) {
					SafeFile meinZiel = new SafeFile(zielTif + File.separator + dateien[i].getName());
					dateien[i].copyFile(meinZiel, false);
				}
				if (task != null) {
					task.setProgress((int) ((i + 1) * 98d / dateien.length + 1));
					if (task.isInterrupted()) {
						throw new InterruptedException();
					}
				}
			}
			if (task != null) {
				task.setWorkDetail(null);
			}
		}
	}

	/**
	 * starts copying all directories configured in goobi_config.properties parameter "processDirs" to export folder 
	 * 
	 * @param myProzess the process object
	 * @param zielVerzeichnis the destination directory
	 * @throws IOException
	 */		
	private void directoryDownload(ProcessObject myProzess, String zielVerzeichnis) throws IOException{
	
		String[] processDirs = ConfigMain.getStringArrayParameter("processDirs");
		
		for(String processDir : processDirs) {
		
			SafeFile srcDir = new SafeFile(FilenameUtils.concat(fi.getProcessDataDirectory(), processDir.replace("(processtitle)", myProzess.getTitle())));
			SafeFile dstDir = new SafeFile(FilenameUtils.concat(zielVerzeichnis, processDir.replace("(processtitle)", myProzess.getTitle())));
		
			if(srcDir.isDirectory()) {
			    srcDir.copyDir(dstDir);
			}
		}
	}

	/**
	 * The method setTask() can be used to pass in a task instance. If that is
	 * passed in, the progress in it will be updated during processing and
	 * occurring errors will be passed to it to be visible in the task manager
	 * screen.
	 * 
	 * @param task
	 *            task object to submit progress updates and errors to
	 */
	public void setTask(EmptyTask task) {
		this.task = task;
	}
}
