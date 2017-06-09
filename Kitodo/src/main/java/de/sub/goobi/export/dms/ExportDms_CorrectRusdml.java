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

import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.UghHelperException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;

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

public class ExportDms_CorrectRusdml {
    private final Prefs myPrefs;
    private List<DocStruct> docStructsOhneSeiten;
    private final Process myProcess;
    private final DigitalDocument myDocument;
    private static final Logger logger = LogManager.getLogger(ExportDms_CorrectRusdml.class);

    /**
     * Constructor.
     *
     * @param process
     *            object
     * @param inPrefs
     *            Prefs object
     * @param inGdzfile
     *            Fileformat object
     */
    public ExportDms_CorrectRusdml(Process process, Prefs inPrefs, Fileformat inGdzfile) throws PreferencesException {
        myPrefs = inPrefs;
        myDocument = inGdzfile.getDigitalDocument();
        myProcess = process;
    }

    /**
     * Start correction.
     *
     * @return String
     */
    public String correctionStart() throws DocStructHasNoTypeException, MetadataTypeNotAllowedException,
            ExportFileException, UghHelperException {
        String atsPpnBand;
        DocStruct logicalTopstruct = myDocument.getLogicalDocStruct();
        docStructsOhneSeiten = new ArrayList<>();

        /*
         * Prozesseigenschaften ermitteln
         */
        atsPpnBand = BeanHelper.determineWorkpieceProperty(myProcess, "ATS")
                + BeanHelper.determineWorkpieceProperty(myProcess, "TSL") + "_";
        String ppn = BeanHelper.determineWorkpieceProperty(myProcess, "PPN digital");
        if (!ppn.startsWith("PPN")) {
            ppn = "PPN" + ppn;
        }
        atsPpnBand += ppn;
        String bandnummer = BeanHelper.determineWorkpieceProperty(myProcess, "Band");
        if (bandnummer != null && bandnummer.length() > 0) {
            atsPpnBand += "_" + BeanHelper.determineWorkpieceProperty(myProcess, "Band");
        }

        /*
         * DocStruct rukursiv durchlaufen und die Metadaten prüfen
         */
        evaluateRusdmlDocStructPages(logicalTopstruct);
        rusdmlPathImageFilesKorrigieren(myDocument.getPhysicalDocStruct(), "./" + atsPpnBand + "_tif");
        rusdmlAddMissingMetadata(logicalTopstruct, myProcess);

        return atsPpnBand;
    }

    /**
     * Alle Strukturelemente rekursiv durchlaufen und den Elternelementen die
     * Seiten der Kinder zuweisen.
     *
     * @param inStruct
     *            DocStruct object
     */
    private void evaluateRusdmlDocStructPages(DocStruct inStruct)
            throws DocStructHasNoTypeException, MetadataTypeNotAllowedException {
        dropRusdmlMetadata(inStruct);
        rusdmlDropPersons(inStruct);
        rusdmlUmlauteDemaskieren(inStruct);
        rusdmlCheckMetadata(inStruct);

        /* hat das Docstruct keine Bilder, wird es in die Liste genommen */
        if (inStruct.getAllToReferences().size() == 0 && inStruct.getType().getAnchorClass() == null) {
            docStructsOhneSeiten.add(inStruct);
        }

        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (inStruct.getAllChildren() != null) {
            for (Iterator<DocStruct> iter = inStruct.getAllChildren().iterator(); iter.hasNext();) {
                DocStruct child = iter.next();
                evaluateRusdmlDocStructPages(child);
            }
        }
    }

    /**
     * Alle nicht benötigten Metadaten des RUSDML-Projektes rauswerfen.
     *
     * @param inStruct
     *            DocStruct
     */

