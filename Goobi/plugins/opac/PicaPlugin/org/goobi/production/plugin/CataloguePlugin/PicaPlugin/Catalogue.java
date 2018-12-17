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

package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;

class Catalogue {
    private static final Logger logger = Logger.getLogger(Catalogue.class);
    private final String title;
    private final String description;
    private final String scheme;
    private final String address;
    private final String database;
    private final int port;
    private final String ucnf;
    private final String charset;
    private final List<Setvalue> beautify;
    private final Map<String,ResolveRule> resolveRules = new HashMap<>();

    Catalogue(String title, String description, String scheme, String address, String database, int port, String charset,
            String ucnf, List<Setvalue> beautifySetList, Collection<ResolveRule> resolveRules) {

        this.title = title;
        this.description = description;
        this.scheme = scheme;
        this.address = address;
        this.database = database;
        this.port = port;
        this.beautify = beautifySetList;
        this.charset = charset;
        this.ucnf = ucnf;
        for(ResolveRule rule:resolveRules){
            this.resolveRules.put(rule.getIdentifier(), rule);
        }
    }

    String getTitle() {
        return title;
    }

    String getDescription() {
        return description;
    }

    String getScheme() {
        return scheme;
    }

    String getAddress() {
        return address;
    }

    String getDatabase() {
        return database;
    }

    int getPort() {
        return port;
    }

    String getCharset() {
        return charset;
    }

    @SuppressWarnings("unchecked")
    Node executeBeautifier(Node myHitlist) {
        /* Ausgabe des Opac-Ergebnissen in Datei */

        if (!PicaPlugin.getTempDir().equals("") && new File(PicaPlugin.getTempDir()).canWrite()) {
            debugMyNode(myHitlist, FilenameUtils.concat(PicaPlugin.getTempDir(), "opacBeautifyBefore.xml"));
        }

        /*
         * --------------------- aus dem Dom-Node ein JDom-Object machen -------------------
         */
        Document doc = new DOMBuilder().build(myHitlist.getOwnerDocument());

        /*
         * --------------------- Im JDom-Object alle Felder durchlaufen und die notwendigen Ersetzungen vornehmen -------------------
         */
        /* alle Records durchlaufen */
        List<Element> elements = doc.getRootElement().getChildren();
        for (Element el : elements) {
            // Element el = (Element) it.next();
            /* in jedem Record den Beautifier anwenden */
            executeBeautifierForElement(el);
        }

        /*
         * --------------------- aus dem JDom-Object wieder ein Dom-Node machen -------------------
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
     * Beautifier für ein JDom-Object durchführen ================================================================
     */
    @SuppressWarnings("unchecked")
    private void executeBeautifierForElement(Element el) {
        for (Setvalue setvalue : beautify) {
            int moreOccurrences;
            HashSet<Element> processed = new HashSet<>();
            do {
            Element elementToChange = null;
            Element tagged = null;
            moreOccurrences = 0;
            boolean merelyCount = false;
            /* eine Kopie der zu prüfenden Elemente anlegen (damit man darin löschen kann) */
            ArrayList<Condition> conditions = new ArrayList<>(setvalue
                    .getConditions());
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

                    if (setvalue.getTag().equals(tag)) {
                        if (!merelyCount) {
                            tagged = field;
                        }
                        if (setvalue.getSubtag().equals(subtag) && !processed.contains(subfield)) {
                            if(!merelyCount) {
                                elementToChange = subfield;
                            }
                            moreOccurrences++;
                        }
                    }
                    /*
                     * wenn die Werte des Subfeldes in der Liste der zu prüfenden Beautifier-Felder stehen, dieses aus der Liste der Beautifier
                     * entfernen
                     */
                    if(!merelyCount){
                    for (Condition condition : setvalue.getConditions()) {
                            if (condition.getTag().equals(tag) && condition.getSubtag().equals(subtag)
                                    && !processed.contains(subfield)) {
                            matcher = Pattern.compile(condition.getValue()).matcher(value);
                            if ((condition.getMode().equals("matches") && matcher.matches()) || matcher.find()) {
                                conditions.remove(condition);
                                if ((conditions.size() == 0) && subfield.equals(elementToChange)){
                                    merelyCount = true;
                                }
                            }
                        }
                    }
                    }
                }
            }
            /*
             * --------------------- wenn in der Kopie der zu prüfenden Elemente keine Elemente mehr enthalten sind, kann der zu ändernde Wert
             * wirklich geändert werden -------------------
             */
            if (conditions.size() == 0) {
                if (elementToChange == null) {
                    if (tagged == null) {
                        tagged = new Element("field");
                        tagged.setAttribute("tag", setvalue.getTag());
                        el.addContent(tagged);
                    }
                    elementToChange = new Element("subfield");
                    elementToChange.setAttribute("code", setvalue.getSubtag());
                    tagged.addContent(elementToChange);
                }
                if (setvalue.getMode().equals("replace")) {
                    elementToChange.setText(fillIn(setvalue.getValue(), matcher));
                } else if (setvalue.getMode().equals("prepend")) {
                    elementToChange.setText(fillIn(setvalue.getValue(), matcher).concat(
                            elementToChange.getText()));
                } else if (setvalue.getMode().equals("unescapeXml")) {
                    elementToChange.setText(StringEscapeUtils.unescapeXml(fillIn(setvalue
                            .getValue(), matcher)));
                } else {
                    elementToChange.setText(elementToChange.getText().concat(
                            fillIn(setvalue.getValue(), matcher)));
                }
            }
            if(elementToChange != null) {
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
     * If the marked string contains the replacement mark <code>&#123;@}</code>,
     * the matcher’s find() operation will be invoked over and over again and
     * all match results are concatenated and inserted in place of the
     * replacement marks.
     *
     * Otherwise, all replacement marks <code>{1}</code>, <code>{2}</code>,
     * <code>{3}</code>, … will be replaced by the capturing groups matched by
     * the matcher.
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
     * Print given DomNode to defined File ================================================================
     */
    private void debugMyNode(Node inNode, String fileName) {
        try (FileOutputStream output = new FileOutputStream(fileName)) {
            XMLOutputter outputter = new XMLOutputter();
            Document tempDoc = new DOMBuilder().build(inNode.getOwnerDocument());
            outputter.output(tempDoc.getRootElement(), output);
        } catch (FileNotFoundException e) {
            logger.error("debugMyNode(Node, String)", e);
        } catch (IOException e) {
            logger.error("debugMyNode(Node, String)", e);
        }

    }

    String getUncf() {
        return ucnf;
    }

    Map<String,ResolveRule> getResolveRules() {
        return resolveRules;
    }

}
