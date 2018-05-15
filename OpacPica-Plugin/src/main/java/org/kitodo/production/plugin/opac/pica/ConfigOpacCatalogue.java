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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;

class ConfigOpacCatalogue {
    private static final Logger logger = LogManager.getLogger(ConfigOpacCatalogue.class);
    private String title = "";
    private String description = "";
    private String address = "";
    private String database = "";
    private int port = 80;
    private String cbs;
    private String charset = "iso-8859-1";
    private final ArrayList<ConfigOpacCatalogueBeautifier> beautifySetList;

    private ConfigOpacCatalogue(String title, String desciption, String address, String database, String iktlist,
            int port, ArrayList<ConfigOpacCatalogueBeautifier> inBeautifySetList, String opacType) {
        this.title = title;
        this.description = desciption;
        this.address = address;
        this.database = database;
        this.port = port;
        this.beautifySetList = inBeautifySetList;
    }

    // Constructor that also takes a charset, a quick hack for DPD-81
    ConfigOpacCatalogue(String title, String desciption, String address, String database, String iktlist, int port,
            String charset, String cbs, ArrayList<ConfigOpacCatalogueBeautifier> inBeautifySetList, String opacType) {
        // Call the contructor above
        this(title, desciption, address, database, iktlist, port, inBeautifySetList, opacType);
        this.charset = charset;
        this.setCbs(cbs);
    }

    String getTitle() {
        return this.title;
    }

    String getDescription() {
        return this.description;
    }

    String getAddress() {
        return this.address;
    }

    String getDatabase() {
        return this.database;
    }

    int getPort() {
        return this.port;
    }

    String getCharset() {
        return this.charset;
    }

    @SuppressWarnings("unchecked")
    Node executeBeautifier(Node myHitlist) {
        /* Ausgabe des Opac-Ergebnissen in Datei */

        if (!PicaPlugin.getTempDir().equals("") && new File(PicaPlugin.getTempDir()).canWrite()) {
            debugMyNode(myHitlist, FilenameUtils.concat(PicaPlugin.getTempDir(), "opacBeautifyBefore.xml"));
        }

        /*
         * aus dem Dom-Node ein JDom-Object machen
         */
        Document doc = new DOMBuilder().build(myHitlist.getOwnerDocument());

        /*
         * Im JDom-Object alle Felder durchlaufen und die notwendigen
         * Ersetzungen vornehmen
         */
        /* alle Records durchlaufen */
        List<Element> elements = doc.getRootElement().getChildren();
        for (Element el : elements) {
            // Element el = (Element) it.next();
            /* in jedem Record den Beautifier anwenden */
            executeBeautifierForElement(el);
        }

        /*
         * aus dem JDom-Object wieder ein Dom-Node machen
         */
        DOMOutputter doutputter = new DOMOutputter();
        try {
            myHitlist = doutputter.output(doc);
            myHitlist = myHitlist.getFirstChild();
        } catch (JDOMException e) {
            logger.error("JDOMException in executeBeautifier(Node)", e);
        }

        /* Ausgabe des überarbeiteten Opac-Ergebnisses */
        if (!PicaPlugin.getTempDir().equals("") && new File(PicaPlugin.getTempDir()).canWrite()) {
            debugMyNode(myHitlist, FilenameUtils.concat(PicaPlugin.getTempDir(), "opacBeautifyAfter.xml"));
        }
        return myHitlist;
    }

