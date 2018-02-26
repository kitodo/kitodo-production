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
    private Prefs myPrefs;
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Constructor.
     */
    public ImportZentralblatt() {
    }

    /**
     * Parse.
     *
     * @param reader
     *            BufferedReader object
     * @param process
     *            Process object
     */
    protected void parse(BufferedReader reader, Process process)
            throws IOException, WrongImportFileException, TypeNotAllowedForParentException,
            TypeNotAllowedAsChildException, MetadataTypeNotAllowedException, WriteException {
        logger.debug("ParsenZentralblatt() - start");
        this.myPrefs = serviceManager.getRulesetService().getPreferences(process.getRuleset());
        String processId = String.valueOf(process.getId().intValue());
        String separator = ":";
        boolean isParagraph = false;
        boolean isFirstTitle = true;
        LinkedList<DocStruct> listArticle = new LinkedList<>();

        // preparation of the document structure
        DigitalDocument dd = new DigitalDocument();
        DocStructType dst = this.myPrefs.getDocStrctTypeByName("Periodical");
        DocStruct dsPeriodical = dd.createDocStruct(dst);
        dst = this.myPrefs.getDocStrctTypeByName("PeriodicalVolume");
        DocStruct dsPeriodicalVolume = dd.createDocStruct(dst);
        dsPeriodical.addChild(dsPeriodicalVolume);

        String line;

        // go through all lines
        while ((line = reader.readLine()) != null) {
            // if the line is empty, it is the end of a paragraph
            if (line.length() == 0) {
                isParagraph = false;
            } else {
                // check if the string contains correct xml characters
                String xmlTauglich = checkXmlSuitability(line);
                if (xmlTauglich.length() > 0) {
                    throw new WrongImportFileException("Parsingfehler (nicht druckbares Zeichen) der Importdatei "
                            + "in der Zeile <br/>" + xmlTauglich);
                }

                // if it is a new paragraph, add it as a new article in the list
                if (!isParagraph) {
                    DocStructType dstLocal = this.myPrefs.getDocStrctTypeByName("Article");
                    DocStruct ds = dd.createDocStruct(dstLocal);
                    listArticle.add(ds);
                    isParagraph = true;
                    isFirstTitle = true;
                }

                // determine the position of the separator
                int separatorPosition = line.indexOf(separator);
                // if there is no separator throw parse error
                if (separatorPosition == -1) {
                    logger.error("Import() - Parsingfehler (kein Doppelpunkt) der Importdatei in der Zeile <br/>"
                            + maskHtmlTags(line));
                    throw new WrongImportFileException("Parsingfehler (kein Doppelpunkt) der Importdatei in "
                            + "der Zeile <br/>" + maskHtmlTags(line));
                } else {
                    String left = line.substring(0, separatorPosition).trim();
                    String right = line.substring(separatorPosition + 1, line.length()).trim();
                    parseArticle(listArticle.getLast(), left, right, isFirstTitle);

                    // if it was a title, the next one is no longer the first title
                    if (left.equals("TI")) {
                        isFirstTitle = false;
                    }

                    // if it is just the journal name, call the magazine
                    if (left.equals("J")) {
                        parseGeneral(dsPeriodical, left, right);
                    }

                    // if it is just a year, then for the current band
                    if (left.equals("Y")) {
                        parseGeneral(dsPeriodicalVolume, left, right);
                    }

                    // if it is just a year, then for the current band
                    if (left.equals("V")) {
                        parseGeneral(dsPeriodicalVolume, left, right);
                    }

                    /*
                     * wenn es gerade die Heftnummer ist, dann jetzt dem richtigen Heft zuordnen und
                     * dieses ggf. noch vorher anlegen
                     */
                    if (left.equals("I")) {
                        DocStruct dsPeriodicalIssue = parseIssueAssignment(dsPeriodicalVolume, right, dd);
                        dsPeriodicalIssue.addChild(listArticle.getLast());
                    }
                }
            }
        }

        // physical tree (pages)
        dst = this.myPrefs.getDocStrctTypeByName("BoundBook");
        DocStruct dsBoundBook = dd.createDocStruct(dst);

        // now build the structure and write in xml

        // DigitalDocument dd = new DigitalDocument();
        dd.setLogicalDocStruct(dsPeriodical);
        dd.setPhysicalDocStruct(dsBoundBook);
        try {
            Fileformat gdzfile = new XStream(this.myPrefs);
            gdzfile.setDigitalDocument(dd);

            // save file in the right place
            gdzfile.write(ConfigCore.getKitodoDataDirectory() + processId + File.separator + "meta.xml");
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("Import aborted: ", e.getMessage());
            logger.error(e);
        }
        logger.debug("ParsenZentralblatt() - Ende");
    }

    private String checkXmlSuitability(String text) {
        int length = text.length();
        String result = "";
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (!isValidXMLChar(c)) {
                result = maskHtmlTags(text.substring(0, i)) + "<span class=\"parsingfehler\">" + c + "</span>";
                if (length > i) {
                    result += maskHtmlTags(text.substring(i + 1, length));
                }
                break;
            }
        }
        return result;
    }

    private static boolean isValidXMLChar(char c) {
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
     * Funktion für das Ermitteln des richtigen Heftes für einen Artikel Liegt das
     * Heft noch nicht in dem Volume vor, wird es angelegt. Als Rückgabe kommt das
     * Heft als DocStruct
     *
     * @param dsPeriodicalVolume
     *            DocStruct object
     * @param right
     *            String
     * @return DocStruct of periodical
     */
    private DocStruct parseIssueAssignment(DocStruct dsPeriodicalVolume, String right,
            DigitalDocument inDigitalDocument)
            throws TypeNotAllowedForParentException, MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {
        DocStructType dst;
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("CurrentNo");
        DocStruct dsPeriodicalIssue = null;
        // first check if the booklet already exists
        List<DocStruct> myList = dsPeriodicalVolume.getAllChildrenByTypeAndMetadataType("PeriodicalIssue", "CurrentNo");
        if (myList != null && myList.size() != 0) {
            for (DocStruct dsIntern : myList) {
                // logger.debug(dsIntern.getAllMetadataByType(mdt).getFirst());
                Metadata metadata = dsIntern.getAllMetadataByType(mdt).get(0);
                // logger.debug("und der Wert ist: " + myMD1.getValue());
                if (metadata.getValue().equals(right)) {
                    dsPeriodicalIssue = dsIntern;
                }
            }
        }
        // if the booklet could not be found, create it now
        if (dsPeriodicalIssue == null) {
            dst = this.myPrefs.getDocStrctTypeByName("PeriodicalIssue");
            dsPeriodicalIssue = inDigitalDocument.createDocStruct(dst);
            Metadata myMD = new Metadata(mdt);
            // TODO: should this be set?
            // myMD.setType(mdt);
            myMD.setValue(right);
            dsPeriodicalIssue.addMetadata(myMD);
            dsPeriodicalVolume.addChild(dsPeriodicalIssue);
        }
        return dsPeriodicalIssue;
    }

    /**
     * General parsing.
     */
    private void parseGeneral(DocStruct inStruct, String left, String right)
            throws WrongImportFileException, MetadataTypeNotAllowedException {

        Metadata md;
        MetadataType mdt;

        // J: Zeitschrift
        // V: Band
        // I: Heft
        // Y: Jahrgang

        // Zeitschriftenname
        if (left.equals("J")) {
            mdt = this.myPrefs.getMetadataTypeByName("TitleDocMain");
            List<? extends Metadata> myList = inStruct.getAllMetadataByType(mdt);
            // if no journal name has been assigned yet, then assign now
            if (myList.size() == 0) {
                md = new Metadata(mdt);
                // md.setType(mdt);
                md.setValue(right);
                inStruct.addMetadata(md);
            } else {
                // a journal name has already been assigned, check if this is the same
                md = myList.get(0);
                if (!right.equals(md.getValue())) {
                    throw new WrongImportFileException("Parsingfehler: verschiedene Zeitschriftennamen in der Datei ('"
                            + md.getValue() + "' & '" + right + "')");
                }
            }
            return;
        }

        // Jahrgang
        if (left.equals("Y")) {
            mdt = this.myPrefs.getMetadataTypeByName("PublicationYear");
            List<? extends Metadata> list = inStruct.getAllMetadataByType(mdt);

            // if no journal name has been assigned yet, then assign now
            if (list.size() == 0) {
                md = new Metadata(mdt);
                // md.setType(mdt);
                md.setValue(right);
                inStruct.addMetadata(md);
            }
            return;
        }

        // Bandnummer
        if (left.equals("V")) {
            mdt = this.myPrefs.getMetadataTypeByName("CurrentNo");
            List<? extends Metadata> list = inStruct.getAllMetadataByType(mdt);

            // if no band number has been assigned yet, then assign now
            if (list.size() == 0) {
                md = new Metadata(mdt);
                md.setValue(right);
                inStruct.addMetadata(md);
            } else {
                // a band number has already been assigned, check if this is the same
                md = list.get(0);
                if (!right.equals(md.getValue())) {
                    throw new WrongImportFileException("Parsingfehler: verschiedene Bandangaben in der Datei ('"
                            + md.getValue() + "' & '" + right + "')");
                }
            }
        }
    }

    /**
     * Parse article.
     *
     * @param docStruct
     *            article
     * @param left
     *            String
     * @param right
     *            String
     * @param isFirstTitle
     *            true or false
     */
    private void parseArticle(DocStruct docStruct, String left, String right, boolean isFirstTitle)
            throws MetadataTypeNotAllowedException, WrongImportFileException {

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

        /*
         * erledigt
         *
         * TI: Titel AU: Autor LA: Sprache NH: Namensvariationen CC: MSC 2000 KW:
         * Keywords AN: Zbl und/oder JFM Nummer P: Seiten
         */

        // title
        if (left.equals("TI")) {
            if (isFirstTitle) {
                docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("TitleDocMain"), right));
            } else {
                docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("MainTitleTranslated"), right));
            }
            return;
        }

        // language
        if (left.equals("LA")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("DocLanguage"), right.toLowerCase()));
            return;
        }

        // ZBLIdentifier
        if (left.equals("AN")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLIdentifier"), right));
            return;
        }

        // ZBLPageNumber
        if (left.equals("P")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLPageNumber"), right));
            return;
        }

        // ZBLSource
        if (left.equals("SO")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLSource"), right));
            return;
        }

        // ZBLAbstract
        if (left.equals("AB")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLAbstract"), right));
            return;
        }

        // ZBLReviewAuthor
        if (left.equals("RV")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLReviewAuthor"), right));
            return;
        }

        // ZBLCita
        if (left.equals("CI")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLCita"), right));
            return;
        }

        // ZBLTempID
        if (left.equals("DE")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLTempID"), right));
            return;
        }

        // ZBLReviewLink
        if (left.equals("SI")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLReviewLink"), right));
            return;
        }

        // ZBLIntern
        if (left.equals("XX")) {
            docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ZBLIntern"), right));
            return;
        }

        // Keywords
        if (left.equals("KW")) {
            StringTokenizer tokenizer = new StringTokenizer(right, ";");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("Keyword"), token.trim()));
            }
            return;
        }

        // Autoren als Personen
        if (left.equals("AU")) {
            StringTokenizer tokenizer = new StringTokenizer(right, ";");
            while (tokenizer.hasMoreTokens()) {
                Person p = new Person(this.myPrefs.getMetadataTypeByName("ZBLAuthor"));
                String token = tokenizer.nextToken();

                if (!token.contains(",")) {
                    throw new WrongImportFileException(
                            "Parsingfehler: Vorname nicht mit Komma vom Nachnamen getrennt ('" + token + "')");
                }

                p.setLastname(token.substring(0, token.indexOf(',')).trim());
                p.setFirstname(token.substring(token.indexOf(',') + 1, token.length()).trim());
                p.setRole("ZBLAuthor");
                docStruct.addPerson(p);
            }
            return;
        }

        // AutorVariationen als Personen
        if (left.equals("NH")) {
            StringTokenizer tokenizer = new StringTokenizer(right, ";");
            while (tokenizer.hasMoreTokens()) {
                Person p = new Person(this.myPrefs.getMetadataTypeByName("AuthorVariation"));
                String token = tokenizer.nextToken();

                if (!token.contains(",")) {
                    throw new WrongImportFileException(
                            "Parsingfehler: Vorname nicht mit Komma vom Nachnamen getrennt ('" + token + "')");
                }

                p.setLastname(token.substring(0, token.indexOf(',')).trim());
                p.setFirstname(token.substring(token.indexOf(',') + 1, token.length()).trim());
                p.setRole("AuthorVariation");
                docStruct.addPerson(p);
            }
            return;
        }

        // MSC 2000 - ClassificationMSC
        if (left.equals("CC")) {
            StringTokenizer tokenizer = new StringTokenizer(right);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                docStruct.addMetadata(prepareMetadata(this.myPrefs.getMetadataTypeByName("ClassificationMSC"), token.trim()));
            }
        }
    }

    private Metadata prepareMetadata(MetadataType mdt, String right) throws MetadataTypeNotAllowedException {
        Metadata metadata = new Metadata(mdt);
        metadata.setValue(right);
        return metadata;
    }

}
