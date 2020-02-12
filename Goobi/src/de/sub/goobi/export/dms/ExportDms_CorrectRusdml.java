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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class ExportDms_CorrectRusdml {
    private final Prefs myPrefs;
    private List<DocStruct> docStructsOhneSeiten;
    private final Prozess myProzess;
    private final DigitalDocument mydocument;
    private static final Logger logger = Logger.getLogger(ExportDms_CorrectRusdml.class);

    public ExportDms_CorrectRusdml(Prozess inProzess, Prefs inPrefs, Fileformat inGdzfile) throws PreferencesException {
        myPrefs = inPrefs;
        mydocument = inGdzfile.getDigitalDocument();
        myProzess = inProzess;
    }

    /* =============================================================== */

    public String correctionStart() throws DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException {
        String atsPpnBand;
        DocStruct logicalTopstruct = mydocument.getLogicalDocStruct();
        docStructsOhneSeiten = new ArrayList<DocStruct>();

        /*
         * -------------------------------- Prozesseigenschaften ermitteln
         * --------------------------------
         */
        atsPpnBand = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "ATS")
                + BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "TSL") + "_";
        String ppn = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "PPN digital");
        if (!ppn.startsWith("PPN")) {
            ppn = "PPN" + ppn;
        }
        atsPpnBand += ppn;
        String bandnummer = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "Band");
        if (bandnummer != null && bandnummer.length() > 0) {
            atsPpnBand += "_" + BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "Band");
        }

        /*
         * -------------------------------- DocStruct rukursiv durchlaufen und
         * die Metadaten prüfen --------------------------------
         */
        RusdmlDocStructPagesAuswerten(logicalTopstruct);
        RusdmlPathImageFilesKorrigieren(mydocument.getPhysicalDocStruct(), "./" + atsPpnBand + "_" + ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif"));
        RusdmlAddMissingMetadata(logicalTopstruct, myProzess);

        return atsPpnBand;
    }

    /* =============================================================== */

    /**
     * alle Strukturelemente rekursiv durchlaufen und den Elternelementen die
     * Seiten der Kinder zuweisen
     *
     * @param inStruct
     * @throws MetadataTypeNotAllowedException
     * @throws DocStructHasNoTypeException
     */
    private void RusdmlDocStructPagesAuswerten(DocStruct inStruct) throws DocStructHasNoTypeException, MetadataTypeNotAllowedException {
        RusdmlDropMetadata(inStruct);
        RusdmlDropPersons(inStruct);
        RusdmlUmlauteDemaskieren(inStruct);
        RusdmlCheckMetadata(inStruct);

        /* hat das Docstruct keine Bilder, wird es in die Liste genommen */
        if (inStruct.getAllToReferences().size() == 0 && inStruct.getType().getAnchorClass() == null) {
            docStructsOhneSeiten.add(inStruct);
        }

        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (inStruct.getAllChildren() != null) {
            for (Iterator<DocStruct> iter = inStruct.getAllChildren().iterator(); iter.hasNext();) {
                DocStruct child = iter.next();
                RusdmlDocStructPagesAuswerten(child);
            }
        }
    }

    /* =============================================================== */

    /**
     * alle nicht benötigten Metadaten des RUSDML-Projektes rauswerfen
     *
     * @param inStruct
     * @throws MetadataTypeNotAllowedException
     * @throws DocStructHasNoTypeException
     * @throws MetadataTypeNotAllowedException
     * @throws DocStructHasNoTypeException
     */

    private void RusdmlDropMetadata(DocStruct inStruct) throws DocStructHasNoTypeException, MetadataTypeNotAllowedException {
        String titelRu = "";
        String titelOther = "";
        String language = "";

        if (inStruct.getAllVisibleMetadata() != null) {
            List<Metadata> kopie = new ArrayList<Metadata>(inStruct.getAllMetadata());
            for (Metadata meta : kopie) {
                // Metadata meta = (Metadata) iter.next();

                /*
                 * -------------------------------- jetzt alle nicht benötigten
                 * Metadaten löschen --------------------------------
                 */
                if (meta.getType().getName().equals("RUSMainTitle")) {
                    titelRu = meta.getValue();
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("TitleDocMain")) {
                    titelOther = meta.getValue();
                    inStruct.getAllMetadata().remove(meta);
                }

                if (meta.getType().getName().equals("DocLanguage")) {
                    meta.setValue(meta.getValue().toLowerCase());
                    language = meta.getValue();
                }

                if (meta.getType().getName().equals("RUSPublisher")) {
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("RUSPlaceOfPublication")) {
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("RUSPublicationHouse")) {
                    inStruct.getAllMetadata().remove(meta);
                }

                if (meta.getType().getName().equals("ZBLSource")) {
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("ZBLIntern")) {
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("ZBLPageNumber")) {
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("ZBLReviewLink")) {
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("ZBLReviewAuthor")) {
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("ZBLCita")) {
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("ZBLTempID")) {
                    inStruct.getAllMetadata().remove(meta);
                }

                /*
                 * den Abstrakt des ZBLs übernehmen, aber nur die 255 ersten
                 * Zeichen
                 */
                if (meta.getType().getName().equals("ZBLAbstract")) {
                    MetadataType mdt = myPrefs.getMetadataTypeByName("Abstract");
                    meta.setType(mdt);
                    if (meta.getValue().length() > 255) {
                        meta.setValue(meta.getValue().substring(0, 254));
                    }
                }
            }
        }

        /*
         * -------------------------------- nachdem alle Metadaten durchlaufen
         * wurden, jetzt abhängig vom Sprachcode den richtigen MainTitle
         * zuweisen --------------------------------
         */
        MetadataType mdt_org = myPrefs.getMetadataTypeByName("TitleDocMain");
        Metadata meta_org = new Metadata(mdt_org);
        MetadataType mdt_trans = myPrefs.getMetadataTypeByName("MainTitleTranslated");
        Metadata meta_trans = new Metadata(mdt_trans);
        if (language.equals("ru")) {
            meta_org.setValue(titelRu);
            meta_trans.setValue(titelOther);
        } else {
            meta_trans.setValue(titelRu);
            meta_org.setValue(titelOther);
        }

        if (meta_org.getValue() != null && meta_org.getValue().length() > 0) {
            inStruct.addMetadata(meta_org);
        }
        if (meta_trans.getValue() != null && meta_trans.getValue().length() > 0) {
            inStruct.addMetadata(meta_trans);
        }
    }

    /* =============================================================== */

    /**
     * alle nicht benötigten Personen rauswerfen
     *
     * @param inStruct
     */
    private void RusdmlDropPersons(DocStruct inStruct) {
        if (inStruct.getAllPersons() != null) {
            List<Person> kopie = new ArrayList<Person>(inStruct.getAllPersons());
            for (Iterator<Person> iter = kopie.iterator(); iter.hasNext();) {
                Metadata meta = iter.next();
                if (meta.getType().getName().equals("ZBLAuthor")) {
                    inStruct.getAllPersons().remove(meta);
                }
            }
        }
    }

    /* =============================================================== */

    /**
     * alle zu ändernden Metadaten ändern
     *
     * @param inStruct
     */
    private void RusdmlCheckMetadata(DocStruct inStruct) {
        /*
         * -------------------------------- generell ausführen
         * --------------------------------
         */
        if (inStruct.getType().getName().equals("Illustration")) {
            DocStructType dst = myPrefs.getDocStrctTypeByName("Figure");
            inStruct.setType(dst);
        }
    }

    /* =============================================================== */

    private void RusdmlPathImageFilesKorrigieren(DocStruct phys, String inNeuerWert) throws ExportFileException {
        MetadataType MDTypeForPath = myPrefs.getMetadataTypeByName("pathimagefiles");
        List<? extends Metadata> alleMetadaten = phys.getAllMetadataByType(MDTypeForPath);
        if (alleMetadaten.size() > 0) {
            for (Metadata meta : alleMetadaten) {
                meta.setValue(inNeuerWert);
            }
        } else {
            throw new ExportFileException("Exportfehler: Imagepfad noch nicht gesetzt");
        }
    }

    /* =============================================================== */

    /**
     * dabei die zentralen Projekteinstellungen in der xml-Konfiguration
     * berücksichtigen
     *
     * @param inTopStruct
     * @param myProzess
     * @throws ExportFileException
     * @throws UghHelperException
     * @throws DocStructHasNoTypeException
     * @throws MetadataTypeNotAllowedException
     */
    private void RusdmlAddMissingMetadata(DocStruct inTopStruct, Prozess myProzess) throws ExportFileException, UghHelperException {
        /*
         * -------------------------------- bei fehlender digitaler PPN:
         * Fehlermeldung und raus --------------------------------
         */
        String PPN = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "PPN digital");
        if (PPN.length() == 0) {
            throw new ExportFileException("Exportfehler: Keine PPN digital vorhanden");
        }
        RusdmlAddMissingMetadata(inTopStruct, myProzess, PPN);
    }

    /**
     * Fehlende Metadaten für Rusdml ergänzen
     *
     * @param inTopStruct
     * @param myProzess
     * @param PPN
     */
    private void RusdmlAddMissingMetadata(DocStruct inTopStruct, Prozess myProzess, String PPN) {
        /*
         * -------------------------------- Eigenschaften aus dem Werkstück
         * holen --------------------------------
         */
        String Titel = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "Haupttitel");
        String Verlag = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "Verlag");
        String Ort = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "Erscheinungsort");
        String ISSN = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "ISSN");
        String BandNummer = BeanHelper.WerkstueckEigenschaftErmitteln(myProzess, "Band");

        /*
         * -------------------------------- die Metadaten erzeugen
         * --------------------------------
         */
        Metadata mdVerlag = null;
        Metadata mdOrt = null;
        Metadata mdISSN = null;
        Metadata mdPPN = null;
        Metadata mdPPNBand = null;
        Metadata mdSorting = null;
        try {
            Metadata mdTitel = new Metadata(myPrefs.getMetadataTypeByName("TitleDocMain"));
            mdTitel.setValue(Titel);
            mdVerlag = new Metadata(myPrefs.getMetadataTypeByName("PublisherName"));
            mdVerlag.setValue(Verlag);
            mdOrt = new Metadata(myPrefs.getMetadataTypeByName("PlaceOfPublication"));
            mdOrt.setValue(Ort);
            mdISSN = new Metadata(myPrefs.getMetadataTypeByName("ISSN"));
            mdISSN.setValue(ISSN);
            mdPPN = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
            mdPPN.setValue("PPN" + PPN);
            mdPPNBand = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
            mdPPNBand.setValue("PPN" + PPN + "_" + BandNummer);
            mdSorting = new Metadata(myPrefs.getMetadataTypeByName("CurrentNoSorting"));
        } catch (MetadataTypeNotAllowedException e1) {
            logger.error(e1);
        }
        try {
            int BandInt = Integer.parseInt(BandNummer) * 10;
            mdSorting.setValue(String.valueOf(BandInt));
        } catch (NumberFormatException e) {
        }

        /*
         * -------------------------------- die Metadaten der Zeitschrift
         * zuweisen --------------------------------
         */
        inTopStruct.getAllMetadataByType(myPrefs.getMetadataTypeByName("TitleDocMain")).get(0).setValue(Titel);

        try {
            inTopStruct.addMetadata(mdVerlag);
            inTopStruct.addMetadata(mdOrt);
            inTopStruct.addMetadata(mdPPN);
            inTopStruct.addMetadata(mdISSN);
        } catch (Exception e) {
        }

        /*
         * -------------------------------- die Metadaten dem Band zuweisen
         * --------------------------------
         */
        DocStruct structBand = inTopStruct.getAllChildren().get(0);
        if (structBand != null) {

            try {
                structBand.addMetadata(mdVerlag);
                structBand.addMetadata(mdOrt);
                structBand.addMetadata(mdPPNBand);
                structBand.addMetadata(mdSorting);
            } catch (MetadataTypeNotAllowedException e) {

            } catch (DocStructHasNoTypeException e) {

            }

        }
    }

    /* =============================================================== */

    /**
     * Alle Metadaten eines Strukturelements durchlaufen und deren Umlaute
     * maskieren
     *
     * @param inStruct
     */

    private void RusdmlUmlauteDemaskieren(DocStruct inStruct) {
        List<Metadata> kopie = inStruct.getAllMetadata();
        if (kopie != null) {
            for (Metadata meta : kopie) {
                /* in den Metadaten die Umlaute entfernen */
                RusdmlUmlauteDemaskieren1(meta);
            }
        }
    }

    /* =============================================================== */

    private void RusdmlUmlauteDemaskieren1(Metadata meta) {
        String neuerWert = meta.getValue();
        if (neuerWert == null) {
            return;
        }
        neuerWert = neuerWert.replaceAll("\\\\star", "\u002a");
        neuerWert = neuerWert.replaceAll("\\\\times", "\u00d7");
        neuerWert = neuerWert.replaceAll("\\\\div", "\u00f7");
        neuerWert = neuerWert.replaceAll("\\\\dot G", "\u0120");
        neuerWert = neuerWert.replaceAll("\\\\Gamma", "\u0393");
        neuerWert = neuerWert.replaceAll("\\\\Delta", "\u00394");
        neuerWert = neuerWert.replaceAll("\\\\Lambda", "\u039b");
        neuerWert = neuerWert.replaceAll("\\\\Sigma", "\u03a3");
        neuerWert = neuerWert.replaceAll("\\\\Omega", "\u03a9");
        neuerWert = neuerWert.replaceAll("\\\\alpha", "\u03b1");
        neuerWert = neuerWert.replaceAll("\\\\beta", "\u03b2");
        neuerWert = neuerWert.replaceAll("\\\\gamma", "\u03b3");
        neuerWert = neuerWert.replaceAll("\\\\delta", "\u002a");
        neuerWert = neuerWert.replaceAll("\\\\epsilon", "\u03b5");
        neuerWert = neuerWert.replaceAll("\\\\zeta", "\u03b6");
        neuerWert = neuerWert.replaceAll("\\\\eta", "\u03b7");
        neuerWert = neuerWert.replaceAll("\\\\theta", "\u03b8");
        neuerWert = neuerWert.replaceAll("\\\\lambda", "\u03bb");
        neuerWert = neuerWert.replaceAll("\\\\mu", "\u03bc");
        neuerWert = neuerWert.replaceAll("\\\\nu", "\u03bd");
        neuerWert = neuerWert.replaceAll("\\\\pi", "\u03c0");
        neuerWert = neuerWert.replaceAll("\\\\sigma", "\u03c3");
        neuerWert = neuerWert.replaceAll("\\\\phi", "\u03c6");
        neuerWert = neuerWert.replaceAll("\\\\omega", "\u03c9");
        neuerWert = neuerWert.replaceAll("\\\\ell", "\u2113");
        neuerWert = neuerWert.replaceAll("\\\\rightarrow", "\u2192");
        neuerWert = neuerWert.replaceAll("\\\\sim", "\u223c");
        neuerWert = neuerWert.replaceAll("\\\\le", "\u2264");
        neuerWert = neuerWert.replaceAll("\\\\ge", "\u2265");
        neuerWert = neuerWert.replaceAll("\\\\odot", "\u2299");
        neuerWert = neuerWert.replaceAll("\\\\infty", "\u221e");
        neuerWert = neuerWert.replaceAll("\\\\circ", "\u2218");
        neuerWert = neuerWert.replaceAll("\\\\dot\\{P\\}", "\u1e56");
        neuerWert = neuerWert.replaceAll("\\\\symbol\\{94\\}", "\u005e");
        neuerWert = neuerWert.replaceAll("\\\\symbol\\{126\\}", "\u007e");
        neuerWert = neuerWert.replaceAll("\\\\u g", "\u011f");
        neuerWert = neuerWert.replaceAll("\\\\AE ", "\u00c6");
        neuerWert = neuerWert.replaceAll("\\\\ae ", "\u00e6");
        neuerWert = neuerWert.replaceAll("\\\\oe ", "\u0153");
        neuerWert = neuerWert.replaceAll("\\\\OE ", "\u0152");
        neuerWert = neuerWert.replaceAll("\\\\uu ", "u");
        neuerWert = neuerWert.replaceAll("\\\\UU ", "U");
        neuerWert = neuerWert.replaceAll("\\\\Dj ", "Dj");
        neuerWert = neuerWert.replaceAll("\\\\dj ", "dj");
        neuerWert = neuerWert.replaceAll("\\\\c\\{c\\}", "\u00e7");
        neuerWert = neuerWert.replaceAll("\\\\c c", "\u00e7");
        neuerWert = neuerWert.replaceAll("\\\\c\\{C\\}", "\u00c7");
        neuerWert = neuerWert.replaceAll("\\\\c C", "\u00c7");
        // NOTE The following one only for schummling and correcting errors!
        neuerWert = neuerWert.replaceAll("\\{\\\\ss \\}", "\u00dF");
        neuerWert = neuerWert.replaceAll("\\{\\\\ss\\}", "\u00dF");
        neuerWert = neuerWert.replaceAll("\\{\\\\ss\\}", "\u00dF");
        neuerWert = neuerWert.replaceAll("\\\\ss ", "\u00df");
        neuerWert = neuerWert.replaceAll("\\\\aa ", "\u00e5");
        neuerWert = neuerWert.replaceAll("\\\\AA ", "\u00c5");
        neuerWert = neuerWert.replaceAll("\\\\dh ", "\u00f0");
        neuerWert = neuerWert.replaceAll("\\\\th ", "\u00fe");
        neuerWert = neuerWert.replaceAll("\\\\'a", "á");
        neuerWert = neuerWert.replaceAll("\\\\'A", "Á");
        neuerWert = neuerWert.replaceAll("\\\\`a", "à");
        neuerWert = neuerWert.replaceAll("\\\\`A", "À");
        neuerWert = neuerWert.replaceAll("\\\\\\^a", "â");
        neuerWert = neuerWert.replaceAll("\\\\\\^A", "Â");
        neuerWert = neuerWert.replaceAll("\\\\~a", "\u00e3");
        neuerWert = neuerWert.replaceAll("\\\\~A", "\u00c3");
        neuerWert = neuerWert.replaceAll("\\\\\\\"A", "Ä");
        neuerWert = neuerWert.replaceAll("\\\\\\\"a", "ä");
        neuerWert = neuerWert.replaceAll("\\\\'e", "é");
        neuerWert = neuerWert.replaceAll("\\\\'E", "É");
        neuerWert = neuerWert.replaceAll("\\\\`e", "è");
        neuerWert = neuerWert.replaceAll("\\\\`E", "È");
        neuerWert = neuerWert.replaceAll("\\\\\\^e", "e");
        neuerWert = neuerWert.replaceAll("\\\\\\^E", "Ê");
        neuerWert = neuerWert.replaceAll("\\\\\\\"E", "\u00cb");
        neuerWert = neuerWert.replaceAll("\\\\\\\"e", "\u00eb");
        neuerWert = neuerWert.replaceAll("\\\\'i", "í");
        neuerWert = neuerWert.replaceAll("\\\\'I", "Í");
        neuerWert = neuerWert.replaceAll("\\\\`i", "ì");
        neuerWert = neuerWert.replaceAll("\\\\`I", "Ì");
        neuerWert = neuerWert.replaceAll("\\\\\\^i", "î");
        neuerWert = neuerWert.replaceAll("\\\\\\^I", "Î");
        neuerWert = neuerWert.replaceAll("\\\\\\\"I", "\u00cf");
        neuerWert = neuerWert.replaceAll("\\\\\\\"i", "\u00ef");
        neuerWert = neuerWert.replaceAll("\\\\~n", "\u00f1");
        neuerWert = neuerWert.replaceAll("\\\\~N", "\u00d1");
        neuerWert = neuerWert.replaceAll("\\\\'o", "ó");
        neuerWert = neuerWert.replaceAll("\\\\'O", "Ó");
        neuerWert = neuerWert.replaceAll("\\\\`o", "ò");
        neuerWert = neuerWert.replaceAll("\\\\`O", "Ò");
        neuerWert = neuerWert.replaceAll("\\\\\\^o", "ô");
        neuerWert = neuerWert.replaceAll("\\\\\\^O", "Ô");
        neuerWert = neuerWert.replaceAll("\\\\~o", "\u00f5");
        neuerWert = neuerWert.replaceAll("\\\\~O", "\u00d5");
        neuerWert = neuerWert.replaceAll("\\\\\\\"O", "Ö");
        neuerWert = neuerWert.replaceAll("\\\\\\\"o", "ö");
        neuerWert = neuerWert.replaceAll("\\\\'u", "ú");
        neuerWert = neuerWert.replaceAll("\\\\'U", "Ú");
        neuerWert = neuerWert.replaceAll("\\\\`u", "ù");
        neuerWert = neuerWert.replaceAll("\\\\`U", "Ù");
        neuerWert = neuerWert.replaceAll("\\\\\\^u", "û");
        neuerWert = neuerWert.replaceAll("\\\\\\^U", "Û");
        neuerWert = neuerWert.replaceAll("\\\\\"U", "Ü");
        neuerWert = neuerWert.replaceAll("\\\\\"u", "ü");
        neuerWert = neuerWert.replaceAll("\\\\'y", "ý");
        neuerWert = neuerWert.replaceAll("\\\\'Y", "Ý");
        neuerWert = neuerWert.replaceAll("\\\\\\\"y", "\u00ff");
        neuerWert = neuerWert.replaceAll("\\\\H ", "\"");
        neuerWert = neuerWert.replaceAll("\\\\O", "\u00d8");
        neuerWert = neuerWert.replaceAll("\\\\o", "\u00f8");
        neuerWert = neuerWert.replaceAll("\\\\'C", "\u0106");
        neuerWert = neuerWert.replaceAll("\\\\'c", "\u0107");
        neuerWert = neuerWert.replaceAll("\\\\v C", "\u010c");
        neuerWert = neuerWert.replaceAll("\\\\v c", "\u010d");
        neuerWert = neuerWert.replaceAll("\\\\v S", "\u0160");
        neuerWert = neuerWert.replaceAll("\\\\v s", "\u0161");
        neuerWert = neuerWert.replaceAll("\\\\v Z", "\u017d");
        neuerWert = neuerWert.replaceAll("\\\\v z", "\u017e");
        neuerWert = neuerWert.replaceAll("\\\\v r", "\u0159");
        neuerWert = neuerWert.replaceAll("\\\\'s", "\u015b");
        neuerWert = neuerWert.replaceAll("\\\\'S", "\u015a");
        neuerWert = neuerWert.replaceAll("\\\\L", "\u0141");
        neuerWert = neuerWert.replaceAll("\\\\l", "\u0142");
        neuerWert = neuerWert.replaceAll("\\\\'N", "\u0143");
        neuerWert = neuerWert.replaceAll("\\\\'n", "\u0144");
        neuerWert = neuerWert.replaceAll("\\\\'t", "\u0165");
        neuerWert = neuerWert.replaceAll("\\\\=u", "\u016b");
        neuerWert = neuerWert.replaceAll("\\\\'z", "\u017a");
        neuerWert = neuerWert.replaceAll("\\\\.Z", "\u017b");
        neuerWert = neuerWert.replaceAll("\\\\.z", "\u017c");
        neuerWert = neuerWert.replaceAll("\\\\#", "\u0023");
        neuerWert = neuerWert.replaceAll("\\\\%", "\u0025");
        neuerWert = neuerWert.replaceAll("\\\\_", "\u005f");
        neuerWert = neuerWert.replaceAll("\\\\~ ", " ");
        neuerWert = neuerWert.replaceAll("\\\\=", "");

        meta.setValue(neuerWert);
    }
}
