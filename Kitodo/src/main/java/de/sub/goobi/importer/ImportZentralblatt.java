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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.WrongImportFileException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.XStream;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen
 * Eigenschaften und erlaubt die Bearbeitung der Schrittdetails.
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */
public class ImportZentralblatt {
    private static final Logger logger = LogManager.getLogger(ImportZentralblatt.class);
    private String separator;
    private final Helper help;
    private Prefs myPrefs;
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Allgemeiner Konstruktor ().
     */
    public ImportZentralblatt() {
        this.help = new Helper();
    }

    /**
     * Parse.
     *
     * @param reader
     *            BufferedReader object
     * @param inProzess
     *            Process object
     */
    protected void parse(BufferedReader reader, Process inProzess)
            throws IOException, WrongImportFileException, TypeNotAllowedForParentException,
            TypeNotAllowedAsChildException, MetadataTypeNotAllowedException, WriteException {
        logger.debug("ParsenZentralblatt() - start");
        this.myPrefs = serviceManager.getRulesetService().getPreferences(inProzess.getRuleset());
        String prozessID = String.valueOf(inProzess.getId().intValue());
        this.separator = ":";
        boolean istAbsatz = false;
        boolean istErsterTitel = true;
        LinkedList<DocStruct> listArtikel = new LinkedList<>();

        /*
         * Vorbereitung der Dokumentenstruktur
         */
        DigitalDocument dd = new DigitalDocument();
        DocStructType dst = this.myPrefs.getDocStrctTypeByName("Periodical");
        DocStruct dsPeriodical = dd.createDocStruct(dst);
        dst = this.myPrefs.getDocStrctTypeByName("PeriodicalVolume");
        DocStruct dsPeriodicalVolume = dd.createDocStruct(dst);
        dsPeriodical.addChild(dsPeriodicalVolume);

        /*
         * alle Zeilen durchlaufen
         */
        String line;
        while ((line = reader.readLine()) != null) {
            // logger.debug(line);

            /*
             * wenn die Zeile leer ist, ist es das Ende eines Absatzes
             */
            if (line.length() == 0) {
                istAbsatz = false;
                /* wenn die Zeile nicht leer ist, den Inhalt prüfen */
            } else {

                /* prüfen ob der String korrekte xml-Zeichen enthält */
                String xmlTauglich = checkXmlSuitability(line);
                if (xmlTauglich.length() > 0) {
                    throw new WrongImportFileException("Parsingfehler (nicht druckbares Zeichen) der Importdatei "
                            + "in der Zeile <br/>" + xmlTauglich);
                }

                /*
                 * wenn es gerade ein neuer Absatz ist, diesen als neuen Artikel
                 * in die Liste übernehmen
                 */
                if (!istAbsatz) {
                    DocStructType dstLocal = this.myPrefs.getDocStrctTypeByName("Article");
                    DocStruct ds = dd.createDocStruct(dstLocal);
                    listArtikel.add(ds);
                    // logger.debug("--------------- neuer Artikel
                    // ----------------");
                    istAbsatz = true;
                    istErsterTitel = true;
                }

                /* Position des Trennzeichens ermitteln */
                int posTrennzeichen = line.indexOf(this.separator);
                /* wenn kein Trennzeichen vorhanden, Parsingfehler */
                if (posTrennzeichen == -1) {
                    logger.error("Import() - Parsingfehler (kein Doppelpunkt) der Importdatei in der Zeile <br/>"
                            + maskHtmlTags(line));
                    throw new WrongImportFileException("Parsingfehler (kein Doppelpunkt) der Importdatei in "
                            + "der Zeile <br/>" + maskHtmlTags(line));
                } else {
                    String myLeft = line.substring(0, posTrennzeichen).trim();
                    String myRight = line.substring(posTrennzeichen + 1, line.length()).trim();
                    parseArticle(listArtikel.getLast(), myLeft, myRight, istErsterTitel);

                    /*
                     * wenn es ein Titel war, ist der nächste nicht mehr der
                     * erste Titel
                     */
                    if (myLeft.equals("TI")) {
                        istErsterTitel = false;
                    }

                    /*
                     * wenn es gerade der Zeitschriftenname ist, die Zeitschrift
                     * benennen
                     */
                    if (myLeft.equals("J")) {
                        parseGeneral(dsPeriodical, myLeft, myRight);
                    }

                    /*
                     * wenn es gerade eine Jahresangabe ist, dann für den
                     * aktuellen Band
                     */
                    if (myLeft.equals("Y")) {
                        parseGeneral(dsPeriodicalVolume, myLeft, myRight);
                    }

                    /*
                     * wenn es gerade eine Jahresangabe ist, dann für den
                     * aktuellen Band
                     */
                    if (myLeft.equals("V")) {
                        parseGeneral(dsPeriodicalVolume, myLeft, myRight);
                    }

                    /*
                     * wenn es gerade die Heftnummer ist, dann jetzt dem
                     * richtigen Heft zuordnen und dieses ggf. noch vorher
                     * anlegen
                     */
                    if (myLeft.equals("I")) {
                        DocStruct dsPeriodicalIssue = parsenIssueAssignment(dsPeriodicalVolume, myRight, dd);
                        dsPeriodicalIssue.addChild(listArtikel.getLast());
                    }
                }
            }
        }

        /*
         * physischer Baum (Seiten)
         */
        dst = this.myPrefs.getDocStrctTypeByName("BoundBook");
        DocStruct dsBoundBook = dd.createDocStruct(dst);

        /*
         * jetzt die Gesamtstruktur bauen und in xml schreiben
         */
        // DigitalDocument dd = new DigitalDocument();
        dd.setLogicalDocStruct(dsPeriodical);
        dd.setPhysicalDocStruct(dsBoundBook);
        try {
            Fileformat gdzfile = new XStream(this.myPrefs);
            gdzfile.setDigitalDocument(dd);

            /*
             * Datei am richtigen Ort speichern
             */
            gdzfile.write(ConfigCore.getKitodoDataDirectory() + prozessID + File.separator + "meta.xml");
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("Import aborted: ", e.getMessage());
            logger.error(e);
        }
        logger.debug("ParsenZentralblatt() - Ende");
    }