    /**
     * Beautifier für ein JDom-Object durchführen.
     */
    @SuppressWarnings("unchecked")
    private void executeBeautifierForElement(Element el) {
        for (ConfigOpacCatalogueBeautifier beautifier : this.beautifySetList) {
            int moreOccurrences;
            HashSet<Element> processed = new HashSet<>();
            do {
                Element elementToChange = null;
                Element tagged = null;
                moreOccurrences = 0;
                boolean merelyCount = false;
                /*
                 * eine Kopie der zu prüfenden Elemente anlegen (damit man darin
                 * löschen kann
                 */
                ArrayList<ConfigOpacCatalogueBeautifierElement> prooflist = new ArrayList<>(
                        beautifier.getTagElementsToProof());
                /* von jedem Record jedes Field durchlaufen */
                List<Element> elements = el.getChildren("field");
                Matcher matcher = null;
                for (Element field : elements) {
                    String tag = field.getAttributeValue("tag");
                    /* von jedem Field alle Subfelder durchlaufen */
                    List<Element> subelements = field.getChildren("subfield");
                    for (Element subfield : subelements) {
                        String subtag = subfield.getAttributeValue("code");
                        String value = subfield.getText();

                        if (beautifier.getTagElementToChange().getTag().equals(tag)) {
                            if (!merelyCount) {
                                tagged = field;
                            }
                            if (beautifier.getTagElementToChange().getSubtag().equals(subtag)
                                    && !processed.contains(subfield)) {
                                if (!merelyCount) {
                                    elementToChange = subfield;
                                }
                                moreOccurrences++;
                            }
                        }
                        /*
                         * wenn die Werte des Subfeldes in der Liste der zu
                         * prüfenden Beutifier-Felder stehen, dieses aus der
                         * Liste der Beautifier entfernen
                         */
                        if (!merelyCount) {
                            for (ConfigOpacCatalogueBeautifierElement cocbe : beautifier.getTagElementsToProof()) {
                                if (cocbe.getTag().equals(tag) && cocbe.getSubtag().equals(subtag)
                                        && !processed.contains(subfield)) {
                                    matcher = Pattern.compile(cocbe.getValue()).matcher(value);
                                    if (cocbe.getMode().equals("matches") && matcher.matches() || matcher.find()) {
                                        prooflist.remove(cocbe);
                                        if (prooflist.isEmpty() && subfield.equals(elementToChange)) {
                                            merelyCount = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                /*
                 * wenn in der Kopie der zu prüfenden Elemente keine Elemente
                 * mehr enthalten sind, kann der zu ändernde Wert wirklich
                 * geändert werden
                 */
                if (prooflist.isEmpty()) {
                    if (elementToChange == null) {
                        if (tagged == null) {
                            tagged = new Element("field");
                            tagged.setAttribute("tag", beautifier.getTagElementToChange().getTag());
                            el.addContent(tagged);
                        }
                        elementToChange = new Element("subfield");
                        elementToChange.setAttribute("code", beautifier.getTagElementToChange().getSubtag());
                        tagged.addContent(elementToChange);
                    }
                    if (beautifier.getTagElementToChange().getMode().equals("replace")) {
                        elementToChange.setText(fillIn(beautifier.getTagElementToChange().getValue(), matcher));
                    } else if (beautifier.getTagElementToChange().getMode().equals("prepend")) {
                        elementToChange.setText(fillIn(beautifier.getTagElementToChange().getValue(), matcher)
                                .concat(elementToChange.getText()));
                    } else if (beautifier.getTagElementToChange().getMode().equals("unescapeXml")) {
                        elementToChange.setText(StringEscapeUtils
                                .unescapeXml(fillIn(beautifier.getTagElementToChange().getValue(), matcher)));
                    } else {
                        elementToChange.setText(elementToChange.getText()
                                .concat(fillIn(beautifier.getTagElementToChange().getValue(), matcher)));
                    }
                }
                if (elementToChange != null) {
                    processed.add(elementToChange);
                }
            } while (moreOccurrences > 1);
        }

    }

    /**
     * The function fillIn() replaces marks in a given string by values derived
     * from match results. There are two different mechanisms available for
     * replacement.
     *
     * <p>
     * If the marked string contains the replacement mark <code>{\\@}</code>,
     * the matcher’s find() operation will be invoked over and over again and
     * all match results are concatenated and inserted in place of the
     * replacement marks.
     * </p>
     *
     * <p>
     * Otherwise, all replacement marks <code>{1}</code>, <code>{2}</code>,
     * <code>{3}</code>, … will be replaced by the capturing groups matched by
     * the matcher.
     * </p>
     *
     * @param markedString
     *            a string with replacement markers
     * @param matcher
     *            a matcher who’s values shall be inserted
     * @return the string with the replacements filled in
     * @throws IndexOutOfBoundsException
     *             If there is no capturing group in the pattern with the given
     *             index
     */
    private static String fillIn(String markedString, Matcher matcher) {
        if (matcher == null) {
            return markedString;
        }
        if (markedString.contains("{@}")) {
            StringBuilder composer = new StringBuilder();
            composer.append(matcher.group());
            while (matcher.find()) {
                composer.append(matcher.group());
            }
            return markedString.replaceAll("\\{@\\}", composer.toString());
        } else {
            StringBuffer replaced = new StringBuffer();
            Matcher replacer = Pattern.compile("\\{(\\d+)\\}").matcher(markedString);
            while (replacer.find()) {
                replacer.appendReplacement(replaced, matcher.group(Integer.parseInt(replacer.group(1))));
            }
            replacer.appendTail(replaced);
            return replaced.toString();
        }
    }

    /**
     * Print given DomNode to defined File.
     */
    private void debugMyNode(Node inNode, String fileName) {
        try (FileOutputStream output = new FileOutputStream(fileName)) {
            XMLOutputter outputter = new XMLOutputter();
            Document tempDoc = new DOMBuilder().build(inNode.getOwnerDocument());
            outputter.output(tempDoc.getRootElement(), output);
        } catch (IOException e) {
            logger.error("debugMyNode(Node, String)", e);
        }

    }

    /**
     * @param cbs
     *            the cbs to set
     */
    private void setCbs(String cbs) {
        this.cbs = cbs;
    }

    /**
     * @return the cbs
     */
    String getCbs() {
        return this.cbs;
    }

}
