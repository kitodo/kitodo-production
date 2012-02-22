/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.helper;

//TODO: Replace with a VFS
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.export.download.TiffHeader;
import de.sub.goobi.config.ConfigMain;

public class WebDav {
	private static final Logger myLogger = Logger.getLogger(WebDav.class);

	/*
	 * ##################################################### ##################################################### ## ## Kopieren bzw. symbolische
	 * Links für einen Prozess in das Benutzerhome ## #####################################################
	 * ####################################################
	 */

	/**
	 * Retrieve all folders from one directory ================================================================
	 */

	public List<String> UploadFromHomeAlle(String inVerzeichnis) {
		List<String> rueckgabe = new ArrayList<String>();
		Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
		String VerzeichnisAlle;

		try {
			VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
		} catch (Exception ioe) {
			myLogger.error("Exception UploadFromHomeAlle()", ioe);
			Helper.setFehlerMeldung("UploadFromHomeAlle abgebrochen, Fehler", ioe.getMessage());
			return rueckgabe;
		}

		File benutzerHome = new File(VerzeichnisAlle);

		FilenameFilter filter = new FilenameFilter() {
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
					data = data.substring(0, data.length()-1);
				}
				if (data.contains("/")) {
					data = data.substring(data.lastIndexOf("/"));
				}
			}
			return new ArrayList<String>(Arrays.asList(dateien));
		}

	}

	/**
	 * Remove Folders from Directory ================================================================
	 */
	// TODO: Use generic types
	public void removeFromHomeAlle(List<String> inList, String inVerzeichnis) {
		String VerzeichnisAlle;
		Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
		try {
			VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
		} catch (Exception ioe) {
			myLogger.error("Exception RemoveFromHomeAlle()", ioe);
			Helper.setFehlerMeldung("Upload stoped, error", ioe.getMessage());
			return;
		}

		for (Iterator<String> it = inList.iterator(); it.hasNext();) {
			String myname = (String) it.next();
			String command = ConfigMain.getParameter("script_deleteSymLink") + " ";
			command += VerzeichnisAlle + myname;
			try {
				Runtime.getRuntime().exec(command);
			} catch (java.io.IOException ioe) {
				myLogger.error("IOException UploadFromHomeAlle()", ioe);
				Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
				return;
			}
		}
	}

	public void UploadFromHome(Prozess myProzess) {
		Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
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
		if (inBenutzer != null && inBenutzer.isMitMassendownload())
			nach += myProzess.getProjekt().getTitel() + File.separator;
		nach += myProzess.getTitel() + " [" + myProzess.getId() + "]";

		/* Leerzeichen maskieren */
		nach = nach.replaceAll(" ", "__");
		File benutzerHome = new File(nach);

		String command = ConfigMain.getParameter("script_deleteSymLink") + " ";
		command += benutzerHome;

		try {
			// TODO: Use ProcessBuilder
			Runtime.getRuntime().exec(command);
		} catch (java.io.IOException ioe) {
			myLogger.error("IOException UploadFromHome", ioe);
			Helper.setFehlerMeldung("Aborted upload from home, error", ioe.getMessage());
		}
	}

	public void DownloadToHome(Prozess myProzess, int inSchrittID, boolean inNurLesen) {
		Helper help = new Helper();
		saveTiffHeader(myProzess);
		Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
		String von = "";
		String userHome = "";

		try {
			von = myProzess.getImagesDirectory();
			/* UserHome ermitteln */
			userHome = aktuellerBenutzer.getHomeDir();

			/* bei Massendownload muss auch das Projekt- und Fertig-Verzeichnis existieren */
			if (aktuellerBenutzer.isMitMassendownload()) {
				File projekt = new File(userHome + myProzess.getProjekt().getTitel());
				if (!projekt.exists())
					help.createUserDirectory(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
				projekt = new File(userHome + "fertig" + File.separator);
				if (!projekt.exists())
					help.createUserDirectory(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
			}

		} catch (Exception ioe) {
			myLogger.error("Exception DownloadToHome()", ioe);
			Helper.setFehlerMeldung("Aborted download to home, error", ioe.getMessage());
			return;
		}

		/*
		 * abhängig davon, ob der Download als "Massendownload" in einen Projektordner erfolgen soll oder nicht, das Zielverzeichnis definieren
		 */
		String processLinkName = myProzess.getTitel() + "__[" + myProzess.getId() + "]";
		String nach = userHome;
		if (aktuellerBenutzer.isMitMassendownload() && myProzess.getProjekt() != null)
			nach += myProzess.getProjekt().getTitel() + File.separator;
		nach += processLinkName;

		/* Leerzeichen maskieren */
		nach = nach.replaceAll(" ", "__");

		myLogger.info("von: " + von);
		myLogger.info("nach: " + nach);

		File imagePfad = new File(von);
		File benutzerHome = new File(nach);

		// wenn der Ziellink schon existiert, dann abbrechen
		if (benutzerHome.exists())
			return;

		String command = ConfigMain.getParameter("script_createSymLink") + " ";
		command += imagePfad + " " + benutzerHome + " ";
		if (inNurLesen)
			command += ConfigMain.getParameter("UserForImageReading", "root");
		else
			command += aktuellerBenutzer.getLogin();
		try {
			// Runtime.getRuntime().exec(command);

			Helper.callShell2(command);
			// Helper.setMeldung("Verzeichnis in Benutzerhome angelegt: ", processLinkName);
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
			if (new File(inProzess.getImagesDirectory() + "tiffwriter.conf").exists())
				return;
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
			Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
			String VerzeichnisAlle = aktuellerBenutzer.getHomeDir() + inVerzeichnis;
			File benutzerHome = new File(VerzeichnisAlle);
			FilenameFilter filter = new FilenameFilter() {
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

}
