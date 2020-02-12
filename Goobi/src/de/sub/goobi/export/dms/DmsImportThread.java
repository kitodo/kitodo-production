/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.goobi.export.dms;

import java.io.BufferedReader;
import java.io.File;

import org.goobi.io.SafeFile;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;

public class DmsImportThread extends Thread {
    private static final Logger logger = Logger.getLogger(DmsImportThread.class);
    private SafeFile fileError;
    private SafeFile fileXml;
    private SafeFile fileSuccess;
    private SafeFile folderImages;
    private long timeFileSuccess;
    private long timeFileError;

    public String rueckgabe = "";

    public boolean stop = false;

    public DmsImportThread(Prozess inProzess, String inAts) {
        setDaemon(true);
        /* aus Kompatibilitätsgründen auch noch die Fehlermeldungen an alter Stelle, ansonsten lieber in neuem FehlerOrdner */
        if (inProzess.getProjekt().getDmsImportErrorPath() == null || inProzess.getProjekt().getDmsImportErrorPath().length() == 0) {
            this.fileError = new SafeFile(inProzess.getProjekt().getDmsImportRootPath(), inAts + ".log");
        } else {
            this.fileError = new SafeFile(inProzess.getProjekt().getDmsImportErrorPath(), inAts + ".log");
        }

        this.fileXml = new SafeFile(inProzess.getProjekt().getDmsImportRootPath(), inAts + ".xml");
        this.fileSuccess = new SafeFile(inProzess.getProjekt().getDmsImportSuccessPath(), inAts + ".xml");
        if (inProzess.getProjekt().isDmsImportCreateProcessFolder()) {
            this.fileSuccess = new SafeFile(inProzess.getProjekt().getDmsImportSuccessPath(), inProzess.getTitel() + File.separator + inAts + ".xml");
        }

        this.folderImages = new SafeFile(inProzess.getProjekt().getDmsImportImagesPath(), inAts + "_" + ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif"));

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
                        try (BufferedReader r = new BufferedReader(this.fileError.createFileReader())) {
                            String aLine = r.readLine();
                            while (aLine != null) {
                                myBuf.append(aLine);
                                myBuf.append(" ");
                                aLine = r.readLine();
                            }
                        }
                        this.rueckgabe = myBuf.toString();

                    }
                    if (this.fileSuccess.exists() && this.fileSuccess.getAbsoluteFile().lastModified() > this.timeFileSuccess) {
                        this.stop = true;
                    }
                }
            } catch (Throwable t) {
                logger.error("Unexception exception", t);
            }
        }
        if (!ConfigMain.getBooleanParameter("exportWithoutTimeLimit")) {
            /* Images wieder löschen */
            this.folderImages.deleteDir();
        }
    }

    public void stopThread() {
        this.rueckgabe = "Import wurde wegen Zeitüberschreitung abgebrochen";
        this.stop = true;
    }

}
