package de.sub.goobi.helper;

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

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.export.download.TiffHeader;

public class WebDav implements Serializable {

	private static final long serialVersionUID = -1929234096626965538L;
	private static final Logger myLogger = Logger.getLogger(WebDav.class);

	/*
 	 * Kopieren bzw. symbolische Links für einen Prozess in das Benutzerhome	
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
		} catch (Exception ioe) {
			myLogger.error("Exception UploadFromHomeAlle()", ioe);
			Helper.setFehlerMeldung("UploadFromHomeAlle abgebrochen, Fehler", ioe.getMessage());
			return rueckgabe;
		}

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
            FilesystemHelper.deleteSymLink(VerzeichnisAlle + myname);
		}
	}

	public void UploadFromHome(Prozess myProzess) {
		Benutzer aktuellerBenutzer = Helper.getCurrentUser();
        if (aktuellerBenutzer != null) {
        	UploadFromHome(aktuellerBenutzer, myProzess);
        }
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

        FilesystemHelper.deleteSymLink(benutzerHome.getAbsolutePath());
	}

	public void DownloadToHome(Prozess myProzess, int inSchrittID, boolean inNurLesen) {
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
                FilesystemHelper.createDirectoryForUser(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
                
				projekt = new File(userHome + DONEDIRECTORYNAME);
                FilesystemHelper.createDirectoryForUser(projekt.getAbsolutePath(), aktuellerBenutzer.getLogin());
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
            	ShellScript.legacyCallShell2(command);
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

}
