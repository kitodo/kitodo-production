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

package de.sub.kitodo.export.dms;

import de.sub.kitodo.config.ConfigCore;

import java.io.BufferedReader;
import java.io.File;

import org.apache.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.io.SafeFile;

public class DmsImportThread extends Thread {
    private static final Logger myLogger = Logger.getLogger(DmsImportThread.class);
    private SafeFile fileError;
    private SafeFile fileXml;
    private SafeFile fileSuccess;
    private SafeFile folderImages;
    private long timeFileSuccess;
    private long timeFileError;

    public String result = "";

    public boolean stop = false;

    /**
     * Constructor.
     *
     * @param process
     *            object
     * @param ats
     *            String
     */
    public DmsImportThread(Process process, String ats) {
        setDaemon(true);
        /*
         * aus Kompatibilitätsgründen auch noch die Fehlermeldungen an alter
         * Stelle, ansonsten lieber in neuem FehlerOrdner
         */
        if (process.getProject().getDmsImportErrorPath() == null
                || process.getProject().getDmsImportErrorPath().length() == 0) {
            this.fileError = new SafeFile(process.getProject().getDmsImportRootPath(), ats + ".log");
        } else {
            this.fileError = new SafeFile(process.getProject().getDmsImportErrorPath(), ats + ".log");
        }

        this.fileXml = new SafeFile(process.getProject().getDmsImportRootPath(), ats + ".xml");
        this.fileSuccess = new SafeFile(process.getProject().getDmsImportSuccessPath(), ats + ".xml");
        if (process.getProject().isDmsImportCreateProcessFolder()) {
            this.fileSuccess = new SafeFile(process.getProject().getDmsImportSuccessPath(),
                    process.getTitle() + File.separator + ats + ".xml");
        }

        this.folderImages = new SafeFile(process.getProject().getDmsImportImagesPath(), ats + "_tif");

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
                    if (this.fileError.exists()
                            && this.fileError.getAbsoluteFile().lastModified() > this.timeFileError) {
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
                        this.result = myBuf.toString();

                    }
                    if (this.fileSuccess.exists()
                            && this.fileSuccess.getAbsoluteFile().lastModified() > this.timeFileSuccess) {
                        this.stop = true;
                    }
                }
            } catch (Throwable t) {
                myLogger.error("Unexception exception", t);
            }
        }
        if (!ConfigCore.getBooleanParameter("exportWithoutTimeLimit")) {
            /* Images wieder löschen */
            this.folderImages.deleteDir();
        }
    }

    public void stopThread() {
        this.result = "Import wurde wegen Zeitüberschreitung abgebrochen";
        this.stop = true;
    }

}
