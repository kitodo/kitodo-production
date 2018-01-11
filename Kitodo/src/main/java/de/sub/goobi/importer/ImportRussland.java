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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.UghImplementation;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen
 * Eigenschaften und erlaubt die Bearbeitung der Schrittdetails.
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */
public class ImportRussland {
    private static final Logger logger = LogManager.getLogger(ImportRussland.class);
    private DocStructInterface logicalTopstruct;
    private Process prozess;
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Allgemeiner Konstruktor ().
     */
    public ImportRussland() {
    }

    /**
     * Parsen.
     *
     * @param reader
     *            BufferedReader object
     * @param inProzess
     *            Process object
     */
    protected void parse(BufferedReader reader, Process inProzess) throws IOException, WrongImportFileException,
            MetadataTypeNotAllowedException, ReadException, PreferencesException, WriteException {

        /*
         * prüfen, ob die Importdatei korrekt ist und wirklich zu dem Prozess
         * gehört
         */
        this.prozess = inProzess;
        String prozessID = String.valueOf(inProzess.getId().intValue());
        String line = reader.readLine();
        // logger.info(line + " : " + myProzesseID);
        if (line == null) {
            throw new WrongImportFileException("Importfehler: ungültige Importdatei oder falsche Kodierung");
        }

        if (!line.equals("+ " + prozessID + " (ProzessID)")) {
            throw new WrongImportFileException(
                    "Importfehler: Importdatei gehört zu einem anderen Werk ('" + prozessID + "' <> '" + line + "')");
        }

        /*
         * xml-Datei einlesen und Hauptelement ermitteln
         */
        FileformatInterface gdzfile = serviceManager.getProcessService().readMetadataFile(inProzess);
        DigitalDocumentInterface mydocument;
        mydocument = gdzfile.getDigitalDocument();
        this.logicalTopstruct = mydocument.getLogicalDocStruct();
        deleteRussianData(this.logicalTopstruct);
        // if (1 == 1) {
        // gdzfile.Write(help.metadatenverzeichnis() + myProzesseID +
        // "/meta.xml");
        // return;
        // }

        /*
         * alle Zeilen durchlaufen
         */
        List<String> listeDaten = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            // logger.info(line);
            if (line.length() == 0) {

                /*
                 * immer wenn die Zeile leer ist, können die gesammelten Daten
                 * aus der gesammelten Liste ausgewertet werden
                 */
                analyzeParagraph(listeDaten);
                /* Liste wieder zurücksetzen */
                listeDaten = new ArrayList<>();
            } else if (!line.substring(0, 1).equals("+")) {
                /*
                 * wenn zeile kein Kommentar ist, Zeile in Liste für Auswertung
                 * übernehmen
                 */
                if (line.length() > 3) {
                    listeDaten.add(line);
                }
            }
        }

