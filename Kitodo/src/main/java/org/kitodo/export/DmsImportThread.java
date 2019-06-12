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

package org.kitodo.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;

public class DmsImportThread extends Thread {
    private static final Logger logger = LogManager.getLogger(DmsImportThread.class);
    private File fileError;
    private File fileXml;
    private File fileSuccess;
    private File folderImages;
    private long timeFileSuccess;
    private long timeFileError;
    private String result = "";
    private boolean stop = false;

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
        Project project = process.getProject();

        this.folderImages = new File(project.getDmsImportRootPath(), ats + "_tif");

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
                    processOnError();
                    processOnSuccess();
                }
            } catch (IOException e) {
                logger.error("Problem with file processing!", e);
            } catch (InterruptedException e) {
                logger.error("Current thread was interrupted!", e);
                Thread.currentThread().interrupt();
            }
        }

        removeImages();
    }

    void stopThread() {
        this.result = "Import wurde wegen Zeitüberschreitung abgebrochen";
        this.stop = true;
    }

    /**
     * Get result.
     *
     * @return value of result
     */
    public String getResult() {
        return result;
    }

    private void processOnError() throws IOException {
        if (this.fileError.exists() && this.fileError.getAbsoluteFile().lastModified() > this.timeFileError) {
            this.stop = true;
            this.result = readErrorFile();
        }
    }

    private void processOnSuccess() {
        if (this.fileSuccess.exists() && this.fileSuccess.getAbsoluteFile().lastModified() > this.timeFileSuccess) {
            this.stop = true;
        }
    }

    private String readErrorFile() throws IOException {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Beim Import ist ein Importfehler aufgetreten: ");
        try (InputStream inputStream = ServiceManager.getFileService().read(this.fileError.toURI());
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = r.readLine();
            while (Objects.nonNull(line)) {
                errorMessage.append(line);
                errorMessage.append(" ");
                line = r.readLine();
            }
        }
        return errorMessage.toString();
    }

    private void removeImages() {
        if (!ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_WITHOUT_TIME_LIMIT)) {
            try {
                ServiceManager.getFileService().delete(folderImages.toURI());
            } catch (IOException e) {
                logger.warn("IOException. Could not delete image folder");
            }
        }
    }
}
