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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

public class DmsImportThread extends Thread {
    private static final Logger logger = LogManager.getLogger(DmsImportThread.class);
    private File fileError;
    private File fileXml;
    private File fileSuccess;
    private File folderImages;
    private long timeFileSuccess;
    private long timeFileError;

    private static final ServiceManager serviceManager = new ServiceManager();

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
            this.fileError = new File(process.getProject().getDmsImportRootPath(), ats + ".log");
        } else {
            this.fileError = new File(process.getProject().getDmsImportErrorPath(), ats + ".log");
        }

        this.fileXml = new File(process.getProject().getDmsImportRootPath(), ats + ".xml");
        this.fileSuccess = new File(process.getProject().getDmsImportSuccessPath(), ats + ".xml");
        if (process.getProject().isDmsImportCreateProcessFolder()) {
            this.fileSuccess = new File(process.getProject().getDmsImportSuccessPath(),
                    process.getTitle() + File.separator + ats + ".xml");
        }

        this.folderImages = new File(process.getProject().getDmsImportImagesPath(), ats + "_tif");

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
                        try (BufferedReader r = new BufferedReader(
                                new InputStreamReader(serviceManager.getFileService().read(this.fileError.toURI())))) {
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
            } catch (InterruptedException | IOException | RuntimeException e) {
                Helper.setErrorMessage("Unexpected exception", logger, e);
            }
        }
        if (!ConfigCore.getBooleanParameter("exportWithoutTimeLimit")) {
            /* Images wieder löschen */
            try {
                serviceManager.getFileService().delete(folderImages.toURI());
            } catch (IOException e) {
                logger.warn("IOException. Could not delete image folder");
            }
        }
    }

    public void stopThread() {
        this.result = "Import wurde wegen Zeitüberschreitung abgebrochen";
        this.stop = true;
    }

}
