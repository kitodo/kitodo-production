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
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Task;

/**
 * Import von Metadaten aus upgeloadeten Dateien
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 25.06.2005
 */
public class Import {
    private static final Logger logger = LogManager.getLogger(Import.class);
    private String importFehler = "";
    private String importMeldung = "";
    private Task mySchritt;
    private UploadedFile upDatei;

    /**
     * Allgemeiner Konstruktor ().
     */
    public Import() {
    }

    /**
     * Start.
     *
     * @return String
     */
    public String start() {
        logger.info("Import start - start");
        this.importFehler = "";
        this.importMeldung = "";
        try {
            // Einlesen(prozessID.toString());
            read();
        } catch (Exception e) {
            this.importFehler = "An error occurred: " + e.getMessage();
            logger.error(e);
        }
        logger.info("Import start - ende");
        return "";
    }

    private void read() throws IOException, WrongImportFileException, TypeNotAllowedForParentException,
            TypeNotAllowedAsChildException, MetadataTypeNotAllowedException, ReadException, PreferencesException,
            WriteException {
        logger.debug("Einlesen() - start");
        BufferedReader reader = null;
        try {

            /*
             * prüfen ob es ein russischer oder ein zbl-Import ist und
             * entsprechende Routine aufrufen
             */

            /* russischer Import */
            if (this.mySchritt.isTypeImportFileUpload() && this.mySchritt.isTypeExportRussian()) {
                String gesamteDatei = new String(this.upDatei.getBytes(), StandardCharsets.UTF_16LE);
                reader = new BufferedReader(new StringReader(gesamteDatei));
                ImportRussland myImport = new ImportRussland();
                myImport.parse(reader, this.mySchritt.getProcess());
                this.importMeldung = "Der russische Import wurde erfolgreich abgeschlossen";
            }

            /* Zentralblatt-Import */
            if (this.mySchritt.isTypeImportFileUpload() && !this.mySchritt.isTypeExportRussian()) {
                String gesamteDatei = new String(this.upDatei.getBytes(), StandardCharsets.ISO_8859_1);
                reader = new BufferedReader(new StringReader(gesamteDatei));
                ImportZentralblatt myImport = new ImportZentralblatt();
                myImport.parse(reader, this.mySchritt.getProcess());
                this.importMeldung = "Der Zentralblatt-Import wurde erfolgreich abgeschlossen";
            }

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("Die Datei kann nicht geschlossen werden", e);
                }
            }
        }
        /* wenn alles ok ist, 0 zurückgeben */
        logger.debug("Einlesen() - Ende");
    }

    /*
     * allgemeine Getter und Setter
     */

    public String getImportFehler() {
        return this.importFehler;
    }

    public String getImportMeldung() {
        return this.importMeldung;
    }

    public UploadedFile getUpDatei() {
        return this.upDatei;
    }

    public void setUpDatei(UploadedFile inUpDatei) {
        this.upDatei = inUpDatei;
    }

    public Task getMySchritt() {
        return this.mySchritt;
    }

    public void setMySchritt(Task mySchritt) {
        this.mySchritt = mySchritt;
    }

}
