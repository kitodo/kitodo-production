/**
 * This file is part of the pica opac import plugin for the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://digiverso.com 
 *          - http://www.intranda.com
 * 
 * Copyright 2011 - 2013, intranda GmbH, Göttingen
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package de.intranda.goobi.plugins;

import java.io.IOException;

import java.util.Iterator;
import java.util.StringTokenizer;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IOpacPlugin;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Node;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.mets.XStream;
import ugh.fileformats.opac.PicaPlus;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.unigoettingen.sub.search.opac.Catalogue;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;
import de.unigoettingen.sub.search.opac.GetOpac;
import de.unigoettingen.sub.search.opac.Query;

@PluginImplementation
public class PicaOpacImport implements IOpacPlugin {
    private static final Logger myLogger = Logger.getLogger(PicaOpacImport.class);

    private int hitcount;
    private String gattung = "Aa";
    private String atstsl;
    ConfigOpacCatalogue coc;
    private boolean verbose = false;

    /* (non-Javadoc)
     * @see de.sub.goobi.Import.IOpac#OpacToDocStruct(java.lang.String, java.lang.String, java.lang.String, ugh.dl.Prefs, boolean)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Fileformat search(String inSuchfeld, String inSuchbegriff, ConfigOpacCatalogue catalogue, Prefs inPrefs) throws Exception {
        /*
         * -------------------------------- Katalog auswählen --------------------------------
         */
        coc = catalogue;
        if (this.coc == null) {
            throw new IOException("Catalogue not found: " + coc.getTitle() + ", please check Configuration in goobi_opac.xml");
        }
        Catalogue cat =
                new Catalogue(this.coc.getDescription(), this.coc.getAddress(), this.coc.getPort(), this.coc.getCbs(), this.coc.getDatabase());
        if (verbose) {
            Helper.setMeldung(null, Helper.getTranslation("CatalogueUsage") + ": ", this.coc.getDescription());
        }
        GetOpac myOpac = new GetOpac(cat);
        myOpac.setData_character_encoding(this.coc.getCharset());
        Query myQuery = new Query(inSuchbegriff, inSuchfeld);
        /* im Notfall ohne Treffer sofort aussteigen */
        this.hitcount = myOpac.getNumberOfHits(myQuery);
        if (this.hitcount == 0) {
            return null;
        }

        /*
         * -------------------------------- Opac abfragen und erhaltenes Dom-Dokument in JDom-Dokument umwandeln --------------------------------
         */
        Node myHitlist = myOpac.retrievePicaNode(myQuery, 1);
        /* Opac-Beautifier aufrufen */
        myHitlist = this.coc.executeBeautifier(myHitlist);
        Document myJdomDoc = new DOMBuilder().build(myHitlist.getOwnerDocument());
        Element myFirstHit = myJdomDoc.getRootElement().getChild("record");

        /* von dem Treffer den Dokumententyp ermitteln */
        this.gattung = getGattung(myFirstHit);

        myLogger.debug("Gattung: " + this.gattung);
        /*
         * -------------------------------- wenn der Treffer ein Volume eines Multivolume-Bandes ist, dann das Sammelwerk überordnen
         * --------------------------------
         */
        // if (isMultivolume()) {
        if (getOpacDocType().isMultiVolume()) {
            /* Sammelband-PPN ermitteln */
            String multiVolumePpn = getPpnFromParent(myFirstHit, "036D", "9");
            if (multiVolumePpn != "") {
                /* Sammelband aus dem Opac holen */

                myQuery = new Query(multiVolumePpn, "12");
                /* wenn ein Treffer des Parents im Opac gefunden wurde */
                if (myOpac.getNumberOfHits(myQuery) == 1) {
                    Node myParentHitlist = myOpac.retrievePicaNode(myQuery, 1);
                    /* Opac-Beautifier aufrufen */
                    myParentHitlist = this.coc.executeBeautifier(myParentHitlist);
                    /* Konvertierung in jdom-Elemente */
                    Document myJdomDocMultivolumeband = new DOMBuilder().build(myParentHitlist.getOwnerDocument());

                    /* Testausgabe */
                    // XMLOutputter outputter = new XMLOutputter();
                    // FileOutputStream output = new
                    // FileOutputStream("D:/fileParent.xml");
                    // outputter.output(myJdomDocMultivolumeband.getRootElement(),
                    // output);
                    /* dem Rootelement den Volume-Treffer hinzufügen */
                    myFirstHit.getParent().removeContent(myFirstHit);
                    myJdomDocMultivolumeband.getRootElement().addContent(myFirstHit);

                    /* Testausgabe */
                    // output = new FileOutputStream("D:/fileFull.xml");
                    // outputter.output(myJdomDocMultivolumeband.getRootElement(),
                    // output);
                    myJdomDoc = myJdomDocMultivolumeband;
                    myFirstHit = myJdomDoc.getRootElement().getChild("record");

                    /* die Jdom-Element wieder zurück zu Dom konvertieren */
                    DOMOutputter doutputter = new DOMOutputter();
                    myHitlist = doutputter.output(myJdomDocMultivolumeband);
                    /*
                     * dabei aber nicht das Document, sondern das erste Kind nehmen
                     */
                    myHitlist = myHitlist.getFirstChild();
                }
            }
        }

        /*
         * -------------------------------- wenn der Treffer ein Contained Work ist, dann übergeordnetes Werk --------------------------------
         */
        // if (isContainedWork()) {
        if (getOpacDocType().isContainedWork()) {
            /* PPN des übergeordneten Werkes ermitteln */
            String ueberGeordnetePpn = getPpnFromParent(myFirstHit, "021A", "9");
            if (ueberGeordnetePpn != "") {
                /* Sammelband aus dem Opac holen */
                myQuery = new Query(ueberGeordnetePpn, "12");
                /* wenn ein Treffer des Parents im Opac gefunden wurde */
                if (myOpac.getNumberOfHits(myQuery) == 1) {
                    Node myParentHitlist = myOpac.retrievePicaNode(myQuery, 1);
                    /* Opac-Beautifier aufrufen */
                    myParentHitlist = this.coc.executeBeautifier(myParentHitlist);
                    /* Konvertierung in jdom-Elemente */
                    Document myJdomDocParent = new DOMBuilder().build(myParentHitlist.getOwnerDocument());
                    Element myFirstHitParent = myJdomDocParent.getRootElement().getChild("record");
                    /* Testausgabe */
                    // XMLOutputter outputter = new XMLOutputter();
                    // FileOutputStream output = new
                    // FileOutputStream("D:/fileParent.xml");
                    // outputter.output(myJdomDocParent.getRootElement(),
                    // output);
                    /*
                     * alle Elemente des Parents übernehmen, die noch nicht selbst vorhanden sind
                     */
                    if (myFirstHitParent.getChildren() != null) {

                        for (Iterator<Element> iter = myFirstHitParent.getChildren().iterator(); iter.hasNext();) {
                            Element ele = iter.next();
                            if (getElementFromChildren(myFirstHit, ele.getAttributeValue("tag")) == null) {
                                myFirstHit.getChildren().add(getCopyFromJdomElement(ele));
                            }
                        }
                    }
                }
            }
        }

        /*
         * -------------------------------- aus Opac-Ergebnis RDF-Datei erzeugen --------------------------------
         */
        /* XML in Datei schreiben */
        //		 XMLOutputter outputter = new XMLOutputter();
        //		 FileOutputStream output = new
        //		 FileOutputStream("/home/robert/temp_opac.xml");
        //		 outputter.output(myJdomDoc.getRootElement(), output);

        /* myRdf temporär in Datei schreiben */
        // myRdf.write("D:/temp.rdf.xml");

        /* zugriff auf ugh-Klassen */
        PicaPlus pp = new PicaPlus(inPrefs);
        pp.read(myHitlist);
        DigitalDocument dd = pp.getDigitalDocument();
        Fileformat ff = new XStream(inPrefs);
        ff.setDigitalDocument(dd);
        /* BoundBook hinzufügen */
        DocStructType dst = inPrefs.getDocStrctTypeByName("BoundBook");
        DocStruct dsBoundBook = dd.createDocStruct(dst);
        dd.setPhysicalDocStruct(dsBoundBook);
        /* Inhalt des RDF-Files überprüfen und ergänzen */
        checkMyOpacResult(ff.getDigitalDocument(), inPrefs, myFirstHit, verbose);
        // rdftemp.write("D:/PicaRdf.xml");
        return ff;
    }

    /**
     * DocType (Gattung) ermitteln
     * 
     * @param inHit
     * @return
     */
    @SuppressWarnings("unchecked")
    private String getGattung(Element inHit) {

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
    private String getSubelementValue(Element inElement, String attributeValue) {
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
     * die PPN des übergeordneten Bandes (MultiVolume: 036D-9 und ContainedWork: 021A-9) ermitteln
     * 
     * @param inElement
     * @return
     */
    @SuppressWarnings("unchecked")
    private String getPpnFromParent(Element inHit, String inFeldName, String inSubElement) {
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

    /* (non-Javadoc)
     * @see de.sub.goobi.Import.IOpac#getHitcount()
     */
    @Override
    public int getHitcount() {
        return this.hitcount;
    }

    /*
     * ##################################################### ##################################################### ## ## Erg�nze das Docstruct um
     * zusätzliche Opac-Details ## ##################################################### ####################################################
     */

    private void checkMyOpacResult(DigitalDocument inDigDoc, Prefs inPrefs, Element myFirstHit, boolean verbose) {
        UghHelper ughhelp = new UghHelper();
        DocStruct topstruct = inDigDoc.getLogicalDocStruct();
        DocStruct boundbook = inDigDoc.getPhysicalDocStruct();
        DocStruct topstructChild = null;
        Element mySecondHit = null;

        /*
         * -------------------------------- bei Multivolumes noch das Child in xml und docstruct ermitteln --------------------------------
         */
        // if (isMultivolume()) {
        if (getOpacDocType().isMultiVolume()) {
            try {
                topstructChild = topstruct.getAllChildren().get(0);
            } catch (RuntimeException e) {
            }
            mySecondHit = (Element) myFirstHit.getParentElement().getChildren().get(1);
        }

        /*
         * -------------------------------- vorhandene PPN als digitale oder analoge einsetzen --------------------------------
         */
        String ppn = getElementFieldValue(myFirstHit, "003@", "0");
        ughhelp.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", "");
        if (this.gattung.toLowerCase().startsWith("o")) {
            ughhelp.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", ppn);
        } else {
            ughhelp.replaceMetadatum(topstruct, inPrefs, "CatalogIDSource", ppn);
        }

        /*
         * -------------------------------- wenn es ein multivolume ist, dann auch die PPN prüfen --------------------------------
         */
        if (topstructChild != null && mySecondHit != null) {
            String secondHitppn = getElementFieldValue(mySecondHit, "003@", "0");
            ughhelp.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", "");
            if (this.gattung.toLowerCase().startsWith("o")) {
                ughhelp.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", secondHitppn);
            } else {
                ughhelp.replaceMetadatum(topstructChild, inPrefs, "CatalogIDSource", secondHitppn);
            }
        }

        /*
         * -------------------------------- den Main-Title bereinigen --------------------------------
         */
        String myTitle = getElementFieldValue(myFirstHit, "021A", "a");
        /*
         * wenn der Fulltittle nicht in dem Element stand, dann an anderer Stelle nachsehen (vor allem bei Contained-Work)
         */
        if (myTitle == null || myTitle.length() == 0) {
            myTitle = getElementFieldValue(myFirstHit, "021B", "a");
        }
        ughhelp.replaceMetadatum(topstruct, inPrefs, "TitleDocMain", myTitle.replaceAll("@", ""));

        /*
         * -------------------------------- Sorting-Titel mit Umlaut-Konvertierung --------------------------------
         */
        if (myTitle.indexOf("@") != -1) {
            myTitle = myTitle.substring(myTitle.indexOf("@") + 1);
        }
        ughhelp.replaceMetadatum(topstruct, inPrefs, "TitleDocMainShort", myTitle);

        /*
         * -------------------------------- bei multivolumes den Main-Title bereinigen --------------------------------
         */
        if (topstructChild != null && mySecondHit != null) {
            String fulltitleMulti = getElementFieldValue(mySecondHit, "021A", "a").replaceAll("@", "");
            ughhelp.replaceMetadatum(topstructChild, inPrefs, "TitleDocMain", fulltitleMulti);
        }

        /*
         * -------------------------------- bei multivolumes den Sorting-Titel mit Umlaut-Konvertierung --------------------------------
         */
        if (topstructChild != null && mySecondHit != null) {
            String sortingTitleMulti = getElementFieldValue(mySecondHit, "021A", "a");
            if (sortingTitleMulti.indexOf("@") != -1) {
                sortingTitleMulti = sortingTitleMulti.substring(sortingTitleMulti.indexOf("@") + 1);
            }
            ughhelp.replaceMetadatum(topstructChild, inPrefs, "TitleDocMainShort", sortingTitleMulti);
            // sortingTitle = sortingTitleMulti;
        }

        /*
         * -------------------------------- Sprachen - Konvertierung auf zwei Stellen --------------------------------
         */
        String sprache = getElementFieldValue(myFirstHit, "010@", "a");
        sprache = ughhelp.convertLanguage(sprache);
        ughhelp.replaceMetadatum(topstruct, inPrefs, "DocLanguage", sprache);

        /*
         * -------------------------------- bei multivolumes die Sprachen - Konvertierung auf zwei Stellen --------------------------------
         */
        if (topstructChild != null && mySecondHit != null) {
            String spracheMulti = getElementFieldValue(mySecondHit, "010@", "a");
            spracheMulti = ughhelp.convertLanguage(spracheMulti);
            ughhelp.replaceMetadatum(topstructChild, inPrefs, "DocLanguage", spracheMulti);
        }

        /*
         * -------------------------------- ISSN --------------------------------
         */
        String issn = getElementFieldValue(myFirstHit, "005A", "0");
        ughhelp.replaceMetadatum(topstruct, inPrefs, "ISSN", issn);

        /*
         * -------------------------------- Copyright --------------------------------
         */
        String copyright = getElementFieldValue(myFirstHit, "037I", "a");
        ughhelp.replaceMetadatum(boundbook, inPrefs, "copyrightimageset", copyright);

        /*
         * -------------------------------- Format --------------------------------
         */
        String format = getElementFieldValue(myFirstHit, "034I", "a");
        ughhelp.replaceMetadatum(boundbook, inPrefs, "FormatSourcePrint", format);

        /*
         * -------------------------------- Umfang --------------------------------
         */
        String umfang = getElementFieldValue(myFirstHit, "034D", "a");
        ughhelp.replaceMetadatum(topstruct, inPrefs, "SizeSourcePrint", umfang);

        /*
         * -------------------------------- Signatur --------------------------------
         */
        String sig = getElementFieldValue(myFirstHit, "209A", "c");
        if (sig.length() > 0) {
            sig = "<" + sig + ">";
        }
        sig += getElementFieldValue(myFirstHit, "209A", "f") + " ";
        sig += getElementFieldValue(myFirstHit, "209A", "a");
        ughhelp.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", sig.trim());
        if (sig.trim().length() == 0) {
            myLogger.debug("Signatur part 1: " + sig);
            myLogger.debug(myFirstHit.getChildren());
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
            ughhelp.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", sig.trim());
        }
        myLogger.debug("Signatur full: " + sig);

        /*
         * -------------------------------- Ats Tsl Vorbereitung --------------------------------
         */
        myTitle = myTitle.toLowerCase();
        myTitle = myTitle.replaceAll("&", "");

        /*
         * -------------------------------- bei nicht-Zeitschriften Ats berechnen --------------------------------
         */
        // if (!gattung.startsWith("ab") && !gattung.startsWith("ob")) {
        String autor = getElementFieldValue(myFirstHit, "028A", "a").toLowerCase();
        if (autor == null || autor.equals("")) {
            autor = getElementFieldValue(myFirstHit, "028A", "8").toLowerCase();
        }
        this.atstsl = createAtstsl(myTitle, autor);

        /*
         * -------------------------------- bei Zeitschriften noch ein PeriodicalVolume als Child einfügen --------------------------------
         */
        // if (isPeriodical()) {
        if (getOpacDocType().isPeriodical()) {
            try {
                DocStructType dstV = inPrefs.getDocStrctTypeByName("PeriodicalVolume");
                DocStruct dsvolume = inDigDoc.createDocStruct(dstV);
                topstruct.addChild(dsvolume);
            } catch (TypeNotAllowedForParentException e) {
                e.printStackTrace();
            } catch (TypeNotAllowedAsChildException e) {
                e.printStackTrace();
            }
        }

    }

    /* (non-Javadoc)
     * @see de.sub.goobi.Import.IOpac#createAtstsl(java.lang.String, java.lang.String)
     */
    @Override
    public String createAtstsl(String myTitle, String autor) {
        String myAtsTsl = "";
        if (autor != null && !autor.equals("")) {
            /* autor */
            if (autor.length() > 4) {
                myAtsTsl = autor.substring(0, 4);
            } else {
                myAtsTsl = autor;
                /* titel */
            }

            if (myTitle.length() > 4) {
                myAtsTsl += myTitle.substring(0, 4);
            } else {
                myAtsTsl += myTitle;
            }
        }

        /*
         * -------------------------------- bei Zeitschriften Tsl berechnen --------------------------------
         */
        // if (gattung.startsWith("ab") || gattung.startsWith("ob")) {
        if (autor == null || autor.equals("")) {
            myAtsTsl = "";
            StringTokenizer tokenizer = new StringTokenizer(myTitle);
            int counter = 1;
            while (tokenizer.hasMoreTokens()) {
                String tok = tokenizer.nextToken();
                if (counter == 1) {
                    if (tok.length() > 4) {
                        myAtsTsl += tok.substring(0, 4);
                    } else {
                        myAtsTsl += tok;
                    }
                }
                if (counter == 2 || counter == 3) {
                    if (tok.length() > 2) {
                        myAtsTsl += tok.substring(0, 2);
                    } else {
                        myAtsTsl += tok;
                    }
                }
                if (counter == 4) {
                    if (tok.length() > 1) {
                        myAtsTsl += tok.substring(0, 1);
                    } else {
                        myAtsTsl += tok;
                    }
                }
                counter++;
            }
        }
        /* im ATS-TSL die Umlaute ersetzen */

        myAtsTsl = myAtsTsl.replaceAll("[\\W]", "");
        return myAtsTsl;
    }

    @SuppressWarnings("unchecked")
    private Element getElementFromChildren(Element inHit, String inTagName) {
        for (Iterator<Element> iter2 = inHit.getChildren().iterator(); iter2.hasNext();) {
            Element myElement = iter2.next();
            String feldname = myElement.getAttributeValue("tag");
            // System.out.println(feldname);
            /*
             * wenn es das gesuchte Feld ist, dann den Wert mit dem passenden Attribut zurückgeben
             */
            if (feldname.equals(inTagName)) {
                return myElement;
            }
        }
        return null;
    }

    /**
     * rekursives Kopieren von Elementen, weil das Einfügen eines Elements an einen anderen Knoten mit dem Fehler abbricht, dass das einzufügende
     * Element bereits einen Parent hat ================================================================
     */
    @SuppressWarnings("unchecked")
    private Element getCopyFromJdomElement(Element inHit) {
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

    @SuppressWarnings("unchecked")
    private String getElementFieldValue(Element myFirstHit, String inFieldName, String inAttributeName) {

        for (Iterator<Element> iter2 = myFirstHit.getChildren().iterator(); iter2.hasNext();) {
            Element myElement = iter2.next();
            String feldname = myElement.getAttributeValue("tag");
            /*
             * wenn es das gesuchte Feld ist, dann den Wert mit dem passenden Attribut zurückgeben
             */
            if (feldname.equals(inFieldName)) {
                return getFieldValue(myElement, inAttributeName);
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String getFieldValue(Element inElement, String attributeValue) {
        String rueckgabe = "";

        for (Iterator<Element> iter = inElement.getChildren().iterator(); iter.hasNext();) {
            Element subElement = iter.next();
            if (subElement.getAttributeValue("code").equals(attributeValue)) {
                rueckgabe = subElement.getValue();
            }
        }
        return rueckgabe;
    }

    /* (non-Javadoc)
     * @see de.sub.goobi.Import.IOpac#getAtstsl()
     */
    @Override
    public String getAtstsl() {
        return this.atstsl;
    }

    /*
     * ##################################################### ##################################################### ## ## Publikationstypen aus der
     * Konfiguration auslesen ## ##################################################### ####################################################
     */

    // public boolean isMonograph() {
    // if (gattung != null && config.getParameter("docTypeMonograph",
    // "").contains(gattung.substring(0, 2)))
    // return true;
    // else
    // return false;
    // }
    // public boolean isPeriodical() {
    // if (gattung != null && config.getParameter("docTypePeriodical",
    // "").contains(gattung.substring(0, 2)))
    // return true;
    // else
    // return false;
    // }
    //
    // public boolean isMultivolume() {
    // if (gattung != null && config.getParameter("docTypeMultivolume",
    // "").contains(gattung.substring(0, 2)))
    // return true;
    // else
    // return false;
    // }
    //
    // public boolean isContainedWork() {
    // if (gattung != null
    // && config.getParameter("docTypeContainedWork",
    // "").contains(gattung.substring(0, 2)))
    // return true;
    // else
    // return false;
    // }
    /* (non-Javadoc)
     * @see de.sub.goobi.Import.IOpac#getOpacDocType(boolean)
     */
    @Override
    public ConfigOpacDoctype getOpacDocType() {
        try {
            ConfigOpac co = new ConfigOpac();
            ConfigOpacDoctype cod = co.getDoctypeByMapping(this.gattung.substring(0, 2), this.coc.getTitle());
            if (cod == null) {
                if (verbose) {
                    Helper.setFehlerMeldung(Helper.getTranslation("CatalogueUnKnownType") + ": ", this.gattung);
                }
                cod = new ConfigOpac().getAllDoctypes().get(0);
                this.gattung = cod.getMappings().get(0);
                if (verbose) {
                    Helper.setFehlerMeldung(Helper.getTranslation("CatalogueChangeDocType") + ": ", this.gattung + " - " + cod.getTitle());
                }
            }
            return cod;
        } catch (IOException e) {
            myLogger.error("OpacDoctype unknown", e);
            if (verbose) {
                Helper.setFehlerMeldung(Helper.getTranslation("CatalogueUnKnownType"), e);
            }
            return null;
        }
    }

    @Override
    public PluginType getType() {
        return PluginType.Opac;
    }

    @Override
    public String getTitle() {
        return "PICA";
    }

    @Override
    public String getDescription() {
        return "PICA";
    }
}