package de.sub.goobi.helper;

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Export.download.TiffHeader;
import de.sub.goobi.config.ConfigMain;

public class WebDav implements Serializable {

	private static final long serialVersionUID = -1929234096626965538L;
	private static final Logger myLogger = Logger.getLogger(WebDav.class);

	/*
	 * #####################################################
	 * ##################################################### ## ## Kopieren bzw.
	 * symbolische Links für einen Prozess in das Benutzerhome ##
	 * #####################################################
	 * ####################################################
	 */

	private static String DONEDIRECTORYNAME = "fertig/";
	public WebDav(){
		DONEDIRECTORYNAME =ConfigMain.getParameter("doneDirectoryName", "fertig/");

		
	}
	
	
	/**
	 * Retrieve all folders from one directory
	 * ================================================================
	 */

	public List<String> UploadFromHomeAlle(String inVerzeichnis) {
		List<String> rueckgabe = new ArrayList<String>();
		Benutzer aktuellerBenutzer = Helper.getCurrentUser();
		String VerzeichnisAlle;

		try {
			VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
			// Helper.setTomcatBenutzerrechte(VerzeichnisAlle);
		} catch (Exception ioe) {
			myLogger.error("Exception UploadFromHomeAlle()", ioe);
			Helper.setFehlerMeldung("UploadFromHomeAlle abgebrochen, Fehler", ioe.getMessage());
			return rueckgabe;
		}

		// myLogger.debug("Upload-Verzeichnis: " + VerzeichnisAlle);
		File benutzerHome = new File(VerzeichnisAlle);

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("]");
			}
		};
		String[] dateien = benutzerHome.list(filter);
		if (dateien == null) {
			return new ArrayList<String>();
		} else {
			for (String data : dateien) {
				if (data.endsWith("/") || data.endsWith("\\")) {
					data = data.substring(0, data.length() - 1);
				}
				if (data.contains("/")) {
					data = data.substring(data.lastIndexOf("/"));
				}
			}
			return new ArrayList<String>(Arrays.asList(dateien));
		}

	}

	/**
	 * Remove Folders from Directory
	 * ================================================================
	 */
	// TODO: Use generic types
	public void removeFromHomeAlle(List<String> inList, String inVerzeichnis) {
		String VerzeichnisAlle;
		Benutzer aktuellerBenutzer = Helper.getCurrentUser();
		try {
			VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
		} catch (Exception ioe) {
			myLogger.error("Exception RemoveFromHomeAlle()", ioe);
			Helper.setFehlerMeldung("Upload stoped, error", ioe.getMessage());
			return;
		}

		for (Iterator<String> it = inList.iterator(); it.hasNext();) {
			String myname = it.next();
			String command = ConfigMain.getParameter("script_deleteSymLink") + " ";
			command += VerzeichnisAlle + myname;
			// myLogger.debug(command);
			try {
				
				Helper.callShell(command);
			} catch (java.io.IOException ioe) {
				myLogger.error("IOException UploadFromHomeAlle()", ioe);
				Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
				return;
			} catch (InterruptedException e) {
				myLogger.error("IOException UploadFromHomeAlle()", e);
				Helper.setFehlerMeldung("Aborted upload from home, error", e.getMessage());
				return;
			}
		}
	}

	public void UploadFromHome(Prozess myProzess) {
		Benutzer aktuellerBenutzer = Helper.getCurrentUser();
		UploadFromHome(aktuellerBenutzer, myProzess);
	}

	public void UploadFromHome(Benutzer inBenutzer, Prozess myProzess) {
		String nach = "";

		try {
			nach = inBenutzer.getHomeDir();
		} catch (Exception ioe) {
			myLogger.error("Exception UploadFromHome(...)", ioe);
			Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
			return;
		}

		/* prüfen, ob Benutzer Massenupload macht */
		if (inBenutzer != null && inBenutzer.isMitMassendownload()) {
			nach += myProzess.getProjekt().getTitel() + File.separator;
			File projectDirectory = new File (nach = nach.replaceAll(" ", "__"));
			if (!projectDirectory.exists() && !projectDirectory.mkdir()) {
				List<String> param = new ArrayList<String>();
				param.add(String.valueOf(nach.replaceAll(" ", "__")));
				Helper.setFehlerMeldung(Helper.getTranslation("MassDownloadProjectCreationError", param));
				myLogger.error("Can not create project directory " + nach.replaceAll(" ", "__"));
				return;
			}
		}
		nach += myProzess.getTitel() + " [" + myProzess.getId() + "]";

		/* Leerzeichen maskieren */
		nach = nach.replaceAll(" ", "__");
		File benutzerHome = new File(nach);

		String command = ConfigMain.getParameter("script_deleteSymLink") + " ";
		command += benutzerHome;
		// myLogger.debug(command);

		try {
			// TODO: Use ProcessBuilder
			Helper.callShell(command);
		} catch (java.io.IOException ioe) {
			myLogger.error("IOException UploadFromHome", ioe);
			Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
		} catch (InterruptedException e) {
			myLogger.error("IOException UploadFromHome", e);
			Helper.setFehlerMeldung("Aborted upload from home, error", e.getMessage());

		}
	}

	public void DownloadToHome(Prozess myProzess, int inSchrittID, boolean inNurLesen) {
		Helper help = new Helper();
		saveTiffHeader(myProzess);
		Benutzer aktuellerBenutzer = Helper.getCurrentUser();
		String von = "";
		String userHome = "";

		try {
			von = myProzess.getImagesDirectory();
			/* UserHome ermitteln */
			userHome = aktuellerBenutzer.getHomeDir();

			/*
			 * bei Massendownload muss auch das Projekt- und Fertig-Verzeichnis
			 * existieren
			 */
			if (aktuellerBenutzer.isMitMassendownload()) {
				File projekt = new File(userHome + myProzess.getProjekt().getTitel());
				if (!projekt.exists()) {
					help.createUserDirectory(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
				}
				projekt = new File(userHome + DONEDIRECTORYNAME);
				if (!projekt.exists()) {
					help.createUserDirectory(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
				}
			}

		} catch (Exception ioe) {
			myLogger.error("Exception DownloadToHome()", ioe);
			Helper.setFehlerMeldung("Aborted download to home, error", ioe.getMessage());
			return;
		}

		/*
		 * abhängig davon, ob der Download als "Massendownload" in einen
		 * Projektordner erfolgen soll oder nicht, das Zielverzeichnis
		 * definieren
		 */
		String processLinkName = myProzess.getTitel() + "__[" + myProzess.getId() + "]";
		String nach = userHome;
		if (aktuellerBenutzer.isMitMassendownload() && myProzess.getProjekt() != null) {
			nach += myProzess.getProjekt().getTitel() + File.separator;
		}
		nach += processLinkName;

		/* Leerzeichen maskieren */
		nach = nach.replaceAll(" ", "__");

		myLogger.info("von: " + von);
		myLogger.info("nach: " + nach);

		File imagePfad = new File(von);
		File benutzerHome = new File(nach);

		// wenn der Ziellink schon existiert, dann abbrechen
		if (benutzerHome.exists()) {
			return;
		}

		String command = ConfigMain.getParameter("script_createSymLink") + " ";
		command += imagePfad + " " + benutzerHome + " ";
		if (inNurLesen) {
			command += ConfigMain.getParameter("UserForImageReading", "root");
		} else {
			command += aktuellerBenutzer.getLogin();
		}
		try {
			// Runtime.getRuntime().exec(command);

			Helper.callShell2(command);
			// Helper.setMeldung("Verzeichnis in Benutzerhome angelegt: ",
			// processLinkName);
		} catch (java.io.IOException ioe) {
			myLogger.error("IOException DownloadToHome()", ioe);
			Helper.setFehlerMeldung("Download aborted, IOException", ioe.getMessage());
		} catch (InterruptedException e) {
			myLogger.error("InterruptedException DownloadToHome()", e);
			Helper.setFehlerMeldung("Download aborted, InterruptedException", e.getMessage());
			myLogger.error(e);
		}
	}

	private void saveTiffHeader(Prozess inProzess) {
		try {
			/* prüfen, ob Tiff-Header schon existiert */
			if (new File(inProzess.getImagesDirectory() + "tiffwriter.conf").exists()) {
				return;
			}
			TiffHeader tif = new TiffHeader(inProzess);
			BufferedWriter outfile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inProzess.getImagesDirectory()
					+ "tiffwriter.conf"), "utf-8"));
			outfile.write(tif.getTiffAlles());
			outfile.close();
		} catch (Exception e) {
			Helper.setFehlerMeldung("Download aborted", e);
			myLogger.error(e);
		}
	}

	public int getAnzahlBaende(String inVerzeichnis) {
		try {
			Benutzer aktuellerBenutzer = Helper.getCurrentUser();
			String VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
			File benutzerHome = new File(VerzeichnisAlle);
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith("]");
				}
			};
			return benutzerHome.list(filter).length;
		} catch (Exception e) {
			myLogger.error(e);
			return 0;
		}
	}

	// TODO: Remove this Methods - Use FileUtils, as log as it's still there ;-)
	/*
	 * public int getAnzahlImages(String inVerzeichnis) { try { return
	 * getAnzahlImages2(new File(inVerzeichnis)); } catch (Exception e) {
	 * myLogger.error(e); return 0; } }
	 * 
	 * // Process all files and directories under dir private int
	 * getAnzahlImages2(File inDir) { int anzahl = 0; if (inDir.isDirectory()) {
	 * // die Images zählen
	 * 
	 * FilenameFilter filter = new FilenameFilter() { public boolean accept(File
	 * dir, String name) { return name.endsWith(".tif"); } }; anzahl =
	 * inDir.list(filter).length;
	 * 
	 * //die Unterverzeichnisse durchlaufen String[] children = inDir.list();
	 * for (int i = 0; i < children.length; i++) { anzahl +=
	 * getAnzahlImages2(new File(inDir, children[i])); } } return anzahl; }
	 */
}