    private String checkXmlSuitability(String text) {
        int laenge = text.length();
        String rueckgabe = "";
        for (int i = 0; i < laenge; i++) {
            char c = text.charAt(i);

            if (!isValidXMLChar(c)) {
                rueckgabe = maskHtmlTags(text.substring(0, i)) + "<span class=\"parsingfehler\">" + c + "</span>";
                if (laenge > i) {
                    rueckgabe += maskHtmlTags(text.substring(i + 1, laenge));
                }
                break;
            }
        }
        return rueckgabe;
    }

    private static final boolean isValidXMLChar(char c) {
        switch (c) {
            case 0x9:
            case 0xa: // line feed, '\n'
            case 0xd: // carriage return, '\r'
                return true;
            default:
                return ((0x20 <= c && c <= 0xd7ff) || (0xe000 <= c && c <= 0xfffd));
        }
    }

    private String maskHtmlTags(String in) {
        return (in.replaceAll("<", "&lt;")).replaceAll(">", "&gt");
    }

    /**
     * Funktion für das Ermitteln des richtigen Heftes für einen Artikel Liegt
     * das Heft noch nicht in dem Volume vor, wird es angelegt. Als Rückgabe
     * kommt das Heft als DocStruct
     *
     * @param dsPeriodicalVolume
     *            DocStruct object
     * @param myRight
     *            String
     * @return DocStruct of periodical
     */
    private DocStruct parsenIssueAssignment(DocStruct dsPeriodicalVolume, String myRight,
            DigitalDocument inDigitalDocument)
            throws TypeNotAllowedForParentException, MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {
        DocStructType dst;
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("CurrentNo");
        DocStruct dsPeriodicalIssue = null;
        /* erstmal prüfen, ob das Heft schon existiert */
        List<DocStruct> myList = dsPeriodicalVolume.getAllChildrenByTypeAndMetadataType("PeriodicalIssue", "CurrentNo");
        if (myList != null && myList.size() != 0) {
            for (DocStruct dsIntern : myList) {
                // logger.debug(dsIntern.getAllMetadataByType(mdt).getFirst());
                Metadata metadata = dsIntern.getAllMetadataByType(mdt).get(0);
                // logger.debug("und der Wert ist: " + myMD1.getValue());
                if (metadata.getValue().equals(myRight)) {
                    dsPeriodicalIssue = dsIntern;
                }
            }
        }
        /* wenn das Heft nicht gefunden werden konnte, jetzt anlegen */
        if (dsPeriodicalIssue == null) {
            dst = this.myPrefs.getDocStrctTypeByName("PeriodicalIssue");
            dsPeriodicalIssue = inDigitalDocument.createDocStruct(dst);
            Metadata myMD = new Metadata(mdt);
            // myMD.setType(mdt);
            myMD.setValue(myRight);
            dsPeriodicalIssue.addMetadata(myMD);
            dsPeriodicalVolume.addChild(dsPeriodicalIssue);
        }
        return dsPeriodicalIssue;
    }

