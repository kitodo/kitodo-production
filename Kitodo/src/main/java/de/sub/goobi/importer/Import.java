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

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;

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
    private static final Logger myLogger = Logger.getLogger(Import.class);
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
    public String Start() {
        myLogger.info("Import Start - start");
        this.importFehler = "";
        this.importMeldung = "";
        try {
            // Einlesen(prozessID.toString());
            Einlesen();
        } catch (Exception e) {
            this.importFehler = "An error occurred: " + e.getMessage();
            myLogger.error(e);
        }
        myLogger.info("Import Start - ende");
        return "";
    }

    private void Einlesen()
            throws IOException, WrongImportFileException, TypeNotAllowedForParentException,
            TypeNotAllowedAsChildException, MetadataTypeNotAllowedException, ReadException, InterruptedException,
            PreferencesException, SwapException, DAOException, WriteException {
        myLogger.debug("Einlesen() - Start");
        BufferedReader reader = null;
        try {

            /*
             * prüfen ob es ein russischer oder ein zbl-Import ist und entsprechende Routine aufrufen
             */

            /* russischer Import */
            if (this.mySchritt.isTypeImportFileUpload() && this.mySchritt.isTypeExportRussian() == true) {
                String gesamteDatei = new String(this.upDatei.getBytes(), StandardCharsets.UTF_16LE);
                reader = new BufferedReader(new StringReader(gesamteDatei));
                ImportRussland myImport = new ImportRussland();
                myImport.Parsen(reader, this.mySchritt.getProcess());
                this.importMeldung = "Der russische Import wurde erfolgreich abgeschlossen";
            }

            /* Zentralblatt-Import */
            if (this.mySchritt.isTypeImportFileUpload() && this.mySchritt.isTypeExportRussian() == false) {
                String gesamteDatei = new String(this.upDatei.getBytes(), StandardCharsets.ISO_8859_1);
                reader = new BufferedReader(new StringReader(gesamteDatei));
                ImportZentralblatt myImport = new ImportZentralblatt();
                myImport.Parsen(reader, this.mySchritt.getProcess());
                this.importMeldung = "Der Zentralblatt-Import wurde erfolgreich abgeschlossen";
            }

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    myLogger.error("Die Datei kann nicht geschlossen werden", e);
                }
            }
        }
        /* wenn alles ok ist, 0 zurückgeben */
        myLogger.debug("Einlesen() - Ende");
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