    private void dropRusdmlMetadata(DocStruct inStruct)
            throws DocStructHasNoTypeException, MetadataTypeNotAllowedException {
        String titleRu = "";
        String titleOther = "";
        String language = "";

        if (inStruct.getAllVisibleMetadata() != null) {
            List<Metadata> kopie = new ArrayList<Metadata>(inStruct.getAllMetadata());
            for (Metadata meta : kopie) {
                // Metadata meta = (Metadata) iter.next();

                /*
                 * jetzt alle nicht benötigten Metadaten löschen
                 */
                if (meta.getType().getName().equals("RUSMainTitle")) {
                    titleRu = meta.getValue();
                    inStruct.getAllMetadata().remove(meta);
                }
                if (meta.getType().getName().equals("TitleDocMain")) {
                    titleOther = meta.getValue();
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
         * nachdem alle Metadaten durchlaufen wurden, jetzt abhängig vom
         * Sprachcode den richtigen MainTitle zuweisen
         */
        MetadataType mdtOrg = myPrefs.getMetadataTypeByName("TitleDocMain");
        Metadata metaOrg = new Metadata(mdtOrg);
        MetadataType mdtTrans = myPrefs.getMetadataTypeByName("MainTitleTranslated");
        Metadata metaTrans = new Metadata(mdtTrans);
        if (language.equals("ru")) {
            metaOrg.setValue(titleRu);
            metaTrans.setValue(titleOther);
        } else {
            metaTrans.setValue(titleRu);
            metaOrg.setValue(titleOther);
        }

        if (metaOrg.getValue() != null && metaOrg.getValue().length() > 0) {
            inStruct.addMetadata(metaOrg);
        }
        if (metaTrans.getValue() != null && metaTrans.getValue().length() > 0) {
            inStruct.addMetadata(metaTrans);
        }
    }

    /**
     * Alle nicht benötigten Personen rauswerfen.
     *
     * @param inStruct
     *            DocStruct object
     */
    private void rusdmlDropPersons(DocStruct inStruct) {
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

    /**
     * Alle zu ändernden Metadaten ändern.
     *
     * @param inStruct
     *            DocStruct object
     */
    private void rusdmlCheckMetadata(DocStruct inStruct) {
        /*
         * generell ausführen
         */
        if (inStruct.getType().getName().equals("Illustration")) {
            DocStructType dst = myPrefs.getDocStrctTypeByName("Figure");
            inStruct.setType(dst);
        }
    }

    private void rusdmlPathImageFilesKorrigieren(DocStruct phys, String inNeuerWert) throws ExportFileException {
        MetadataType mdTypeForPath = myPrefs.getMetadataTypeByName("pathimagefiles");
        List<? extends Metadata> alleMetadaten = phys.getAllMetadataByType(mdTypeForPath);
        if (alleMetadaten.size() > 0) {
            for (Metadata meta : alleMetadaten) {
                meta.setValue(inNeuerWert);
            }
        } else {
            throw new ExportFileException("Exportfehler: Imagepfad noch nicht gesetzt");
        }
    }

    /**
     * Dabei die zentralen Projekteinstellungen in der xml-Konfiguration
     * berücksichtigen.
     *
     * @param inTopStruct
     *            DocStruct object
     * @param myProcess
     *            Process object
     */
    private void rusdmlAddMissingMetadata(DocStruct inTopStruct, Process myProcess) throws ExportFileException {
        /*
         * bei fehlender digitaler PPN: Fehlermeldung und raus
         */
        String ppn = BeanHelper.determineWorkpieceProperty(myProcess, "PPN digital");
        if (ppn.length() == 0) {
            throw new ExportFileException("Exportfehler: Keine PPN digital vorhanden");
        }
        rusdmlAddMissingMetadata(inTopStruct, myProcess, ppn);
    }

    /**
     * Fehlende Metadaten für Rusdml ergänzen.
     *
     * @param inTopStruct
     *            DocStruct object
     * @param myProcess
     *            Process object
     * @param ppn
     *            String
     */
    private void rusdmlAddMissingMetadata(DocStruct inTopStruct, Process myProcess, String ppn) {
        /*
         * Eigenschaften aus dem Werkstück holen
         */
        String title = BeanHelper.determineWorkpieceProperty(myProcess, "Haupttitel");
        String verlag = BeanHelper.determineWorkpieceProperty(myProcess, "Verlag");
        String place = BeanHelper.determineWorkpieceProperty(myProcess, "Erscheinungsort");
        String ISSN = BeanHelper.determineWorkpieceProperty(myProcess, "ISSN");
        String bandNumber = BeanHelper.determineWorkpieceProperty(myProcess, "Band");

        /*
         * die Metadaten erzeugen
         */
        Metadata mdVerlag = null;
        Metadata mdPlace = null;
        Metadata mdISSN = null;
        Metadata mdPPN = null;
        Metadata mdPPNBand = null;
        Metadata mdSorting = null;
        try {
            Metadata mdTitle = new Metadata(myPrefs.getMetadataTypeByName("TitleDocMain"));
            mdTitle.setValue(title);
            mdVerlag = new Metadata(myPrefs.getMetadataTypeByName("PublisherName"));
            mdVerlag.setValue(verlag);
            mdPlace = new Metadata(myPrefs.getMetadataTypeByName("PlaceOfPublication"));
            mdPlace.setValue(place);
            mdISSN = new Metadata(myPrefs.getMetadataTypeByName("ISSN"));
            mdISSN.setValue(ISSN);
            mdPPN = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
            mdPPN.setValue("PPN" + ppn);
            mdPPNBand = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
            mdPPNBand.setValue("PPN" + ppn + "_" + bandNumber);
            mdSorting = new Metadata(myPrefs.getMetadataTypeByName("CurrentNoSorting"));
        } catch (MetadataTypeNotAllowedException e1) {
            logger.error(e1);
        }
        try {
            int bandInt = Integer.parseInt(bandNumber) * 10;
            mdSorting.setValue(String.valueOf(bandInt));
        } catch (NumberFormatException e) {
        }

        /*
         * die Metadaten der Zeitschrift zuweisen
         */
        inTopStruct.getAllMetadataByType(myPrefs.getMetadataTypeByName("TitleDocMain")).get(0).setValue(title);

        try {
            inTopStruct.addMetadata(mdVerlag);
            inTopStruct.addMetadata(mdPlace);
            inTopStruct.addMetadata(mdPPN);
            inTopStruct.addMetadata(mdISSN);
        } catch (Exception e) {
        }

        /*
         * die Metadaten dem Band zuweisen
         */
        DocStruct structBand = inTopStruct.getAllChildren().get(0);
        if (structBand != null) {

            try {
                structBand.addMetadata(mdVerlag);
                structBand.addMetadata(mdPlace);
                structBand.addMetadata(mdPPNBand);
                structBand.addMetadata(mdSorting);
            } catch (MetadataTypeNotAllowedException e) {

            } catch (DocStructHasNoTypeException e) {

            }

        }
    }

    /**
     * Alle Metadaten eines Strukturelements durchlaufen und deren Umlaute
     * maskieren.
     *
     * @param inStruct
     *            DocStruct object
     */

    private void rusdmlUmlauteDemaskieren(DocStruct inStruct) {
        List<Metadata> kopie = inStruct.getAllMetadata();
        if (kopie != null) {
            for (Metadata meta : kopie) {
                /* in den Metadaten die Umlaute entfernen */
                rusdmlUmlauteDemaskieren(meta);
            }
        }
    }

    private void rusdmlUmlauteDemaskieren(Metadata meta) {
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
