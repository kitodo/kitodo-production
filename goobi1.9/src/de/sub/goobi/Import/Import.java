package de.sub.goobi.Import;
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
import de.sub.goobi.Beans.Schritt;
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
		this.importFehler = "";
		this.importMeldung = "";
		try {
			// Einlesen(prozessID.toString());
			Einlesen();
		} catch (Exception e) {
			this.importFehler = "An error occured: " + e.getMessage();
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
			if (this.mySchritt.isTypImportFileUpload() && this.mySchritt.isTypExportRus() == true) {
				String gesamteDatei = new String(this.upDatei.getBytes(), "UTF-16LE");
				reader = new BufferedReader(new StringReader(gesamteDatei));
				ImportRussland myImport = new ImportRussland();
				myImport.Parsen(reader, this.mySchritt.getProzess());
				this.importMeldung = "Der russische Import wurde erfolgreich abgeschlossen";
			}

			/* Zentralblatt-Import */
			if (this.mySchritt.isTypImportFileUpload() && this.mySchritt.isTypExportRus() == false) {
				String gesamteDatei = new String(this.upDatei.getBytes(), "ISO8859_1"); // ISO8859_1 UTF-8
				reader = new BufferedReader(new StringReader(gesamteDatei));
				ImportZentralblatt myImport = new ImportZentralblatt();
				myImport.Parsen(reader, this.mySchritt.getProzess());
				this.importMeldung = "Der Zentralblatt-Import wurde erfolgreich abgeschlossen";
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
		return this.importFehler;
	}

	public String getImportMeldung() {
		return this.importMeldung;
	}

	public UploadedFile getUpDatei() {
		return this.upDatei;
	}

	public void setUpDatei(UploadedFile inUpDatei) {
		this.upDatei = inUpDatei;
	}

	public Schritt getMySchritt() {
		return this.mySchritt;
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
