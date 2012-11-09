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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.Prozess;
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
		if (inProzess.getProjekt().getDmsImportErrorPath() == null || inProzess.getProjekt().getDmsImportErrorPath().length() == 0) {
			this.fileError = new File(inProzess.getProjekt().getDmsImportRootPath(), inAts + ".log");
		} else {
			this.fileError = new File(inProzess.getProjekt().getDmsImportErrorPath(), inAts + ".log");
		}

		this.fileXml = new File(inProzess.getProjekt().getDmsImportRootPath(), inAts + ".xml");
		this.fileSuccess = new File(inProzess.getProjekt().getDmsImportSuccessPath(), inAts + ".xml");
		if (inProzess.getProjekt().isDmsImportCreateProcessFolder()) {
			this.fileSuccess = new File(inProzess.getProjekt().getDmsImportSuccessPath(), inProzess.getTitel() + File.separator + inAts + ".xml");
		}

		this.folderImages = new File(inProzess.getProjekt().getDmsImportImagesPath(), inAts + "_tif");

		if (this.fileError.exists()) {
			this.timeFileError = this.fileError.getAbsoluteFile().lastModified();
		}
		if (this.fileSuccess.exists()) {
			this.timeFileSuccess = this.fileSuccess.getAbsoluteFile().lastModified();
		}
	}

	@Override
	public void run() {
		while (!this.stop) {
			try {
				Thread.sleep(550);
				if (!this.fileXml.exists() && (this.fileError.exists() || this.fileSuccess.exists())) {
					if (this.fileError.exists() && this.fileError.getAbsoluteFile().lastModified() > this.timeFileError) {
						this.stop = true;
						/* die Logdatei mit der Fehlerbeschreibung einlesen */
						StringBuffer myBuf = new StringBuffer();
						myBuf.append("Beim Import ist ein Importfehler aufgetreten: ");
						BufferedReader r = new BufferedReader(new FileReader(this.fileError));
						String aLine = r.readLine();
						while (aLine != null) {
							myBuf.append(aLine);
							myBuf.append(" ");
							aLine = r.readLine();
						}
						r.close();
						this.rueckgabe = myBuf.toString();

					}
					if (this.fileSuccess.exists() && this.fileSuccess.getAbsoluteFile().lastModified() > this.timeFileSuccess) {
						this.stop = true;
					}
				}
			} catch (Throwable t) {
				myLogger.error("Unexception exception", t);
			}
		}
		if (!ConfigMain.getBooleanParameter("exportWithoutTimeLimit")) {
			/* Images wieder löschen */
			Helper.deleteDir(this.folderImages);
		}
	}

	public void stopThread() {
		this.rueckgabe = "Import wurde wegen Zeitüberschreitung abgebrochen";
		this.stop = true;
	}

}