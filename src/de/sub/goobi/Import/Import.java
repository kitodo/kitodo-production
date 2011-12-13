package de.sub.goobi.Import;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.WrongImportFileException;

/**
 * Import von Metadaten aus upgeloadeten Dateien
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 25.06.2005
 */
public class Import {
	private static final Logger myLogger = Logger.getLogger(Import.class);
	private String importFehler = "";
	private String importMeldung = "";
	// private Integer importArt;
	// private Integer prozessID;
	// private Prozess prozess;
	private Schritt mySchritt;
	private UploadedFile upDatei;

	/**
	 * Allgemeiner Konstruktor ()
	 */
	public Import() {
	}

	public String Start() {
		myLogger.info("Import Start - start");
		importFehler = "";
		importMeldung = "";
		try {
			// Einlesen(prozessID.toString());
			Einlesen();
		} catch (Exception e) {
			importFehler = "An error occured: " + e.getMessage();
			myLogger.error(e);
		}
		myLogger.info("Import Start - ende");
		return "";
	}

	private void Einlesen() throws IOException, WrongImportFileException, TypeNotAllowedForParentException, TypeNotAllowedAsChildException,
			MetadataTypeNotAllowedException, ReadException, InterruptedException, PreferencesException, SwapException, DAOException, WriteException {
		myLogger.debug("Einlesen() - Start");
		BufferedReader reader = null;
		try {

			/*
			 * -------------------------------- prüfen ob es ein russischer oder ein zbl-Import ist und entsprechende Routine aufrufen
			 * --------------------------------
			 */

			/* russischer Import */
			if (mySchritt.isTypImportFileUpload() && mySchritt.isTypExportRus() == true) {
				String gesamteDatei = new String((byte[]) upDatei.getBytes(), "UTF-16LE");
				reader = new BufferedReader(new StringReader(gesamteDatei));
				ImportRussland myImport = new ImportRussland();
				myImport.Parsen(reader, mySchritt.getProzess());
				importMeldung = "Der russische Import wurde erfolgreich abgeschlossen";
			}

			/* Zentralblatt-Import */
			if (mySchritt.isTypImportFileUpload() && mySchritt.isTypExportRus() == false) {
				String gesamteDatei = new String((byte[]) upDatei.getBytes(), "ISO8859_1"); // ISO8859_1 UTF-8
				reader = new BufferedReader(new StringReader(gesamteDatei));
				ImportZentralblatt myImport = new ImportZentralblatt();
				myImport.Parsen(reader, mySchritt.getProzess());
				importMeldung = "Der Zentralblatt-Import wurde erfolgreich abgeschlossen";
			}

		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					myLogger.error("Die Datei kann nicht geschlossen werden", e);
				}
			}
		}
		/* wenn alles ok ist, 0 zurückgeben */
		myLogger.debug("Einlesen() - Ende");
	}

	/*
	 * ##################################################### ##################################################### ## ## allgemeine Getter und Setter
	 * ## ##################################################### ####################################################
	 */

	public String getImportFehler() {
		return importFehler;
	}

	public String getImportMeldung() {
		return importMeldung;
	}

	public UploadedFile getUpDatei() {
		return upDatei;
	}

	public void setUpDatei(UploadedFile inUpDatei) {
		upDatei = inUpDatei;
	}

	public Schritt getMySchritt() {
		return mySchritt;
	}

	public void setMySchritt(Schritt mySchritt) {
		this.mySchritt = mySchritt;
	}

	// public Integer getImportArt() {
	// return importArt;
	// }
	//
	// public void setImportArt(Integer inimportArt) {
	// importArt = inimportArt;
	// }

	// public Prozess getProzess() {
	// return prozess;
	// }
	//
	// public void setProzess(Prozess prozess) {
	// this.prozess = prozess;
	// }

	// public Integer getProzessID() {
	// return prozessID;
	// }
	//   
	// public void setProzessID(Integer prozessID) {
	// this.prozessID = prozessID;
	// }
}