    /**
     * General parsing.
     */
    private void parseGeneral(DocStruct inStruct, String myLeft, String myRight)
            throws WrongImportFileException, MetadataTypeNotAllowedException {

        // logger.debug(myLeft);
        // logger.debug(myRight);
        // logger.debug("---");
        Metadata md;
        MetadataType mdt;

        // J: Zeitschrift
        // V: Band
        // I: Heft
        // Y: Jahrgang

        /*
         * Zeitschriftenname
         */
        if (myLeft.equals("J")) {
            mdt = this.myPrefs.getMetadataTypeByName("TitleDocMain");
            List<? extends ugh.dl.Metadata> myList = inStruct.getAllMetadataByType(mdt);
            /* wenn noch kein Zeitschriftenname vergeben wurde, dann jetzt */
            if (myList.size() == 0) {
                md = new Metadata(mdt);
                // md.setType(mdt);
                md.setValue(myRight);
                inStruct.addMetadata(md);
            } else {
                /*
                 * wurde schon ein Zeitschriftenname vergeben, prüfen, ob dieser
                 * genauso lautet
                 */
                md = myList.get(0);
                if (!myRight.equals(md.getValue())) {
                    throw new WrongImportFileException("Parsingfehler: verschiedene Zeitschriftennamen in der Datei ('"
                            + md.getValue() + "' & '" + myRight + "')");
                }
            }
            return;
        }

        /*
         * Jahrgang
         */
        if (myLeft.equals("Y")) {
            mdt = this.myPrefs.getMetadataTypeByName("PublicationYear");
            List<? extends ugh.dl.Metadata> myList = inStruct.getAllMetadataByType(mdt);

            /* wenn noch kein Zeitschriftenname vergeben wurde, dann jetzt */
            if (myList.size() == 0) {
                md = new Metadata(mdt);
                // md.setType(mdt);
                md.setValue(myRight);
                inStruct.addMetadata(md);
            } else {

                /*
                 * wurde schon ein Zeitschriftenname vergeben, prüfen, ob dieser
                 * genauso lautet
                 */
                /*
                 * da Frau Jansch ständig Importprobleme mit jahrübergreifenden
                 * Bänden hat, jetzt mal auskommentiert
                 */
                // md = myList.get(0);
                // if (!myRight.equals(md.getValue()))
                // throw new WrongImportFileException("Parsingfehler:
                // verschiedene Jahresangaben in der Datei ('"
                // + md.getValue() + "' & '" + myRight + "')");
            }
            return;
        }

        /*
         * Bandnummer
         */
        if (myLeft.equals("V")) {
            mdt = this.myPrefs.getMetadataTypeByName("CurrentNo");
            List<? extends ugh.dl.Metadata> myList = inStruct.getAllMetadataByType(mdt);

            /* wenn noch keine Bandnummer vergeben wurde, dann jetzt */
            if (myList.size() == 0) {
                md = new Metadata(mdt);
                md.setValue(myRight);
                inStruct.addMetadata(md);
            } else {

                /*
                 * wurde schon eine Bandnummer vergeben, prüfen, ob dieser
                 * genauso lautet
                 */
                md = myList.get(0);
                if (!myRight.equals(md.getValue())) {
                    throw new WrongImportFileException("Parsingfehler: verschiedene Bandangaben in der Datei ('"
                            + md.getValue() + "' & '" + myRight + "')");
                }
            }
        }
    }