        /*
         * Datei abschliessend wieder speichern
         */
        serviceManager.getFileService().writeMetadataFile(gdzfile, inProzess);
        logger.debug("ParsenRussland() - Ende");
    }

    private void analyzeParagraph(List<String> inListe)
            throws ugh.exceptions.MetadataTypeNotAllowedException, WrongImportFileException {
        if (inListe.size() == 0) {
            return;
        }

        String detail = inListe.get(0);
        String meineDetailNr = detail.substring(0, 3);

        if (meineDetailNr.equals("080") || meineDetailNr.equals("090")) {
            setArticleDetails(inListe);
        } else if (meineDetailNr.equals("010")) {
            setJournalDetails(inListe);
        } else if (meineDetailNr.equals("050")) {
            setBandDetails(inListe);
        } else {
            throw new WrongImportFileException("Parsingfehler: Neuer Block mit ungültigem ersten Identifier ('" + detail
                    + "'), möglicherweise sind an einer falschen Stelle Zeilenumbrüche eingefügt worden.");
        }
    }

    private void setJournalDetails(List<String> inListe) throws MetadataTypeNotAllowedException {
        /* zunächst alle Details durchlaufen und der Zeitschrift hinzufügenl */
        for (String journalDetail : inListe) {
            String meineDetailNr = journalDetail.substring(0, 3);
            // logger.debug("---- " + meinDetail);

            /* Zeitschrift Titel russisch */
            if (meineDetailNr.equals("020")) {
                addMetadata(this.logicalTopstruct, "RUSMainTitle", journalDetail);
            }

            /* Zeitschrift Herausgeber (wiederholbar) */
            if (meineDetailNr.equals("030")) {
                addMetadata(this.logicalTopstruct, "RUSPublisher", journalDetail);
            }

            /* Zeitschrift Ort (wiederholbar) */
            if (meineDetailNr.equals("040")) {
                addMetadata(this.logicalTopstruct, "RUSPlaceOfPublication", journalDetail);
            }

            /* Verlag / Publishing house - russisch */
            if (meineDetailNr.equals("042")) {
                addMetadata(this.logicalTopstruct, "RUSPublicationHouse", journalDetail);
            }
        }
    }

    private void setBandDetails(List<String> inListe) throws MetadataTypeNotAllowedException {
        DocStructInterface ds = this.logicalTopstruct.getAllChildren().get(0);
        // logger.info(ds.getType().getName());
        /* zunächst alle Details durchlaufen und dem Band hinzufügenl */
        for (String bandDetail : inListe) {
            String meineDetailNr = bandDetail.substring(0, 3);

            /* Band Herausgeber (wiederholbar) */
            if (meineDetailNr.equals("060")) {
                addMetadata(ds, "RUSPublisher", bandDetail);
            }

            /* Band Ort (wiederholbar) */
            if (meineDetailNr.equals("070")) {
                addMetadata(ds, "RUSPlaceOfPublication", bandDetail);
            }
        }
    }

    private void setArticleDetails(List<String> inListe)
            throws MetadataTypeNotAllowedException, WrongImportFileException {
        boolean artikelGefunden = false;

        /*
         * zunächst alle Details durchlaufen und die ZBL-ID des Artikels
         * ermitteln
         */
        String zblID = "";
        for (String articleDetail : inListe) {
            if (articleDetail.substring(0, 3).equals("090")) {
                zblID = articleDetail.substring(4).trim();
                break;
            }
        }

        /* für das Debugging bei Problemen */
        // if (zblID.equals("0843.11050"))
        // logger.warn("gesuchte ID");

        /*
         * alle Hefte und Artikel durchlaufen und den richtigen Artikel mit der
         * selben ZBL-ID finden
         */
        MetadataTypeInterface metadataTypeId = serviceManager.getRulesetService().getPreferences(this.prozess.getRuleset())
                .getMetadataTypeByName("ZBLIdentifier");
        MetadataTypeInterface metadataTypeTempId = serviceManager.getRulesetService().getPreferences(this.prozess.getRuleset())
                .getMetadataTypeByName("ZBLTempID");
        DocStructInterface band = this.logicalTopstruct.getAllChildren().get(0);
        // logger.info(band.getType().getName());
        List<DocStructInterface> listHefte = band.getAllChildren();
        if (listHefte != null) {
            for (Iterator<DocStructInterface> iter = listHefte.iterator(); iter.hasNext();) {
                DocStructInterface heft = iter.next();
                List<DocStructInterface> listArtikel = heft.getAllChildren();
                if (listArtikel != null) {
                    /*
                     * jetzt alle Artikel durchlaufen, bis der richtige Artikel
                     * gefunden wurde
                     */
                    for (DocStructInterface article : listArtikel) {
                        // logger.info(artikel.getType().getName());
                        if (article.getAllMetadataByType(metadataTypeId).size() > 0
                                || article.getAllMetadataByType(metadataTypeTempId).size() > 0) {
                            MetadataInterface md;
                            if (article.getAllMetadataByType(metadataTypeId).size() > 0) {
                                md = article.getAllMetadataByType(metadataTypeId).get(0);
                            } else {
                                md = article.getAllMetadataByType(metadataTypeTempId).get(0);
                            }
                            // logger.debug(md.getValue());
                            if (md.getValue().equals(zblID)) {
                                // logger.info("------------ Artikel gefunden
                                // -------------");
                                artikelGefunden = true;
                                /*
                                 * jetzt alle Details durchlaufen und dem
                                 * Artikel hinzufügenl
                                 */
                                for (String detail : inListe) {
                                    String meineDetailNr = detail.substring(0, 3);

                                    /* Artikel Autor russisch (wiederholbar) */
                                    if (meineDetailNr.equals("120")) {
                                        addPerson(article, "Author", detail);
                                    }

                                    /* Artikel Autor-Variation (wiederholbar) */
                                    if (meineDetailNr.equals("130")) {
                                        addPerson(article, "AuthorVariation", detail);
                                    }

                                    /*
                                     * Artikel Autor-Kontributor (wiederholbar)
                                     */
                                    if (meineDetailNr.equals("140")) {
                                        addPerson(article, "Contributor", detail);
                                    }

                                    /*
                                     * Artikel Person als Subjekt des Artikels
                                     * (wiederholbar)
                                     */
                                    if (meineDetailNr.equals("150")) {
                                        addMetadata(article, "PersonAsSubject", detail);
                                    }

                                    /* Artikel Titel russisch */
                                    if (meineDetailNr.equals("170")) {
                                        addMetadata(article, "RUSMainTitle", detail);
                                    }

                                    /*
                                     * Artikel Klassifikation UDK (wiederholbar)
                                     */
                                    if (meineDetailNr.equals("190")) {
                                        addMetadata(article, "ClassificationUDK", detail);
                                    }

                                    /* Artikel Keywords russisch */
                                    if (meineDetailNr.equals("210")) {
                                        addMetadata(article, "RUSKeyword", detail);
                                    }
                                }
                                return;
                            }
                        }
                    }
                }

                if (!iter.hasNext() && !artikelGefunden) {
                    throw new WrongImportFileException(
                            "Parsingfehler: Artikel mit der ZBL-ID wurde nicht gefunden ('" + zblID + "')");
                }
            }
        } else {
            throw new WrongImportFileException(
                    "Parsingfehler: Es sind bisher keine Artikel angelegt worden, zu denen Daten ergänzt werden könnten");
        }
    }

    private void deleteRussianData(DocStructInterface inStrukturelement) {
        /*
         * von dem aktuellen Stukturelement alle Metadaten durchlaufen und das
         * gesuchte löschen
         */
        if (inStrukturelement.getAllVisibleMetadata() != null) {
            LinkedList<MetadataInterface> listMetas = new LinkedList<>(inStrukturelement.getAllMetadata());
            for (MetadataInterface meta : listMetas) {
                String myMetaName = meta.getType().getName();

                /*
                 * wenn die Metadatentypen die russischen sind, werden sie aus
                 * der Liste entfernt
                 */
                if (myMetaName.equals("PersonAsSubject") || myMetaName.equals("RUSMainTitle")
                        || myMetaName.equals("ClassificationUDK") || myMetaName.equals("RUSKeyword")
                        || myMetaName.equals("RUSPublisher") || myMetaName.equals("RUSPlaceOfPublication")
                        || myMetaName.equals("RUSPublicationHouse") || myMetaName.equals("RUSPublisher")) {
                    inStrukturelement.removeMetadata(meta);
                }
            }
        }

        /*
         * von dem aktuellen Stukturelement alle Personen durchlaufen und die
         * gesuchten löschen
         */
        if (inStrukturelement.getAllPersons() != null) {
            List<PersonInterface> listPersons = new ArrayList<>(inStrukturelement.getAllPersons());
            for (PersonInterface p : listPersons) {
                if (p.getRole().equals("Author")) {
                    inStrukturelement.removePerson(p);
                }
            }
        }

        /*
         * von dem aktuellen Stukturelement alle Kinder durchlaufen und rekursiv
         * durchlaufen
         */
        List<DocStructInterface> listKinder = inStrukturelement.getAllChildren();
        if (listKinder != null) {
            /* es gibt Kinder-Strukturelemente, also alle Kinder durchlaufen */
            for (DocStructInterface kind : listKinder) {
                deleteRussianData(kind);
            }
        }
    }

    private void addMetadata(DocStructInterface inStruct, String inMdtName, String inDetail)
            throws MetadataTypeNotAllowedException {
        MetadataTypeInterface mdt = serviceManager.getRulesetService().getPreferences(this.prozess.getRuleset())
                .getMetadataTypeByName(inMdtName);
        MetadataInterface md = UghImplementation.INSTANCE.createMetadata(mdt);
        try {
            md.setValue(inDetail.substring(4).trim());

            /*
             * prüfen, ob das Metadatum schon existiert, wenn nein, neu anlegen
             */

            // LinkedList list = inStruct.getAllChildren();
            // if (list != null) {
            //
            // /* jetzt alle Artikel durchlaufen, bis der richtige Artikel
            // gefunden wurde */
            // for (Iterator firstIterator = listArtikel.iterator();
            // firstIterator.hasNext();) {
            // DocStruct artikel = (DocStruct) firstIterator.next();
            // Metadata md = (Metadata)
            // artikel.getAllMetadataByType(mdt).getFirst();
            // logger.debug(md.getValue());
            // if (md.getValue().equals(zblID)) {
            // logger.info("------------ Artikel gefunden -------------");
            inStruct.addMetadata(md);
        } catch (Exception e) {
            logger.error("Import fehlgeschlagen: " + inDetail, e);
        }
    }

    private void addPerson(DocStructInterface inStruct, String inRole, String inDetail)
            throws MetadataTypeNotAllowedException, WrongImportFileException {
        PersonInterface p = UghImplementation.INSTANCE.createPerson(
            serviceManager.getRulesetService().getPreferences(this.prozess.getRuleset()).getMetadataTypeByName(inRole));
        String pName = inDetail.substring(4).trim();
        if (pName.length() == 0) {
            return;
        }
        if (!pName.contains(",")) {
            throw new WrongImportFileException(
                    "Parsingfehler: Vorname nicht mit Komma vom Nachnamen getrennt ('" + inDetail + "')");
        }
        p.setLastname(pName.substring(0, pName.indexOf(",")).trim());
        p.setFirstname(pName.substring(pName.indexOf(",") + 1, pName.length()).trim());
        p.setRole(inRole);
        // MetadataType mdt =
        // prozess.getRegelsatz().getPreferences().getMetadataTypeByName(inRole);
        // p.setType(mdt);
        inStruct.addPerson(p);
    }
}
