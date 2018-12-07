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

package org.kitodo.production.plugin.opac.pica;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.w3c.dom.Node;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.fileformats.mets.XStream;
import ugh.fileformats.opac.PicaPlus;

/**
 * The class PicaPlugin is the main class of the Goobi PICA catalogue plugin
 * implementation. It provides the public methods void configure(Map) [*] Object
 * find(String, long) String getDescription() [*] Map getHit(Object, long, long)
 * long getNumberOfHits(Object, long) String getTitle() [*] void
 * setPreferences(Prefs) boolean supportsCatalogue(String) void
 * useCatalogue(String)
 *
 * as specified by org.goobi.production.plugin.UnspecificPlugin [*] and
 * org.goobi.production.plugin.CataloguePlugin.CataloguePlugin.
 *
 * Parts of the code of this class have been ported from ancient class
 * <kbd>org.goobi.production.plugin.opac.PicaOpacImport</kbd>.
 *
 * @author Partly based on previous works of other authors who didn’t leave
 *         their names
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
@PluginImplementation
public class PicaPlugin implements Plugin {

    private static final Logger logger = LogManager.getLogger(PicaPlugin.class);
    /**
     * The constant OPAC_CONFIGURATION_FILE holds the name of the PICA plug-in
     * languages mapping file. This is a text file with lines in form
     * replacement—space—stringToReplace used to replace the value from PICA+
     * field “010@” subfield “a” (the replacement will be saved in DocStruct
     * “DocLanguage”) The file is optional. To use this functionality, the file
     * must be located in {@link #configDir}.
     */
    static final String LANGUAGES_MAPPING_FILE = "kitodo_opacLanguages.txt";

    /**
     * The constant OPAC_CONFIGURATION_FILE holds the name of the PICA plug-in
     * main configuration file. Required. The file must be located in
     * {@link #configDir}.
     */
    static final String OPAC_CONFIGURATION_FILE = "kitodo_opac.xml";

    /**
     * The field configDir holds a reference to the file system directory where
     * configuration files are read from. The field is initialised by Production
     * that calls {@link #configure(Map)}.
     */
    private static String configDir;

    /**
     * The field tempDir holds a reference to the file system directory where
     * temporary files are written in. Thus, servlet container needs write
     * access to that directory. The field is initialised by Production that
     * calls {@link #configure(Map)}.
     */
    private static String tempDir;

    /**
     * The field preferences holds the UGH preferences.
     */
    private Prefs preferences;

    /**
     * The field configuration holds the catalogue configuration.
     */
    private ConfigOpacCatalogue configuration;

    /**
     * The field catalogue holds the catalogue.
     */
    private Catalogue catalogue;

    /**
     * The field client holds the catalogue client used to access the catalogue.
     */
    private GetOpac client;

    /**
     * The method configure() accepts a Map with configuration parameters. Two
     * entries, "configDir" and "tempDir", are expected.
     *
     * configDir must point to a directory on the local file system where the
     * plug-in can read individual configuration files from. The configuration
     * file, "kitodo_opac.xml" is expected in that directory.
     *
     * @param configuration
     *            a Map with configuration parameters
     * @see org.goobi.production.plugin.UnspecificPlugin#configure(Map)
     */
    public void configure(Map<String, String> configuration) {
        configDir = configuration.get("configDir");
        tempDir = configuration.get("tempDir");
    }

    /**
     * The function find() initially queries the library catalogue with the
     * given query. If successful, it returns a FindResult with the number of
     * hits.
     *
     * @param query
     *            a query String. See
     *            {@link org.goobi.production.plugin.CataloguePlugin.QueryBuilder}
     *            for the semantics of the query.
     * @param timeout
     *            timeout in milliseconds after which the operation shall return
     * @return a FindResult that may be used for future operations on the query
     * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#find(String,
     *      long)
     */
    public Object find(String query, long timeout) {
        try {
            Query queryObject = new Query(query);
            int hits = client.getNumberOfHits(queryObject, timeout);
            if (hits > 0) {
                return new FindResult(queryObject, hits);
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * The function getConfigDir() provides a reference to the file system
     * directory where configuration files are read from.
     *
     * @return the file system directory with the configuration files
     */
    static String getConfigDir() {
        return configDir;
    }

    /**
     * The function getDescription() returns a human-readable description of the
     * plug-in’s functionality in English. The parameter language is ignored.
     *
     * @param language
     *            desired language of the human-readable description (support is
     *            optional)
     * @return a human-readable description of the plug-in’s functionality
     * @see org.goobi.production.plugin.UnspecificPlugin#getDescription(Locale)
     */
    public static String getDescription(Locale language) {
        return "The PICA plugin can be used to access PICA library catalogue systems.";
    }

    /**
     * The function getHit() returns the hit with the given index from the given
     * search result as a Map&lt;String, Object&gt;. The map contains the full
     * hit as "fileformat", the docType as "type" and some bibliographic
     * metadata for Production to be able to show a short hit display as
     * supposed in {@link org.goobi.production.plugin.CataloguePlugin.Hit}.
     *
     * @param searchResult
     *            a FindResult created by {@link #find(String, long)}
     * @param index
     *            the zero-based index of the hit
     * @param timeout
     *            a timeout in milliseconds after which the operation shall
     *            return
     * @return a Map with the hit
     * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#getHit(Object,
     *      long, long)
     */
    public Map<String, Object> getHit(Object searchResult, long index, long timeout) {
        if (!(searchResult instanceof FindResult)) {
            throw new ClassCastException();
        }
        Query myQuery = ((FindResult) searchResult).getQuery();

        Element myFirstHit;
        String gattung;
        ConfigOpacDoctype cod;
        Fileformat ff;
        try {
            /*
             * Opac abfragen und erhaltenes Dom-Dokument in JDom-Dokument
             * umwandeln
             */
            Node myHitlist = client.retrievePicaNode(myQuery, (int) index, (int) index + 1, timeout);
            /* Opac-Beautifier aufrufen */
            myHitlist = configuration.executeBeautifier(myHitlist);
            Document myJdomDoc = new DOMBuilder().build(myHitlist.getOwnerDocument());
            myFirstHit = myJdomDoc.getRootElement().getChild("record");

            /* von dem Treffer den Dokumententyp ermitteln */
            gattung = getGattung(myFirstHit);
            cod = ConfigOpac.getDoctypeByMapping(gattung.length() > 2 ? gattung.substring(0, 2) : gattung,
                configuration.getTitle());
            if (cod == null) {
                cod = ConfigOpac.getAllDoctypes().get(0);
                gattung = cod.getMappings().get(0);
            }

            /*
             * wenn der Treffer ein Volume eines Multivolume-Bandes ist, dann
             * das Sammelwerk überordnen
             */
            // if (isMultivolume()) {
            if (cod.isMultiVolume()) {
                /* Sammelband-PPN ermitteln */
                String multiVolumePpn = getPpnFromParent(myFirstHit, "036D", "9");
                if (!multiVolumePpn.equals("")) {
                    /* Sammelband aus dem Opac holen */

                    myQuery = new Query(multiVolumePpn, "12");
                    /* wenn ein Treffer des Parents im Opac gefunden wurde */
                    if (client.getNumberOfHits(myQuery, timeout) == 1) {
                        Node myParentHitlist = client.retrievePicaNode(myQuery, 1, timeout);
                        /* Opac-Beautifier aufrufen */
                        myParentHitlist = configuration.executeBeautifier(myParentHitlist);
                        /* Konvertierung in jdom-Elemente */
                        Document myJdomDocMultivolumeband = new DOMBuilder().build(myParentHitlist.getOwnerDocument());

                        /* dem Rootelement den Volume-Treffer hinzufügen */
                        myFirstHit.getParent().removeContent(myFirstHit);
                        myJdomDocMultivolumeband.getRootElement().addContent(myFirstHit);

                        myJdomDoc = myJdomDocMultivolumeband;
                        myFirstHit = myJdomDoc.getRootElement().getChild("record");

                        /* die Jdom-Element wieder zurück zu Dom konvertieren */
                        DOMOutputter doutputter = new DOMOutputter();
                        myHitlist = doutputter.output(myJdomDocMultivolumeband);
                        /*
                         * dabei aber nicht das Document, sondern das erste Kind
                         * nehmen
                         */
                        myHitlist = myHitlist.getFirstChild();
                    }
                }
            }

            /*
             * wenn der Treffer ein Volume eines Periodical-Bandes ist, dann die
             * Serie überordnen
             */
            if (cod.isPeriodical()) {
                /* Sammelband-PPN ermitteln */
                String serialPublicationPpn = getPpnFromParent(myFirstHit, "036F", "9");
                if (!serialPublicationPpn.equals("")) {
                    /* Sammelband aus dem Opac holen */

                    myQuery = new Query(serialPublicationPpn, "12");
                    /* wenn ein Treffer des Parents im Opac gefunden wurde */
                    if (client.getNumberOfHits(myQuery, timeout) == 1) {
                        Node myParentHitlist = client.retrievePicaNode(myQuery, 1, timeout);
                        /* Opac-Beautifier aufrufen */
                        myParentHitlist = configuration.executeBeautifier(myParentHitlist);
                        /* Konvertierung in jdom-Elemente */
                        Document myJdomDocMultivolumeband = new DOMBuilder().build(myParentHitlist.getOwnerDocument());

                        /* dem Rootelement den Volume-Treffer hinzufügen */
                        myFirstHit.getParent().removeContent(myFirstHit);
                        myJdomDocMultivolumeband.getRootElement().addContent(myFirstHit);

                        myJdomDoc = myJdomDocMultivolumeband;
                        myFirstHit = myJdomDoc.getRootElement().getChild("record");

                        /* die Jdom-Element wieder zurück zu Dom konvertieren */
                        DOMOutputter doutputter = new DOMOutputter();
                        myHitlist = doutputter.output(myJdomDocMultivolumeband);
                        /*
                         * dabei aber nicht das Document, sondern das erste Kind
                         * nehmen
                         */
                        myHitlist = myHitlist.getFirstChild();
                    }
                }
            }

            /*
             * wenn der Treffer ein Contained Work ist, dann übergeordnetes Werk
             */
            // if (isContainedWork()) {
            if (cod.isContainedWork()) {
                /* PPN des übergeordneten Werkes ermitteln */
                String ueberGeordnetePpn = getPpnFromParent(myFirstHit, "021A", "9");
                if (!ueberGeordnetePpn.equals("")) {
                    /* Sammelband aus dem Opac holen */
                    myQuery = new Query(ueberGeordnetePpn, "12");
                    /* wenn ein Treffer des Parents im Opac gefunden wurde */
                    if (client.getNumberOfHits(myQuery, timeout) == 1) {
                        Node myParentHitlist = client.retrievePicaNode(myQuery, 1, timeout);
                        /* Opac-Beautifier aufrufen */
                        myParentHitlist = configuration.executeBeautifier(myParentHitlist);
                        /* Konvertierung in jdom-Elemente */
                        Document myJdomDocParent = new DOMBuilder().build(myParentHitlist.getOwnerDocument());
                        Element myFirstHitParent = myJdomDocParent.getRootElement().getChild("record");
                        /*
                         * alle Elemente des Parents übernehmen, die noch nicht
                         * selbst vorhanden sind
                         */
                        if (myFirstHitParent.getChildren() != null) {
                            for (@SuppressWarnings("unchecked")
                            Iterator<Element> iter = myFirstHitParent.getChildren().iterator(); iter.hasNext();) {
                                Element ele = iter.next();
                                if (getElementFromChildren(myFirstHit, ele.getAttributeValue("tag")) == null) {
                                    @SuppressWarnings("unchecked")
                                    List<Element> children = myFirstHit.getChildren();
                                    children.add(getCopyFromJdomElement(ele));
                                }
                            }
                        }
                    }
                }
            }

            /*
             * aus Opac-Ergebnis RDF-Datei erzeugen
             */

            /* zugriff auf ugh-Klassen */
            PicaPlus pp = new PicaPlus(preferences);
            pp.read(myHitlist);
            DigitalDocument dd = pp.getDigitalDocument();
            ff = new XStream(preferences);
            ff.setDigitalDocument(dd);
            /* BoundBook hinzufügen */
            DocStructType dst = preferences.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);
            /* Inhalt des RDF-Files überprüfen und ergänzen */
            checkMyOpacResult(ff.getDigitalDocument(), preferences, myFirstHit, cod, gattung);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return createResult(cod.getTitle(), myFirstHit, ff);
    }

    /**
     * DocType (Gattung) ermitteln.
     *
     * @param inHit
     *            input element
     * @return empty String
     */
    @SuppressWarnings("unchecked")
    private static String getGattung(Element inHit) {
        if (inHit == null) {
            return "";
        }
        for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
            Element tempElement = iter.next();
            String feldname = tempElement.getAttributeValue("tag");
            // System.out.println(feldname);
            if (feldname.equals("002@")) {
                return getSubelementValue(tempElement, "0");
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private static String getSubelementValue(Element inElement, String attributeValue) {
        String rueckgabe = "";

        for (Iterator<Element> iter = inElement.getChildren().iterator(); iter.hasNext();) {
            Element subElement = iter.next();
            if (subElement.getAttributeValue("code").equals(attributeValue)) {
                rueckgabe = subElement.getValue();
            }
        }
        return rueckgabe;
    }

    /**
     * Die PPN des übergeordneten Bandes (MultiVolume: 036D-9 und ContainedWork:
     * 021A-9) ermitteln.
     *
     * @param inHit
     *            input element
     * @return PPN
     */
    @SuppressWarnings("unchecked")
    private static String getPpnFromParent(Element inHit, String inFeldName, String inSubElement) {
        for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
            Element tempElement = iter.next();
            String feldname = tempElement.getAttributeValue("tag");
            // System.out.println(feldname);
            if (feldname.equals(inFeldName)) {
                return getSubelementValue(tempElement, inSubElement);
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private static Element getElementFromChildren(Element inHit, String inTagName) {
        for (Iterator<Element> iter2 = inHit.getChildren().iterator(); iter2.hasNext();) {
            Element myElement = iter2.next();
            String feldname = myElement.getAttributeValue("tag");
            // System.out.println(feldname);
            /*
             * wenn es das gesuchte Feld ist, dann den Wert mit dem passenden
             * Attribut zurückgeben
             */
            if (feldname.equals(inTagName)) {
                return myElement;
            }
        }
        return null;
    }

    /**
     * Rekursives Kopieren von Elementen, weil das Einfügen eines Elements an
     * einen anderen Knoten mit dem Fehler abbricht, dass das einzufügende
     * Element bereits einen Parent hat.
     */
    @SuppressWarnings("unchecked")
    private static Element getCopyFromJdomElement(Element inHit) {
        Element myElement = new Element(inHit.getName());
        myElement.setText(inHit.getText());
        /* jetzt auch alle Attribute übernehmen */
        if (inHit.getAttributes() != null) {
            for (Iterator<Attribute> iter = inHit.getAttributes().iterator(); iter.hasNext();) {
                Attribute att = iter.next();
                myElement.getAttributes().add(new Attribute(att.getName(), att.getValue()));
            }
        }
        /* jetzt auch alle Children übernehmen */
        if (inHit.getChildren() != null) {

            for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
                Element ele = iter.next();
                myElement.addContent(getCopyFromJdomElement(ele));
            }
        }
        return myElement;
    }

    /*
     * Ergänze das Docstruct um zusätzliche Opac-Details
     */

    private static void checkMyOpacResult(DigitalDocument inDigDoc, Prefs inPrefs, Element myFirstHit,
            ConfigOpacDoctype cod, String gattung) {
        DocStruct topstruct = inDigDoc.getLogicalDocStruct();
        DocStruct boundbook = inDigDoc.getPhysicalDocStruct();
        DocStruct topstructChild = null;
        Element mySecondHit = null;

        /*
         * bei Multivolumes noch das Child in xml und docstruct ermitteln
         */
        // if (isMultivolume()) {
        if (cod.isMultiVolume()) {
            try {
                topstructChild = (DocStruct) topstruct.getAllChildren().get(0);
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
            }
            mySecondHit = (Element) myFirstHit.getParentElement().getChildren().get(1);
        }

        /*
         * vorhandene PPN als digitale oder analoge einsetzen
         */
        String ppn = getElementFieldValue(myFirstHit, "003@", "0");
        UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", "");
        if (gattung.toLowerCase().startsWith("o")) {
            UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", ppn);
        } else {
            UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDSource", ppn);
        }

        /*
         * wenn es ein multivolume ist, dann auch die PPN prüfen
         */
        if (topstructChild != null && mySecondHit != null) {
            String secondHitppn = getElementFieldValue(mySecondHit, "003@", "0");
            UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", "");
            if (gattung.toLowerCase().startsWith("o")) {
                UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", secondHitppn);
            } else {
                UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDSource", secondHitppn);
            }
        }

        /*
         * den Main-Title bereinigen
         */
        String myTitle = getElementFieldValue(myFirstHit, "021A", "a");
        /*
         * wenn der Fulltittle nicht in dem Element stand, dann an anderer
         * Stelle nachsehen (vor allem bei Contained-Work)
         */
        if (myTitle == null || myTitle.length() == 0) {
            myTitle = getElementFieldValue(myFirstHit, "021B", "a");
        }
        UGHUtils.replaceMetadatum(topstruct, inPrefs, "TitleDocMain", myTitle.replaceAll("@", ""));

        /*
         * Sorting-Titel mit Umlaut-Konvertierung
         */
        if (myTitle.contains("@")) {
            myTitle = myTitle.substring(myTitle.indexOf('@') + 1);
        }
        UGHUtils.replaceMetadatum(topstruct, inPrefs, "TitleDocMainShort", myTitle);

        /*
         * bei multivolumes den Main-Title bereinigen
         */
        if (topstructChild != null && mySecondHit != null) {
            String fulltitleMulti = getElementFieldValue(mySecondHit, "021A", "a").replaceAll("@", "");
            UGHUtils.replaceMetadatum(topstructChild, inPrefs, "TitleDocMain", fulltitleMulti);
        }

        /*
         * bei multivolumes den Sorting-Titel mit Umlaut-Konvertierung
         */
        if (topstructChild != null && mySecondHit != null) {
            String sortingTitleMulti = getElementFieldValue(mySecondHit, "021A", "a");
            if (sortingTitleMulti.contains("@")) {
                sortingTitleMulti = sortingTitleMulti.substring(sortingTitleMulti.indexOf('@') + 1);
            }
            UGHUtils.replaceMetadatum(topstructChild, inPrefs, "TitleDocMainShort", sortingTitleMulti);
            // sortingTitle = sortingTitleMulti;
        }

        /*
         * Sprachen - Konvertierung auf zwei Stellen
         */
        Iterable<String> sprachen = getElementFieldValues(myFirstHit, "010@", "a");
        sprachen = UGHUtils.convertLanguages(sprachen);
        UGHUtils.replaceMetadatum(topstruct, inPrefs, "DocLanguage", sprachen);

        /*
         * bei multivolumes die Sprachen - Konvertierung auf zwei Stellen
         */
        if (topstructChild != null && mySecondHit != null) {
            Iterable<String> sprachenMulti = getElementFieldValues(mySecondHit, "010@", "a");
            sprachenMulti = UGHUtils.convertLanguages(sprachenMulti);
            UGHUtils.replaceMetadatum(topstructChild, inPrefs, "DocLanguage", sprachenMulti);
        }

        /*
         * ISSN
         */
        String issn = getElementFieldValue(myFirstHit, "005A", "0");
        UGHUtils.replaceMetadatum(topstruct, inPrefs, "ISSN", issn);

        /*
         * Copyright
         */
        String copyright = getElementFieldValue(myFirstHit, "037I", "a");
        UGHUtils.replaceMetadatum(boundbook, inPrefs, "copyrightimageset", copyright);

        /*
         * Format
         */
        String format = getElementFieldValue(myFirstHit, "034I", "a");
        UGHUtils.replaceMetadatum(boundbook, inPrefs, "FormatSourcePrint", format);

        /*
         * Umfang
         */
        String umfang = getElementFieldValue(myFirstHit, "034D", "a");
        UGHUtils.replaceMetadatum(topstruct, inPrefs, "SizeSourcePrint", umfang);

        /*
         * Signatur
         */
        String sig = getElementFieldValue(myFirstHit, "209A", "c");
        if (sig.length() > 0) {
            sig = "<" + sig + ">";
        }
        sig += getElementFieldValue(myFirstHit, "209A", "f") + " ";
        sig += getElementFieldValue(myFirstHit, "209A", "a");
        UGHUtils.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", sig.trim());
        if (sig.trim().length() == 0) {
            sig = getElementFieldValue(myFirstHit, "209A/01", "c");
            if (sig.length() > 0) {
                sig = "<" + sig + ">";
            }
            sig += getElementFieldValue(myFirstHit, "209A/01", "f") + " ";
            sig += getElementFieldValue(myFirstHit, "209A/01", "a");
            if (mySecondHit != null) {
                sig += getElementFieldValue(mySecondHit, "209A", "f") + " ";
                sig += getElementFieldValue(mySecondHit, "209A", "a");
            }
            UGHUtils.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", sig.trim());
        }

        /*
         * bei Zeitschriften noch ein PeriodicalVolume als Child einfügen
         */
        // if (isPeriodical()) {
        if (cod.isPeriodical() && topstruct.getAllChildren() == null) {
            try {
                DocStructType dstV = inPrefs.getDocStrctTypeByName("PeriodicalVolume");
                DocStruct dsvolume = inDigDoc.createDocStruct(dstV);
                topstruct.addChild(dsvolume);
            } catch (TypeNotAllowedAsChildException e) {
                logger.error(e);
            }
        }
    }

    /**
     * The function getElementFieldValue() returns the values of the last
     * grandchild element of the first child element from a given JDOM Element
     * whose “code” attribute equals the given attribute name and whose parents’
     * “tag” attribute equals the given field name—or an empty String if there
     * is no such grandchild element.
     *
     * @param myFirstHit
     *            JDOM Element whose descendant elements are to be examined
     * @param inFieldName
     *            tag attribute value to compare to
     * @param inAttributeName
     *            code attribute value to compare to
     * @return values of the last grandchild of the first child whose code
     *         attribute equals and whose parents’ tag attribute equals, too
     */
    @SuppressWarnings("unchecked")
    private static String getElementFieldValue(Element myFirstHit, String inFieldName, String inAttributeName) {

        for (Iterator<Element> iter2 = myFirstHit.getChildren().iterator(); iter2.hasNext();) {
            Element myElement = iter2.next();
            String feldname = myElement.getAttributeValue("tag");
            /*
             * wenn es das gesuchte Feld ist, dann den Wert mit dem passenden
             * Attribut zurückgeben
             */
            if (feldname.equals(inFieldName)) {
                return getFieldValue(myElement, inAttributeName);
            }
        }
        return "";
    }

    /**
     * The function getElementFieldValues() returns the values of all grandchild
     * elements from a given JDOM Element whose “code” attribute equals the
     * given attribute name and whose parents’ “tag” attribute equals the given
     * field name—or an empty Collection if there is no such grandchild element.
     *
     * @param myFirstHit
     *            JDOM Element whose descendant elements are to be examined
     * @param inFieldName
     *            tag attribute value to compare to
     * @param inAttributeName
     *            code attribute value to compare to
     * @return values of all grandchild elements whose code attribute equals and
     *         whose parents’ tag attribute equals, too
     */
    @SuppressWarnings("unchecked")
    private static Iterable<String> getElementFieldValues(Element myFirstHit, String inFieldName,
            String inAttributeName) {
        LinkedList<String> result = new LinkedList<>();
        for (Iterator<Element> iter2 = myFirstHit.getChildren().iterator(); iter2.hasNext();) {
            Element myElement = iter2.next();
            String feldname = myElement.getAttributeValue("tag");
            /*
             * wenn es das gesuchte Feld ist, dann den Wert mit dem passenden
             * Attribut zurückgeben
             */
            if (feldname.equals(inFieldName)) {
                result.addAll(getFieldValues(myElement, inAttributeName));
            }
        }
        return result;
    }

    /**
     * The function getFieldValue() returns the value of the last elements from
     * a given JDOM Element whose “code” attribute equals the given attribute
     * value or the empty String if there is no such child element.
     *
     * @param inElement
     *            JDOM Element whose child elements are to be examined
     * @param attributeValue
     *            code attribute value to compare to
     * @return values of all child elements whose code attribute equals the
     *         attribute value
     */
    @SuppressWarnings("unchecked")
    private static String getFieldValue(Element inElement, String attributeValue) {
        String rueckgabe = "";

        for (Iterator<Element> iter = inElement.getChildren().iterator(); iter.hasNext();) {
            Element subElement = iter.next();
            if (subElement.getAttributeValue("code").equals(attributeValue)) {
                rueckgabe = subElement.getValue();
            }
        }
        return rueckgabe;
    }

    /**
     * The function getFieldValues() returns the values of all child elements
     * from a given JDOM Element whose “code” attribute equals the given
     * attribute value or an empty Collection if there is no such child element.
     *
     * @param inElement
     *            JDOM Element whose child elements are to be examined
     * @param attributeValue
     *            code attribute value to compare to
     * @return values of all child elements whose code attribute equals the
     *         attribute value
     */
    @SuppressWarnings("unchecked")
    private static Collection<String> getFieldValues(Element inElement, String attributeValue) {
        List<String> rueckgabe = new LinkedList<>();

        for (Iterator<Element> iter = inElement.getChildren().iterator(); iter.hasNext();) {
            Element subElement = iter.next();
            if (subElement.getAttributeValue("code").equals(attributeValue)) {
                rueckgabe.add(subElement.getValue());
            }
        }
        return rueckgabe;
    }

    /**
     * The function createResult() creates a Map&lt;String, Object&gt; as result
     * of getHit(). The map contains the full hit as "fileformat", the docType
     * as "type" and some bibliographic metadata for Production to be able to
     * show a short hit display as supposed in
     * {@link org.goobi.production.plugin.CataloguePlugin.Hit}
     *
     * @param docType
     *            the DocType of the hit
     * @param hit
     *            the hit data as JDom Element
     * @param fileformat
     *            the hit data as Fileformat
     * @return a Map with the hit
     */
    private static Map<String, Object> createResult(String docType, Element hit, Fileformat fileformat) {
        final LocalTime dayEnd = new LocalTime(23, 59, 59, 999);

        Map<String, Object> result = new HashMap<>(20);
        LocalDate today = new LocalDate();

        result.put("fileformat", fileformat);
        result.put("type", docType);

        // add some basic metadata
        String accessed = getElementFieldValue(hit, "208@", "a");
        try {
            LocalDate date = toRecentLocalDate(accessed, today);
            result.put("accessed", date.toDateTime(date.isEqual(today) ? new LocalTime() : dayEnd).toString());
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }

        String lastName = getElementFieldValue(hit, "028A", "a");
        if (lastName.equals("")) {
            lastName = getElementFieldValue(hit, "028A", "l");
        }
        String firstName = getElementFieldValue(hit, "028A", "d");
        if (firstName.equals("")) {
            firstName = getElementFieldValue(hit, "028A", "P");
        }
        String middleTitle = getElementFieldValue(hit, "028A", "c");
        String author = lastName + (!firstName.equals("") ? ", " : "") + firstName
                + (!middleTitle.equals("") ? " " : "") + middleTitle;
        if (author.equals("")) {
            author = getElementFieldValue(hit, "028A", "8");
        }
        if (author.equals("")) {
            String lastName2 = getElementFieldValue(hit, "028C", "a");
            if (lastName2.equals("")) {
                lastName2 = getElementFieldValue(hit, "028C", "l");
            }
            String firstName2 = getElementFieldValue(hit, "028C", "d");
            if (firstName2.equals("")) {
                firstName2 = getElementFieldValue(hit, "028C", "P");
            }
            String middleTitle2 = getElementFieldValue(hit, "028C", "c");
            author = lastName2 + (!firstName2.equals("") ? ", " : "") + firstName2
                    + (!middleTitle2.equals("") ? " " : "") + middleTitle2;
        }
        result.put("creator", author);

        String date = getElementFieldValue(hit, "201B", "0");
        try {
            LocalDate localDate = toRecentLocalDate(date, today);
            result.put("date", localDate.toString());
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }

        result.put("edition", getElementFieldValue(hit, "032@", "a"));
        result.put("format", docType.startsWith("O") ? "internet" : "monograph");
        result.put("number", getElementFieldValue(hit, "036E", "l"));
        result.put("place", getElementFieldValue(hit, "033A", "p"));
        result.put("publisher", getElementFieldValue(hit, "033A", "n"));
        result.put("series", getElementFieldValue(hit, "036E", "a"));

        String subseries = getElementFieldValue(hit, "021A", "d");
        if (subseries == null || subseries.length() == 0) {
            subseries = getElementFieldValue(hit, "021B", "d");
        }
        if (subseries == null || subseries.length() == 0) {
            subseries = getElementFieldValue(hit, "027D", "d");
        }
        result.put("subseries", subseries);

        String title = getElementFieldValue(hit, "021A", "a");
        if (title == null || title.length() == 0) {
            title = getElementFieldValue(hit, "021B", "a");
        }
        if (title == null || title.length() == 0) {
            title = getElementFieldValue(hit, "027D", "a");
        }
        String titleLong = getElementFieldValue(hit, "021A", "d");
        if (titleLong != null && titleLong.length() > 0) {
            title = title + " : " + titleLong;
        }
        result.put("title", title.replaceAll("@", ""));

        result.put("url", getElementFieldValue(hit, "209R", "a"));
        result.put("year", getElementFieldValue(hit, "011@", "a"));
        return result;
    }

    /**
     * The function toRecentLocalDate() interprets a String of scheme "dd-mm-yy"
     * as a LocalDate within the last 100 years up to a given reference date.
     *
     * @param dateString
     *            a date String to interpret dd-mm-yy
     * @param upTo
     *            a reference date
     * @return the date value as LocalDate
     */
    private static LocalDate toRecentLocalDate(String dateString, LocalDate upTo) {
        int centuryPrefix = upTo.getYear() / 100;
        String[] fields = dateString.split("-");
        int year = (100 * centuryPrefix) + Integer.parseInt(fields[2]);
        if (year > upTo.getYear()) {
            year -= 100;
        }
        return new LocalDate(year, Integer.parseInt(fields[1]), Integer.parseInt(fields[0]));
    }

    /**
     * The function getNumberOfHits() returns the number of hits from a given
     * search result.
     *
     * @param searchResult
     *            the reference to the search whose number of hits shall be
     *            looked up
     * @param timeout
     *            ignored because there is no network acceess in this step
     * @return the number of hits
     * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#getNumberOfHits(Object,
     *      long)
     */
    public static long getNumberOfHits(Object searchResult, long timeout) {
        if (searchResult instanceof FindResult) {
            return ((FindResult) searchResult).getHits();
        } else {
            throw new ClassCastException();
        }
    }

    /**
     * The function getTempDir() provides a reference to the file system
     * directory where temporary files are written in.
     *
     * @return the file system directory where to write temporary files
     */
    static String getTempDir() {
        return tempDir;
    }

    /**
     * The function getDescription() returns a human-readable name for the
     * plug-in in English. The parameter language is ignored.
     *
     * @param language
     *            desired language of the human-readable name (support is
     *            optional)
     * @return a human-readable name for the plug-in
     * @see org.goobi.production.plugin.UnspecificPlugin#getTitle(Locale)
     */
    public static String getTitle(Locale language) {
        return "PICA Catalogue Plugin";
    }

    /**
     * The function setPreferences is called by Production to set the UGH
     * preferences to be used.
     *
     * @param preferences
     *            the UGH preferences
     * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#setPreferences(Prefs)
     */
    public void setPreferences(PrefsInterface preferences) {
        this.preferences = (Prefs) preferences;
    }

    /**
     * The function supportsCatalogue() investigates whether the plug-in is able
     * to access a catalogue identified by the given String. (This depends on
     * the configuration.)
     *
     * @param catalogue
     *            a String indentifying the catalogue
     * @return whether the plug-in is able to acceess that catalogue
     * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#supportsCatalogue(String)
     */
    public static boolean supportsCatalogue(String catalogue) {
        return ConfigOpac.getCatalogueByName(catalogue) != null;
    }

    /**
     * The function useCatalogue() sets a catalogue to be used.
     *
     * @param catalogueID
     *            a String indentifying the catalogue
     * @throws ParserConfigurationException
     *             if a DocumentBuilder cannot be created which satisfies the
     *             configuration requested
     * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#useCatalogue(String)
     */
    public void useCatalogue(String catalogueID) throws ParserConfigurationException {
        this.configuration = ConfigOpac.getCatalogueByName(catalogueID);
        this.catalogue = new Catalogue(configuration);
        GetOpac catalogueClient = new GetOpac(catalogue);
        catalogueClient.setCharset(configuration.getCharset());
        this.client = catalogueClient;
    }
}
