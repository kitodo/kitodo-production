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

package de.sub.goobi.export.dms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;

public class DmsImportThread extends Thread {
	private static final Logger myLogger = Logger.getLogger(DmsImportThread.class);
	private File fileError;
	private File fileXml;
	private File fileSuccess;
	private File folderImages;
	private long timeFileSuccess;
	private long timeFileError;

	public String rueckgabe = "";

	public boolean stop = false;

	public DmsImportThread(Prozess inProzess, String inAts) {
		setDaemon(true);
		/* aus Kompatibilitätsgründen auch noch die Fehlermeldungen an alter Stelle, ansonsten lieber in neuem FehlerOrdner */
		if (inProzess.getProjekt().getDmsImportErrorPath() == null || inProzess.getProjekt().getDmsImportErrorPath().length() == 0)
			fileError = new File(inProzess.getProjekt().getDmsImportRootPath(), inAts + ".log");
		else
			fileError = new File(inProzess.getProjekt().getDmsImportErrorPath(), inAts + ".log");

		fileXml = new File(inProzess.getProjekt().getDmsImportRootPath(), inAts + ".xml");
		fileSuccess = new File(inProzess.getProjekt().getDmsImportSuccessPath(), inAts + ".xml");
		if (inProzess.getProjekt().isDmsImportCreateProcessFolder())
			fileSuccess = new File(inProzess.getProjekt().getDmsImportSuccessPath(), inProzess.getTitel() + File.separator + inAts + ".xml");

		folderImages = new File(inProzess.getProjekt().getDmsImportImagesPath(), inAts + "_tif");

		if (fileError.exists())
			timeFileError = fileError.getAbsoluteFile().lastModified();
		if (fileSuccess.exists())
			timeFileSuccess = fileSuccess.getAbsoluteFile().lastModified();
	}

	public void run() {
		while (!stop) {
			try {
				Thread.sleep(550);
				if (!fileXml.exists() && (fileError.exists() || fileSuccess.exists())) {
					if (fileError.exists() && fileError.getAbsoluteFile().lastModified() > timeFileError) {
						stop = true;
						/* die Logdatei mit der Fehlerbeschreibung einlesen */
						StringBuffer myBuf = new StringBuffer();
						myBuf.append("Beim Import ist ein Importfehler aufgetreten: ");
						BufferedReader r = new BufferedReader(new FileReader(fileError));
						String aLine = r.readLine();
						while (aLine != null) {
							myBuf.append(aLine);
							myBuf.append(" ");
							aLine = r.readLine();
						}
						r.close();
						rueckgabe = myBuf.toString();

					}
					if (fileSuccess.exists() && fileSuccess.getAbsoluteFile().lastModified() > timeFileSuccess) {
						stop = true;
					}
				}
			} catch (Throwable t) {
				myLogger.error("Unexception exception", t);
			}
		}
		if (!ConfigMain.getBooleanParameter("exportWithoutTimeLimit")) {
			/* Images wieder löschen */
			Helper.deleteDir(folderImages);
		}
	}

	public void stopThread() {
		rueckgabe = "Import wurde wegen Zeitüberschreitung abgebrochen";
		stop = true;
	}

}