    /**
     * Parse article.
     */
    private void parseArticle(DocStruct inStruct, String myLeft, String myRight, boolean istErsterTitel)
            throws MetadataTypeNotAllowedException, WrongImportFileException {
        // logger.debug(myLeft);
        // logger.debug(myRight);
        // logger.debug("---");
        Metadata md;
        MetadataType mdt;

        // J: Zeitschrift
        // V: Band
        // I: Heft
        // Y: Jahrgang
        // SO: Quelle (fuer uns intern)
        // AR: Author (Referenz)
        // BR: Biographische Referenz
        // AB: Abstract-Review
        // DE: Vorlaeufige AN-Nummer (eher fuer uns intern)
        // SI: Quellenangabe für Rezension im Zentralblatt
        //

        /*
         * erledigt
         *
         * TI: Titel AU: Autor LA: Sprache NH: Namensvariationen CC: MSC 2000
         * KW: Keywords AN: Zbl und/oder JFM Nummer P: Seiten
         */

        /*
         * Titel
         */
        if (myLeft.equals("TI")) {
            if (istErsterTitel) {
                mdt = this.myPrefs.getMetadataTypeByName("TitleDocMain");
            } else {
                mdt = this.myPrefs.getMetadataTypeByName("MainTitleTranslated");
            }
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * Sprache
         */
        if (myLeft.equals("LA")) {
            mdt = this.myPrefs.getMetadataTypeByName("DocLanguage");
            md = new Metadata(mdt);
            md.setValue(myRight.toLowerCase());
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLIdentifier
         */
        if (myLeft.equals("AN")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLIdentifier");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLPageNumber
         */
        if (myLeft.equals("P")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLPageNumber");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLSource
         */
        if (myLeft.equals("SO")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLSource");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLAbstract
         */
        if (myLeft.equals("AB")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLAbstract");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLReviewAuthor
         */
        if (myLeft.equals("RV")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLReviewAuthor");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLCita
         */
        if (myLeft.equals("CI")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLCita");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLTempID
         */
        if (myLeft.equals("DE")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLTempID");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLReviewLink
         */
        if (myLeft.equals("SI")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLReviewLink");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * ZBLIntern
         */
        if (myLeft.equals("XX")) {
            mdt = this.myPrefs.getMetadataTypeByName("ZBLIntern");
            md = new Metadata(mdt);
            md.setValue(myRight);
            inStruct.addMetadata(md);
            return;
        }

        /*
         * Keywords
         */
        if (myLeft.equals("KW")) {
            StringTokenizer tokenizer = new StringTokenizer(myRight, ";");
            while (tokenizer.hasMoreTokens()) {
                md = new Metadata(this.myPrefs.getMetadataTypeByName("Keyword"));
                String myTok = tokenizer.nextToken();
                md.setValue(myTok.trim());
                inStruct.addMetadata(md);
            }
            return;
        }

        /*
         * Autoren als Personen
         */
        if (myLeft.equals("AU")) {
            StringTokenizer tokenizer = new StringTokenizer(myRight, ";");
            while (tokenizer.hasMoreTokens()) {
                Person p = new Person(this.myPrefs.getMetadataTypeByName("ZBLAuthor"));
                String myTok = tokenizer.nextToken();

                if (!myTok.contains(",")) {
                    throw new WrongImportFileException(
                            "Parsingfehler: Vorname nicht mit Komma vom Nachnamen getrennt ('" + myTok + "')");
                }

                p.setLastname(myTok.substring(0, myTok.indexOf(",")).trim());
                p.setFirstname(myTok.substring(myTok.indexOf(",") + 1, myTok.length()).trim());
                p.setRole("ZBLAuthor");
                inStruct.addPerson(p);
            }
            return;
        }

        /*
         * AutorVariationen als Personen
         */
        if (myLeft.equals("NH")) {
            StringTokenizer tokenizer = new StringTokenizer(myRight, ";");
            while (tokenizer.hasMoreTokens()) {
                Person p = new Person(this.myPrefs.getMetadataTypeByName("AuthorVariation"));
                String myTok = tokenizer.nextToken();

                if (!myTok.contains(",")) {
                    throw new WrongImportFileException(
                            "Parsingfehler: Vorname nicht mit Komma vom Nachnamen getrennt ('" + myTok + "')");
                }

                p.setLastname(myTok.substring(0, myTok.indexOf(",")).trim());
                p.setFirstname(myTok.substring(myTok.indexOf(",") + 1, myTok.length()).trim());
                p.setRole("AuthorVariation");
                inStruct.addPerson(p);
            }
            return;
        }

        /*
         * MSC 2000 - ClassificationMSC
         */
        if (myLeft.equals("CC")) {
            StringTokenizer tokenizer = new StringTokenizer(myRight);
            while (tokenizer.hasMoreTokens()) {
                md = new Metadata(this.myPrefs.getMetadataTypeByName("ClassificationMSC"));
                String myTok = tokenizer.nextToken();
                md.setValue(myTok.trim());
                inStruct.addMetadata(md);
            }
        }
    }

}
