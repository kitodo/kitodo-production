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

package de.sub.goobi.importer;

import de.sub.goobi.helper.exceptions.WrongImportFileException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.kitodo.data.database.beans.Task;

import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

/**
 * Import von Metadaten aus upgeloadeten Dateien
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 25.06.2005
 */
public class Import {
    private static final Logger logger = LogManager.getLogger(Import.class);
    private String importError = "";
    private String importMessage = "";
    private Task task;
    private UploadedFile uploadedFile;

    /**
     * Constructor.
     */
    public Import() {
    }

    /**
     * Start import.
     *
     * @return String
     */
    public String start() {
        logger.info("Import start - start");
        this.importError = "";
        this.importMessage = "";
        try {
            read();
        } catch (Exception e) {
            this.importError = "An error occurred: " + e.getMessage();
            logger.error(e);
        }
        logger.info("Import start - ende");
        return "";
    }

    private void read() throws IOException, WrongImportFileException, TypeNotAllowedForParentException,
            TypeNotAllowedAsChildException, MetadataTypeNotAllowedException, ReadException, PreferencesException,
            WriteException {
        logger.debug("Einlesen() - start");

        // Russian import
        if (this.task.isTypeImportFileUpload() && this.task.isTypeExportRussian()) {
            String fileContent = new String(this.uploadedFile.getBytes(), StandardCharsets.UTF_16LE);
            try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
                ImportRussland importRussland = new ImportRussland();
                importRussland.parse(reader, this.task.getProcess());
                this.importMessage = "Der russische Import wurde erfolgreich abgeschlossen";
            }
        }

        // Zentralblatt import
        if (this.task.isTypeImportFileUpload() && !this.task.isTypeExportRussian()) {
            String fileContent = new String(this.uploadedFile.getBytes(), StandardCharsets.ISO_8859_1);
            try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
                ImportZentralblatt importZentralblatt = new ImportZentralblatt();
                importZentralblatt.parse(reader, this.task.getProcess());
                this.importMessage = "Der Zentralblatt-Import wurde erfolgreich abgeschlossen";
            }
        }

        logger.debug("Einlesen() - Ende");
    }

    /**
     * Get message with import error.
     * 
     * @return import error as String
     */
    public String getImportError() {
        return this.importError;
    }

    /**
     * Get message with import information.
     * 
     * @return import message as String
     */
    public String getImportMessage() {
        return this.importMessage;
    }

    /**
     * Get uploaded file.
     * 
     * @return uploaded file
     */
    public UploadedFile getUploadedFile() {
        return this.uploadedFile;
    }

    /**
     * Set uploaded file.
     * 
     * @param uploadedFile
     *            as UploadedFile
     */
    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    /**
     * Get task for import.
     * 
     * @return Task object
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * Set task for import.
     * 
     * @param task
     *            as Task object
     */
    public void setTask(Task task) {
        this.task = task;
    }
}
