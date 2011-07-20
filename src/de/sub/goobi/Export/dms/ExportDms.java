package de.sub.goobi.Export.dms;

import java.io.File;
import java.io.FilenameFilter;
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
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Export.download.ExportMets;
import de.sub.goobi.Metadaten.MetadatenImagesHelper;
import de.sub.goobi.Metadaten.MetadatenVerifizierung;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class ExportDms extends ExportMets {
	private static final Logger myLogger = Logger.getLogger(ExportDms.class);
	private Helper help = new Helper();
	ConfigProjects cp;
	private boolean exportWithImages = true;

	public final static String DIRECTORY_SUFFIX = "_tif";

	public ExportDms() {
	}

	public ExportDms(boolean exportImages) {
		exportWithImages = exportImages;
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
	public void startExport(Prozess myProzess, String inZielVerzeichnis) throws IOException, InterruptedException, WriteException,
			PreferencesException, DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException,
			SwapException, DAOException, TypeNotAllowedForParentException {
		myPrefs = myProzess.getRegelsatz().getPreferences();
		cp = new ConfigProjects(myProzess.getProjekt());
		String atsPpnBand = myProzess.getTitel();

		/*
		 * -------------------------------- Dokument einlesen --------------------------------
		 */
		Fileformat gdzfile;
		Fileformat newfile;
		try {
			gdzfile = myProzess.readMetadataFile();
			switch (MetadataFormat.findFileFormatsHelperByName(myProzess.getProjekt().getFileFormatDmsExport())) {
			case METS:
				newfile = new MetsModsImportExport(myPrefs);
				break;

			case METS_AND_RDF:
				newfile = new RDFFile(myPrefs);
				break;

			default:
				newfile = new RDFFile(myPrefs);
				break;
			}

			newfile.setDigitalDocument(gdzfile.getDigitalDocument());
			gdzfile = newfile;
		} catch (Exception e) {
			help.setFehlerMeldung("Export abgebrochen, xml-LeseFehler bei: " + myProzess.getTitel(), e);
			myLogger.error("Export abgebrochen, xml-LeseFehler", e);
			return;
		}

		/* nur beim Rusdml-Projekt die Metadaten aufbereiten */
		ConfigProjects cp = new ConfigProjects(myProzess.getProjekt());
		// TODO: Remove this
		if (cp.getParamList("dmsImport.check").contains("rusdml")) {
			ExportDms_CorrectRusdml expcorr = new ExportDms_CorrectRusdml(myProzess, myPrefs, gdzfile);
			atsPpnBand = expcorr.correctionStart();
		}

		trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());

		/*
		 * -------------------------------- Metadaten validieren --------------------------------
		 */

		if (ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
			MetadatenVerifizierung mv = new MetadatenVerifizierung();
			if (!mv.validate(gdzfile, myPrefs, myProzess))
				return;
		}

		/*
		 * -------------------------------- Speicherort vorbereiten und downloaden --------------------------------
		 */
		String zielVerzeichnis;
		File benutzerHome;
		if (myProzess.getProjekt().isUseDmsImport()) {
			zielVerzeichnis = myProzess.getProjekt().getDmsImportImagesPath();
			benutzerHome = new File(zielVerzeichnis);

			/* ggf. noch einen Vorgangsordner anlegen */
			if (myProzess.getProjekt().isDmsImportCreateProcessFolder()) {
				benutzerHome = new File(benutzerHome + File.separator + myProzess.getTitel());
				zielVerzeichnis = benutzerHome.getAbsolutePath();
				/* alte Import-Ordner löschen */
				if (!Helper.deleteDir(benutzerHome)) {
					help.setFehlerMeldung("Export canceled, Process: " + myProzess.getTitel(), "Import folder could not be cleared");
					return;
				}
				/* alte Success-Ordner löschen */
				File successFile = new File(myProzess.getProjekt().getDmsImportSuccessPath() + File.separator + myProzess.getTitel());
				if (!Helper.deleteDir(successFile)) {
					help.setFehlerMeldung("Export canceled, Process: " + myProzess.getTitel(), "Success folder could not be cleared");
					return;
				}
				/* alte Error-Ordner löschen */
				File errorfile = new File(myProzess.getProjekt().getDmsImportErrorPath() + File.separator + myProzess.getTitel());
				if (!Helper.deleteDir(errorfile)) {
					help.setFehlerMeldung("Export canceled, Process: " + myProzess.getTitel(), "Error folder could not be cleared");
					return;
				}

				if (!benutzerHome.exists())
					benutzerHome.mkdir();
			}

		} else {
			zielVerzeichnis = inZielVerzeichnis + atsPpnBand + File.separator;
			// wenn das Home existiert, erst löschen und dann neu anlegen
			benutzerHome = new File(zielVerzeichnis);
			if (!Helper.deleteDir(benutzerHome)) {
				help.setFehlerMeldung("Export abgebrochen, Fehler bei: " + myProzess.getTitel(), "das Homeverzeichnis konnte nicht geleert werden");
				return;
			}
			prepareUserDirectory(zielVerzeichnis);
		}

		/*
		 * -------------------------------- der eigentliche Download der Images --------------------------------
		 */
		try {
			if (exportWithImages) {
				imageDownload(myProzess, benutzerHome, atsPpnBand, DIRECTORY_SUFFIX);
			}
		} catch (Exception e) {
			help.setFehlerMeldung("Export abgebrochen, Fehler bei: " + myProzess.getTitel(), e);
			return;
		}

		/*
		 * -------------------------------- zum Schluss Datei an gewünschten Ort exportieren entweder direkt in den Import-Ordner oder ins
		 * Benutzerhome anschliessend den Import-Thread starten --------------------------------
		 */
		if (myProzess.getProjekt().isUseDmsImport()) {
			if (MetadataFormat.findFileFormatsHelperByName(myProzess.getProjekt().getFileFormatDmsExport()) == MetadataFormat.METS) {
				/* Wenn METS, dann per writeMetsFile schreiben... */
				writeMetsFile(myProzess, benutzerHome + File.separator + atsPpnBand + ".xml", gdzfile);
			} else {
				/* ...wenn nicht, nur ein Fileformat schreiben. */
				gdzfile.write(benutzerHome + File.separator + atsPpnBand + ".xml");
			}

			// TODO generischer lösen
			/* ggf. sollen im Export mets und rdf geschrieben werden */
			if (MetadataFormat.findFileFormatsHelperByName(myProzess.getProjekt().getFileFormatDmsExport()) == MetadataFormat.METS_AND_RDF) {
				writeMetsFile(myProzess, benutzerHome + File.separator + atsPpnBand + ".mets.xml", gdzfile);
			}

			help.setMeldung(null, myProzess.getTitel() + ": ", "DMS-Import wurde gestartet");
			DmsImportThread agoraThread = new DmsImportThread(myProzess, atsPpnBand);
			agoraThread.start();
			if (!ConfigMain.getBooleanParameter("exportWithoutTimeLimit")) {
				try {
					/* 30 Sekunden auf den Thread warten, evtl. killen */
					agoraThread.join(myProzess.getProjekt().getDmsImportTimeOut().longValue());
					if (agoraThread.isAlive()) {
						agoraThread.stopThread();
					}
				} catch (InterruptedException e) {
					help.setFehlerMeldung(myProzess.getTitel() + ": Fehler beim Import - ", e.getMessage());
					myLogger.error(myProzess.getTitel() + ": Fehler beim Import", e);
				}
				if (agoraThread.rueckgabe.length() > 0)
					help.setFehlerMeldung(myProzess.getTitel() + ": ", agoraThread.rueckgabe);
				else {
					help.setMeldung(null, myProzess.getTitel() + ": ", "DMS-Import abgeschlossen");
					/* Success-Ordner wieder löschen */
					if (myProzess.getProjekt().isDmsImportCreateProcessFolder()) {
						File successFile = new File(myProzess.getProjekt().getDmsImportSuccessPath() + File.separator + myProzess.getTitel());
						Helper.deleteDir(successFile);
					}
				}
			}
		} else {
			/* ohne Agora-Import die xml-Datei direkt ins Home schreiben */
			if (MetadataFormat.findFileFormatsHelperByName(myProzess.getProjekt().getFileFormatDmsExport()) == MetadataFormat.METS) {
				writeMetsFile(myProzess, zielVerzeichnis + atsPpnBand + ".xml", gdzfile);
			} else {
				gdzfile.write(zielVerzeichnis + atsPpnBand + ".xml");
			}

			help.setMeldung(null, myProzess.getTitel() + ": ", "Export abgeschlossen");
		}
	}

	/**
	 * run through all metadata and children of given docstruct to trim the strings calls itself recursively
	 */
	private void trimAllMetadata(DocStruct inStruct) {
		/* trimm all metadata values */
		if (inStruct.getAllMetadata() != null) {
			for (Metadata md : inStruct.getAllMetadata()) {
				md.setValue(md.getValue().trim());
			}
		}

		/* run through all children of docstruct */
		if (inStruct.getAllChildren() != null) {
			for (DocStruct child : inStruct.getAllChildren()) {
				trimAllMetadata(child);
			}
		}
	}

	public void imageDownload(Prozess myProzess, File benutzerHome, String atsPpnBand, final String ordnerEndung) throws IOException,
			InterruptedException, SwapException, DAOException {
		/*
		 * -------------------------------- erstmal alle Filter --------------------------------
		 */
		// FilenameFilter filterTifDateien = new FilenameFilter() {
		// public boolean accept(File dir, String name) {
		// return name.endsWith(".tif");
		// }
		// };

		/*
		 * -------------------------------- dann den Ausgangspfad ermitteln --------------------------------
		 */
		Helper help = new Helper();
		File tifOrdner = new File(myProzess.getImagesTifDirectory());

		/*
		 * -------------------------------- jetzt die Ausgangsordner in die Zielordner kopieren --------------------------------
		 */
		if (tifOrdner != null) {
			File zielTif = new File(benutzerHome + File.separator + atsPpnBand + ordnerEndung);

			/* bei Agora-Import einfach den Ordner anlegen */
			if (myProzess.getProjekt().isUseDmsImport()) {
				if (!zielTif.exists())
					zielTif.mkdir();
			} else {
				/* wenn kein Agora-Import, dann den Ordner mit Benutzerberechtigung neu anlegen */
				Benutzer myBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
				try {
					help.createUserDirectory(zielTif.getAbsolutePath(), myBenutzer.getLogin());
				} catch (Exception e) {
					help.setFehlerMeldung("Export abgebrochen, Fehler", "das Zielverzeichnis konnte nicht angelegt werden");
					myLogger.error("Export abgebrochen, das Zielverzeichnis konnte nicht angelegt werden", e);
				}
			}

			/* jetzt den eigentlichen Kopiervorgang */
			FilenameFilter filter = help.getFilter();
			File[] dateien = tifOrdner.listFiles(MetadatenImagesHelper.filter);
			for (int i = 0; i < dateien.length; i++) {
				File meinZiel = new File(zielTif + File.separator + dateien[i].getName());
				Helper.copyFile(dateien[i], meinZiel);
			}
		}
	}
}