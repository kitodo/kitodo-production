/*
 * Copyright by intranda GmbH 2013. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.kitodo.production.plugin.importer.massimport.sru;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.helper.metadata.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.helper.metadata.LegacyPrefsHelper;
import org.kitodo.production.plugin.importer.massimport.googlecode.fascinator.redbox.sru.SRUClient;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class SRUHelper {
    private static final Logger logger = LogManager.getLogger(SRUHelper.class);
    private static final Namespace SRW = Namespace.getNamespace("srw", "http://www.loc.gov/zing/srw/");
    private static final Namespace PICA = Namespace.getNamespace("pica", "info:srw/schema/5/picaXML-v1.0");

    // private static final Namespace DC = Namespace.getNamespace("dc",
    // "http://purl.org/dc/elements/1.1/");
    // private static final Namespace DIAG = Namespace.getNamespace("diag",
    // "http://www.loc.gov/zing/srw/diagnostic/");
    // private static final Namespace XCQL = Namespace.getNamespace("xcql",
    // "http://www.loc.gov/zing/cql/xcql/");

    /**
     * Private constructor to hide the implicit public one.
     */
    private SRUHelper() {

    }

    /**
     * Search.
     *
     * @param ppn
     *            String
     * @param address
     *            String
     * @return String
     */
    public static String search(String ppn, String address) {
        SRUClient client;
        try {
            client = new SRUClient("http://" + address, "picaxml", null, null);
            return client.getSearchResponse("pica.ppn=" + ppn);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * Parse result.
     *
     * @param resultString
     *            String
     * @return Node
     */
    public static Node parseResult(String resultString)
            throws IOException, JDOMException, ParserConfigurationException {
        final String recordString = "record";
        final String tag = "tag";
        final String occurrence = "occurrence";
        // removed validation against external dtd
        SAXBuilder builder = new SAXBuilder(false);
        builder.setValidation(false);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document doc = builder.build(new StringReader(resultString));
        // srw:searchRetrieveResponse
        Element root = doc.getRootElement();
        // <srw:records>
        Element srwRecords = root.getChild("records", SRW);
        if (srwRecords == null) {
            return null;
        }
        // <srw:record>
        Element srwRecord = srwRecords.getChild(recordString, SRW);
        // <srw:recordData>
        if (srwRecord != null) {
            Element recordData = srwRecord.getChild("recordData", SRW);
            Element record = recordData.getChild(recordString, PICA);

            // generate an answer document
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            org.w3c.dom.Document answer = docBuilder.newDocument();
            org.w3c.dom.Element collection = answer.createElement("collection");
            answer.appendChild(collection);

            org.w3c.dom.Element picaRecord = answer.createElement(recordString);
            collection.appendChild(picaRecord);

            @SuppressWarnings("unchecked")
            List<Element> data = record.getChildren();
            for (Element datafield : data) {
                if (datafield.getAttributeValue(tag) != null) {
                    org.w3c.dom.Element field = answer.createElement("field");
                    picaRecord.appendChild(field);
                    if (datafield.getAttributeValue(occurrence) != null) {
                        field.setAttribute(occurrence, datafield.getAttributeValue(occurrence));
                    }
                    field.setAttribute(tag, datafield.getAttributeValue(tag));
                    @SuppressWarnings("unchecked")
                    List<Element> subfields = datafield.getChildren();
                    for (Element sub : subfields) {
                        org.w3c.dom.Element subfield = answer.createElement("subfield");
                        field.appendChild(subfield);
                        subfield.setAttribute("code", sub.getAttributeValue("code"));
                        Text text = answer.createTextNode(sub.getText());
                        subfield.appendChild(text);
                    }
                }
            }
            return answer.getDocumentElement();
        }
        return null;
    }

    /**
     * Parse pica format.
     *
     * @param pica
     *            Node
     * @param prefs
     *            Prefs
     * @return Fileformat
     */
    public static LegacyMetsModsDigitalDocumentHelper parsePicaFormat(Node pica, LegacyPrefsHelper prefs)
            throws ReadException, PreferencesException {

        throw new UnsupportedOperationException("Dead code pending removal");
    }
}
